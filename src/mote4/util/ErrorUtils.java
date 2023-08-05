package mote4.util;

import mote4.scenegraph.Window;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.alcGetCurrentContext;
import static org.lwjgl.openal.ALC10.alcGetError;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS;

/**
 *
 * @author Peter
 */
public class ErrorUtils {

    private static boolean debug = true;

    public static void debug(boolean d) { debug = d; }
    public static boolean debug() { return debug; }
    
    public static void checkFBOCompleteness(int ID) {
        if (!debug)
            return;
        int error = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        switch (error) {
            case GL_FRAMEBUFFER_COMPLETE:
                    break;
            case GL_FRAMEBUFFER_UNDEFINED:
                throw new RuntimeException( "FrameBuffer: " + ID
                        + ", has caused a GL_FRAMEBUFFER_UNDEFINED exception." );
            case GL_FRAMEBUFFER_UNSUPPORTED:
                throw new RuntimeException( "FrameBuffer: " + ID
                        + ", has caused a GL_FRAMEBUFFER_UNSUPPORTED exception." );
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                    throw new RuntimeException( "FrameBuffer: " + ID
                                    + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT exception." );
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                    throw new RuntimeException( "FrameBuffer: " + ID
                                    + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT exception." );
            case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
                    throw new RuntimeException( "FrameBuffer: " + ID
                                    + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE exception." );
            case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS:
                    throw new RuntimeException( "FrameBuffer: " + ID
                                    + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS exception." );
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                throw new RuntimeException( "FrameBuffer: " + ID
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER exception." );
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                    throw new RuntimeException( "FrameBuffer: " + ID
                                    + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER exception." );
            default:
                    throw new RuntimeException("Unexpected reply from glCheckFramebufferStatus: " + error);
        }
    }

    public static void checkGLError() {
        if (!debug)
            return;
        if (Window.getWindowID() == -1)
            return;
        int error = glGetError();
        switch (error) {
            case GL_NO_ERROR:
                break;
            case GL_INVALID_ENUM:
                throw new RuntimeException("glGetError result: GL_INVALID_ENUM");
            case GL_INVALID_VALUE:
                throw new RuntimeException("glGetError result: GL_INVALID_VALUE");
            case GL_INVALID_OPERATION:
                throw new RuntimeException("glGetError result: GL_INVALID_OPERATION");
            case GL_INVALID_FRAMEBUFFER_OPERATION:
                throw new RuntimeException("glGetError result: GL_INVALID_FRAMEBUFFER_OPERATION");
            case GL_OUT_OF_MEMORY:
                throw new RuntimeException("glGetError result: GL_OUT_OF_MEMORY");
            default:
                throw new RuntimeException("Unexpected glGetError result: "+error);
        }
    }

    public static void checkALError() {
        if (!debug)
            return;
        int error = alcGetError(alcGetCurrentContext());
        switch (error) {
            case AL_NO_ERROR:
                break;
            case AL_INVALID_NAME:
                throw new RuntimeException("alGetError result: AL_INVALID_NAME: Invalid name parameter.");
            case AL_INVALID_ENUM:
                throw new RuntimeException("alGetError result: AL_INVALID_ENUM: Invalid parameter.");
            case AL_INVALID_VALUE:
                throw new RuntimeException("alGetError result: AL_INVALID_VALUE: Invalid enum parameter value.");
            case AL_INVALID_OPERATION:
                throw new RuntimeException("alGetError result: AL_INVALID_OPERATION: Illegal call.");
            case AL_OUT_OF_MEMORY:
                throw new RuntimeException("alGetError result: AL_OUT_OF_MEMORY: Unable to allocate memory.");
            default:
                throw new RuntimeException("Unexpected alGetError result: "+error);
        }
    }
}
