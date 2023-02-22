package mote4.scenegraph.target;

import mote4.util.ErrorUtils;
import mote4.util.texture.TextureMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_2D_MULTISAMPLE;
import static org.lwjgl.opengl.GL32.glTexImage2DMultisample;

/**
 * A FBO with multiple color attachment points.
 * Useful for using multiple render targets from a single fragment shader.
 * @author Peter
 */
public class MSAAMultiColorFBO extends Target {

    private final IntBuffer drawBuffers;
    private int[] colorTextureID;
    private int   depthRenderBufferID,
                  stencilRenderBufferID;
    private String[] textureName;

    /**
     * Creates a framebuffer object.
     * @param w Width of the texture
     * @param h Height of the texture
     * @param useDepthBuffer Whether a depth attachment should be used.
     * @param useStencilBuffer Whether a stencil attachment should be used.  This also creates a depth attachment.
     * @param numAttachments The number of color attachments to create.
     */
    public MSAAMultiColorFBO(int w, int h, int numSamples, int numAttachments, boolean useDepthBuffer, boolean useStencilBuffer, int... formats) {
        this(w,h,numSamples,numAttachments,useDepthBuffer,useStencilBuffer,null,formats);
    }

    /** @param buffers Optional argument to pass in texture IDs to reuse as buffers.
     */
    public MSAAMultiColorFBO(int w, int h, int numSamples, int numAttachments, boolean useDepthBuffer, boolean useStencilBuffer, int[] buffers, int... formats) {
        width = w;
        height = h;
        textureName = new String[numAttachments];
        if (formats.length != numAttachments)
            throw new IllegalArgumentException();

        // create the list of attachments to bind when bind() is called
        drawBuffers = BufferUtils.createIntBuffer(numAttachments);
        for (int i = 0; i < numAttachments; i++)
            drawBuffers.put(GL_COLOR_ATTACHMENT0+i);
        drawBuffers.flip();

        bufferIndex = glGenFramebuffers(); // create a framebuffer

        // bind the framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, bufferIndex);

        ////// initialize color textures //////
        colorTextureID = new int[numAttachments];
        for (int i = 0; i < numAttachments; i++)
        {
            if (buffers != null && buffers[i] != -1) {
                // reuse existing buffer indices, if provided
                colorTextureID[i] = buffers[i];
                continue;
            }
            colorTextureID[i] = glGenTextures(); // create a new texture

            // bind the texture
            glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, colorTextureID[i]);
            glTexParameteri(GL_TEXTURE_2D_MULTISAMPLE, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D_MULTISAMPLE, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            // create the texture data
            int internalFormat = formats[i];
            int type;
            int format;
            switch (internalFormat) {
                case GL_RGBA8: case GL_RGBA16:
                    type = GL_INT;
                    format = GL_RGBA;
                    break;
                case GL_RGB8: case GL_RGB16:
                    type = GL_INT;
                    format = GL_RGB;
                    break;
                case GL_RGB16F: case GL_RGB32F:
                    type = GL_FLOAT;
                    format = GL_RGB;
                    break;
                case GL_RGBA16F: case GL_RGBA32F:
                    type = GL_FLOAT;
                    format = GL_RGBA;
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, numSamples, internalFormat, width, height, true);

            // attach the texture to the framebuffer
            glFramebufferTexture2D(GL_FRAMEBUFFER,      // must be GL_FRAMEBUFFER
                    GL_COLOR_ATTACHMENT0 + i,       // color attachment point
                    GL_TEXTURE_2D_MULTISAMPLE,            // texture type
                    colorTextureID[i],        // texture ID
                    0);                   // mipmap level
        }
        ////// initialize depth buffer but not stencil buffer //////

        if (useDepthBuffer && !useStencilBuffer)
        {
            // create a renderbuffer for the depth buffer
            depthRenderBufferID = glGenRenderbuffers();
            // initialize depth renderbuffer
            glBindRenderbuffer(GL_RENDERBUFFER, depthRenderBufferID); // bind the depth renderbuffer
            glRenderbufferStorageMultisample(GL_RENDERBUFFER,          // must be GL_RENDERBUFFER
                                numSamples,
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
            glRenderbufferStorageMultisample(GL_RENDERBUFFER,
                                  numSamples,
                                  GL_DEPTH24_STENCIL8,
                                  width, height);
            // attach to the framebuffer
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
    public MSAAMultiColorFBO addToTextureMap(String name, int index, boolean filter) {
        TextureMap.add(colorTextureID[index], GL_TEXTURE_2D_MULTISAMPLE, name);
        TextureMap.get(name).filter(filter);
        textureName[index] = name;
        return this;
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

        for (int i = 0; i < colorTextureID.length; i++) {
            if (textureName[i] != null)
                TextureMap.delete(textureName[i]);
            else
                glDeleteTextures(colorTextureID[i]);
        }

        glDeleteFramebuffers(bufferIndex);
    }
}
