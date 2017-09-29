package mote4.util.vertex.mesh;

import mote4.util.ErrorUtils;

import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Vertex Array Object wrapper.
 * Allows easy creation/destruction of needed buffers and prevents bugs with memory.
 * @author Peter
 */
public class VAO implements Mesh {

    private boolean destroyed;
    private final int vaoId, primitiveType, numVertices;
    private final int[] vbos, attribInds;
    
    public VAO(int vaoId, int[] vbos, int[] attribInds, int type, int nV) {
        this.vaoId = vaoId;
        this.vbos = vbos;
        this.attribInds = attribInds;
        primitiveType = type;
        numVertices = nV;
        destroyed = false;
    }
    
    @Override
    public void render() {
        if (destroyed)
            throw new IllegalStateException("Attempted to render destroyed VAO mesh.");

        // Bind to the VAO that has all the information about the quad vertices
        glBindVertexArray(vaoId);
        for (int i : attribInds)
            glEnableVertexAttribArray(i);

        // Draw the vertices
        glDrawArrays(primitiveType, 0, numVertices);

        // Put everything back to default (deselect)
        for (int i : attribInds)
            glDisableVertexAttribArray(i);
        glBindVertexArray(0);
    }

    @Override
    public void destroy() {
        if (!destroyed) {
            destroyed = true;

            // disable the VBO index from the VAO attributes list
            glBindVertexArray(vaoId);
            for (int i : attribInds)
                glDisableVertexAttribArray(i);

            // delete the VBOs
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            for (int i : vbos)
                glDeleteBuffers(i);

            // delete the VAO
            glBindVertexArray(0);
            glDeleteVertexArrays(vaoId);

            ErrorUtils.checkGLError();
        }
    }
}