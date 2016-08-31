package mote4.util.shader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import mote4.util.texture.TextureMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL11;

/**
 * Utility for setting shader uniform variables.
 * The utility will attempt to apply the uniform value to the current shader program.
 * @author Peter
 */
public class Uniform {
    /**
     * Returns the position of the specified uniform variable.
     * @param program The program containing the variable.
     * @param var The name of the variable.
     * @return  The position of the variable.
     */
    public static int getVarPos(int program, String var) {
        return GL20.glGetUniformLocation(program, var);
    }
    
    /**
     * Sets the value of the specified uniform variable.
     * @param name The name of the variable.
     * @param values The value or values to be set.
     */
    public static void varFloat(String name, float... values) {
        int loc = GL20.glGetUniformLocation(ShaderMap.getCurrent(), name);
        //Util.checkGLError();
        switch(values.length) {
            case 1: GL20.glUniform1f(loc, values[0]); break;
            case 2: GL20.glUniform2f(loc, values[0], values[1]); break;
            case 3: GL20.glUniform3f(loc, values[0], values[1], values[2]); break;
            case 4: GL20.glUniform4f(loc, values[0], values[1], values[2], values[3]); break;
            default: {
                FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
                buffer.put(values);
                buffer.rewind();
                GL20.glUniform1fv(loc, buffer);
            }
        }
        //Util.checkGLError();
    }
    /**
     * Sets the value of the specified uniform variable.
     * @param name The name of the variable.
     * @param values The value or values to be set.
     */
    public static void varInt(String name, int... values) {
        int loc = GL20.glGetUniformLocation(ShaderMap.getCurrent(), name);
        //Util.checkGLError();
        switch(values.length) {
            case 1: {GL20.glUniform1i(loc, values[0]); break;}
            case 2: {GL20.glUniform2i(loc, values[0], values[1]); break;}
            case 3: {GL20.glUniform3i(loc, values[0], values[1], values[2]); break;}
            case 4: {GL20.glUniform4i(loc, values[0], values[1], values[2], values[3]); break;}
            default: {
                IntBuffer buffer = BufferUtils.createIntBuffer(values.length);
                buffer.put(values);
                buffer.rewind();
                GL20.glUniform1iv(loc, buffer);
            }
        }
        //Util.checkGLError();
    }
    /**
     * Sets the value of the specified 4x4 matrix uniform variable.
     * @param name The name of the variable.
     * @param buffer The 4x4 matrix to set.
     */
    public static void mat4(String name, FloatBuffer buffer) {
        int matrixLoc = GL20.glGetUniformLocation(ShaderMap.getCurrent(), name);
        GL20.glUniformMatrix4fv(matrixLoc, false, buffer);
    }
    /**
     * Sets the value of the specified 3x3 matrix uniform variable.
     * @param name The name of the variable.
     * @param buffer The 3x3 matrix to set.
     */
    public static void mat3(String name, FloatBuffer buffer) {
        int matrixLoc = GL20.glGetUniformLocation(ShaderMap.getCurrent(), name);
        //if (matrixLoc == -1)
        //    throw new RuntimeException("The uniform value '"+name+"' does not exist in the specified program.");
        GL20.glUniformMatrix3fv(matrixLoc, false, buffer);
    }
    /**
     * Sets the value of the specified sampler uniform variable.
     * @param name The name of the variable in the shader.
     * @param textureIndex Which texture to send to the shader.  This is NOT the texture handle, but the active texture index.
     */
    public static void sampler(String name, int textureIndex) {
        int texLoc = GL20.glGetUniformLocation(ShaderMap.getCurrent(), name);
        if (texLoc == -1) {
            System.err.println("The uniform sampler '"+name+"' does not exist in the specified program.");
        }
        GL20.glUniform1i(texLoc, textureIndex);
    }
    /**
     * Sets the value of the specified sampler uniform variable.
     * The specified texture is set to the active texture number specified.
     * The texture is linearly filtered.
     * The active texture is not changed after this call.
     * @param name The name of the variable in the shader.
     * @param textureIndex Which active texture number to set.
     * @param texName The name of the texture to set.
     */
    public static void samplerAndTextureFiltered(String name, int textureIndex, String texName) {
        int currentTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        GL13.glActiveTexture(GL13.GL_TEXTURE0+textureIndex);
        TextureMap.bindFiltered(texName);
        int texLoc = GL20.glGetUniformLocation(ShaderMap.getCurrent(), name);
        if (texLoc == -1) {
            System.err.println("The uniform sampler '"+name+"' does not exist in the specified program.");
        }
        GL20.glUniform1i(texLoc, textureIndex);
        GL13.glActiveTexture(currentTexture);
    }
    /**
     * Sets the value of the specified sampler uniform variable.
     * The specified texture is set to the active texture number specified.
     * The texture is not filtered.
     * The active texture is not changed after this call.
     * @param name The name of the variable in the shader.
     * @param textureIndex Which active texture number to set.
     * @param texName The name of the texture to set.
     */
    public static void samplerAndTextureUnfiltered(String name, int textureIndex, String texName) {
        int currentTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        GL13.glActiveTexture(GL13.GL_TEXTURE0+textureIndex);
        TextureMap.bindUnfiltered(texName);
        int texLoc = GL20.glGetUniformLocation(ShaderMap.getCurrent(), name);
        if (texLoc == -1) {
            System.err.println("The uniform sampler '"+name+"' does not exist in the specified program.");
        }
        GL20.glUniform1i(texLoc, textureIndex);
        GL13.glActiveTexture(currentTexture);
    }
    /**
     * Sets the value of the specified uniform array of floats.
     * @param name The name of the variable.
     * @param componentSize How many components each array element has.
     * @param values The values to be set.
     */
    public static void arrayFloat(String name, int componentSize, float[] values) {
        int loc = GL20.glGetUniformLocation(ShaderMap.getCurrent(), name);
        //Util.checkGLError();
        FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
        buffer.put(values);
        buffer.rewind();
        switch (componentSize) {
            case 1: GL20.glUniform1fv(loc, buffer); break;
            case 2: GL20.glUniform2fv(loc, buffer); break;
            case 3: GL20.glUniform3fv(loc, buffer); break;
            case 4: GL20.glUniform4fv(loc, buffer); break;
            default: 
                throw new IllegalArgumentException("The max supported component size for a uniform array is 4");
        }
        //Util.checkGLError();
    }
}