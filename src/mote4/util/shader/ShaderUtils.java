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
     * Load all shaders specified in an index file.
     * The index file must be in the res/shaders directory.
     */
    public static void loadIndex(String filename) {
        try {
            BufferedReader br = FileIO.getBufferedReader("/res/shaders/"+filename);
            String in;
            while((in = br.readLine()) != null) {
                if (in.isEmpty() || in.startsWith("#")) // skip empty lines or comments
                    continue;
                String[] keys = in.split("\t+");
                if (keys.length >= 3) {
                    for (int i = 0; i < keys.length; i++)
                        keys[i] = keys[i].trim();
                    String[] source = new String[keys.length-1];
                    int[] shaders = new int[keys.length-1];
                    for (int i = 0; i < source.length; i++) {
                        source[i] = ShaderUtils.loadSource(keys[i]);
                        int type;
                        if (keys[i].endsWith(".vert"))
                            type = VERTEX;
                        else if (keys[i].endsWith(".geom"))
                            type = GEOMETRY;
                        else if (keys[i].endsWith(".frag"))
                            type = FRAGMENT;
                        else
                            throw new IllegalArgumentException("Incorrect extension for shader: "+keys[i]+"\nOnly .vert .geom and .frag are supported.");
                        shaders[i] = ShaderUtils.compileShaderFromSource(source[i], type);
                    }
                    ShaderUtils.addProgram(shaders, keys[keys.length-1]);

                } else
                    System.out.println("Invalid shader index line: "+in);
            }
        } catch (IOException e) {
            System.err.println("Error reading shader index file.");
            e.printStackTrace();
            Window.destroy();
        }
    }

    /**
     * Loads a file located in res/shaders and returns it as a string.
     * File name/path must include its extension.
     * @param sourcePath The name of the file.
     * @return A string containing the content of the file.
     */
    public static String loadSource(String sourcePath) {
        return loadSourceAsStringBuilder(sourcePath).toString();
    }
    /**
     * Loads a file located in res/shaders and returns it as a string.
     * File name/path must include its extension.
     * @param sourcePath The name of the file.
     * @return A string containing the content of the file.
     */
    public static StringBuilder loadSourceAsStringBuilder(String sourcePath) {
        StringBuilder source = new StringBuilder();
        String line = null;
        try 
        {
            BufferedReader reader = FileIO.getBufferedReader("/res/shaders/"+sourcePath);
            while ((line = reader.readLine()) != null) {
                source.append(line).append("\n");
            }
            reader.close();
        } 
        catch (IOException e) 
        {
            System.err.println("Problem loading shader: "+sourcePath+"\nMost likely the file could not be found.");
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
        GL20.glShaderSource(id, source);
        GL20.glCompileShader(id);
        
        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(id, 1000);
            System.err.println("#####\nError compiling shader:\n" + source+"\n#####\n"+log+"\n#####");
            Window.destroy();
            return -1;
        }
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
        }
        GL20.glLinkProgram(id);
        GL20.glValidateProgram(id);
        //Util.checkGLError();
        ShaderMap.add(id, shaders, name);
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
}
