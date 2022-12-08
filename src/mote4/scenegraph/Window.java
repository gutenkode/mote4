package mote4.scenegraph;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import mote4.scenegraph.target.Framebuffer;
import mote4.util.ErrorUtils;
import mote4.util.audio.ALContext;
import mote4.util.audio.AudioPlayback;
import mote4.util.shader.ShaderMap;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.mesh.MeshMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.GL_SHADING_LANGUAGE_VERSION;
import static org.lwjgl.stb.STBImage.stbi_load;
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
    private static long currentMonitor;
    private static double DELTA_MAX = .04; // highest delta value a frame can have
    
    private static final int RESIZABLE = GLFW_TRUE;
    private static String windowTitle = "mote4 Engine";
    
    private static boolean useVsync = true, // default setting for vsync unless specified
                            displayDelta = false,
                            isFullscreen = false,
                            windowHasFocus = false;
    private static int targetFps = 60, // when vsync is disabled, use this framerate
                        windowWidth = -1, windowHeight = -1, // window size
                        fbWidth = -1, fbHeight = -1; // framebuffer size
    private static double cursorX = -1, cursorY = -1, // cursor position
                          deltaTime, currentTime;
    
    private static ArrayList<Layer> layers;
    private static Layer defaultLayer;

    /**
     * Initialize the GLFW window and OpenGL context.
     * The window will be fullscreen at the monitor's native resolution.
     */
    public static void initFullscreen() {
        if (window != -1)
            return; // the window has already been initialized
        isFullscreen = true;
        init(0,0,true,false,0,0);
    }
    /**
     * Initialize the GLFW window and OpenGL context.
     * The window will be in windowed mode.
     * @param w
     * @param h
     */
    public static void initWindowed(int w, int h) {
        if (window != -1)
            return; // the window has already been initialized
        isFullscreen = false;
        init(w,h,false,false,0,0);
    }
    /**
     * Initializes a windowed context based on the size of the monitor.
     * @param percentHeight Percent size of the monitor the window height should be. For example,
     *                      .5 would be 50%, or a window half the height of the display.
     * @param aspectRatio The aspect ratio of the window, used to calculate the width.
     */
    public static void initWindowedPercent(double percentHeight, double aspectRatio) {
        if (window != -1)
            return; // the window has already been initialized
        isFullscreen = false;
        init(0, 0, false,true,percentHeight,aspectRatio);
    }

    private static void init(int w, int h, boolean fullscreen, boolean percent, double percentHeight, double aspectRatio) {
        if (window != -1)
            return; // the window has already been initialized

        defaultLayer = new Layer(Framebuffer.getDefault());
        layers = new ArrayList<>();

        createContext(w,h,fullscreen,percent,percentHeight,aspectRatio);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();
        ErrorUtils.checkGLError();

        System.out.println("OpenGL version: " + glGetString(GL_VERSION));
        System.out.println("LWJGL version:  " + Version.getVersion());
        System.out.println("GLSL Version:   " + glGetString(GL_SHADING_LANGUAGE_VERSION));
        System.out.println("Renderer:       " + glGetString(GL_RENDERER));
    }
    private static void createContext(int initWidth, int initHeight, boolean fullscreen, boolean percent, double percentHeight, double aspectRatio) {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();
 
        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW.");
 
        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, RESIZABLE); // set the window resizable state
        if (System.getProperty("os.name").toLowerCase().contains("mac"))
            glfwWindowHint(GLFW_AUTO_ICONIFY, GLFW_FALSE); // mac fullscreen behavior works better with this disabled
        else
            glfwWindowHint(GLFW_AUTO_ICONIFY, GLFW_TRUE); // whether the window auto-minimizes on focus loss when fullscreen

        // target OpenGL 3.3 core
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        // create the window
        currentMonitor = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(currentMonitor);
        if (fullscreen) {
            // width and height are ignored
            window = glfwCreateWindow(mode.width(), mode.height(), windowTitle, currentMonitor, NULL);
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

            // get the resolution of the primary monitor,
            // center the window
            glfwSetWindowPos(
                window,
                (mode.width() - initWidth) / 2,
                (mode.height() - initHeight) / 2
            );
        }

        // default callbacks and handling for window resizing
        createCallbacks();

        // make the OpenGL context current
        glfwMakeContextCurrent(window);
        setVsync(useVsync); // will disable vsync if windowed, otherwise vsync state depends on flag
 
        // make the window visible
        glfwShowWindow(window);
    }

    private static void createCallbacks()
    {
        // key callback, called every time a key is pressed, repeated, or released
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

        // callback for window focus gain/loss
        glfwSetWindowFocusCallback(window, (window, focused) -> {
            windowHasFocus = focused;
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

    /**
     * Run the game loop with the current list of Layers and Scenes.
     * @param fps The framerate to run at. Vsync overrides any value.  A value <=0 means unlimited framerate.
     */
    public static void loop(int fps) {
        targetFps = fps;loop();
    }
    public static void loop() {
        glfwSetTime(0); // largely unnecessary
        double lastTime = glfwGetTime();
        glClearColor(0, 0, 0, 0);
        
        // if the default layer has any scenes, add it to the END of the list
        if (defaultLayer.numScenes() > 0)
            layers.add(defaultLayer);
        // initialize scenes
        for (Layer l : layers)
            l.framebufferResized(fbWidth, fbHeight);
 
        // run the rendering loop until the user has attempted to close the window
        try {
            while (!glfwWindowShouldClose(window)) {
                // calculate deltaTime from start of last frame
                currentTime = glfwGetTime();
                deltaTime = (currentTime - lastTime);
                lastTime = currentTime;
                if (displayDelta) {
                    double displayFps = 1/deltaTime;
                    double displayDelta = deltaTime*1000;
                    glfwSetWindowTitle(window,"Delta: "+String.format("%.2f", displayDelta)+"\tFPS: "+String.format("%.1f", displayFps)+"/"+targetFps);
                }

                AudioPlayback.updateMusic(); // TODO call this in a separate thread to prevent missed updates

                deltaTime = Math.min(DELTA_MAX, deltaTime); // prevent delta from exceeding 1/25, 25fps

                for (Layer l : layers)
                    l.update(currentTime, deltaTime);
                // all updates are performed before all renders
                for (Layer l : layers) {
                    l.makeCurrent();
                    l.render(currentTime, deltaTime);
                }

                glfwSwapBuffers(window); // swap the color buffers

                // Poll for window events. The key callback above will only be
                // invoked during this call.
                glfwPollEvents();

                if (!isFullscreen || !useVsync) // sync manually if vsync is disabled or in windowed mode
                    sync(targetFps);
                else
                    glfwSwapInterval(1); // TODO this fixes vsync not applying when it is enabled and fullscreen is enabled, but it's called every frame... probably fine?
            }
            System.out.println("Window was closed, terminating...");
        } catch (Exception e) {
            System.err.println("Uncaught exception in game loop:");
            e.printStackTrace();
        }
        destroy();
    }
    /**
     * Shuts down GLFW and all resources used in the engine.
     * The program will shut down after this call.
     */
    public static void destroy() {
        try {
            System.out.println("Shutting down...");
            ALContext.destroyContext(); // deletes all audio buffers and sources

            MeshMap.clear();
            ShaderMap.clear();
            TextureMap.clear();

            // free the window callbacks and destroy the window
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
            // terminate GLFW and free the error callback
            glfwTerminate();
            glfwSetErrorCallback(null).free();

            System.out.println("GLFW terminated.");
        } catch (Exception e) {
            System.err.println("An error occurred while shutting down:");
            e.printStackTrace();
            System.exit(1);
        }
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
                } else if (t < sleepTime) {
                    // burn the last few CPU cycles to ensure accuracy
                    Thread.yield();
                } else {
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
        if (isFullscreen) {
            if (useVsync)
                glfwSwapInterval(1);
            else
                glfwSwapInterval(0);
        } else
            glfwSwapInterval(0); // do not use vsync when windowed
            
    }
    public static boolean isVsyncEnabled() { return useVsync; }
    public static void setFPS(int fps) {
        targetFps = fps;
    }
    public static int getFPS() {
        return targetFps;
    }
    public static void displayDeltaInTitle(boolean enable) {
        displayDelta = enable;
    }
    public static int getCurrentMonitorRefreshRate() {
        return glfwGetVideoMode(currentMonitor).refreshRate();
    }
    
    // Window utilities

    /**
     * Sets the window title. If deltaTime time information is displaying in
     * the window title, it will not be disabled.  If deltaTime time information
     * is disabled later, the name will be set to the last one provided to this method.
     * @param title 
     */
    public static void setTitle(String title) {
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

    public static boolean windowHasFocus() { return windowHasFocus; }

    public static int getNumMonitors() {
        PointerBuffer glfwMonitors = glfwGetMonitors();
        return glfwMonitors.limit();
    }

    public static long getCurrentMonitor() {
        return currentMonitor;
    }

    public static void setCurrentMonitor(int monitorIndex, boolean fullscreen) {
        PointerBuffer glfwMonitors = glfwGetMonitors();
        currentMonitor = glfwMonitors.get(monitorIndex);
        if (fullscreen)
            setFullscreen();
        else
            setWindowedPercent(.85, 16 / 9d);
    }

    public static String getMonitorName(int monitorIndex) {
        PointerBuffer glfwMonitors = glfwGetMonitors();
        return glfwGetMonitorName(glfwMonitors.get(monitorIndex));
    }

    public static String getMonitorName(long monitor) {
        return glfwGetMonitorName(monitor);
    }

    public static int getCurrentMonitorIndex() {
        PointerBuffer glfwMonitors = glfwGetMonitors();
        for (int i = 0; i < glfwMonitors.limit(); i++) {
            if (glfwMonitors.get(i) == currentMonitor)
                return i;
        }
        return 0;
    }

    public static int[] getMonitorDefaultMode(int monitorIndex) {
        PointerBuffer glfwMonitors = glfwGetMonitors();
        var monitor = glfwMonitors.get(monitorIndex);

        var vidMode = glfwGetVideoMode(monitor);

        return new int[] { vidMode.width(), vidMode.height(), vidMode.refreshRate() };
    }

    /**
     * Loads an image and sets it as the application icon.
     */
    public static void setWindowIcon(String filepath) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer imgData;
            IntBuffer comp = stack.mallocInt(1);
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);

            imgData = stbi_load(filepath, w, h, comp, 4);
            if (imgData == null)
                throw new IllegalArgumentException("Could not read image '" + filepath + "': ByteBuffer is null");

            GLFWImage image = GLFWImage.malloc();
            GLFWImage.Buffer buf = GLFWImage.malloc(1);
            image.set(w.get(), h.get(), imgData);
            buf.put(0, image);
            glfwSetWindowIcon(window, buf);
        }
    }

    /**
     * Switch to a windowed display mode.
     * The window will be centered in the screen.
     * @param w
     * @param h
     */
    public static void setWindowed(int w, int h) {
        GLFWVidMode mode = glfwGetVideoMode(currentMonitor);
        glfwWindowHint(GLFW_RED_BITS, mode.redBits());
        glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
        glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());
        glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());
        glfwSetWindowMonitor(window, NULL,
            (mode.width() - w) / 2, // x,y position
            (mode.height() - h) / 2,
            w, h, mode.refreshRate());
        isFullscreen = false;
        glfwSwapInterval(0); // auto-disable vsync
    }
    /**
     * Switch to windowed context based on the size of the monitor.
     * @param percentHeight Percent size of the monitor the window height should be. For example,
     *                      .5 would be 50%, or a window half the height of the display.
     * @param aspectRatio The aspect ratio of the window, used to calculate the width.
     */
    public static void setWindowedPercent(double percentHeight, double aspectRatio) {
        GLFWVidMode mode = glfwGetVideoMode(currentMonitor);

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
        isFullscreen = false;
        glfwSwapInterval(0); // auto-disable vsync
    }
    /**
     * Switch to an exclusive fullscreen mode.
     */
    public static void setFullscreen() {
        GLFWVidMode mode = glfwGetVideoMode(currentMonitor);
        glfwWindowHint(GLFW_RED_BITS, mode.redBits());
        glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
        glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());
        glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());
        glfwSetWindowMonitor(window, currentMonitor, 0,0, mode.width(), mode.height(), mode.refreshRate());
        isFullscreen = true;
        if (useVsync)
            glfwSwapInterval(1); // auto-enable vsync, if flag is set
        else
            glfwSwapInterval(0);

        // The following two lines screw up on (some?) linux
        //glDrawBuffer(0);
        //glClear(GL_COLOR_BUFFER_BIT);
    }
    public static boolean isFullscreen() { return isFullscreen; }
    
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

    // Timing utilities
    public static double delta() { return deltaTime; }
    public static double time() { return currentTime; }
}