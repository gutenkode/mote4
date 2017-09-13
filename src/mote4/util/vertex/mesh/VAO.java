package mote4.util.vertex.mesh;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

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
        GL30.glBindVertexArray(vaoId);
        for (int i : attribInds)
            GL20.glEnableVertexAttribArray(i);

        // Draw the vertices
        GL11.glDrawArrays(primitiveType, 0, numVertices);

        // Put everything back to default (deselect)
        for (int i : attribInds)
            GL20.glDisableVertexAttribArray(i);
        GL30.glBindVertexArray(0);
    }

    @Override
    public void destroy() {
        if (!destroyed) {
            destroyed = true;
            // disable the VBO index from the VAO attributes list
            for (int i : attribInds)
                GL20.glDisableVertexAttribArray(i);

            // delete the VBOs
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            for (int i : vbos)
                GL15.glDeleteBuffers(i);

            // delete the VAO
            GL30.glBindVertexArray(0);
            GL30.glDeleteVertexArrays(vaoId);
        }
    }
}