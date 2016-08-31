package mote4.util.matrix;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import mote4.util.vector.Matrix4f;
import mote4.util.vector.Vector3f;
import mote4.util.shader.Uniform;

/**
 * Transforms the camera position in a scene.
 * @author Peter
 */
public class ViewMatrix extends TransformationMatrix {
    
    public ViewMatrix() {
        matrix = new Matrix4f();
        matrix.setIdentity();
    }
    
    /**
     * Sets the view matrix to an orthographic view, making walls and floors appear like 2D tiles.
     * Does not reset to identity first.
     */
    public void setOrthoView() {
        matrix.rotate((float)Math.PI/4, new Vector3f(1,0,0));
        float sqrt2 = (float)Math.sqrt(2);
        matrix.scale(new Vector3f(1,sqrt2,sqrt2));
    }
    
    @Override
    public void makeCurrent() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.store(buffer);
        buffer.flip();
        
        Uniform.mat4("viewMatrix", buffer);
    }
}