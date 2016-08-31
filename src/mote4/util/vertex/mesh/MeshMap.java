package mote4.util.vertex.mesh;

import java.util.HashMap;

/**
 *
 * @author Peter
 */
public class MeshMap {
    
    private static HashMap<String, Mesh> map = new HashMap<>();
    
    public static void add(Mesh m, String name) {
        map.put(name, m);
    }
    public static Mesh get(String name) {
        return map.get(name);
    }
    public static void render(String name) {
        map.get(name).render();
    }
    public static void delete(String name) {
        if (map.containsKey(name)) {
            map.get(name).destroy();
            map.remove(name);
        }
    }
    public static void clear() {
        for (Mesh m : map.values())
            m.destroy();
        map.clear();
    }
}
