package mote4.scenegraph;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.nio.IntBuffer;
import java.util.ArrayList;
import mote4.scenegraph.target.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
 
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;
 
/**
 * Encapsulates the windowing system and related functions.
 * Sets default callbacks for keyboard and mouse input.
 * 
 * Also manages the rendering root, containing Layers which contain Scenes.
 * @author Peter
 */
public class Window {
 
    private static long window = -1; // the glfw window handle
    private static long variableYieldTime, lastTime; // used in sync()
    
    private static final int RESIZABLE = GLFW_TRUE,
                             DEFAULT_VSYNC = 1;
    private static String windowTitle = "Game";
    
    private static boolean useVsync = (DEFAULT_VSYNC != 0),
                           displayDelta = false;
    private static int targetFps = 60, // when vsync is disabled, use this framerate
                       windowWidth = -1, windowHeight = -1, // window size
                       fbWidth = -1, fbHeight = -1; // framebuffer size
    private static double cursorX = -1, cursorY = -1; // cursor position
    
    private static ArrayList<Layer> layers;
    private static Layer defaultLayer;
    private static Framebuffer framebuffer;


    /**
     * Initialize the GLFW window and OpenGL context.
     * The window will be fullscreen at the monitor's native resolution.
     */
    public static void initFullscreen() { init(0,0,true,false,0,0); }
    /**
     * Initialize the GLFW window and OpenGL context.
     * The window will be in windowed mode.
     * @param w
     * @param h
     */
    public static void initWindowed(int w, int h) { init(w,h,false,false,0,0); }
    /**
     * Initializes a windowed context based on the size of the monitor.
     * @param percentHeight Percent size of the monitor the window height should be. For example,
     *                      .5 would be 50%, or a window half the height of the display.
     * @param aspectRatio The aspect ratio of the window, used to calculate the width.
     */
    public static void initWindowedPercent(double percentHeight, double aspectRatio) { init(0, 0, false,true,percentHeight,aspectRatio); }

    private static void init(int w, int h, boolean fullscreen, boolean percent, double percentHeight, double aspectRatio) {
        if (window != -1)
            return; // the window has already been initialized

        useVsync = (DEFAULT_VSYNC != 0);
            
        framebuffer = new Framebuffer();
        defaultLayer = new Layer(framebuffer);
        layers = new ArrayList<>();

        createContext(w,h,fullscreen,percent,percentHeight,aspectRatio);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
        System.out.println("GLSL Version:   " + GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
        System.out.println("Renderer:       " + GL11.glGetString(GL11.GL_RENDERER));
    }
    private static void createContext(int initWidth, int initHeight, boolean fullscreen, boolean percent, double percentHeight, double aspectRatio) {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();
 
        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW.");
 
        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, RESIZABLE); // set the window resizable state
        glfwWindowHint(GLFW_AUTO_ICONIFY, GLFW_TRUE);
        
        // target OpenGL 3.3 core
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        // create the window
        GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (fullscreen) {
            // width and height are ignored
            window = glfwCreateWindow(mode.width(), mode.height(), windowTitle, glfwGetPrimaryMonitor(), NULL);
            if (window == NULL)
                throw new RuntimeException("Failed to create the GLFW window");
        } else {
            if (percent) {
                initHeight = (int)(mode.height()*percentHeight);
                initWidth = (int)(initHeight*aspectRatio);
            }

            window = glfwCreateWindow(initWidth, initHeight, windowTitle, NULL, NULL);
            if (window == NULL)
                throw new RuntimeException("Failed to create the GLFW window");

            // get the resolution of the primary monitor
            // center the window
            glfwSetWindowPos(
                window,
                (mode.width() - initWidth) / 2,
                (mode.height() - initHeight) / 2
            );
        }

        // default callbacks and handling for window resizing
        createCallbacks();
 
        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(DEFAULT_VSYNC);
 
        // Make the window visible
        glfwShowWindow(window);
    }
    private static void createCallbacks() {
        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in our rendering loop
        });
        // callback for mouse position
        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            cursorX = xpos;
            cursorY = ypos;
        });
        // callbacks for window and framebuffer size
        glfwSetWindowSizeCallback(window, (window, width, height) -> {
            windowWidth = width;
            windowHeight = height;
        });
        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            fbWidth = width;
            fbHeight = height;
            if (fbWidth > 0 && fbHeight > 0) // ignore invalid size updates, e.g. alt-tabbing
                for (Layer l : layers)
                    l.framebufferResized(fbWidth, fbHeight); // re-evaluate ALL scenes, just to be safe
        });
        // initialize values
        IntBuffer b1 = BufferUtils.createIntBuffer(1);
        IntBuffer b2 = BufferUtils.createIntBuffer(1);
        glfwGetWindowSize(window, b1, b2);
        windowWidth = b1.get(0);
        windowHeight = b2.get(0);
        glfwGetFramebufferSize(window, b1, b2);
        fbWidth = b1.get(0);
        fbHeight = b2.get(0);
    }
    
    public static void addScene(Scene s) {
        defaultLayer.addScene(s);
    }
    public static void addLayer(Layer l) {
        layers.add(l);
    }
    
    public static void loop() {
        double lastTime = glfwGetTime();
        glClearColor(0, 0, 0, 0);
        
        // if the default layer has any scenes, add it to the end of the list
        if (defaultLayer.numScenes() > 0)
            layers.add(defaultLayer);
        // initialize scenes
        for (Layer l : layers)
            l.framebufferResized(fbWidth, fbHeight);
 
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            
            double currTime = glfwGetTime();
            double delta = (currTime-lastTime);
            lastTime = currTime;
            if (displayDelta) {
                double printDelta = delta*1000;
                String timeStr = "Delta: "+printDelta;
                timeStr = timeStr.substring(0, 11);
                glfwSetWindowTitle(window, timeStr);
            }
            
            for (Layer l : layers) {
                l.update(delta);
                l.makeCurrent();
                l.render(delta);
            }
            
            if (!useVsync)
                sync(targetFps);
            
            glfwSwapBuffers(window); // swap the color buffers
            
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
        destroy();
    }
    /**
     * Shuts down GLFW and all resources used in the engine.
     * The program will terminate after this call.
     */
    public static void destroy() {
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
        
        System.out.println("GLFW terminated.");
        System.exit(0);
    }
    
    /**
     * An accurate sync method that adapts automatically
     * to the system it runs on to provide reliable results.
     * 
     * @param fps The desired frame rate, in frames per second
     * @author kappa (On the LWJGL Forums)
     */
    private static void sync(int fps) {
        if (fps <= 0) return;
          
        long sleepTime = 1000000000 / fps; // nanoseconds to sleep this frame
        // yieldTime + remainder micro & nano seconds if smaller than sleepTime
        long yieldTime = Math.min(sleepTime, variableYieldTime + sleepTime % (1000*1000));
        long overSleep = 0; // time the sync goes over by
          
        try {
            while (true) {
                long t = System.nanoTime() - lastTime;
                  
                if (t < sleepTime - yieldTime) {
                    Thread.sleep(1);
                }else if (t < sleepTime) {
                    // burn the last few CPU cycles to ensure accuracy
                    Thread.yield();
                }else {
                    overSleep = t - sleepTime;
                    break; // exit while loop
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lastTime = System.nanoTime() - Math.min(overSleep, sleepTime);
             
            // auto tune the time sync should yield
            if (overSleep > variableYieldTime) {
                // increase by 200 microseconds (1/5 a ms)
                variableYieldTime = Math.min(variableYieldTime + 200*1000, sleepTime);
            }
            else if (overSleep < variableYieldTime - 200*1000) {
                // decrease by 2 microseconds
                variableYieldTime = Math.max(variableYieldTime - 2*1000, 0);
            }
        }
    }
    
    // Game loop utilities
    public static void setVsync(boolean enable) {
        useVsync = enable;
        if (useVsync)
            glfwSwapInterval(1);
        else
            glfwSwapInterval(0);
            
    }
    public static void setFPS(int fps) {
        targetFps = fps;
    }
    public static void displayDeltaInTitle(boolean enable) {
        displayDelta = enable;
    }
    
    // Window utilities
    /**
     * Sets the window title. If delta time information is displaying in
     * the window title, it will not be disabled.  If delta time information
     * is disabled later, the name will be reset to the last one provided
     * to this method.
     * @param title 
     */
    public static void setWindowTitle(String title) {
        windowTitle = title;
        if (window != -1)
            glfwSetWindowTitle(window, title);
    }
    public static long getWindowID() { return window; }
    /**
     * Returns the size of the main framebuffer, in pixels.
     * @return 
     */
    public static int[] getFramebufferSize() {
        return new int[] {fbWidth, fbHeight};
    }
    /**
     * Returns the size of the window, in screen size.
     * On hidpi screens, this is may not be the same size as the framebuffer.
     * @return 
     */
    public static int[] getWindowSize() {
        return new int[] {windowWidth, windowHeight};
    }
    /**
     * Returns the size of the display, in screen size.  Same scale as
     * getWindowSize().
     * @return 
     */
    public static int[] getDisplaySize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new int[] {screenSize.width,
                          screenSize.height};
    }

    /**
     * Switch to a windowed display mode.
     * The window will be centered in the screen.
     * @param w
     * @param h
     */
    public static void setWindowed(int w, int h) {
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(monitor);
        glfwWindowHint(GLFW_RED_BITS, mode.redBits());
        glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
        glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());
        glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());
        glfwSetWindowMonitor(window, NULL,
            (mode.width() - w) / 2, // x,y position
            (mode.height() - h) / 2,
            w, h, mode.refreshRate());
    }
    /**
     * Switch to windowed context based on the size of the monitor.
     * @param percentHeight Percent size of the monitor the window height should be. For example,
     *                      .5 would be 50%, or a window half the height of the display.
     * @param aspectRatio The aspect ratio of the window, used to calculate the width.
     */
    public static void setWindowedPercent(double percentHeight, double aspectRatio) {
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(monitor);

        int h = (int)(mode.height()*percentHeight);
        int w = (int)(h*aspectRatio);

        glfwWindowHint(GLFW_RED_BITS, mode.redBits());
        glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
        glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());
        glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());
        glfwSetWindowMonitor(window, NULL,
                (mode.width() - w) / 2, // x,y position
                (mode.height() - h) / 2,
                w, h, mode.refreshRate());
    }
    /**
     * Switch to an exclusive fullscreen mode.
     */
    public static void setFullscreen() {
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(monitor);
        glfwWindowHint(GLFW_RED_BITS, mode.redBits());
        glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
        glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());
        glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());
        glfwSetWindowMonitor(window, monitor, 0,0, mode.width(), mode.height(), mode.refreshRate());
    }
    
    // Cursor utilities
    /**
     * The cursor will be invisible but not grabbed.
     * @param hide
     */
    public static void setCursorHidden(boolean hide) {
        if (hide)
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        else
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }
    /**
     * The position of the cursor, in screen coordinates.
     * Divide by the values of getWindowSize() for normalized (0,1) coordinates.
     * @return 
     */
    public static double[] getCursorPos() {
        return new double[] {cursorX, cursorY};
    }
    
    // Keyboard utilities
    public static boolean keyPressed(int key) {
        return glfwGetKey(window, key) == 1;
    }
}