package mote4.util.shader;

import java.nio.FloatBuffer;

import mote4.util.ErrorUtils;
import mote4.util.texture.TextureMap;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Utility for setting shader uniform variables.
 * The utility will attempt to apply the uniform value to the current shader program.
 * @author Peter
 */
public class Uniform {

    private static int getLoc(String uniformName) {
        int loc = glGetUniformLocation(ShaderMap.getCurrent(), uniformName);
        if (ErrorUtils.debug() && loc == -1) {
            System.err.println("The uniform '"+uniformName+"' does not exist in the program '"+ShaderMap.getCurrentName()+"'.");
        }
        return loc;
    }
    
    /**
     * Sets the value of the specified uniform variable.
     * @param uniformName The name of the uniform.
     * @param values The value or values to be set.
     */
    public static void vec(String uniformName, float... values) {
        int loc = getLoc(uniformName);
        switch(values.length) {
            case 1: glUniform1f(loc, values[0]); break;
            case 2: glUniform2f(loc, values[0], values[1]); break;
            case 3: glUniform3f(loc, values[0], values[1], values[2]); break;
            case 4: glUniform4f(loc, values[0], values[1], values[2], values[3]); break;
            default:
                throw new IllegalArgumentException("The max component size for a vec is 4.");
        }
        ErrorUtils.checkGLError();
    }

    /**
     * Sets the value of the specified uniform variable.
     * @param uniformName The name of the uniform.
     * @param values The value or values to be set.
     */
    public static void vecInt(String uniformName, int... values) {
        int loc = getLoc(uniformName);
        switch(values.length) {
            case 1: glUniform1i(loc, values[0]); break;
            case 2: glUniform2i(loc, values[0], values[1]); break;
            case 3: glUniform3i(loc, values[0], values[1], values[2]); break;
            case 4: glUniform4i(loc, values[0], values[1], values[2], values[3]); break;
            default:
                throw new IllegalArgumentException("The max component size for a vec is 4.");
        }
        ErrorUtils.checkGLError();
    }

    /**
     * Sets the value of the specified uniform array of floats.
     * @param uniformName The name of the uniform.
     * @param componentSize How many components each array element has.
     * @param values The values to be set.
     */
    public static void array(String uniformName, int componentSize, float... values) {
        int loc = getLoc(uniformName);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
        buffer.put(values);
        buffer.rewind();
        switch (componentSize) {
            case 1: glUniform1fv(loc, buffer); break;
            case 2: glUniform2fv(loc, buffer); break;
            case 3: glUniform3fv(loc, buffer); break;
            case 4: glUniform4fv(loc, buffer); break;
            default:
                throw new IllegalArgumentException("The max component size for a vec is 4.");
        }
        ErrorUtils.checkGLError();
    }

    /**
     * Sets the value of the specified 4x4 matrix uniform variable.
     * @param uniformName The name of the uniform.
     * @param buffer The 4x4 matrix to set.
     */
    public static void mat4(String uniformName, FloatBuffer buffer) {
        int matrixLoc = getLoc(uniformName);
        glUniformMatrix4fv(matrixLoc, false, buffer);
    }

    /**
     * Sets the value of the specified 3x3 matrix uniform variable.
     * @param uniformName The name of the uniform.
     * @param buffer The 3x3 matrix to set.
     */
    public static void mat3(String uniformName, FloatBuffer buffer) {
        int matrixLoc = getLoc(uniformName);
        glUniformMatrix3fv(matrixLoc, false, buffer);
    }

    /**
     * Sets the value of the specified sampler uniform variable.
     * @param uniformName The name of the uniform.
     * @param textureIndex Which texture to send to the shader.  This is NOT the texture handle, but the active texture index.
     */
    public static void sampler(String uniformName, int textureIndex) {
        int texLoc = getLoc(uniformName);
        glUniform1i(texLoc, textureIndex);
    }

    /**
     * Sets the value of the specified sampler uniform variable.
     * The specified texture is set to the active texture number specified.
     * The active texture is not changed after this call.
     * @param uniformName The name of the uniform.
     * @param textureIndex Which active texture number to set.
     * @param texName The name of the texture to set.
     * @param filter Whether the texture should be filtered.
     */
    public static void sampler(String uniformName, int textureIndex, String texName, boolean filter) {
        int currentTexture = glGetInteger(GL_ACTIVE_TEXTURE);
        glActiveTexture(GL_TEXTURE0 + textureIndex);
        if (filter)
            TextureMap.bindFiltered(texName);
        else
            TextureMap.bindUnfiltered(texName);
        sampler(uniformName, textureIndex);
        glActiveTexture(currentTexture);
        ErrorUtils.checkGLError();
    }
}