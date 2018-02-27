package mote4.util.vertex.mesh;

import java.util.HashMap;

/**
 * Global store for meshes.
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
    public static boolean contains(String name) {
        return map.containsKey(name);
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
