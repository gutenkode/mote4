package mote4.util.vertex.builder;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import mote4.util.FileIO;
import mote4.util.vertex.mesh.VAO;

/**
 *
 * @author Peter
 */
public class MeshBuilder {
    
    private ArrayList<Float> vertices, texCoords, colors, normals;
    private int vertexSize, texSize, colorSize;
    private boolean incColor = false, incTex = false, incNormal = false;
    private int vertexDrawHint = GL_STATIC_DRAW,
                texDrawHint    = GL_STATIC_DRAW,
                colorDrawHint  = GL_STATIC_DRAW,
                normalDrawHint = GL_STATIC_DRAW;
    
    // these are the 'in' values for shaders
    public final int VERTEX_ATTRIB     = 0,
                     COLOR_ATTRIB      = 1,
                     TEXCOORD_ATTRIB   = 2,
                     NORMAL_ATTRIB     = 3;
    
    public MeshBuilder(int s) {
        vertexSize = s;
        vertices = new ArrayList<>();
    }
    
    public void includeTexCoords(int s) {
        incTex = true;
        texSize = s;
        texCoords = new ArrayList<>();
    }
    public void includeColors(int s) {
        incColor = true;
        colorSize = s;
        colors = new ArrayList<>();
    }
    public void includeNormals() {
        incNormal = true;
        normals = new ArrayList<>();
    }
    
    public void vertices(float... v) {
        for (float f : v)
            vertices.add(f);
    }
    public void texCoords(float... v) {
        if (!incTex)
            throw new IllegalStateException("This MeshBuilder does not include tex coords.");
        for (float f : v)
            texCoords.add(f);
    }
    public void colors(float... v) {
        if (!incColor)
            throw new IllegalStateException("This MeshBuilder does not include colors.");
        for (float f : v)
            colors.add(f);
    }
    public void normals(float... v) {
        if (!incNormal)
            throw new IllegalStateException("This MeshBuilder does not include normals.");
        for (float f : v)
            normals.add(f);
    }
    
    /**
     * Sets the OpenGL drawing hint for the vertex buffer.
     * @param hint Must be one of STREAM_DRAW, STREAM_READ, STREAM_COPY, 
     * STATIC_DRAW, STATIC_READ, STATIC_COPY, DYNAMIC_DRAW, DYNAMIC_READ, DYNAMIC_COPY
     */
    public void vertexDrawHint(int hint) {
        vertexDrawHint = hint;
    }
    
    public VAO constructVAO(int primitiveType) {
        
        checkCompleteness();
        
        int vaoId = GL30.glGenVertexArrays(); // construct the VAO
        ArrayList<Integer> vbos = new ArrayList<>();
        ArrayList<Integer> attribs = new ArrayList<>();
        
        // bind the vertex array
        GL30.glBindVertexArray(vaoId); // "start recording the calls I make.."
        
        {
            int vertexVboId = glGenBuffers(); // VBO for vertices
            vbos.add(vertexVboId);
            attribs.add(VERTEX_ATTRIB);

            // put the ArrayList of vertices in a FloatBuffer
            float[] fArray = new float[vertices.size()];
            for (int i = 0; i < vertices.size(); i++) {
                fArray[i] = vertices.get(i);
            }
            FloatBuffer vertexData = BufferUtils.createFloatBuffer(vertices.size());
            vertexData.put(fArray);
            vertexData.flip();

            GL20.glEnableVertexAttribArray(VERTEX_ATTRIB);  // record in VAO

            glBindBuffer(GL_ARRAY_BUFFER, vertexVboId);
            glBufferData(GL_ARRAY_BUFFER, vertexData, vertexDrawHint);

            GL20.glVertexAttribPointer(
               VERTEX_ATTRIB,      // shader attribute
               vertexSize,         // size
               GL11.GL_FLOAT,      // type
               false,              // normalized?
               0,                  // stride
               0                   // array buffer offset
            );
            
            glBindBuffer(GL_ARRAY_BUFFER, 0); // unbind VBO
        }
        
        if (incColor)
        {
            int colorVboId = glGenBuffers(); // VBO for colors
            vbos.add(colorVboId);
            attribs.add(COLOR_ATTRIB);

            // put the ArrayList of colors in a FloatBuffer
            float[] fArray = new float[colors.size()];
            for (int i = 0; i < colors.size(); i++) {
                fArray[i] = colors.get(i);
            }
            FloatBuffer colorData = BufferUtils.createFloatBuffer(colors.size());
            colorData.put(fArray);
            colorData.flip();

            GL20.glEnableVertexAttribArray(COLOR_ATTRIB);  // record in VAO

            glBindBuffer(GL_ARRAY_BUFFER, colorVboId);
            glBufferData(GL_ARRAY_BUFFER, colorData, colorDrawHint);

            GL20.glVertexAttribPointer(
               COLOR_ATTRIB,       // shader attribute
               colorSize,          // size
               GL11.GL_FLOAT,      // type
               false,              // normalized?
               0,                  // stride
               0                   // array buffer offset
            );
            
            glBindBuffer(GL_ARRAY_BUFFER, 0); // unbind VBO
        }
        
        if (incTex)
        {
            int texCoordVboId = glGenBuffers(); // VBO for tex coords
            vbos.add(texCoordVboId);
            attribs.add(TEXCOORD_ATTRIB);

            // put the ArrayList of tex coords in a FloatBuffer
            float[] fArray = new float[texCoords.size()];
            for (int i = 0; i < texCoords.size(); i++) {
                fArray[i] = texCoords.get(i);
            }
            FloatBuffer texCoordData = BufferUtils.createFloatBuffer(texCoords.size());
            texCoordData.put(fArray);
            texCoordData.flip();

            GL20.glEnableVertexAttribArray(TEXCOORD_ATTRIB);  // record in VAO

            glBindBuffer(GL_ARRAY_BUFFER, texCoordVboId);
            glBufferData(GL_ARRAY_BUFFER, texCoordData, texDrawHint);

            GL20.glVertexAttribPointer(
               TEXCOORD_ATTRIB,    // shader attribute
               texSize,            // size
               GL11.GL_FLOAT,      // type
               false,              // normalized?
               0,                  // stride
               0                   // array buffer offset
            );
            
            glBindBuffer(GL_ARRAY_BUFFER, 0); // unbind VBO
        }
        
        if (incNormal)
        {
            int normalVboId = glGenBuffers(); // VBO for normals
            vbos.add(normalVboId);
            attribs.add(NORMAL_ATTRIB);

            // put the ArrayList of tex coords in a FloatBuffer
            float[] fArray = new float[normals.size()];
            for (int i = 0; i < normals.size(); i++) {
                fArray[i] = normals.get(i);
            }
            FloatBuffer normalData = BufferUtils.createFloatBuffer(normals.size());
            normalData.put(fArray);
            normalData.flip();

            GL20.glEnableVertexAttribArray(NORMAL_ATTRIB);  // record in VAO

            glBindBuffer(GL_ARRAY_BUFFER, normalVboId);
            glBufferData(GL_ARRAY_BUFFER, normalData, normalDrawHint);

            GL20.glVertexAttribPointer(
               NORMAL_ATTRIB,      // shader attribute
               3,                  // size
               GL11.GL_FLOAT,      // type
               false,              // normalized?
               0,                  // stride
               0                   // array buffer offset
            );
            
            glBindBuffer(GL_ARRAY_BUFFER, 0); // unbind VBO
        }
        
        // unbind the VAO
        GL30.glBindVertexArray(0);
        
        // pass the list of VBO handles to the VAO object
        int[] vboArray = new int[vbos.size()];
        for (int i = 0; i < vboArray.length; i++)
            vboArray[i] = vbos.get(i);
        
        // pass the list of shader attribs to the VAO object
        int[] attribArray = new int[attribs.size()];
        for (int i = 0; i < attribArray.length; i++)
            attribArray[i] = attribs.get(i);
        
        return new VAO(vaoId, vboArray, attribArray, primitiveType, vertices.size()/vertexSize); // wrap it all up nicely
    }
    
    private void checkCompleteness() {
        float numVertices = vertices.size()/(float)vertexSize;
        if (numVertices%1 != 0)
            throw new IllegalStateException("Incomplete list of vertices ("+numVertices+").");
        if (incTex) 
        {
            float numTex = texCoords.size()/(float)texSize;
            if (numTex%1 != 0)
                throw new IllegalStateException("Incomplete list of tex coords ("+numTex+").");
            if (numVertices != numTex)
                throw new IllegalStateException("Number of vertices ("+numVertices+") and number of tex coords ("+numTex+") do not match.");
        }
        if (incColor)
        {
            float numColors = colors.size()/(float)colorSize;
            if (numColors%1 != 0)
                throw new IllegalStateException("Incomplete list of colors ("+numColors+").");
            if (numVertices != numColors)
                throw new IllegalStateException("Number of vertices ("+numVertices+") and number of colors ("+numColors+") do not match.");
        }
        if (incNormal)
        {
            float numNormals = normals.size()/3f;
            if (numNormals%1 != 0)
                throw new IllegalStateException("Incomplete list of normals ("+numNormals+").");
            if (numVertices != numNormals)
                throw new IllegalStateException("Number of vertices ("+numVertices+") and number of normals ("+numNormals+") do not match.");
        }
    }
    
    /**
     * Clears all stored vertices.
     * Does not enable/disable other states.
     */
    public void clearVertices() {
        vertices.clear();
    }
    /**
     * Clears all stored vertices and values.
     * Does not enable/disable other states.
     */
    public void clearAll() {
        vertices.clear();
        if (incColor)
            colors.clear();
        if (incTex)
            texCoords.clear();
        if (incNormal)
            normals.clear();
    }
}