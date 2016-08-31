package mote4.util;

import org.lwjgl.opengl.ARBFramebufferObject;

/**
 *
 * @author Peter
 */
public class FBOUtils {
    
    public static void checkCompleteness(int ID) {
        int framebuffer = ARBFramebufferObject.glCheckFramebufferStatus( ARBFramebufferObject.GL_FRAMEBUFFER ); 
        switch ( framebuffer ) {
            case ARBFramebufferObject.GL_FRAMEBUFFER_COMPLETE:
                    break;
            case ARBFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                    throw new RuntimeException( "FrameBuffer: " + ID
                                    + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT exception." );
            case ARBFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                    throw new RuntimeException( "FrameBuffer: " + ID
                                    + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT exception." );
            /*
            case ARBFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
                    throw new RuntimeException( "FrameBuffer: " + ID
                                    + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS exception." );
            */ 
            case ARBFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                    throw new RuntimeException( "FrameBuffer: " + ID
                                    + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER exception." );
            /*
            case ARBFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_FORMATS:
                    throw new RuntimeException( "FrameBuffer: " + ID
                                    + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_FORMATS exception" );
            */
            case ARBFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                    throw new RuntimeException( "FrameBuffer: " + ID
                                    + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER exception." );
            default:
                    throw new RuntimeException( "Unexpected reply from glCheckFramebufferStatus: " + framebuffer );
        }
    }
}
