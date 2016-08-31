package mote4.scenegraph.target;

/**
 * A dummy Target which does nothing.
 * Inelegant solution to an engine design issue.  Allows a Scene to bind its own
 * target at runtime without wasting overhead in a Layer binding a target.
 * @author Peter
 */
public class EmptyTarget extends Target {
    
    @Override
    public void makeCurrent() {}

    @Override
    public void destroy() {}
    
}
