package mote4.util.matrix;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import mote4.util.shader.Uniform;

/**
 * Transforms a model's position relative to the origin in a scene.
 * @author Peter
 */
public class ModelMatrix extends TransformationMatrix {
    
    public ModelMatrix() {
        super();
        matrix = new Matrix4f();
        matrix.identity();
    }
    
    @Override
    public void bind() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.get(buffer);
        
        Uniform.mat4("modelMatrix", buffer);
    }
}