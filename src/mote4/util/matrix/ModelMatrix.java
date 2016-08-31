package mote4.util.matrix;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import mote4.util.vector.Matrix4f;
import mote4.util.shader.Uniform;

/**
 * Transforms a model's position relative to the origin in a scene.
 * @author Peter
 */
public class ModelMatrix extends TransformationMatrix {
    
    public ModelMatrix() {
        matrix = new Matrix4f();
        matrix.setIdentity();
    }
    
    @Override
    public void makeCurrent() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.store(buffer);
        buffer.flip();
        
        Uniform.mat4("modelMatrix", buffer);
    }
}