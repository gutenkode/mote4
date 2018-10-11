package mote4.util.vertex.mesh;

import mote4.scenegraph.Window;
import mote4.util.FileIO;
import mote4.util.vertex.builder.StaticMeshBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Global store for meshes.
 * @author Peter
 */
public class MeshMap {
    
    private static HashMap<String, Mesh> map = new HashMap<>();

    /**
     * Load all models specified in an index file.
     * The index file must be in the res/models directory.
     */
    public static void loadIndex(String filename) {
        try {
            BufferedReader br = FileIO.getBufferedReader("/res/models/"+filename);
            String in;
            while((in = br.readLine()) != null) {
                if (in.isEmpty() || in.startsWith("#")) // skip empty lines or comments
                    continue;
                String[] keys = in.split("\t+");
                if (keys.length >= 2) {
                    for (int i = 0; i < keys.length; i++)
                        keys[i] = keys[i].trim();
                    if (keys[0].endsWith(".obj")) {
                        keys[0] = keys[0].substring(0,keys[0].length()-4);
                        add(StaticMeshBuilder.constructVAOFromOBJ(keys[0], false), keys[1]);
                    } else
                        System.err.println("Unsupported model format, only .obj files can be loaded: "+keys[0]);
                } else
                    System.out.println("Invalid shader index line: "+in);
            }
        } catch (IOException e) {
            System.err.println("Error reading model index file.");
            e.printStackTrace();
            Window.destroy();
        }
    }
    
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
