package mote4.util;

import com.google.common.io.ByteStreams;
import mote4.scenegraph.Window;
import org.lwjgl.BufferUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

/**
 * File reading utilities.
 * @author Peter
 */
public class FileIO {

    private static Module currentModule;
    //private static ClassLoader currentClassLoader;
    static {
        currentModule = FileIO.class.getModule();
        //currentClassLoader = FileIO.class.getClassLoader();
    }

    /**
     * Returns an InputStream for a given filepath.
     * This method is called for every resource, the other methods in this class are convenience wrappers.
     * @param filepath
     * @return
     */
    public static InputStream getInputStream(String filepath) {
        try {
            //System.out.println("Loading '"+filepath+"' from '"+currentModule+"'...");
            InputStream is = new BufferedInputStream(currentModule.getResourceAsStream(filepath));
            if (is == null) {
                throw new IllegalArgumentException("Could not open '" + filepath + "': InputStream is null");
            }
            return is;
        } catch (IOException e) {
            throw new IllegalArgumentException("IOException while opening '" + filepath + "': "+e.getMessage());
        }
    }

    /**
     * Returns a BufferedReader for a given file.
     * To read resource files, the path should begin with "/res/..."
     * @param filepath
     * @return
     */
    public static BufferedReader getBufferedReader(String filepath) {
        return new BufferedReader(new InputStreamReader(getInputStream(filepath)));
    }


    public static ByteBuffer getByteBuffer(String filepath) {
        byte[] file = getByteArray(filepath);
        ByteBuffer buffer = BufferUtils.createByteBuffer(file.length);
        buffer.put(file);
        buffer.flip();
        return buffer;
    }

    public static byte[] getByteArray(String filepath) {
        try {
            return ByteStreams.toByteArray(getInputStream(filepath));
        } catch (IOException e) {
            e.printStackTrace();
            Window.destroy();
            return null;
        }
    }

    /**
     * Returns the contents of a file as a String.
     * @param filepath
     * @return 
     */
    public static String getString(String filepath) {
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

    public static void setResourceModule(Module m) {
        currentModule = m;
    }
}
