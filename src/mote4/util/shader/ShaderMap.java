package mote4.util.shader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mote4.util.ErrorUtils;
import org.lwjgl.opengl.GL20;

/**
 * Global store for shaders.
 * @author Peter
 */
public class ShaderMap {

    private static String currentName = null;
    private static int currentProgram = 0; // 0 = fixed function
    
    private static Map<String,Integer> programMap = new HashMap<>();
    private static Map<Integer, int[]> shaderMap = new HashMap<>();
    
    /**
     * Adds the shader ID to the map of shader programs.
     * @param id The ID of the shader program.
     * @param shaders List of shaders in this program.
     * @param name The name of the shader.
     */
    public static void add(int id, int[] shaders, String name) {
        if (!programMap.containsValue(id)) {
            programMap.put(name, id);
            shaderMap.put(id, shaders);
        }
    }
    
    /**
     * Makes the specified program active, and sets its ID to the getCurrent return.
     * @param name The program to use.
     */
    public static void use(String name) {
        try {
            int program = programMap.get(name);
            GL20.glUseProgram(program);
            currentProgram = program;
            currentName = name;
        } catch (NullPointerException e) {
            System.err.println(name+" is not a valid shader program!");
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the ID of the current program, as set in this class.
     * @return The ID of the program.
     */
    public static int getCurrent() { return currentProgram; }
    public static String getCurrentName() { return currentName; }
    
    /**
     * Returns the shader program ID associated with the given key.
     * @param name The shader to retrieve the ID of.
     * @return The shader ID.
     */
    public static int get(String name) {
        return programMap.getOrDefault(name, -1);
    }

    /**
     * If the specified program is stored in this map,
     * it will be removed and deleted along with its shaders.
     * @param name
     */
    public static void delete(String name) {
        if (programMap.containsKey(name)) {
            int ind = programMap.get(name);

            int[] shaders = shaderMap.get(ind);
            for (int i : shaders)
                GL20.glDeleteShader(i);
            shaderMap.remove(ind);

            GL20.glDeleteProgram(ind);
            programMap.remove(name);

            ErrorUtils.checkGLError();
        }
    }

    /**
     * Deletes all shaders and programs in the map.
     */
    public static void clear() {
        for (int i : programMap.values()) {
            GL20.glDeleteProgram(i);
        }
        programMap.clear();
        for (int[] ii : shaderMap.values())
            for (int i : ii)
                GL20.glDeleteShader(i);
        shaderMap.clear();
    }

}