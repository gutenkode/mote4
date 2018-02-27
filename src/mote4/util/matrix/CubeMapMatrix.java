package mote4.util.matrix;

import mote4.util.shader.Bindable;
import mote4.util.shader.Uniform;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

/**
 * Stores transforms for a cubemap.
 * @author Peter
 */
public class CubeMapMatrix implements Bindable {

    protected Matrix4f[] matrix;
    private final String uniformName;
    
    public CubeMapMatrix(String un, float near, float far) {
        uniformName = un;
        matrix = new Matrix4f[6];
        for (int i = 0; i < 6; i++) {
            matrix[i] = new Matrix4f();
            //matrix[i].setPerspective(90f,1,near,far);
            setPerspective(matrix[i], near,far);
        }
        // right, left
        matrix[0].rotate( (float)Math.PI/2, 0,1,0);
        matrix[1].rotate(-(float)Math.PI/2, 0,1,0);
        // top, bottom
        matrix[2].rotate( (float)Math.PI/2,1,0,0);
        matrix[3].rotate(-(float)Math.PI/2,1,0,0);
        // near, far
        //matrix[4].rotate(0,0,1,0); // already in position
        matrix[5].rotate((float)Math.PI,0,1,0);
    }

    private void setPerspective(Matrix4f mat, float near, float far) {
        float fieldOfView = 90f;
        float aspectRatio = 1;

        float y_scale = 1/(float)Math.tan(Math.toRadians(fieldOfView / 2f));
        float x_scale = y_scale / aspectRatio;
        float frustum_length = far - near;

        mat.m00(x_scale);
        mat.m11(y_scale);
        mat.m22(-((far + near) / frustum_length));
        mat.m23(-1);
        mat.m32(-((2 * near * far) / frustum_length));
        mat.m33(0);
    }

    @Override
    public void bind() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16*6);
        int i = 0;
        for (Matrix4f mat : matrix) {
            mat.get(i, buffer);
            i += 16;
        }
        
        Uniform.mat4(uniformName, buffer);
    }

    public void translate(float x, float y, float z) {
        for (Matrix4f mat : matrix)
            mat.translate(x,y,z);
    }
}
