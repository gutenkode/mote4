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
     * Sets the view matrix to an orthographic view, making walls and floors appear like 2D tiles.
     * Does not reset to identity first.
     */
    public void setOrthoView() {
        view.rotate((float)Math.PI/4, 1,0,0);
        float sqrt2 = (float)Math.sqrt(2);
        view.scale(1, sqrt2, sqrt2);
    }
}