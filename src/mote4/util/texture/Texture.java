package mote4.util.texture;

import mote4.util.ErrorUtils;
import mote4.util.shader.Bindable;

import static org.lwjgl.opengl.GL11.*;

/**
 * Encapsulates an OpenGL texture.
 * @author Peter
 */
public class Texture implements Bindable {
    
    public final int ID, TYPE;
    
    /**
     * Sets this texture object to an already existing OpenGL texture.
     * @param id 
     * @param type 
     */
    protected Texture(int id, int type) {
        ID = id;
        TYPE = type;
    }

    public void filter(boolean f) {
        if (f)
            bindFiltered();
        else
            bindUnfiltered();
    }
    
    public void bindFiltered() {
        bind();
        glTexParameteri(TYPE, GL_TEXTURE_MIN_FILTER, GL_LINEAR/*_MIPMAP_LINEAR*/);
        glTexParameteri(TYPE, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    }

    public void bindUnfiltered() {
        bind();
        glTexParameteri(TYPE, GL_TEXTURE_MIN_FILTER, GL_NEAREST/*_MIPMAP_LINEAR*/);
        glTexParameteri(TYPE, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    }

    @Override
    public void bind() {
        glBindTexture(TYPE, ID);
    }
    
    public void destroy() {
        glDeleteTextures(ID);
        ErrorUtils.checkGLError();
    }
}
