package mote4.scenegraph.target;

import java.nio.IntBuffer;
import java.util.Arrays;

import mote4.util.ErrorUtils;
import mote4.util.texture.Texture;
import mote4.util.texture.TextureMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.opengl.GL20;

/**
 * A FBO with multiple color attachment points.
 * Useful for using multiple render targets from a single fragment shader.
 * @author Peter
 */
public class MultiColorFBO  extends Target {
    
    private final IntBuffer drawBuffers;
    private int[] colorTextureID;
    private int   depthRenderBufferID,
                  stencilRenderBufferID;
    private String textureName = null;
    
    /**
     * Creates a framebuffer object.
     * @param w Width of the texture
     * @param h Height of the texture
     * @param useDepthBuffer Whether a depth attachment should be used.
     * @param useStencilBuffer Whether a stencil attachment should be used.  This also creates a depth attachment.
     * @param numAttachments The number of color attachments to create.
     */
    public MultiColorFBO(int w, int h, boolean useDepthBuffer, boolean useStencilBuffer, int numAttachments) {
        this(w,h,useDepthBuffer,useStencilBuffer,createEmptyBuffer(numAttachments));
    }
    private static int[] createEmptyBuffer(int num) {
        int[] buffers = new int[num];
        Arrays.fill(buffers, -1);
        return buffers;
    }
    /**
     * Creates a framebuffer object.
     * @param w Width of the texture
     * @param h Height of the texture
     * @param useDepthBuffer Whether a depth attachment should be used.
     * @param useStencilBuffer Whether a stencil attachment should be used.  This also creates a depth attachment.
     * @param buffers A list of color buffers to use in this FBO.  Values of -1 will be replaced with a new buffer,
     *                while all other values are kept as-is, so already existing buffers can be reused in this FBO.
     */
    public MultiColorFBO(int w, int h, boolean useDepthBuffer, boolean useStencilBuffer, int[] buffers) {
        width = w;
        height = h;
        int numAttachments = buffers.length;
        
        // create the list of attachments to bind when bind() is called
        drawBuffers = BufferUtils.createIntBuffer(numAttachments);
        for (int i = 0; i < numAttachments; i++)
            drawBuffers.put(GL_COLOR_ATTACHMENT0+i);
        drawBuffers.flip();
        
        bufferIndex = glGenFramebuffers(); // create a framebuffer
        
        // bind the framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, bufferIndex);
        
        ////// initialize color texture //////
        colorTextureID = new int[numAttachments];
        for (int i = 0; i < numAttachments; i++)
        {
            if (buffers[i] == -1)
                colorTextureID[i] = glGenTextures(); // create a new texture
            else
                colorTextureID[i] = buffers[i]; // use the already-created texture handle

            // bind the texture
            glBindTexture(GL_TEXTURE_2D, colorTextureID[i]);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            // create the texture data
            if (buffers[i] == -1) // only if this is a new buffer
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_INT, (java.nio.ByteBuffer) null);

            // attach the texture to the framebuffer
            glFramebufferTexture2D(GL_FRAMEBUFFER,       // must be GL_FRAMEBUFFER
                    GL_COLOR_ATTACHMENT0 + i, // color attachment point
                    GL_TEXTURE_2D,        // texture type
                    colorTextureID[i],       // texture ID
                    0);                   // mipmap level
        }
        ////// initialize depth buffer but not stencil buffer //////
        
        if (useDepthBuffer && !useStencilBuffer) 
        {
            // create a renderbuffer for the depth buffer
            depthRenderBufferID = glGenRenderbuffers();
            // initialize depth renderbuffer
            glBindRenderbuffer(GL_RENDERBUFFER, depthRenderBufferID); // bind the depth renderbuffer
            
            glRenderbufferStorage(GL_RENDERBUFFER,          // must be GL_RENDERBUFFER
                                     GL_DEPTH_COMPONENT,    // this will be a depth buffer, should this be GL14.GL_DEPTH_COMPONENT24 ?
                                     width, height);        // size

            // attach the depth buffer
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, 
                                         GL_DEPTH_ATTACHMENT, 
                                         GL_RENDERBUFFER, 
                                         depthRenderBufferID); // bind it to the renderbuffer
        }
        
        ////// initialize depth and stencil buffer //////
        
        if (useStencilBuffer) 
        {
            // create a renderbuffer for the stenicl buffer
            stencilRenderBufferID = glGenRenderbuffers();
            // initialize stencil buffer
            glBindRenderbuffer(GL_RENDERBUFFER, stencilRenderBufferID);
            glRenderbufferStorage(GL_RENDERBUFFER, 
                                  GL_DEPTH24_STENCIL8, 
                                  width, height);
            // attatch to the framebuffer
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, 
                                      GL_DEPTH_STENCIL_ATTACHMENT, 
                                      GL_RENDERBUFFER, 
                                      stencilRenderBufferID);
        }
        
        // make sure nothing screwy happened
        ErrorUtils.checkFBOCompleteness(bufferIndex);
    }
    
    /**
     * Adds this fbo to the texture map as a bindable texture.
     * @param name The name to give to the texture.
     * @param index Which color attachment to add.
     * @return Returns this object, to aid in fast scenegraph construction.
     */
    public MultiColorFBO addToTextureMap(String name, int index) {
        TextureMap.add(colorTextureID[index], name);
        textureName = name;
        return this;
    }
    
    /**
     * Binds a new texture to the framebuffer object.
     * @param t The texture to bind.
     * @param index Which color attachment to bind to.
     */
    public void changeTexture(Texture t, int index) {
        glBindFramebuffer(GL_FRAMEBUFFER, bufferIndex);
        glBindTexture(GL_TEXTURE_2D, t.ID);
        // attach the texture to the framebuffer
        glFramebufferTexture2D(GL_FRAMEBUFFER,       // must be GL_FRAMEBUFFER
                               GL_COLOR_ATTACHMENT0+index, // color attatchment point
                               GL_TEXTURE_2D,        // texture type
                               t.ID,       // texture ID
                               0);                   // mipmap level
        colorTextureID[index] = t.ID;
    }

    public int getColorBufferID(int index) { return colorTextureID[index]; }
    
    @Override
    public void makeCurrent() {
        current = this;
        glBindFramebuffer(GL_FRAMEBUFFER, bufferIndex);
        GL20.glDrawBuffers(drawBuffers);
        GL11.glViewport(0, 0, width, height);
    }

    /**
     * Only bind one buffer instead of all buffers.
     * @param index
     */
    public void makeCurrent(int index) {
        current = this;
        glBindFramebuffer(GL_FRAMEBUFFER, bufferIndex);
        GL20.glDrawBuffers(GL_COLOR_ATTACHMENT0+index);
        GL11.glViewport(0, 0, width, height);
    }
    @Override
    void endCurrent() {
        GL20.glDrawBuffers(GL_COLOR_ATTACHMENT0);
    }
    
    @Override
    public void destroy() {
        glDeleteRenderbuffers(depthRenderBufferID);
        glDeleteRenderbuffers(stencilRenderBufferID);
        if (textureName != null)
            TextureMap.delete(textureName);
        else
            for (int i : colorTextureID)
                glDeleteTextures(i);
        glDeleteFramebuffers(bufferIndex);
    }
}
