package mote4.scenegraph;

/**
 * A Scene contains OpenGL state and rendering code, and optimally should not change the current Target.
 * This is where most applications should start, by implementing their own Scene.
 * The engine will handle creating a default Layer and Target for rendering a scene to the screen.
 * @author Peter
 */
public interface Scene {
    void update(double time, double delta);
    void render(double time, double delta);
    /**
     * Called from a Layer the Scene is attached to - will always be the
     * framebuffer dimensions provided by glfw.
     * @param width
     * @param height 
     */
    void framebufferResized(int width, int height);
    void destroy();
}
