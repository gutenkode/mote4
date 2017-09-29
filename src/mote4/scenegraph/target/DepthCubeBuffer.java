package mote4.scenegraph.target;

import mote4.util.texture.TextureMap;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

/**
 * A DepthCubeBuffer stores a depth buffer only.
 * Six separate textures are stored and mapped to a cube, allowing for
 * omnidirectional shadow mapping. Fancy~
 * @author Peter
 */
public class DepthCubeBuffer extends Target {
    
    private int depthTextureID;
    private String textureName;
    
    public DepthCubeBuffer(int size) {
        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS); // enable filtering across cubemap faces

        width = size;
        height = size;
        
        bufferIndex = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, bufferIndex);

        // slower than a render buffer, but it can be easily sampled later
        depthTextureID = glGenTextures();
        
        glBindTexture(GL_TEXTURE_CUBE_MAP, depthTextureID);
        for (int i = 0; i < 6; i++)
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_DEPTH_COMPONENT16,
                         width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (java.nio.ByteBuffer) null);  
        //glBindTexture(GL_TEXTURE_2D, depthTextureID);
        //glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT16, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (java.nio.ByteBuffer) null);
        
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        // special mode for shadow textures (or something)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_R_TO_TEXTURE);

        // replaces calls to glFramebufferTexture2D
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthTextureID, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        // Always check that our framebuffer is ok
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            throw new IllegalStateException("Error constructing DepthCubeTexture.");
        
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    
    /**
     * Adds this fbo to the texture map as a bindable texture.
     * @param name The name to give to the texture.
     */
    public void addToTextureMap(String name) {
        TextureMap.add(depthTextureID, GL_TEXTURE_CUBE_MAP, name);
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
