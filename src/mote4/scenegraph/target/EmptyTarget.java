package mote4.scenegraph.target;

/**
 * A dummy Target which does nothing.
 * @author Peter
 */
public class EmptyTarget extends Target {
    
    @Override
    public void makeCurrent() {}

    @Override
    public void destroy() {}
    
}
