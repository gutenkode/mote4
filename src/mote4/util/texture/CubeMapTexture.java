package mote4.util.texture;

import mote4.scenegraph.Window;
import mote4.util.ErrorUtils;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;

/**
 * Created by Peter on 2/15/17.
 */
public class CubeMapTexture extends Texture {

    protected CubeMapTexture(int id) {
        super(id, GL_TEXTURE_CUBE_MAP);
    }

    public static CubeMapTexture loadCubemap(String filepath, String name) {
        if (TextureMap.textureMap.containsKey(name)) {
            System.err.println("The texture '"+name+"' has already been loaded.");
            return null;
        }
        try {
            BufferedImage image = ImageIO.read(ClassLoader.class.getClass().getResourceAsStream("/res/textures/"+filepath+".png"));
            return loadCubemap(image, name);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not read image: /res/textures/"+filepath+".png");
            e.printStackTrace();
            Window.destroy();
            return null;
        }
    }
    private static CubeMapTexture loadCubemap(BufferedImage image, String name) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4); // 4 bytes per pixel

        // RGBA
        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));      // Red component
                buffer.put((byte) ((pixel >> 8)  & 0xFF));      // Green component
                buffer.put((byte) ( pixel        & 0xFF));      // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));      // Alpha component
            }
        }

        buffer.flip(); // FOR THE LOVE OF GOD DO NOT FORGET THIS

        int textureID = glGenTextures(); //Generate texture ID
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureID); //Bind texture ID

        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS); // enable filtering across cubemap faces
        // filtering
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        // wrap mode
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        //Send texel data to OpenGL
        for (int i = 0; i < 6; i++) {
            glTexImage2D(
                    GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                    0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer
            );
        }
        //glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
        ErrorUtils.checkGLError();

        CubeMapTexture tex = new CubeMapTexture(textureID);
        TextureMap.textureMap.put(name, tex);
        return tex;
    }
}
