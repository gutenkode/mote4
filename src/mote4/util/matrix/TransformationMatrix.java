package mote4.util.matrix;

import mote4.util.shader.Bindable;
import org.joml.Matrix4f;

import java.util.Stack;

/**
 * A transformation matrix is used to set view transformations in a shader.
 * @author Peter
 */
public abstract class TransformationMatrix implements Bindable {
    
    protected Matrix4f matrix;
    protected Stack<Matrix4f> stack;
    
    protected TransformationMatrix() {
        stack = new Stack<>();
    }
    
    /**
     * Translates the matrix.
     * @param x X transformation.
     * @param y Y transformation.
     */
    public void translate(float x, float y) {
        matrix.translate(x, y, 0);
    }
    /**
     * Translates the matrix.
     * @param x X transformation.
     * @param y Y transformation.
     * @param z Z transformation.
     */
    public void translate(float x, float y, float z) {
        matrix.translate(x, y, z);
    }
    /**
     * Rotates the matrix. The x y and z parameters specify a vector to rotate around.
     * The vector should be normalized.
     * @param r Angle to rotate in degrees.
     * @param x X axis.
     * @param y Y axis.
     * @param z Z axis.
     */
    public void rotate(float r, float x, float y, float z) {
        matrix.rotate(r, x, y, z);
    }
    /**
     * Scales the matrix.
     * @param x X scale.
     * @param y Y scale.
     * @param z Z scale.
     */
    public void scale(float x, float y, float z) {
        matrix.scale(x, y, z);
    }
    /**
     * Reset the matrix to the identity matrix.
     */
    public void setIdentity() { matrix.identity(); }
    
    /**
     * Push a copy of the current matrix onto the stack.
     */
    public void push() {
        Matrix4f mat = new Matrix4f();
        matrix.get(mat);
        stack.push(mat);
    }
    /**
     * Pop the top matrix off the stack.
     */
    public void pop() {
        matrix = stack.pop();
    }
}