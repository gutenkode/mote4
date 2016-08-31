package mote4.util.texture;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import javax.imageio.ImageIO;
import mote4.scenegraph.Window;
import mote4.util.FileIO;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;

/**
 * Stores, manages, and binds OpenGL texture handles.
 * @author Peter
 */
public class TextureMap {
    
    private static int bytesPerPixel = 4; // 3 for RGB, 4 for RGBA
    
    private static HashMap<String,Texture> textureMap = new HashMap<>();
    
    /**
     * Adds a texture handle to the map.
     * The type of texture can be specified.
     * @param id Must be value from unique call to glGenTextures().
     * @param type GL_TEXTURE_2D, GL_TEXTURE_3D, GL_TEXTURE_CUBE_MAP, etc.
     * @param name
     */
    public static void add(int id, int type, String name) {
        if (textureMap.containsKey(name)) {
            System.err.println("The texture '"+name+"' is already in the texture map.");
            return;
        }
        textureMap.put(name, new Texture(id, type));
    }
    
    /**
     * Adds a texture handle to the map.
     * @param id Must be value from unique call to glGenTextures().
     * @param name 
     */
    public static void add(int id, String name) {
        if (textureMap.containsKey(name)) {
            System.err.println("The texture '"+name+"' is already in the texture map.");
            return;
        }
        textureMap.put(name, new Texture(id, GL_TEXTURE_2D));
    }
    
    /**
     * Loads the .png file as a texture.
     * @param name The relative path to the file in the /res/textures/ directory, and what to recall the texture with when loaded into the hashmap.
     * @return The texture.
     */
    public static Texture load(String name) {
        return load(name, name);
    }
    /**
     * Loads the .png file as a texture.
     * @param filepath The relative path to the file in the /res/textures/ directory.
     * @param name What to recall the texture with when loaded into the hashmap.
     * @return The texture.
     */
    public static Texture load(String filepath, String name) {
        if (textureMap.containsKey(name)) {
            System.err.println("The texture '"+name+"' has already been loaded.");
            return textureMap.get(name);
        }
        try {
            BufferedImage image = ImageIO.read(ClassLoader.class.getClass().getResourceAsStream("/res/textures/"+filepath+".png"));
            return load(image, name);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not read image: /res/textures/"+filepath+".png");
            e.printStackTrace();
            Window.destroy();
            return null;
        }
    }
    /**
     * Loads the BufferedImage as a texture.
     * @param image The buffered image to load as an OpenGL texture.
     * @param name What to recall the texture with when loaded into the hashmap.
     * @return The texture.
     */
    private static Texture load(BufferedImage image, String name) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        
        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * bytesPerPixel);
        
        if (bytesPerPixel == 4)
            // RGBA
            for(int y = 0; y < image.getHeight(); y++){
                for(int x = 0; x < image.getWidth(); x++){
                    int pixel = pixels[y * image.getWidth() + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF));      // Red component
                    buffer.put((byte) ((pixel >> 8)  & 0xFF));      // Green component
                    buffer.put((byte) ( pixel        & 0xFF));      // Blue component
                    buffer.put((byte) ((pixel >> 24) & 0xFF));      // Alpha component
                }
            }
        else
            // RGB
            for(int y = 0; y < image.getHeight(); y++){
                for(int x = 0; x < image.getWidth(); x++){
                    int pixel = pixels[y * image.getWidth() + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF));      // Red component
                    buffer.put((byte) ((pixel >> 8)  & 0xFF));      // Green component
                    buffer.put((byte) ( pixel        & 0xFF));      // Blue component
                }
            }
        
        buffer.flip(); // FOR THE LOVE OF GOD DO NOT FORGET THIS
        
        // You now have a ByteBuffer filled with the color data of each pixel.
        // Now just create a texture ID and bind it. Then you can load it using
        // whatever OpenGL method you want, for example:
        
        int textureID = glGenTextures(); //Generate texture ID
        glBindTexture(GL_TEXTURE_2D, textureID); //Bind texture ID
        
        //Setup wrap mode
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        
        //Setup texture scaling filtering
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        
        //Send texel data to OpenGL
        if (bytesPerPixel == 4)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        else
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, image.getWidth(), image.getHeight(), 0, GL_RGB, GL_UNSIGNED_BYTE, buffer);
        Texture tex = new Texture(textureID, GL_TEXTURE_2D);
        textureMap.put(name, tex);
        return tex;
    }
    /**
     * Load all textures specified in an index file.
     * The index file must be in the res/textures directory.
     */
    public static void loadIndex(String filename) {
        //System.out.println("Parsing texture index file...");
        BufferedReader br = FileIO.getBufferedReader("/res/textures/"+filename);
        String in;
        try {
            while((in = br.readLine()) != null) {
                if (in.isEmpty())
                    continue;
                String[] keys = in.split("\t",2);
                if (keys.length == 2) {
                    for (int i = 0; i < keys.length; i++)
                        keys[i] = keys[i].trim();
                    //System.out.println(keys[0] + ", " + keys[1]);
                    load(keys[0],keys[1]);
                } else
                    System.out.println("Invalid texture index line: "+in);
            }
        } catch (IOException e) {
            System.err.println("Error reading texture index file.");
            e.printStackTrace();
            Window.destroy();
        }
    }
    
    /**
     * Returns the OpenGL texture ID handle for the specified texture.
     * @param name The texture to retrieve the ID of.
     * @return The texture ID.
     */
    public static int getID(String name) {
        return textureMap.get(name).ID;
    }
    /**
     * Returns the Texture object for the specified texture.
     * @param name The texture to retrieve the ID of.
     * @return The texture ID.
     */
    public static Texture get(String name) {
        return textureMap.get(name);
    }
    
    /**
     * Binds the texture with filtering.
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
     * Binds the texture with no filtering.
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
    
    /**
     * Sets the number of bytes to be used for each pixel in a texture.
     * 3 bytes is RGB, 4 bytes is RGBA
     * No other number will be applied.
     * @param bytes 3 or 4 bytes
     */
    public static void setBytesPerPixel(int bytes) {
        if (bytes == 3 || bytes == 4)
            bytesPerPixel = bytes;
    }
}