package mote4.util.matrix;

import java.util.Stack;
import mote4.util.vector.Matrix4f;
import mote4.util.vector.Vector2f;
import mote4.util.vector.Vector3f;

/**
 * A transformation matrix is used to set view transformations in a shader.
 * @author Peter
 */
public abstract class TransformationMatrix {
    
    protected Matrix4f matrix;
    protected Stack<Matrix4f> stack;
    
    public TransformationMatrix() {
        stack = new Stack<>();
    }
    
    /**
     * Makes this transformation matrix current.
     */
    public abstract void makeCurrent();
    
    /**
     * Returns the matrix.
     * @return The matrix.
     */
    public Matrix4f matrix() { return matrix; }
    
    /**
     * Translates the matrix.
     * @param x X transformation.
     * @param y Y transformation.
     */
    public void translate(float x, float y) {
        matrix.translate(new Vector2f(x,y));
    }
    /**
     * Translates the matrix.
     * @param x X transformation.
     * @param y Y transformation.
     * @param z Z transformation.
     */
    public void translate(float x, float y, float z) {
        matrix.translate(new Vector3f(x,y,z));
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
        matrix.rotate(r, new Vector3f(x,y,z));
    }
    /**
     * Scales the matrix.
     * @param x X scale.
     * @param y Y scale.
     * @param z Z scale.
     */
    public void scale(float x, float y, float z) {
        matrix.scale(new Vector3f(x,y,z));
    }
    /**
     * Resets the matrix to the identity matrix.
     */
    public void setIdentity() { matrix.setIdentity(); }
    
    /**
     * Pushes a copy of the current matrix onto the stack.
     */
    public void push() {
        Matrix4f mat = new Matrix4f();
        Matrix4f.load(matrix, mat);
        stack.push(mat);
    }
    /**
     * Pops the top matrix off the stack.
     */
    public void pop() {
        matrix = stack.pop();
    }
}