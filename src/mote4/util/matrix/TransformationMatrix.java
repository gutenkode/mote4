package mote4.util.matrix;

import mote4.util.shader.Bindable;
import mote4.util.shader.Uniform;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A transformation matrix is used to set view transformations in a shader.
 * @author Peter
 */
public class TransformationMatrix implements Bindable {

    private static FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

    protected String uniformName;
    protected Matrix4f matrix;
    protected List<Matrix4f> stack;
    protected int stackIndex;
    
    public TransformationMatrix(String name) {
        uniformName = name;
        stack = new ArrayList<>();
        stackIndex = -1;
        matrix = new Matrix4f();
        matrix.identity();
    }
    
    public void translate(float x, float y) {
        matrix.translate(x, y, 0);
    }
    public void translate(float x, float y, float z) {
        matrix.translate(x, y, z);
    }
    public void translate(Vector3f vec) {
        matrix.translate(vec.x, vec.y, vec.z);
    }
    public void translate(Vector3d vec) {
        matrix.translate((float)vec.x, (float)vec.y, (float)vec.z);
    }
    public void rotate(float r, float x, float y, float z) {
        matrix.rotate(r, x, y, z);
    }
    public void scale(float x, float y, float z) {
        matrix.scale(x, y, z);
    }

    public void setIdentity() { matrix.identity(); }
    /*
    public void setPerspective(float width, float height, float zNear, float zFar, float fovy) {
        setPerspective(fovy,width/height,zNear,zFar);
    }
    public void setPerspective(float aspect, float zNear, float zFar, float fovy) {
        matrix.identity();
        matrix.setPerspective(fovy,aspect,zNear,zFar);
    }
    */
    public void setOrthographic(float left, float top, float right, float bottom, float zNear, float zFar) {
        //matrix.identity();
        matrix.setOrtho(left,right,bottom,top,zNear,zFar);
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
        //matrix = new Matrix4f();

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
    /* *
     * Creates an orthographic projection.
     * @param left Left clipping plane coordinate.
     * @param top Top clipping plane coordinate.
     * @param right Right clipping plane coordinate.
     * @param bottom Bottom clipping plane coordinate.
     * @param near Near clipping plane coordinate.
     * @param far Far clipping plane coordinate.
     * /
    public void setOrthographic(float left, float top, float right, float bottom, float near, float far) {
        matrix = new Matrix4f();

        matrix.m00(2/(right-left));
        matrix.m11(2/(top-bottom));
        matrix.m22(-2/(far-near));
        matrix.m33(1);
        matrix.m30(-(right+left)/(right-left));
        matrix.m31(-(top+bottom)/(top-bottom));
        matrix.m32(-(far+near)/(far-near));
    }*/
    
    /**
     * Push a copy of the current matrix onto the stack.
     * This saves the current state of the matrix for recalling later.
     */
    public void push() {
        stackIndex++;
        if (stack.size() <= stackIndex) {
            stack.add(new Matrix4f(matrix));
        } else {
            stack.get(stackIndex).set(matrix);
        }
    }
    /**
     * Pop the top matrix off the stack.
     */
    public void pop() {
        matrix.set(stack.get(stackIndex));
        stackIndex--;
    }

    @Override
    public void bind() {
        //FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.get(buffer);
        Uniform.mat4(uniformName, buffer);
    }

    public void bindAs(String name) {
        //FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.get(buffer);
        Uniform.mat4(name, buffer);
    }

    public Matrix4f matrix() { return matrix; }
}