package mote4.util.shader;

import java.util.ArrayList;
import java.util.HashMap;
import org.lwjgl.opengl.GL20;

/**
 *
 * @author Peter
 */
public class ShaderMap {
    
    private static int currentProgram = 0;
    
    private static HashMap<String,Integer> programMap = new HashMap<>();
    private static ArrayList<Integer> shaderMap = new ArrayList<>();
    
    /**
     * Adds the shader's handle to a list of all created shaders.
     * The list is only used when releasing resources.
     * @param id 
     */
    public static void addShader(int id) {
        if (!shaderMap.contains(id))
            shaderMap.add(id);
    }
    
    /**
     * Adds the shader ID to the map of shader programs.
     * @param id The ID of the shader program.
     * @param name The name of the shader.
     */
    public static void addProgram(int id, String name) {
        if (!programMap.containsValue(id))
            programMap.put(name, id);
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
        } catch (NullPointerException e) {
            System.err.println(name+" is not a valid shader program!");
            e.printStackTrace();
        }
    }
    
    /**
     * Enables fixed-function pipeline rendering.
     */
    public static void useFixedFunction() {
        GL20.glUseProgram(0);
        currentProgram = 0;
    }
    
    /**
     * Returns the ID of the current program, as set in this class.
     * @return The ID of the program.
     */
    public static int getCurrent() {
        return currentProgram;
    }
    
    /**
     * Returns the shader program ID associated with the given key.
     * @param name The shader to retrieve the ID of.
     * @return The shader ID.
     */
    public static int get(String name) {
        return programMap.get(name);
    }
    
    /**
     * Deletes all shader programs in the map, and all shaders created through ShaderUtils.createShader().
     */
    public static void clear() {
        for (int i : programMap.values()) {
            GL20.glDeleteProgram(i);
        }
        programMap.clear();
        for (int i : shaderMap)
            GL20.glDeleteShader(i);
        shaderMap.clear();
    }
    
    /**
     * If the specified program is stored in this map, it will be removed and deleted.
     * This method does not delete shaders, only programs.
     * @param name 
     */
    public static void delete(String name) {
        if (programMap.containsKey(name)) {
            int ind = programMap.get(name);
            GL20.glDeleteProgram(ind);
            programMap.remove(name);
        }
    }
}