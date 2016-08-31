package mote4.util.vertex;

/*
* Modified on August 8, 2005
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import mote4.util.vertex.builder.MeshBuilder;
import mote4.util.vertex.mesh.VAO;

/**
 * @author Jeremy Adams (elias4444)
 * Modified for this engine to support vertex buffer objects and vertex array objects.
 *
 * Use these lines if reading from a file
 * FileReader fr = new FileReader(ref);
 * BufferedReader br = new BufferedReader(fr);
 * 
 * Use these lines if reading from within a jar
 * InputStreamReader fr = new InputStreamReader(new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(ref)));
 * BufferedReader br = new BufferedReader(fr);
 */

public class OBJLoader {
    private ArrayList vertexsets = new ArrayList(); // Vertex Coordinates
    private ArrayList vertexsetsnorms = new ArrayList(); // Vertex Coordinates Normals
    private ArrayList vertexsetstexs = new ArrayList(); // Vertex Coordinates Textures
    private ArrayList faces = new ArrayList(); // Array of Faces (vertex sets)
    private ArrayList facestexs = new ArrayList(); // Array of of Faces textures
    private ArrayList facesnorms = new ArrayList(); // Array of Faces normals
    
    private int objectlist;
    private int numpolys = 0;
    
    //// Statistics for drawing ////
    public float toppoint = 0;		// y+
    public float bottompoint = 0;	// y-
    public float leftpoint = 0;		// x-
    public float rightpoint = 0;	// x+
    public float farpoint = 0;		// z-
    public float nearpoint = 0;		// z+
    
    public OBJLoader (BufferedReader ref, boolean centerit) {
        loadobject(ref);
        if (centerit) {
            centerit();
        }
        //opengldrawtolist();
        numpolys = faces.size();
        //cleanup();
    }
    
    private void cleanup() {
        vertexsets.clear();
        vertexsetsnorms.clear();
        vertexsetstexs.clear();
        faces.clear();
        facestexs.clear();
        facesnorms.clear();
    }
    
    private void loadobject(BufferedReader br) {
        int linecounter = 0;
        try {
            
            String newline;
            boolean firstpass = true;
            
            while (((newline = br.readLine()) != null)) {
                linecounter++;
                newline = newline.trim();
                //if (newline.startsWith("vt"))
                    //System.out.println(newline);
                if (newline.length() > 0) {
                    if (newline.startsWith("v ")) {
                    //if (newline.charAt(0) == 'v' && newline.charAt(1) == ' ') {
                        float[] coords = new float[4];
                        String[] coordstext;// = new String[4];
                        coordstext = newline.split("\\s+");
                        for (int i = 1;i < coordstext.length;i++) {
                            coords[i-1] = Float.valueOf(coordstext[i]);
                        }
                        //// check for farpoints ////
                        if (firstpass) {
                            rightpoint = coords[0];
                            leftpoint = coords[0];
                            toppoint = coords[1];
                            bottompoint = coords[1];
                            nearpoint = coords[2];
                            farpoint = coords[2];
                            firstpass = false;
                        }
                        if (coords[0] > rightpoint) {
                            rightpoint = coords[0];
                        }
                        if (coords[0] < leftpoint) {
                            leftpoint = coords[0];
                        }
                        if (coords[1] > toppoint) {
                            toppoint = coords[1];
                        }
                        if (coords[1] < bottompoint) {
                            bottompoint = coords[1];
                        }
                        if (coords[2] > nearpoint) {
                            nearpoint = coords[2];
                        }
                        if (coords[2] < farpoint) {
                            farpoint = coords[2];
                        }
                        /////////////////////////////
                        vertexsets.add(coords);
                    }
                    else if (newline.startsWith("vt")) {
                    //if (newline.charAt(0) == 'v' && newline.charAt(1) == 't') {
                        float[] coords = new float[4];
                        String[] coordstext;// = new String[4];
                        coordstext = newline.split("\\s+");
                        for (int i = 1;i < coordstext.length;i++) {
                            coords[i-1] = Float.valueOf(coordstext[i]);
                        }
                        
                        vertexsetstexs.add(coords);
                    }
                    else if (newline.startsWith("vn")) {
                    //if (newline.charAt(0) == 'v' && newline.charAt(1) == 'n') {
                        float[] coords = new float[4];
                        String[] coordstext;// = new String[4];
                        coordstext = newline.split("\\s+");
                        for (int i = 1;i < coordstext.length;i++) {
                            coords[i-1] = Float.valueOf(coordstext[i]).floatValue();
                        }
                        vertexsetsnorms.add(coords);
                    }
                    
                    //if (isReading) { /********/
                    else if (newline.startsWith("f ")) {
                    //if (newline.charAt(0) == 'f' && newline.charAt(1) == ' ') {
                        String[] coordstext = newline.split("\\s+");
                        int[] v = new int[coordstext.length - 1];
                        int[] vt = new int[coordstext.length - 1];
                        int[] vn = new int[coordstext.length - 1];
                        
                        for (int i = 1;i < coordstext.length;i++) {
                            String fixstring = coordstext[i].replaceAll("//","/0/");
                            String[] tempstring = fixstring.split("/");
                            v[i-1] = Integer.valueOf(tempstring[0]);
                            if (tempstring.length > 1) {
                                vt[i-1] = Integer.valueOf(tempstring[1]);
                            } else {
                                vt[i-1] = 0;
                            }
                            if (tempstring.length > 2) {
                                vn[i-1] = Integer.valueOf(tempstring[2]);
                            } else {
                                vn[i-1] = 0;
                            }
                        }
                        faces.add(v);
                        facestexs.add(vt);
                        facesnorms.add(vn);
                    }
                    
                    //} /***********/
                    /*
                    if (newline.startsWith("g ")) {
                    if (newline.contains("polygon0"))
                    isReading = false;
                    else
                    isReading = true;
                    }*/
                }
            }
            
        } catch (IOException e) {
            System.out.println("Failed to read file: " + br.toString());
            System.out.println(e.getMessage());
            //System.exit(0);
        } catch (NumberFormatException e) {
            System.out.println("Malformed OBJ (on line " + linecounter + "): " + br.toString() + "\r \r" + e.getMessage());
            //System.exit(0);
        }
        
    }
    
    private void centerit() {
        float xshift = (rightpoint-leftpoint) /2f;
        float yshift = (toppoint - bottompoint) /2f;
        float zshift = (nearpoint - farpoint) /2f;
        
        for (int i=0; i < vertexsets.size(); i++) {
            float[] coords = new float[4];
            
            coords[0] = ((float[])(vertexsets.get(i)))[0] - leftpoint - xshift;
            coords[1] = ((float[])(vertexsets.get(i)))[1] - bottompoint - yshift;
            coords[2] = ((float[])(vertexsets.get(i)))[2] - farpoint - zshift;
            
            vertexsets.set(i,coords); // = coords;
        }
        
    }
    
    public float getXWidth() {
        float returnval = 0;
        returnval = rightpoint - leftpoint;
        return returnval;
    }
    
    public float getYHeight() {
        float returnval = 0;
        returnval = toppoint - bottompoint;
        return returnval;
    }
    
    public float getZDepth() {
        float returnval = 0;
        returnval = nearpoint - farpoint;
        return returnval;
    }
    
    public int numpolygons() {
        return numpolys;
    }
    
    /**
     * Loads the model data into a vertex array object.
     * @return
     */
    public VAO createVAO() {
        MeshBuilder builder = new MeshBuilder(3);
        //System.out.println("faces size:    "+faces.size());
        //System.out.println("vertex size:   "+vertexsets.size());
        //System.out.println("normal size:   "+vertexsetsnorms.size());
        //System.out.println("texcoord size: "+vertexsetstexs.size());
        if (!vertexsetsnorms.isEmpty())
            builder.includeNormals();
        if (!vertexsetstexs.isEmpty())
            builder.includeTexCoords(2);
        
        // for every face
        for (int i = 0; i < faces.size(); i++) {
            int[] tempfaces = (int[])(faces.get(i));
            int[] tempfacesnorms = (int[])(facesnorms.get(i));
            int[] tempfacestexs = (int[])(facestexs.get(i));
            
            int[] order;
            //// Quad Begin Header ////
            if (tempfaces.length == 3) {
                order = new int[] {0,1,2};
            } else if (tempfaces.length == 4) {
                order = new int[] {0,1,2, 3,0,2};
            } else {
                throw new IllegalStateException("VAO constructor does not support polygon faces.");
            }
            ////////////////////////////
            
            // for every vertex
            for (int j : order)
            //for (int j = 0; j < tempfaces.length; j++)
            {
                if (tempfacesnorms[j] != 0)
                {
                    float normtempx = ((float[])vertexsetsnorms.get(tempfacesnorms[j] - 1))[0];
                    float normtempy = ((float[])vertexsetsnorms.get(tempfacesnorms[j] - 1))[1];
                    float normtempz = ((float[])vertexsetsnorms.get(tempfacesnorms[j] - 1))[2];
                    builder.normals(new float[] {normtempx, normtempy, normtempz});
                }
                if (tempfacestexs[j] != 0)
                {
                    float textempx = ((float[])vertexsetstexs.get(tempfacestexs[j] - 1))[0];
                    float textempy = ((float[])vertexsetstexs.get(tempfacestexs[j] - 1))[1];
                    //float textempz = ((float[])vertexsetstexs.get(tempfacestexs[w] - 1))[2];
                    builder.texCoords(new float[] {textempx,textempy});//,textempz});
                }
                float tempx = ((float[])vertexsets.get(tempfaces[j] - 1))[0];
                float tempy = ((float[])vertexsets.get(tempfaces[j] - 1))[1];
                float tempz = ((float[])vertexsets.get(tempfaces[j] - 1))[2];
                builder.vertices(new float[] {tempx,tempy,tempz});
            }
            ///////////////////////////
        }
        cleanup();
        return builder.constructVAO(GL11.GL_TRIANGLES);
    }
    
}