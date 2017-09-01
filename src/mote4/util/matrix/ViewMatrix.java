package mote4.util.matrix;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import mote4.util.shader.Uniform;

/**
 * Transforms the camera position in a scene.
 * @author Peter
 */
public class ViewMatrix extends TransformationMatrix {
    
    public ViewMatrix() {
        super();
        matrix = new Matrix4f();
        matrix.identity();
    }
    
    /**
     * Sets the view matrix to an orthographic view, making walls and floors appear like 2D tiles.
     * Does not reset to identity first.
     */
    public void setOrthoView() {
        matrix.rotate((float)Math.PI/4, 1,0,0);
        float sqrt2 = (float)Math.sqrt(2);
        matrix.scale(1, sqrt2, sqrt2);
    }

    @Override
    public void makeCurrent() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.get(buffer);
        
        Uniform.mat4("viewMatrix", buffer);
    }
}