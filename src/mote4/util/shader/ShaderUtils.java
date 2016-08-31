package mote4.util.shader;

import java.io.BufferedReader;
import java.io.IOException;
import mote4.scenegraph.Window;
import mote4.util.FileIO;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

/**
 * This code kindly provided by the lwjgl wiki.
 * It works well enough that I don't feel like writing my own.
 */
public class ShaderUtils {
    
    public static int VERTEX = GL20.GL_VERTEX_SHADER;
    public static int FRAGMENT = GL20.GL_FRAGMENT_SHADER;
    public static int GEOMETRY = GL32.GL_GEOMETRY_SHADER;
    
    /**
     * Loads a file located in res/shaders and returns it in a string.
     * File names must include the extension.
     * @param sourcePath The name of the file.
     * @return A string containing the content of the file.
     */
    public static String loadSource(String sourcePath) {
        StringBuilder source = new StringBuilder();
        try 
        {
            BufferedReader reader = FileIO.getBufferedReader("/res/shaders/"+sourcePath);
            String line;
            while ((line = reader.readLine()) != null) {
                source.append(line).append("\n");
            }
            reader.close();
        } 
        catch (IOException e) 
        {
            System.err.println("Problem loading shader.\nMost likely the file could not be found.");
            e.printStackTrace();
            Window.destroy();
        }
        return source.toString();
    }
    /**
     * Loads a file located in res/shaders and returns it as a StringBuilder.
     * @param name The name of the file.
     * @return A string containing the content of the file.
     */
    public static StringBuilder loadSourceAsStringBuilder(String sourcePath) {
        StringBuilder source = new StringBuilder();
        try 
        {
            BufferedReader reader = FileIO.getBufferedReader("/res/shaders/"+sourcePath);
            String line;
            while ((line = reader.readLine()) != null) {
                source.append(line).append("\n");
            }
            reader.close();
        } 
        catch (IOException e) 
        {
            System.err.println("Problem loading shader.\nMost likely the file could not be found.");
            e.printStackTrace();
            Window.destroy();
        }
        return source;
    }
    /**
     * Creates and compiles a shader of the given type with the given source.
     * If OpenGL returns a compile error, the shader source and error log will be printed and the program will exit.
     * The handle of the shader created will be stored in ShaderMap, but is used only when releasing resources.
     * @param source A string containing the shader source.
     * @param type The type of the shader, either ShaderUtils.VERTEX or ShaderUtils.FRAGMENT.
     * @return The ID of the shader.
     */
    public static int compileShaderFromSource(String source, int type) {
        int id = GL20.glCreateShader(type);
        //Util.checkGLError();
        GL20.glShaderSource(id, source);
        //Util.checkGLError();
        GL20.glCompileShader(id);
        
        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(id, 1000);
            System.err.println("Error compiling shader:\n" + source+"\n------------------\n"+log);
            Window.destroy();
            return -1;
        }
        //Util.checkGLError();
        return id;
    }
    /**
     * Creates a program containing the specified shaders.
     * The program is automatically added to the shader map.
     * @param shaders The shaders to be used.
     * @return The id of the program.
     */
    public static int addProgram(int[] shaders, String name) {
        int id = GL20.glCreateProgram();
        for(int shader : shaders) {
            GL20.glAttachShader(id, shader);
            //Util.checkGLError();
        }
        GL20.glLinkProgram(id);
        GL20.glValidateProgram(id);
        //Util.checkGLError();
        ShaderMap.addProgram(id, name);
        return id;
    }
    /**
     * Creates a program containing the specified shaders.
     * The program is automatically added to the shader map.
     * File names must include the extension.
     * @param vertSource The name of the vertex shader source file.
     * @param fragSource The name of the vertex shader source file.
     * @param name The name of the program to use in the shader map.
     * @return The id of the program.
     */
    public static int addProgram(String vertSource, String fragSource, String name) {
        vertSource = ShaderUtils.loadSource(vertSource);
        fragSource = ShaderUtils.loadSource(fragSource);
        int vert = ShaderUtils.compileShaderFromSource(vertSource, ShaderUtils.VERTEX);
        int frag = ShaderUtils.compileShaderFromSource(fragSource, ShaderUtils.FRAGMENT);
        int prog = ShaderUtils.addProgram(new int[] {vert, frag}, name);
        return prog;
    }
    
    /**
     * Allows GLSL fragment shaders to use different colors for front and back facing colors.
     * Without this, gl_BackColor in the vertex shader is ignored. It is disabled by default.
     */
    public static void enableTwoSide() {
        GL11.glEnable(GL20.GL_VERTEX_PROGRAM_TWO_SIDE);
    }
    /**
     * Disallows GLSL fragment shaders to use different colors for front and back facing colors.
     * With this, gl_BackColor in the vertex shader is ignored. It is disabled by default.
     */
    public static void disableTwoSide() {
        GL11.glDisable(GL20.GL_VERTEX_PROGRAM_TWO_SIDE);
    }
    /**
     * Allows GLSL vertex shaders to set the size of the points draw.
     */
    public static void enablePointSize() {
        GL11.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
    }
    /**
     * Disallows GLSL vertex shaders to set the size of the points draw.
     */
    public static void disablePointSize() {
        GL11.glDisable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
    }
    
    /**
     * @return A string representing the maximum possible version of GLSL obtainable in the current OpenGL context.
     */
    public static String getMaxGLSLVersion() {
        return GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION);
    }
}
