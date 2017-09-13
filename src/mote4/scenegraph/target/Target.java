package mote4.scenegraph.target;

import static org.lwjgl.opengl.ARBFramebufferObject.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.ARBFramebufferObject.glBindFramebuffer;
import org.lwjgl.opengl.GL11;

/**
 * A Target represents a render target to bind to a Layer.
 * This includes the framebuffer and framebuffer objects.
 * @author Peter
 */
public abstract class Target {
    
    protected int width = -1, 
                  height = -1, 
                  bufferIndex = -1;
    
    protected static Target current = null;
    
    /**
     * Get the most recently bound Target.
     * @return 
     */
    public static Target getCurrent() { return current; }
    
    /**
     * Makes this Target the current rendering framebuffer.
     */
    public void makeCurrent() {
        current.endCurrent();
        current = this;
        glBindFramebuffer(GL_FRAMEBUFFER, bufferIndex);
        GL11.glViewport(0, 0, width, height);
    }
    /**
     * Called when a Target is about to be switched out.
     */
    void endCurrent() {}
    public int index() { return bufferIndex; }
    public int width() { return width; }
    public int height() { return height; }
    
    public abstract void destroy();
}
