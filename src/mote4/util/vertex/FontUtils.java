package mote4.util.vertex;

import mote4.scenegraph.Window;
import mote4.util.FileIO;
import mote4.util.vertex.builder.StaticMeshBuilder;
import mote4.util.vertex.mesh.Mesh;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 *
 * @author Peter
 */
public class FontUtils {
    private static final int ROWS    = 16,
                             COLUMNS = 16;
    private static final float ROW_HEIGHT = 1f/ROWS,
                               COLUMN_WIDTH = 1f/COLUMNS;
    private static float letterWidth,
                         letterHeight,
                         lineSpace;
    private static byte charPixelWidth;
    
    private static byte[] metrics;
    private static HashMap<String,byte[]> metricMap = new HashMap<>();
    
    static {
        // default monospaced metric
        metrics = new byte[258];
        charPixelWidth = 16;
        letterWidth = letterHeight = 1;
        lineSpace = letterHeight;
        Arrays.fill(metrics, charPixelWidth);
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
        int stride = 6*2; // 6 vertices per letter, with 2 coords per vertex
        float[] vertices = new float[text.length()*stride]; 
        float[] texCoords = new float[text.length()*stride];
        float yOffset = 0; // incremented every newline
        float xOffset = 0; // incremented every character, reset every newline

        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            if (c == '\n') 
            {
                yOffset += lineSpace;
                xOffset = 0;
            }
            else 
            {
                float currentCharWidth = letterWidth * (float)metrics[(int)c%256]/charPixelWidth;

                // add vertex coordinates
                vertices[i*stride  ] = xPos +xOffset*xScale;
                vertices[i*stride+1] = yPos +yOffset*yScale;

                vertices[i*stride+2] = xPos +xOffset*xScale;
                vertices[i*stride+3] = yPos +letterHeight*yScale +yOffset*yScale;

                vertices[i*stride+4] = xPos +(xOffset+currentCharWidth)*xScale;
                vertices[i*stride+5] = yPos +yOffset*yScale;
                
                vertices[i*stride+6] = xPos +xOffset*xScale;
                vertices[i*stride+7] = yPos +letterHeight*yScale +yOffset*yScale;
                
                vertices[i*stride+8] = xPos +(xOffset+currentCharWidth)*xScale;
                vertices[i*stride+9] = yPos +letterHeight*yScale +yOffset*yScale;

                vertices[i*stride+10] = xPos +(xOffset+currentCharWidth)*xScale;
                vertices[i*stride+11] = yPos +yOffset*yScale;
                
                xOffset += currentCharWidth;

                // texture coords
                int x = ((int)c)%COLUMNS;
                int y = ((int)c)/ROWS;
                float xCoord = x*COLUMN_WIDTH;
                float yCoord = y*ROW_HEIGHT;

                // add texture coordinates
                texCoords[i*stride  ] = xCoord;
                texCoords[i*stride+1] = yCoord;

                texCoords[i*stride+2] = xCoord;
                texCoords[i*stride+3] = yCoord+ROW_HEIGHT*letterHeight;
                
                texCoords[i*stride+4] = xCoord+COLUMN_WIDTH*currentCharWidth;
                texCoords[i*stride+5] = yCoord;
                
                texCoords[i*stride+6] = xCoord;
                texCoords[i*stride+7] = yCoord+ROW_HEIGHT*letterHeight;

                texCoords[i*stride+8] = xCoord+COLUMN_WIDTH*currentCharWidth;
                texCoords[i*stride+9] = yCoord+ROW_HEIGHT*letterHeight;

                texCoords[i*stride+10] = xCoord+COLUMN_WIDTH*currentCharWidth;
                texCoords[i*stride+11] = yCoord;
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
        float yOffset = 0; // incremented every newline
        float xOffset = 0; // incremented every character, reset every newline

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') 
            {
                yOffset += lineSpace;
                xOffset = 0;
            }
            else if (c == '@' && text.charAt(i+1) == '{')
            {
                // this is a color value, parse the contents
                int end = text.indexOf('}', i+2);
                StringTokenizer tok = new StringTokenizer(text.substring(i+2,end),",");
                int colorInd = 0;
                while (tok.hasMoreTokens() && colorInd < 4) {
                    try {
                        colorVal[colorInd] = Float.valueOf(tok.nextToken());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid text color value: "+text.substring(i+2,end));
                    }
                    colorInd++;
                }
                i = end;
            }
            else 
            {
                float newWidth = letterWidth * (float)metrics[(int)c%256]/charPixelWidth;

                // add vertex coordinates
                vertices[i*stride  ] = xPos+xOffset*xScale;
                vertices[i*stride+1] = yPos+yOffset*yScale;

                vertices[i*stride+2] = xPos+xOffset*xScale;
                vertices[i*stride+3] = yPos+letterHeight*yScale+yOffset*yScale;

                vertices[i*stride+4] = xPos+(xOffset+newWidth)*xScale;
                vertices[i*stride+5] = yPos+yOffset*yScale;
                
                vertices[i*stride+6] = xPos+xOffset*xScale;
                vertices[i*stride+7] = yPos+letterHeight*yScale+yOffset*yScale;
                
                vertices[i*stride+8] = xPos+(xOffset+newWidth)*xScale;
                vertices[i*stride+9] = yPos+letterHeight*yScale+yOffset*yScale;

                vertices[i*stride+10] = xPos+(xOffset+newWidth)*xScale;
                vertices[i*stride+11] = yPos+yOffset*yScale;
                
                xOffset += newWidth;

                // texture coords
                int x = ((int)c)%COLUMNS;
                int y = ((int)c)/ROWS;
                float xCoord = x*COLUMN_WIDTH;
                float yCoord = y*ROW_HEIGHT;

                // add texture coordinates
                texCoords[i*stride  ] = xCoord;
                texCoords[i*stride+1] = yCoord;

                texCoords[i*stride+2] = xCoord;
                texCoords[i*stride+3] = yCoord+ROW_HEIGHT*letterHeight;
                
                texCoords[i*stride+4] = xCoord+COLUMN_WIDTH*newWidth;
                texCoords[i*stride+5] = yCoord;
                
                texCoords[i*stride+6] = xCoord;
                texCoords[i*stride+7] = yCoord+ROW_HEIGHT*letterHeight;

                texCoords[i*stride+8] = xCoord+COLUMN_WIDTH*newWidth;
                texCoords[i*stride+9] = yCoord+ROW_HEIGHT*letterHeight;

                texCoords[i*stride+10] = xCoord+COLUMN_WIDTH*newWidth;
                texCoords[i*stride+11] = yCoord;
                
                // add color values
                for (int k = 0; k < 6; k++) {
                    colors[i*stride*2+k*4  ] = colorVal[0];
                    colors[i*stride*2+k*4+1] = colorVal[1];
                    colors[i*stride*2+k*4+2] = colorVal[2];
                    colors[i*stride*2+k*4+3] = colorVal[3];
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
        float maxWidth = 0;
        float width = 0;
        for (int j = 0; j < text.length(); j++) {
            char c = text.charAt(j);
            
            /*if (c == '@' && text.charAt(j+1) == '{')
            {
                // this is a color value
                int end = text.indexOf('}', j+2);
                j = end;
            }
            else*/ if (c == '\n')
            {
                // handle newlines, only return the greatest single line length
                maxWidth = Math.max(maxWidth, width);
                width = 0;
            }
            else {
                int i = (int) c;
                i %= 256;
                width += letterWidth * (float) metrics[i] / charPixelWidth;
            }
        }
        return Math.max(maxWidth, width);
    }
    
    /**
     * Loads a metric file for a font.
     * @param file Filename of the metric data.
     * @param name Name to use when binding this metric.
     */
    public static void loadMetric(String file, String name) {
        try {
            byte[] m = FileIO.getByteArray("/res/textures/" + file + ".metric");
            if (m.length != 258)
                throw new IllegalArgumentException("Metrics file must be 258 bytes long, was " + m.length + " bytes.");
            metricMap.put(name, m);
            useMetric(name);
        } catch (IOException e) {
            e.printStackTrace();
            Window.destroy();
            return;
        }
    }
    public static void useMetric(String name) {
        if (!metricMap.containsKey(name))
            throw new IllegalArgumentException("Font metric '"+name+"' is not loaded.");
        metrics = metricMap.get(name);
        charPixelWidth = metrics[256]; // directly store the width of characters, in pixels
        byte charPixelHeight = metrics[257];
        letterWidth = 1;//(float)charPixelWidth/charPixelHeight; // letterWidth is essentially the aspect ratio of the characters
        lineSpace = letterHeight = 1;
    }

    public static String breakIntoLines(String source, String fontMetric, double lineLengthLimit) {
        useMetric(fontMetric);
        StringBuilder sb = new StringBuilder();
        float spaceLength = getStringWidth(" ");

        String[] lines = source.split("\n", -1);
        int lineIndex = 0;
        for (String line : lines)
        {
            boolean lastLine = lineIndex == lines.length-1;
            lineIndex++;

            float lineLength = 0;
            String[] words = line.split(" ", -1);
            int wordIndex = 0;
            for (String word : words)
            {
                boolean lastWord = wordIndex == words.length-1;
                wordIndex++;

                //word = word.trim();
                float wordLength = getStringWidth(word);

                if (lineLength+spaceLength+wordLength > lineLengthLimit) {
                    sb.append("\n");
                    sb.append(word);
                    if (!lastWord)
                        sb.append(" ");
                    lineLength = wordLength+spaceLength;
                } else {
                    sb.append(word);
                    if (!lastWord)
                        sb.append(" ");
                    lineLength += wordLength+spaceLength;
                }
            }
            if (!lastLine)
                sb.append("\n");
        }
        /*int i = source.length()-1;
        while (i >= 0 && source.charAt(i) == '\n') {
            sb.append("\n"); // re-append trailing newines
            i--;
        }*/
        return sb.toString();//.trim();
    }
}
