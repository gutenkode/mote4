package mote4.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import mote4.scenegraph.Window;

/**
 * Simplifies access to files.
 * @author Peter
 */
public class FileIO {
    /**
     * Returns a BufferedReader for the given file.
     * To read resource files, the path should begin with "/res/..."
     * @param filepath
     * @return
     * @throws NullPointerException 
     */
    public static BufferedReader getBufferedReader(String filepath) {
        try {
            return new BufferedReader(new InputStreamReader(getInputStream(filepath)));
        } catch (NullPointerException e) {
            System.err.println("Error loading file '"+filepath+"':");
            e.printStackTrace();
            Window.destroy();
            return null;
        }
    }
    /**
     * Returns an InputStream for the given filepath.
     * @param filepath
     * @return 
     */
    private static InputStream getInputStream(String filepath) {
        return ClassLoader.class.getResourceAsStream(filepath);
    }
    /**
     * Returns the contents of a file as a String.
     * @param filepath
     * @return 
     */
    public static String readFile(String filepath) {
        BufferedReader reader = getBufferedReader(filepath);
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            reader.close();

            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            Window.destroy();
            return null;
        }
    }
}
