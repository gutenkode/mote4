package mote4.util.matrix;

/**
 *
 * @author Peter
 */
public class Transform {
    public ProjectionMatrix projection;
    public ViewMatrix view;
    public ModelMatrix model;
    
    public Transform() {
        projection = new ProjectionMatrix();
        view = new ViewMatrix();
        model = new ModelMatrix();
    }
    public void makeCurrent() {
        projection.makeCurrent();
        view.makeCurrent();
        model.makeCurrent();
    }
}
