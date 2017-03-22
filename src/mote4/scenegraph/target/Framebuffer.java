package mote4.scenegraph.target;

import mote4.scenegraph.Window;
import static org.lwjgl.opengl.ARBFramebufferObject.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.ARBFramebufferObject.glBindFramebuffer;
import org.lwjgl.opengl.GL11;

/**
 * Represents the standard OpenGL framebuffer.
 * Essentially, a render target with the handle 0 and the dimensions
 * of the window that created it.
 * @author Peter
 */
public class Framebuffer extends Target {

    private static final Framebuffer buf = new Framebuffer();
    public static Framebuffer getDefault() { return buf; }
    
    private Framebuffer() {
        bufferIndex = 0;
    }
    
    @Override
    public void makeCurrent() {
        current = this;
        glBindFramebuffer(GL_FRAMEBUFFER, bufferIndex);
        int[] size = Window.getFramebufferSize();
        GL11.glViewport(0, 0, size[0], size[1]); 
    }
    @Override
    public int width() {
        return Window.getFramebufferSize()[0];
    }
    @Override
    public int height() {
        return Window.getFramebufferSize()[1];
    }

    @Override
    public void destroy() {
        throw new UnsupportedOperationException("Framebuffer has no functionality for destroying buffers.");
    }
}