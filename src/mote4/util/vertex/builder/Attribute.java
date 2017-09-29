package mote4.util.vertex.builder;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import mote4.util.ErrorUtils;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL11.GL_FLOAT;

/**
 * Used in ModernMeshBuilder.
 * @author Peter
 */
public class Attribute {
    
    public final int ATTRIB_INDEX, SIZE;
    private int drawHint = GL_STATIC_DRAW;
    private ArrayList<Float> vertices;
    
    public Attribute(int index, int size) {
        vertices = new ArrayList<>();
        ATTRIB_INDEX = index;
        SIZE = size;
    }
    
    public void add(float... data) {
        for (float f : data)
            vertices.add(f);
    }
    public void add(ArrayList<Float> data) {
        
    }
    
    /**
     * The number of vertices stored in this Attribute.
     * Num vertices = all data/size
     * Fractional values indicate an incomplete buffer.  All attributes
     * must have the same non-fractional number of vertices to build a VAO.
     * @return 
     */
    public double numVertices() {
        return ((double)vertices.size())/SIZE;
    }
    
    /**
     * Called by ModernMeshBuilder to construct a VAO.
     * @return The buffer handle for the data.
     */
    protected int[] build() {
        int vboID = glGenBuffers();

        // convert ArrayList to FloatBuffer
        /*
        float[] fArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            fArray[i] = vertices.get(i);
        }*/
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(vertices.size());
        //vertexData.put(fArray);
        for (Float vertice : vertices) {
            vertexData.put(vertice);
        }
        vertexData.flip();

        glEnableVertexAttribArray(ATTRIB_INDEX);  // record in VAO

        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexData, drawHint);

        glVertexAttribPointer(
            ATTRIB_INDEX,       // shader attribute
            SIZE,               // size
            GL_FLOAT,           // type
            false,              // normalized?
            0,                  // stride
            0                   // array buffer offset
        );

        glBindBuffer(GL_ARRAY_BUFFER, 0); // unbind VBO

        ErrorUtils.checkGLError();
        return new int[] {vboID, ATTRIB_INDEX};
    }
}
