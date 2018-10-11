package mote4.util.texture;

import mote4.scenegraph.Window;
import mote4.util.ErrorUtils;
import mote4.util.FileIO;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static java.lang.Math.round;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * Utility for loading images and creating OpenGL textures.
 *
 * Adapted from a LWJGL demo.
 */
public class ImageUtil {

    private static boolean filterByDefault = false, enableMipmap = false;

    public static void filterByDefault(boolean b) { filterByDefault = b; }
    public static void enableMipmap(boolean b) { enableMipmap = b; }

    protected static Texture loadImage2D(String filepath) { return loadImage(filepath, GL_TEXTURE_2D); }
    protected static Texture loadImageCubemap(String filepath) { return loadImage(filepath, GL_TEXTURE_CUBE_MAP); }
    protected static Texture loadImage(String filepath, int texType) {
        int width, height, components;
        ByteBuffer image, rawImage;


        try (MemoryStack stack = stackPush()) {
            rawImage = FileIO.getByteBuffer(filepath);

            IntBuffer w    = stack.mallocInt(1);
            IntBuffer h    = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            //if (!stbi_info_from_memory(rawImage, w, h, comp))
            //    throw new RuntimeException("Failed to read image information: " + stbi_failure_reason());
            /*
            System.out.println("Image width: " + w.get(0));
            System.out.println("Image height: " + h.get(0));
            System.out.println("Image components: " + comp.get(0));
            System.out.println("Image HDR: " + stbi_is_hdr_from_memory(rawImage));
            */
            // decode the image
            image = stbi_load_from_memory(rawImage, w, h, comp, 0);
            if (image == null)
                throw new RuntimeException("Failed to load image: " + stbi_failure_reason());

            width = w.get(0);
            height = h.get(0);
            components = comp.get(0);

            //if (components == 4) // RGBA
            //    premultiplyAlpha(image, width, height);
            return createTexture(image, width, height, components, texType, filterByDefault, enableMipmap);
        } catch (IOException e) {
            e.printStackTrace();
            Window.destroy();
            return null;
        }
    }

    private static Texture createTexture(ByteBuffer image, int width, int height, int components, int texType, boolean filter, boolean mipmap) {
        int textureID = glGenTextures();
        glBindTexture(texType, textureID);

        int internalFormat, format,
            minFilter, magFilter;

        if (components == 4) {
            internalFormat = GL_RGBA8;
            format = GL_RGBA;
        } else if (components == 3) {
            internalFormat = GL_RGB8;
            format = GL_RGB;
        } else {
            throw new RuntimeException("Unsupported number of components: "+components);
        }

        if (filter) {
            if (mipmap)
                minFilter = GL_LINEAR_MIPMAP_LINEAR;
            else
                minFilter = GL_LINEAR;
            magFilter = GL_LINEAR;
        } else {
            if (mipmap)
                minFilter = GL_NEAREST_MIPMAP_LINEAR;
            else
                minFilter = GL_NEAREST;
            magFilter = GL_NEAREST;
        }

        switch (texType) {
            case GL_TEXTURE_2D:
                // enable maximum antisotropic filtering
                float aniso = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
                glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, aniso);
                // filtering
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);
                // wrap mode
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

                glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, image);
                break;
            case GL_TEXTURE_CUBE_MAP:
                glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS); // enable filtering across cubemap faces
                // filtering
                glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, minFilter);
                glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, magFilter);
                // wrap mode
                glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

                for (int i = 0; i < 6; i++) {
                    glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, image);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid texture type: "+texType);
        }
        if (mipmap)
            glGenerateMipmap(texType);
        ErrorUtils.checkGLError();

        Texture tex = new Texture(textureID, texType);
        return tex;
    }

    private static void premultiplyAlpha(ByteBuffer image, int w, int h) {
        int stride = w * 4;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++)
            {
                int i = y * stride + x * 4;

                float alpha = (image.get(i + 3) & 0xFF) / 255.0f;
                image.put(i + 0, (byte)round(((image.get(i + 0) & 0xFF) * alpha)));
                image.put(i + 1, (byte)round(((image.get(i + 1) & 0xFF) * alpha)));
                image.put(i + 2, (byte)round(((image.get(i + 2) & 0xFF) * alpha)));
            }
        }
    }
}
