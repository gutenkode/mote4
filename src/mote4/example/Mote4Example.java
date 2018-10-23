package mote4.example;

import mote4.scenegraph.Scene;
import mote4.scenegraph.Window;
import mote4.util.ErrorUtils;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.ShaderUtils;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.mesh.MeshMap;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Example project in Mote4.
 * @author Peter
 */
public class Mote4Example implements Scene {

    public static void main(String[] args)
    {
        /*
         * Basic flow of a project in this engine:
         * - initialize the context with the Window class
         * - load resources like shaders and textures (best done with index.txt files)
         * - create one or more Scene objects and add them to Window (Layer can be ignored for most cases)
         * - begin the game loop with Window.loop()
         *
         * Window stores Layers which store Scenes.  Usually you don't need to care about Layers.
         */

        ErrorUtils.debug(true);
        // default windowed resolution, window can be freely resized by default
        Window.initWindowedPercent(.75, 16.0/9.0);
        //Window.initFullscreen();
        Window.setVsync(true);
        Window.displayDeltaInTitle(true);

        //glfwSetWindowAspectRatio(Window.getWindowID(), 16, 9);
        glfwSetWindowSizeLimits(Window.getWindowID(), 640, 480, GLFW_DONT_CARE, GLFW_DONT_CARE);
        
        //Window.setCursorHidden(true);
        Window.setTitle("Engine Test");
        Window.displayDeltaInTitle(true); // displays delta time in window title, overrides previous line

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        loadResources();

        Window.addScene(new Mote4Example());
        Window.loop(); // run the game loop, default 60fps, 0 or less = no framelimit
    }

    private static void loadResources() 
    {
        // create a shader
        // first two arguments are source files, last argument is the shader's handle
        ShaderUtils.addProgram("mote/texture.vert", "mote/texture.frag", "texture");
        ShaderUtils.addProgram("mote/color.vert", "mote/color.frag", "color");
        // first argument is the filename, second argument is texture's handle
        TextureMap.load("mote/crate", "test_tex").filter(true);
        TextureMap.load("mote/font/misterpixel", "font").filter(false);
        // character width metrics for a font
        FontUtils.loadMetric("mote/font/misterpixel","misterpixel");
        
        // load a .obj file into the game, boolean value is whether the model should be re-centered
        Mesh mesh = StaticMeshBuilder.constructVAOFromOBJ("mote/cube", false);
        MeshMap.add(mesh, "test_model");
        // default quad
        MeshMap.add(StaticMeshBuilder.loadQuadMesh(), "quad");

        createKeyCallback();
    }

    private static void createKeyCallback()
    {
        // this function is called whenever a key is pressed
        glfwSetKeyCallback(Window.getWindowID(), (long window, int key, int scancode, int action, int mods) -> {
            // action is GLFW_PRESS, GLFW_REPEAT, or GLFW_RELEASE
            switch (key) {
                case GLFW_KEY_SPACE:
                    if (action == GLFW_PRESS) {
                        filter = !filter;
                        TextureMap.get("test_tex").filter(filter);
                    }
                    break;
                case GLFW_KEY_ESCAPE:
                    Window.destroy();
                    break;
                case GLFW_KEY_1:
                    if (action == GLFW_PRESS) {
                        System.out.println("Set windowed mode.");
                        Window.setWindowedPercent(.75, 16/9.0);
                    }
                    break;
                case GLFW_KEY_2:
                    if (action == GLFW_PRESS) {
                        System.out.println("Set fullscreen mode.");
                        Window.setFullscreen();
                    }
                    break;
                case GLFW_KEY_3:
                    if (action == GLFW_PRESS) {
                        Window.setVsync(!Window.isVsyncEnabled());
                        System.out.println("Set vsync: " + Window.isVsyncEnabled());
                    }
                    break;
            }
        });
    }

    private static boolean filter = true;

    /////////////////////////////////////////////
    
    private Transform transform3D, transform2D;
    private Mesh text;
    private double[] deltas = new double[120];
    private int deltaInd = 0;

    public Mote4Example() 
    {
        // transformation matrices
        // a Transform has a model, view, and projection matrix
        transform3D = new Transform();
        transform2D = new Transform();

        // a mesh that is not stored in the global list
        FontUtils.useMetric("misterpixel");
        text = FontUtils.createString("1: Windowed\n2: Fullscreen\n3: Toggle vsync\nSpace: Filtering",
                .025f,.025f,.05f,.05f);
    }

    @Override
    public void update(double time, double delta)
    {
        // rotate the camera view based on time
        transform3D.view.setIdentity(); // reset transforms
        transform3D.view.translate(0, 0, -3); // pull the camera back from the origin
        transform3D.view.rotate((float)time/2,   1, 0, 0); // around x axis
        transform3D.view.rotate((float)time, 0, 1, 0); // around y axis

        // framerate graph logic
        deltas[deltaInd] = delta;
        deltaInd++;
        deltaInd %= deltas.length;
    }

    @Override
    public void render(double time, double delta)
    {
        // clear the screen
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // render the cube
        glEnable(GL_DEPTH_TEST);
        ShaderMap.use("texture");
        transform3D.bind(); // transform will bind to the CURRENT shader only
        TextureMap.bind("test_tex");
        MeshMap.render("test_model");

        // render the framerate graph
        // only the model matrix is bound to this shader
        glDisable(GL_DEPTH_TEST);
        ShaderMap.use("color");
        for (int i = 0; i < deltas.length; i++)
        {
            float f = (float)deltas[i]*60;
            Uniform.vec("color",f-1,2-f,0,1);
            transform3D.model.translate(-0.9925f+(float)i/deltas.length*2,-1,0);
            transform3D.model.scale(.005f,f/8,1);
            transform3D.model.bind();
            transform3D.model.setIdentity(); // the matrix is reset, but not bound to the shader until bind()
            MeshMap.render("quad");
        }

        // render text
        ShaderMap.use("texture");
        TextureMap.bind("font");
        transform2D.bind();
        text.render();
    }

    @Override
    public void framebufferResized(int width, int height) 
    {
        // called every time the screen is resized, and called once at program start

        //transform2D.projection.setOrthographic(left, top, right, bottom, near, far);
        //transform3D.projection.setPerspective(width, height, near, far, fov);
        float aspectRatio = (float)width/height;
        transform2D.projection.setOrthographic(0, 0, aspectRatio, 1, -1, 1);
        transform3D.projection.setPerspective(width, height, .1f, 100f, 75f);
    }

    @Override
    public void destroy()
    {
        // called when the game loop exits
        text.destroy();
    }
}