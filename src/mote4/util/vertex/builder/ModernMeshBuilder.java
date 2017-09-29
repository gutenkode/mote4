package mote4.util.vertex.builder;

import java.util.ArrayList;

import mote4.util.ErrorUtils;
import mote4.util.vertex.mesh.VAO;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

/**
 * A ModernMeshBuilder requires you to specify the attributes used in a VAO.
 * This allows entirely custom inputs to shaders, but also requires keeping better
 * track of what attributes and indices you are using.  Unless you need data other
 * than vertices, colors, textures, and/or normals, use MeshBuilder and StaticMeshBuilder.
 * @author Peter
 */
public class ModernMeshBuilder {
    
    private final ArrayList<Attribute> attribs;
    
    public ModernMeshBuilder() {
        attribs = new ArrayList<>();
    }
    
    public void addAttrib(Attribute a) {
        attribs.add(a);
    }
    
    public VAO constructVAO(int primitiveType) {
        if (primitiveType == GL11.GL_QUADS)
            throw new IllegalArgumentException("GL_QUADS is not supported.");
        int size = checkCompleteness();
        
        int vaoId = glGenVertexArrays(); // construct the VAO
        ErrorUtils.checkGLError();
        int[] vboArray = new int[attribs.size()];
        int[] attribArray = new int[attribs.size()];
        
        // bind the vertex array
        glBindVertexArray(vaoId); // begin building the VAO
        ErrorUtils.checkGLError();
        
        // build all attribs
        for (int i = 0; i < vboArray.length; i++) {
            int[] data = attribs.get(i).build();
            vboArray[i] = data[0];
            attribArray[i] = data[1];
        }
        
        // unbind the VAO
        glBindVertexArray(0);

        return new VAO(vaoId, vboArray, attribArray, primitiveType, size); // wrap it all up nicely
    }
    
    private int checkCompleteness() {
        int size = (int)attribs.get(0).numVertices();
        for (Attribute a : attribs) {
            double d = a.numVertices();
            if (d != (int)d)
                throw new IllegalStateException("Incomplete list of vertex data in attribute "+a.ATTRIB_INDEX+".");
            if ((int)d != size)
                throw new IllegalStateException("Number of vertices in attribute "+a.ATTRIB_INDEX+" is inconsistent with other attributes.");
        }
        return size;
    }
}
