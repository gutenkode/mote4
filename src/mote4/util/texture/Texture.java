package mote4.util.texture;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glTexParameteri;

/**
 * Encapsulates an OpenGL texture.
 * @author Peter
 */
public class Texture {
    
    public final int ID, TYPE;
    
    /**
     * Sets this texture object to an already existing OpenGL texture.
     * @param id 
     * @param type 
     */
    public Texture(int id, int type) {
        ID = id;
        TYPE = type;
    }
    
    public void bindFiltered() {
        bind();
        glTexParameteri(TYPE, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(TYPE, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    }
    public void bindUnfiltered() {
        bind();
        glTexParameteri(TYPE, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(TYPE, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    }
    public void bind() {
        glBindTexture(TYPE, ID);
    }
    
    public void destroy() {
        glDeleteTextures(ID);
    }
}
