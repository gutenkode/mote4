package mote4.util.matrix;

import java.nio.FloatBuffer;

import mote4.util.shader.Bindable;
import mote4.util.shader.Uniform;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

/**
 * Transformation matrix with a user-definable name.
 * @author Peter
 */
public class GenericMatrix extends TransformationMatrix implements Bindable {
    
    private String uniformName;
    
    protected GenericMatrix(String un) {
        super();
        matrix = new Matrix4f();
        matrix.identity();
        //setPerspectiveMatrix(width,height,.1f,100f, 60f);
        //setOrthographicMatrix(0,0,width,height,-1,1);
        uniformName = un;
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
    public void setPerspective(float width, float height, float near, float far, float fieldOfView) {
        matrix = new Matrix4f();
        
        //float fieldOfView = 60f;
        float aspectRatio = width/height;
        
        float y_scale = 1/(float)Math.tan(Math.toRadians(fieldOfView / 2f));
        float x_scale = y_scale / aspectRatio;
        float frustum_length = far - near;

        matrix.m00(x_scale);
        matrix.m11(y_scale);
        matrix.m22(-((far + near) / frustum_length));
        matrix.m23(-1);
        matrix.m32(-((2 * near * far) / frustum_length));
        matrix.m33(0);
    }
    /**
     * Creates an orthographic projection.
     * @param left Left clipping plane coordinate.
     * @param top Top clipping plane coordinate.
     * @param right Right clipping plane coordinate.
     * @param bottom Bottom clipping plane coordinate.
     * @param near Near clipping plane coordinate.
     * @param far Far clipping plane coordinate.
     */
    public void setOrthographic(float left, float top, float right, float bottom, float near, float far) {
        matrix = new Matrix4f();
        
        matrix.m00(2/(right-left));
        matrix.m11(2/(top-bottom));
        matrix.m22(-2/(far-near));
        matrix.m33(1);
        matrix.m30(-(right+left)/(right-left));
        matrix.m31(-(top+bottom)/(top-bottom));
        matrix.m32(-(far+near)/(far-near));
    }
    
    @Override
    public void bind() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.get(buffer);
        
        Uniform.mat4(uniformName, buffer);
    }
}