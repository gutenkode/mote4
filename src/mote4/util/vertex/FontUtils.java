package mote4.util.vertex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;
import mote4.scenegraph.Window;
import mote4.util.vertex.mesh.Mesh;
import org.lwjgl.opengl.GL11;
import mote4.util.vertex.builder.StaticMeshBuilder;

/**
 *
 * @author Peter
 */
public class FontUtils {
    private static final int ROWS = 16, 
                             COLUMNS = 16;
    private static final float ROW_HEIGHT = 1f/ROWS,
                               COLUMN_WIDTH = 1f/COLUMNS;
    private static float letterWidth = 1,
                         letterHeight = 1,
                         lineSpace = letterHeight*1f;
    private static int charPixelWidth = 16;
    
    private static byte[] metrics;
    private static HashMap<String,byte[]> metricMap = new HashMap<>();
    
    static {
        // default monospaced metric
        metrics = new byte[256];
        Arrays.fill(metrics, (byte)charPixelWidth);
        metricMap.put("monospace", metrics);
    }
    
    /**
     * Creates a Mesh designed to display a bitmap font texture.
     * The string is centered at the origin and scaled to a height of 1.
     * @param text The string to display.
     * @return A vbo containing the string.
     */
    public static Mesh createString(String text) {
        return createString(text,0,0,1,1);
    }
    /**
     * Creates a Mesh designed to display a bitmap font texture.
     * @param text The string to display.
     * @param xPos X offset from origin.
     * @param yPos Y offset from origin.
     * @param xScale X scale.
     * @param yScale Y scale.
     * @return A vbo containing the string.
     */
    public static Mesh createString(String text, float xPos, float yPos, float xScale, float yScale) {
        int stride = 6*2; // 6 vertices per letter, 2 coords per vertex
        float[] vertices = new float[text.length()*stride]; 
        float[] texCoords = new float[text.length()*stride];
        float lineHeight = 0;
        float width = 0;
        
        int i = -1;
        for (int j = 0; j < text.length(); j++) {
            i++;
            char c = text.charAt(j);
            if (c == '\n') 
            {
                i = -1;
                lineHeight += lineSpace;
                width = 0;
            }
            else 
            {
                // texture coords
                int x = ((int)c)%COLUMNS;
                int y = ((int)c)/ROWS;
                float xCoord = x*COLUMN_WIDTH;
                float yCoord = y*ROW_HEIGHT;
                
                //float newWidth = (float)metrics[(int)c]/0x20;
                float newWidth = (float)metrics[(int)c]/charPixelWidth;
                newWidth *= letterWidth;

                // add vertex coordinates
                vertices[j*stride  ] = xPos+width*xScale;
                vertices[j*stride+1] = yPos+lineHeight*yScale;

                vertices[j*stride+2] = xPos+width*xScale;
                vertices[j*stride+3] = yPos+letterHeight*yScale+lineHeight*yScale;

                vertices[j*stride+4] = xPos+(width+newWidth)*xScale;
                vertices[j*stride+5] = yPos+lineHeight*yScale;
                
                vertices[j*stride+6] = xPos+width*xScale;
                vertices[j*stride+7] = yPos+letterHeight*yScale+lineHeight*yScale;
                
                vertices[j*stride+8] = xPos+(width+newWidth)*xScale;
                vertices[j*stride+9] = yPos+letterHeight*yScale+lineHeight*yScale;

                vertices[j*stride+10] = xPos+(width+newWidth)*xScale;
                vertices[j*stride+11] = yPos+lineHeight*yScale;
                
                width += newWidth;

                // add texture coordinates
                texCoords[j*stride  ] = xCoord;
                texCoords[j*stride+1] = yCoord;

                texCoords[j*stride+2] = xCoord;
                texCoords[j*stride+3] = yCoord+ROW_HEIGHT*letterHeight;
                
                texCoords[j*stride+4] = xCoord+COLUMN_WIDTH*newWidth;
                texCoords[j*stride+5] = yCoord;
                
                texCoords[j*stride+6] = xCoord;
                texCoords[j*stride+7] = yCoord+ROW_HEIGHT*letterHeight;

                texCoords[j*stride+8] = xCoord+COLUMN_WIDTH*newWidth;
                texCoords[j*stride+9] = yCoord+ROW_HEIGHT*letterHeight;

                texCoords[j*stride+10] = xCoord+COLUMN_WIDTH*newWidth;
                texCoords[j*stride+11] = yCoord;
            }
        }
        
        return StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLES,
                                              2,vertices,
                                              2,texCoords,
                                              0,null,
                                              null);
    }
    /**
     * Creates a Mesh designed to display a bitmap font texture.
     * This function will also parse color values in the form of @{r,g,b,a} where
     * values are floats of range 0-1.
     * @param text The string to display.
     * @param xPos X offset from origin.
     * @param yPos Y offset from origin.
     * @param xScale X scale.
     * @param yScale Y scale.
     * @return A vbo containing the string.
     */
    public static Mesh createStringColor(String text, float xPos, float yPos, float xScale, float yScale) {
        int stride = 6*2; // 6 vertices per letter, 2 coords per vertex
        float[] vertices = new float[text.length()*stride]; 
        float[] colorVal = new float[] {1,1,1,1};
        float[] colors = new float[text.length()*stride*2];
        float[] texCoords = new float[text.length()*stride]; // color has 4 components
        float lineHeight = 0;
        float width = 0;
        
        int i = -1;
        for (int j = 0; j < text.length(); j++) {
            i++;
            char c = text.charAt(j);
            if (c == '\n') 
            {
                i = -1;
                lineHeight += lineSpace;
                width = 0;
            }
            else if (c == '@' && text.charAt(j+1) == '{') 
            {
                // this is a color value, parse the contents
                int end = text.indexOf('}', j+2);
                StringTokenizer tok = new StringTokenizer(text.substring(j+2,end),",");
                int colorInd = 0;
                while (tok.hasMoreTokens() && colorInd < 4) {
                    try {
                        colorVal[colorInd] = Float.valueOf(tok.nextToken());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid text color value: "+text.substring(j+2,end));
                    }
                    colorInd++;
                }
                j = end;
            }
            else 
            {
                // texture coords
                int x = ((int)c)%COLUMNS;
                int y = ((int)c)/ROWS;
                float xCoord = x*COLUMN_WIDTH;
                float yCoord = y*ROW_HEIGHT;
                
                //float newWidth = (float)metrics[(int)c]/0x20;
                float newWidth = (float)metrics[((int)c)%metrics.length]/charPixelWidth;
                newWidth *= letterWidth;

                // add vertex coordinates
                vertices[j*stride  ] = xPos+width*xScale;
                vertices[j*stride+1] = yPos+lineHeight*yScale;

                vertices[j*stride+2] = xPos+width*xScale;
                vertices[j*stride+3] = yPos+letterHeight*yScale+lineHeight*yScale;

                vertices[j*stride+4] = xPos+(width+newWidth)*xScale;
                vertices[j*stride+5] = yPos+lineHeight*yScale;
                
                vertices[j*stride+6] = xPos+width*xScale;
                vertices[j*stride+7] = yPos+letterHeight*yScale+lineHeight*yScale;
                
                vertices[j*stride+8] = xPos+(width+newWidth)*xScale;
                vertices[j*stride+9] = yPos+letterHeight*yScale+lineHeight*yScale;

                vertices[j*stride+10] = xPos+(width+newWidth)*xScale;
                vertices[j*stride+11] = yPos+lineHeight*yScale;
                
                width += newWidth;

                // add texture coordinates
                texCoords[j*stride  ] = xCoord;
                texCoords[j*stride+1] = yCoord;

                texCoords[j*stride+2] = xCoord;
                texCoords[j*stride+3] = yCoord+ROW_HEIGHT*letterHeight;
                
                texCoords[j*stride+4] = xCoord+COLUMN_WIDTH*newWidth;
                texCoords[j*stride+5] = yCoord;
                
                texCoords[j*stride+6] = xCoord;
                texCoords[j*stride+7] = yCoord+ROW_HEIGHT*letterHeight;

                texCoords[j*stride+8] = xCoord+COLUMN_WIDTH*newWidth;
                texCoords[j*stride+9] = yCoord+ROW_HEIGHT*letterHeight;

                texCoords[j*stride+10] = xCoord+COLUMN_WIDTH*newWidth;
                texCoords[j*stride+11] = yCoord;
                
                // add color values
                for (int k = 0; k < 6; k++) {
                    colors[j*stride*2+k*4  ] = colorVal[0];
                    colors[j*stride*2+k*4+1] = colorVal[1];
                    colors[j*stride*2+k*4+2] = colorVal[2];
                    colors[j*stride*2+k*4+3] = colorVal[3];
                }
            }
        }
        
        return StaticMeshBuilder.constructVAO(GL11.GL_TRIANGLES,
                                              2,vertices,
                                              2,texCoords,
                                              4,colors,
                                              null);
    }
    
    /**
     * Returns the length of the String if it were used to create a Mesh.
     * This function will not include color values in the length.
     * @param text
     * @return 
     */
    public static float getStringWidth(String text) {
        float width = 0;
        for (int j = 0; j < text.length(); j++) {
            char c = text.charAt(j);
            
            if (c == '@' && text.charAt(j+1) == '{') 
            {
                // this is a color value
                int end = text.indexOf('}', j+2);
                j = end;
            } else
                width += letterWidth * (float)metrics[(int)c]/charPixelWidth;
        }
        return width;
    }
    
    public static float lineSpace() { return lineSpace; }
    public static float letterWidth() { return letterWidth; }
    public static float letterHeight() { return letterHeight; }
    public static int charPixelWidth() { return charPixelWidth; }
    
    public static void setLineSpace(float s) { lineSpace = s; }
    public static void setLetterWidth(float w) { letterWidth = w; }
    public static void setLetterHeght(float h) { letterHeight = h; }
    public static void setCharPixelWidth(int w) { charPixelWidth = w; }
    
    /**
     * Loads a metric file for a font.
     * @param file Filename of the metric data.
     * @param name Name to use when binding this metric.
     */
    public static void loadMetric(String file, String name) {
        try {
            InputStream st = ClassLoader.class.getClass().getResourceAsStream("/res/textures/"+file+".dat");
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[256];

            while ((nRead = st.read(data, 0, data.length)) != -1) {
              buffer.write(data, 0, nRead);
            }

            buffer.flush();
            
            metrics = buffer.toByteArray();
            metricMap.put(name, metrics);
                    
            //metrics = Files.readAllBytes(new File("./res/textures/"+file+".dat").toPath());
        } catch(IOException e) {
            e.printStackTrace();
            Window.destroy();
        }
    }
    public static void useMetric(String name) {
        if (!metricMap.containsKey(name))
            throw new IllegalArgumentException("Font metric '"+name+"' is not loaded.");
        metrics = metricMap.get(name);
    }
}
