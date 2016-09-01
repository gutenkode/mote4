package mote4.example;

import mote4.scenegraph.Scene;
import mote4.scenegraph.Window;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.ShaderUtils;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.mesh.MeshMap;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Example project in Mote4.
 * Press space to enable/disable texture filtering.
 * @author Peter
 */
public class Mote4Example implements Scene {

    public static void main(String[] args) 
    {
        System.setProperty("java.awt.headless", "true"); // prevents ImageIO from hanging on OS X
        // default windowed resolution, window can be freely resized by default
        Window.init(1920/2, 1080/2);

        //glfwSetWindowAspectRatio(Window.getWindowID(), 16, 9);
        glfwSetWindowSizeLimits(Window.getWindowID(), 640, 480, GLFW_DONT_CARE, GLFW_DONT_CARE);
        
        //Window.setCursorHidden(true);
        Window.setWindowTitle("Engine Test");
        Window.displayDeltaInTitle(true); // displays delta time in window title, overrides previous line
        
        glEnable(GL_DEPTH_TEST); // for 3D rendering
        loadResources();
        
        Window.addScene(new Mote4Example());
        Window.loop(); // run the game loop, default 60fps
    }
    private static void loadResources() 
    {
        // create a shader that can use a texture to render vertex data
        // first two arguments are source files, last argument is the shader's handle
        ShaderUtils.addProgram("mote/texture.vert", "mote/texture.frag", "texture");
        // first argument is the filename, second argument is texture's handle
        TextureMap.load("mote/crate", "test");
        
        // load a .obj file into the game, boolean value is whether the model should be centered
        Mesh mesh = StaticMeshBuilder.constructVAOFromOBJ("mote/cube", false);
        MeshMap.add(mesh, "cube");
    }
    
    /////////////////////////////////////////////
    
    private Transform transform;
    private boolean filter = true;
    
    public Mote4Example() 
    {
        // transformation matrices
        // a Transform has a model, view, and projection matrix
        transform = new Transform();
        transform.view.translate(0, 0, -3); // pull the camera back from the origin
        
        createKeyCallback();
    }
    private void createKeyCallback()
    {
        // this function is called whenever a key is pressed
        glfwSetKeyCallback(Window.getWindowID(), (long window, int key, int scancode, int action, int mods) -> {
            // GLFW_PRESS, GLFW_REPEAT, GLFW_RELEASE
            switch (key) {
                case GLFW_KEY_SPACE:
                    if (action == GLFW_PRESS)
                        filter = false;
                    else if (action == GLFW_RELEASE)
                        filter = true;
                    break;
            }
        });
    }

    @Override
    public void update(double delta) 
    {
        // rotate the camera view based on delta time
        transform.view.rotate((float)delta*.5f,   1, 0, 0); // around x axis
        transform.view.rotate((float)delta*.5f*2, 0, 1, 0); // around y axis
    }

    @Override
    public void render(double delta) 
    {
        // clear the screen
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        ShaderMap.use("texture");
        transform.makeCurrent(); // Transform will bind to the CURRENT shader
        
        if (filter)
            TextureMap.bindFiltered("test");
        else
            TextureMap.bindUnfiltered("test");
        
        MeshMap.render("cube");
    }

    @Override
    public void framebufferResized(int width, int height) 
    {
        // called every time the screen is resized, and called once at program start
        
        //transform.projection.setOrthographic(left, top, right, bottom, near, far);
        //transform.projection.setPerspective(width, height, near, far, fov);
        transform.projection.setPerspective(width, height, .1f, 100f, 75f);
    }

    @Override
    public void destroy() {
        // called when the game loop exits
    }
    
}