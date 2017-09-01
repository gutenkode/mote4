package mote4.util.matrix;

import java.nio.FloatBuffer;
import java.util.Stack;

import mote4.util.shader.Uniform;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

/**
 *
 * @author Peter
 */
public class CubeMapMatrix {

    protected Matrix4f[] matrix;
    private final String uniformName;
    
    public CubeMapMatrix(String un, float near, float far) {
        uniformName = un;
        matrix = new Matrix4f[6];
        for (int i = 0; i < 6; i++) {
            matrix[i] = new Matrix4f();
            setPerspective(matrix[i], 1,1, near,far, 90f);
        }
        // right, left
        matrix[0].rotate( (float)Math.PI/2, new Vector3f(0,1,0));
        matrix[1].rotate(-(float)Math.PI/2, new Vector3f(0,1,0));
        // top, bottom
        matrix[2].rotate( (float)Math.PI/2,new Vector3f(1,0,0));
        matrix[3].rotate(-(float)Math.PI/2,new Vector3f(1,0,0));
        // near, far
        matrix[5].rotate((float)Math.PI,new Vector3f(0,1,0));
    }
    /**
     * Creates a perspective projection.
     * Width and height are used when finding the aspect ratio, passing 16:9 or 16:10 will function the same.
     * @param width Width of the display.
     * @param height Height of the display.
     * @param near Near clipping plane distance, must be greater than 0.
     * @param far Far clipping plane distance.
     * @param fieldOfView The field of view, recommended to be 60.
     */
    private void setPerspective(Matrix4f mat, float width, float height, float near, float far, float fieldOfView) {
        float aspectRatio = width/height;
        
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
    
    public void makeCurrent() {
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
            mat.translate(new Vector3f(x,y,z));
    }
}
