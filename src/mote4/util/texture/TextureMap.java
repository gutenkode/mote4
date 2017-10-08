package mote4.util.texture;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import javax.imageio.ImageIO;
import mote4.scenegraph.Window;
import mote4.util.ErrorUtils;
import mote4.util.FileIO;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

/**
 * Stores, manages, and binds OpenGL texture handles.
 * TODO restructure the load() code, currently
 * @author Peter
 */
public class TextureMap {

    protected static HashMap<String,Texture> textureMap = new HashMap<>();
    
    public static void add(int id, int type, String name) {
        if (textureMap.containsKey(name)) {
            System.err.println("The texture '"+name+"' is already in the texture map.");
            return;
        }
        textureMap.put(name, new Texture(id, type));
    }
    public static void add(int id, String name) {
        if (textureMap.containsKey(name)) {
            System.err.println("The texture '"+name+"' is already in the texture map.");
            return;
        }
        textureMap.put(name, new Texture(id, GL_TEXTURE_2D));
    }
    public static void add(Texture t, String name) {
        if (textureMap.containsKey(name)) {
            System.err.println("The texture '"+name+"' is already in the texture map.");
            return;
        }
        textureMap.put(name, t);
    }

    /**
     * Loads a .png file as a texture.
     * @param name The relative path to the file in the /res/textures/ directory, and what to recall the texture with when loaded into the hashmap.
     * @return The texture.
     */
    public static Texture load(String name) {
        return load(name, name);
    }
    /**
     * Loads a .png file as a texture.
     * @param filepath The relative path to the file in the /res/textures/ directory.
     * @param name What to recall the texture with when loaded into the hashmap.
     * @return The texture.
     */
    public static Texture load(String filepath, String name) {
        Texture tex = ImageUtil.loadImage2D("/res/textures/"+filepath+".png");
        add(tex, name);
        return tex;
    }
    /**
     * Load a texture as a cubemap.
     * @param filepath
     * @param name
     * @return
     */
    public static Texture loadCubemap(String filepath, String name) {
        Texture tex = ImageUtil.loadImageCubemap("/res/textures/"+filepath+".png");
        add(tex, name);
        return tex;
    }

    /**
     * Load all textures specified in an index file.
     * The index file must be in the res/textures directory.
     */
    public static void loadIndex(String filename) {
        BufferedReader br = FileIO.getBufferedReader("/res/textures/"+filename);
        String in;
        try {
            while((in = br.readLine()) != null) {
                if (in.isEmpty() || in.startsWith("#")) // skip empty lines or comments
                    continue;
                String[] keys = in.split("\t+");
                try {
                    if (keys.length == 2) {
                        // load the texture
                        for (int i = 0; i < keys.length; i++)
                            keys[i] = keys[i].trim();
                        load(keys[0], keys[1]);
                    } else
                        System.out.println("Invalid texture index line: " + in);
                } catch (Exception e) {
                    System.err.println("Error creating texture: '"+keys[0]+", "+keys[1]+"'");
                    e.printStackTrace();
                    Window.destroy();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading texture index file.");
            e.printStackTrace();
            Window.destroy();
        }
    }

    public static Texture get(String name) {
        return textureMap.get(name);
    }
    
    /**
     * Binds the texture with linear filtering.
     * @param name The texture to bind.
     */
    public static void bindFiltered(String name) {
        try {
            textureMap.get(name).bindFiltered();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Attempted to bind unrecognized texture '"+name+"'.");
        }
    }
    /**
     * Binds the texture with nearest-neighbor filtering.
     * @param name The texture to bind.
     */
    public static void bindUnfiltered(String name) {
        try {
            textureMap.get(name).bindUnfiltered();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Attempted to bind unrecognized texture '"+name+"'.");
        }
    }
    /**
     * Binds the texture with no filtering specifications.
     * @param name The texture to bind.
     */
    public static void bind(String name) {
        try {
            textureMap.get(name).bind();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Attempted to bind unrecognized texture '"+name+"'.");
        }
    }
    
    /**
     * Deletes the specified texture.
     * @param name The texture to delete.
     */
    public static void delete(String name) {
        if (textureMap.containsKey(name)) {
            textureMap.get(name).destroy();
            textureMap.remove(name);
        }
    }
    /**
     * Removes all textures, and deletes their data from OpenGL.
     */
    public static void clear() {
        for (Texture t : textureMap.values()) {
            t.destroy();
        }
        textureMap.clear();
    }
}