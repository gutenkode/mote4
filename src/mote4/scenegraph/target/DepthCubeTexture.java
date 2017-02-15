package mote4.scenegraph.target;

import mote4.util.texture.TextureMap;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.ARBFramebufferObject.*;
import static org.lwjgl.opengl.ARBTextureCubeMap.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL32.*;

/**
 * A DepthTexture stores the depth buffer only.
 * Six separate textures are stored and mapped to a cube, allowing for
 * omnidirectional shadow mapping.
 * @author Peter
 */
public class DepthCubeTexture extends Target {
    
    private int depthTextureID;
    private String textureName;
    
    public DepthCubeTexture(int s) {
        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS); // enable filtering across cubemap faces

        width = s;
        height = s;
        
        bufferIndex = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, bufferIndex);

        // slower than a render buffer, but it can be easily sampled later
        depthTextureID = glGenTextures();
        
        glBindTexture(GL_TEXTURE_CUBE_MAP_ARB, depthTextureID);
        for (int i = 0; i < 6; i++)
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X_ARB + i, 0, GL_DEPTH_COMPONENT16, 
                         width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (java.nio.ByteBuffer) null);  
        //glBindTexture(GL_TEXTURE_2D, depthTextureID);
        //glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT16, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (java.nio.ByteBuffer) null);
        
        glTexParameteri(GL_TEXTURE_CUBE_MAP_ARB, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP_ARB, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP_ARB, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP_ARB, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP_ARB, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE); 
        // special mode for shadow textures (or something)
        glTexParameteri(GL_TEXTURE_CUBE_MAP_ARB, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_R_TO_TEXTURE);
        
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
            throw new IllegalStateException("Error constructing DepthCubeTexture.");
        
        glBindTexture(GL_TEXTURE_CUBE_MAP_ARB, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    
    /**
     * Adds this fbo to the texture map as a bindable texture.
     * @param name The name to give to the texture.
     */
    public void addToTextureMap(String name) {
        TextureMap.add(depthTextureID, GL_TEXTURE_CUBE_MAP_ARB, name);
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
