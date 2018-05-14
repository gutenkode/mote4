package mote4.util.vertex.builder;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import mote4.util.ErrorUtils;
import mote4.util.FileIO;
import mote4.util.vertex.OBJLoader;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.mesh.VAO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Quick and dirty mesh creation utility.
 * @author Peter
 */
public class StaticMeshBuilder {
    
    private static int vertexDrawHint = GL_STATIC_DRAW,
                       texDrawHint    = GL_STATIC_DRAW,
                       colorDrawHint  = GL_STATIC_DRAW,
                       normalDrawHint = GL_STATIC_DRAW;
    
    public static final int VERTEX_ATTRIB     = 0,
                            COLOR_ATTRIB      = 1,
                            TEXCOORD_ATTRIB   = 2,
                            NORMAL_ATTRIB     = 3;
    
    public static VAO constructVAO(int primitiveType,
                                   int vsize, float[] vertices,
                                   int tsize, float[] texCoords,
                                   int csize, float[] colors,
                                   float[] normals) 
    {
        if (primitiveType == GL11.GL_QUADS)
            throw new IllegalArgumentException("GL_QUADS is not supported.");
        checkCompleteness(vsize,vertices,tsize,texCoords,csize,colors,normals);
        
        int vaoId = glGenVertexArrays(); // construct the VAO
        ErrorUtils.checkGLError();
        ArrayList<Integer> vbos = new ArrayList<>();
        ArrayList<Integer> attribs = new ArrayList<>();
        
        // bind the vertex array
        glBindVertexArray(vaoId); // "start recording the calls I make.."
        
        {
            int vertexVboId = glGenBuffers(); // VBO for vertices
            vbos.add(vertexVboId);
            attribs.add(VERTEX_ATTRIB);

            FloatBuffer vertexData = BufferUtils.createFloatBuffer(vertices.length);
            vertexData.put(vertices);
            vertexData.flip();

            glEnableVertexAttribArray(VERTEX_ATTRIB);  // record in VAO
            ErrorUtils.checkGLError();

            glBindBuffer(GL_ARRAY_BUFFER, vertexVboId);
            glBufferData(GL_ARRAY_BUFFER, vertexData, vertexDrawHint);

            glVertexAttribPointer(
                VERTEX_ATTRIB,      // shader attribute
                vsize,              // size
                GL11.GL_FLOAT,      // type
                false,              // normalized?
                0,                  // stride
                0                   // array buffer offset
            );
            ErrorUtils.checkGLError();
            
            glBindBuffer(GL_ARRAY_BUFFER, 0); // unbind VBO
        }
        
        if (colors != null)
        {
            int colorVboId = glGenBuffers(); // VBO for colors
            vbos.add(colorVboId);
            attribs.add(COLOR_ATTRIB);

            FloatBuffer colorData = BufferUtils.createFloatBuffer(colors.length);
            colorData.put(colors);
            colorData.flip();

            glEnableVertexAttribArray(COLOR_ATTRIB);  // record in VAO
            ErrorUtils.checkGLError();

            glBindBuffer(GL_ARRAY_BUFFER, colorVboId);
            glBufferData(GL_ARRAY_BUFFER, colorData, colorDrawHint);

            glVertexAttribPointer(
                COLOR_ATTRIB,       // shader attribute
                csize,              // size
                GL11.GL_FLOAT,      // type
                false,              // normalized?
                0,                  // stride
                0                   // array buffer offset
            );
            ErrorUtils.checkGLError();
            
            glBindBuffer(GL_ARRAY_BUFFER, 0); // unbind VBO
        }
        
        if (texCoords != null)
        {
            int texCoordVboId = glGenBuffers(); // VBO for tex coords
            vbos.add(texCoordVboId);
            attribs.add(TEXCOORD_ATTRIB);

            // put the ArrayList of tex coords in a FloatBuffer
            FloatBuffer texCoordData = BufferUtils.createFloatBuffer(texCoords.length);
            texCoordData.put(texCoords);
            texCoordData.flip();

            glEnableVertexAttribArray(TEXCOORD_ATTRIB);  // record in VAO
            ErrorUtils.checkGLError();

            glBindBuffer(GL_ARRAY_BUFFER, texCoordVboId);
            glBufferData(GL_ARRAY_BUFFER, texCoordData, texDrawHint);

            glVertexAttribPointer(
               TEXCOORD_ATTRIB,    // shader attribute
               tsize,              // size
               GL11.GL_FLOAT,      // type
               false,              // normalized?
               0,                  // stride
               0                   // array buffer offset
            );
            ErrorUtils.checkGLError();
            
            glBindBuffer(GL_ARRAY_BUFFER, 0); // unbind VBO
        }
        
        if (normals != null)
        {
            int normalVboId = glGenBuffers(); // VBO for normals
            vbos.add(normalVboId);
            attribs.add(NORMAL_ATTRIB);

            FloatBuffer normalData = BufferUtils.createFloatBuffer(normals.length);
            normalData.put(normals);
            normalData.flip();

            glEnableVertexAttribArray(NORMAL_ATTRIB);  // record in VAO
            ErrorUtils.checkGLError();

            glBindBuffer(GL_ARRAY_BUFFER, normalVboId);
            glBufferData(GL_ARRAY_BUFFER, normalData, normalDrawHint);

            glVertexAttribPointer(
               NORMAL_ATTRIB,      // shader attribute
               3,                  // size
               GL11.GL_FLOAT,      // type
               false,              // normalized?
               0,                  // stride
               0                   // array buffer offset
            );
            ErrorUtils.checkGLError();
            
            glBindBuffer(GL_ARRAY_BUFFER, 0); // unbind VBO
        }
        
        // unbind the VAO
        glBindVertexArray(0);
        
        // pass the list of VBO handles to the VAO object
        int[] vboArray = new int[vbos.size()];
        for (int i = 0; i < vboArray.length; i++)
            vboArray[i] = vbos.get(i);
        
        // pass the list of shader attribs to the VAO object
        int[] attribArray = new int[attribs.size()];
        for (int i = 0; i < attribArray.length; i++)
            attribArray[i] = attribs.get(i);

        return new VAO(vaoId, vboArray, attribArray, primitiveType, vertices.length/vsize); // wrap it all up nicely
    }
    
    private static void checkCompleteness(int vsize, float[] vertices,
                                int tsize, float[] texCoords,
                                int csize, float[] colors,
                                float[] normals) 
    {
        float numVertices = vertices.length/(float)vsize;
        if (numVertices%1 != 0)
            throw new IllegalStateException("Incomplete list of vertices ("+numVertices+").");
        if (texCoords != null) 
        {
            float numTex = texCoords.length/(float)tsize;
            if (numTex%1 != 0)
                throw new IllegalStateException("Incomplete list of tex coords ("+numTex+").");
            if (numVertices != numTex)
                throw new IllegalStateException("Number of vertices ("+numVertices+") and number of tex coords ("+numTex+") do not match.");
        }
        if (colors != null)
        {
            float numColors = colors.length/(float)csize;
            if (numColors%1 != 0)
                throw new IllegalStateException("Incomplete list of colors ("+numColors+").");
            if (numVertices != numColors)
                throw new IllegalStateException("Number of vertices ("+numVertices+") and number of colors ("+numColors+") do not match.");
        }
        if (normals != null)
        {
            float numNormals = normals.length/3f;
            if (numNormals%1 != 0)
                throw new IllegalStateException("Incomplete list of normals ("+numNormals+").");
            if (numVertices != numNormals)
                throw new IllegalStateException("Number of vertices ("+numVertices+") and number of normals ("+numNormals+") do not match.");
        }
    }
    
    /**
     * Constructs a basic normalized quad mesh.
     * Includes texture coordinates and normals.
     * @return 
     */
    public static Mesh loadQuadMesh() {
        return constructVAO(GL11.GL_TRIANGLE_FAN,
                     2, new float[] {-1,1, -1,-1, 1,-1, 1,1},
                     2, new float[] {0,1, 0,0, 1,0, 1,1},
                     0, null,
                     new float[] {0,0,1, 0,0,1, 0,0,1, 0,0,1});
    }

    /**
     * Constructs a basic normalized quad mesh.
     * The normal mesh goes from -1 to 1, this one goes from 0 to 1.
     * Includes texture coordinates and normals.
     * @return
     */
    public static Mesh loadQuarterQuadMesh() {
        return constructVAO(GL11.GL_TRIANGLE_FAN,
                2, new float[] {0,1, 0,0, 1,0, 1,1},
                2, new float[] {0,1, 0,0, 1,0, 1,1},
                0, null,
                new float[] {0,0,1, 0,0,1, 0,0,1, 0,0,1});
    }

    public static Mesh loadCubeMesh() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Load a .obj file as a renderable mesh.
     * @param filename
     * @param center
     * @return 
     */
    public static VAO constructVAOFromOBJ(String filename, boolean center) {
        OBJLoader obj = new OBJLoader(FileIO.getBufferedReader("/res/models/"+filename+".obj"), center);
        VAO vao = obj.createVAO();
        return vao;
    }
}