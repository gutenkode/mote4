package mote4.scenegraph.target;

import mote4.util.texture.TextureMap;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.ARBFramebufferObject.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL32.*;

/**
 * A DepthBuffer stores a depth buffer only.
 * Useful for shadow mapping.
 * @author Peter
 */
public class DepthBuffer extends Target {
    
    private int depthTextureID;
    private String textureName;
    
    public DepthBuffer(int w, int h) {
        width = w;
        height = h;
        
        bufferIndex = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, bufferIndex);

        // slower than a render buffer, but it can be easily sampled later
        depthTextureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, depthTextureID);
		
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT16, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (java.nio.ByteBuffer) null);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        // special mode for shadow textures (or something)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_R_TO_TEXTURE);
        
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthTextureID, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        /*
        // attach the texture to the framebuffer
        glFramebufferTexture2D(GL_FRAMEBUFFER,       // must be GL_FRAMEBUFFER
                               GL_DEPTH_ATTACHMENT,  // color attachment point
                               GL_TEXTURE_2D,        // texture type
                               depthTextureID,       // texture ID
                               0);                   // mipmap level
        //glDrawBuffer(GL_NONE); // No color buffer is drawn to.
        */
        // Always check that our framebuffer is ok
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            throw new IllegalStateException("Error constructing DepthTexture.");
        
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    
    /**
     * Adds this fbo to the texture map as a bindable texture.
     * @param name The name to give to the texture.
     */
    public void addToTextureMap(String name) {
        TextureMap.add(depthTextureID, name);
        textureName = name;
    }
    
    @Override
    public void destroy() {
        if (textureName != null)
            TextureMap.delete(textureName);
        else
            glDeleteTextures(depthTextureID);
        glDeleteFramebuffers(bufferIndex);
    }
}
