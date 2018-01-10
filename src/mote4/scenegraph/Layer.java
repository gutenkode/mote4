package mote4.scenegraph;

import java.util.ArrayList;
import java.util.List;

import mote4.scenegraph.target.Target;

/**
 * A Layer contains a Target and a list of Scenes to render.
 * Each Layer is intended to have a unique Target.  Multiple Scenes that
 * need to be rendered to the same Target should be in the same Layer.
 * @author Peter
 */
public class Layer {
    
    protected Target target;
    protected List<Scene> scenes;
    
    public Layer(Target t) {
        target = t;
        scenes = new ArrayList<>();
    }
    
    /**
     * Adds a scene to the list of scenes to render.
     * Scenes are rendered in the order they are added.
     * @param s 
     */
    public void addScene(Scene s) { scenes.add(s); }
    public void removeScene(Scene s) { scenes.remove(s); }
    public int numScenes() { return scenes.size(); }
    /**
     * Remove all Scenes from this Layer.
     */
    public void clearScenes() { scenes.clear(); }
    
    public void setTarget(Target t) { 
        target = t;
    }
    public Target getTarget() { return target; }
    
    public void update(double time, double delta) {
        for (Scene s : scenes)
            s.update(time, delta);
    }
    public void makeCurrent() {
        target.makeCurrent();
    }
    public void render(double time, double delta) {
        for (Scene s : scenes)
            s.render(time, delta);
    }
    public void framebufferResized(int width, int height) {
        for (Scene s : scenes)
            s.framebufferResized(width, height);
    }
    public void destroy() {
        for (Scene s : scenes)
            s.destroy();
    }
}