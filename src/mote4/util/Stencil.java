package mote4.util;

import static org.lwjgl.opengl.GL11.*;

/**
 * Stencil buffer utility methods.
 * @author Peter
 */
public class Stencil
{
    /**
     * Enables the stencil test.  The window must have been constructed with a bit for stenciling to have any effect.
     * All render calls after this method and before beginStencilRendering will render to the stencil buffer.
     */
    public static void beginStencilTest() {
        glEnable(GL_STENCIL_TEST);

        glStencilFunc(GL_ALWAYS, 1, ~0); // Set stencil to 1 where rendered
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);  // replace where rendered

        glClearStencil(0);
        glClear(GL_STENCIL_BUFFER_BIT);
    }

    /**
     * Begins drawing in the selected stencil buffer areas.
     * All render calls after this statement will only render in areas selected after beginStencilTest.
     * @param inside Whether the area to render is inside or outside of the stenciled geometry.
     */
    public static void beginStencilRendering(boolean inside) {
        int type;
        if (inside)
            type = GL_NOTEQUAL;
        else
            type = GL_EQUAL;

        glStencilFunc(type, 0, ~0); // where the pixel is either equal or not equal to 0
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);  // keep the original pixel
    }

    /**
     * Ends all stencil buffering and returns to normal rendering.
     * The OpenGL state is returned to a normal rendering mode.
     */
    public static void endStencilTest() {
        glDisable(GL_STENCIL_TEST);
    }
}
