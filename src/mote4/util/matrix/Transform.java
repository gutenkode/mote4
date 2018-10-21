package mote4.util.matrix;

import mote4.util.shader.Bindable;
import mote4.util.shader.Uniform;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

/**
 * Encapsulates a projection, view, and model matrix in one object.
 * @author Peter
 */
public class Transform implements Bindable {

    private static FloatBuffer normalMatrixBuffer = BufferUtils.createFloatBuffer(9);
    private static Transform currentTransform;

    public static void rebindCurrentTransform() {
        if (currentTransform != null)
            currentTransform.bind();
    }

    //////////////////

    public final TransformationMatrix projection,view,model;
    private boolean enableSetCurrent = false, enableNormalMatrix = false;
    private Matrix3f normalMatrix;
    
    public Transform() {
        this(false);
    }
    public Transform(boolean e) {
        enableSetCurrent = e;
        projection = new TransformationMatrix("projectionMatrix");
        view = new TransformationMatrix("viewMatrix");
        model = new TransformationMatrix("modelMatrix");
        normalMatrix = new Matrix3f();
    }

    @Override
    public void bind() {
        if (enableSetCurrent)
            currentTransform = this;
        projection.bind();
        view.bind();
        model.bind();
        if (enableNormalMatrix)
            bindWorldspaceNormalMatrix();
    }

    private void bindWorldspaceNormalMatrix() {
        normalMatrix.identity();

        normalMatrix.set(model.matrix);
        normalMatrix.invert();
        normalMatrix.transpose();

        normalMatrix.get(normalMatrixBuffer);
        Uniform.mat3("normalMatrix", normalMatrixBuffer);
    }

    public void enableNormalMatrix(boolean b) { enableNormalMatrix = b; }

    /**
     * Sets the view matrix to an orthographic view, making walls and floors appear like 2D tiles.
     * Does not reset to identity first.
     */
    public void setOrthoView() {
        view.rotate((float)Math.PI/4, 1,0,0);
        float sqrt2 = (float)Math.sqrt(2);
        view.scale(1, sqrt2, sqrt2);
    }
}