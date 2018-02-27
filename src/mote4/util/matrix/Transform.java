package mote4.util.matrix;

import mote4.util.shader.Bindable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Encapsulates a projection, view, and model matrix in one object.
 * @author Peter
 */
public class Transform implements Bindable {

    private static Transform currentTransform;

    public static void rebindCurrentTransform() {
        if (currentTransform != null)
            currentTransform.bind();
    }

    //////////////////

    public TransformationMatrix projection,view,model;
    private boolean enableSetCurrent = false;
    
    public Transform() {
        this(false);
    }
    public Transform(boolean e) {
        enableSetCurrent = e;
        projection = new TransformationMatrix("projectionMatrix");
        view = new TransformationMatrix("viewMatrix");
        model = new TransformationMatrix("modelMatrix");
    }

    @Override
    public void bind() {
        if (enableSetCurrent)
            currentTransform = this;
        projection.bind();
        view.bind();
        model.bind();
    }

    /**
     * TODO this currently DOES NOT WORK!
     * Calculates the 2D screen location of a 3D point when passed through this Transform.
     * Useful for creating 2D UI elements that hover over 3D objects.
     * @return
     */
    public float[] get2DCoords(float... coord) {
        Vector4f vec = new Vector4f(coord[0], coord[1], coord[2],0);
        Matrix4f mvp = new Matrix4f();
        //view.matrix.mul(model.matrix,mvp);
        projection.matrix.mul(view.matrix, mvp);
        mvp.mul(model.matrix);
        mvp.transform(vec);
        //vec = vec.mul(projection.matrix).mul(view.matrix).mul(model.matrix);
        return new float[] {vec.x, vec.y, vec.z};
        //return new float[] {.5f,.5f};
    }

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