package mote4.scenegraph.target;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.*;

import mote4.util.ErrorUtils;
import mote4.util.texture.Texture;
import mote4.util.texture.TextureMap;
import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/**
 * A framebuffer object can be rendered to like the framebuffer, 
 * but can also be bound as a texture.
 * Neat!
 * @author Peter
 */
public class FBO extends Target {
    
    private int colorTextureID, 
                depthRenderBufferID,
                stencilRenderBufferID;
    private String textureName = null;
    
    /**
     * Creates a framebuffer object.
     * There will be no stencil or depth attachment, 
     * and the color attachment point will be 0.
     * @param w Width of the texture
     * @param h Height of the texture
     */
    public FBO(int w, int h) {
        this(w,h,false,false, null);
    }
    
    /**
     * Creates a framebuffer object.
     * @param w Width of the texture
     * @param h Height of the texture
     * @param useDepthBuffer Whether a depth attachment should be used.
     * @param useStencilBuffer Whether a stencil attachment should be used.  This also creates a depth attachment.
     * @param tex If not null, will use this texture as the texture to render to instead of creating one.
     */
    public FBO(int w, int h, boolean useDepthBuffer, boolean useStencilBuffer, Texture tex) {
        width = w;
        height = h;
        
        bufferIndex = glGenFramebuffers(); // create a framebuffer
        
        // bind the framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, bufferIndex);
        
        ////// initialize color texture //////
        
        if (tex == null)
            colorTextureID = glGenTextures(); // create a texture
        else {
            textureName = TextureMap.getName(tex);
            colorTextureID = tex.ID;
        }
        
        // bind the texture
        glBindTexture(GL_TEXTURE_2D, colorTextureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        
        // create the texture data
        if (tex == null)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16, width, height, 0, GL_RGBA, GL_INT, (java.nio.ByteBuffer) null);
        
        // attach the texture to the framebuffer
        glFramebufferTexture2D(GL_FRAMEBUFFER,       // must be GL_FRAMEBUFFER
                               GL_COLOR_ATTACHMENT0, // color attachment point
                               GL_TEXTURE_2D,        // texture type
                               colorTextureID,       // texture ID
                               0);                   // mipmap level
        
        ////// initialize depth buffer but not stencil buffer //////
        
        if (useDepthBuffer && !useStencilBuffer) 
        {
            // create a renderbuffer for the depth buffer
            // using a renderbuffer is faster than using a texture,
            // but it is much harder (read: not implemented) to read it
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
        ErrorUtils.checkGLError();
    }
    
    /**
     * Adds this fbo to the texture map as a bindable texture.
     * @param name The name to give to the texture.
     * @return Returns this object, to aid in fast scenegraph construction.
     */
    public FBO addToTextureMap(String name) {
        if (textureName != null)
            TextureMap.untrack(textureName);
        TextureMap.add(colorTextureID, name);
        textureName = name;
        TextureMap.get(name).filter(true);
        return this;
    }
    
    /**
     * Binds a new texture to the framebuffer object.
     * @param t The texture to bind.
     */
    private void changeTexture(Texture t) {
        textureName = TextureMap.getName(t);

        glBindFramebuffer(GL_FRAMEBUFFER, bufferIndex);
        glBindTexture(GL_TEXTURE_2D, t.ID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        // attach the texture to the framebuffer
        glFramebufferTexture2D(GL_FRAMEBUFFER,       // must be GL_FRAMEBUFFER
                               GL_COLOR_ATTACHMENT0, // color attatchment point
                               GL_TEXTURE_2D,        // texture type
                               t.ID,       // texture ID
                               0);                   // mipmap level
        colorTextureID = t.ID;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Texture getTexture() { return TextureMap.get(textureName); }

    public BufferedImage getAsImage() {
        var format = GL_RGBA;
        int channels = 4;
        /*if (format == GL_RGB)
            channels = 3;*/

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * channels);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        getTexture().bindFiltered();
        glGetTexImage(GL_TEXTURE_2D, 0, format, GL_UNSIGNED_BYTE, buffer);

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                int i = (x + y * width) * channels;

                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                int a = 255;
                if (channels == 4)
                    a = buffer.get(i + 3) & 0xFF;

                image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        ErrorUtils.checkGLError();

        return image;
    }
    
    @Override
    public void destroy() {
        glDeleteRenderbuffers(depthRenderBufferID);
        glDeleteRenderbuffers(stencilRenderBufferID);
        if (textureName != null) {
            TextureMap.delete(textureName);
        } else
            glDeleteTextures(colorTextureID);
        glDeleteFramebuffers(bufferIndex);
    }
}