package mote4.util;

import com.google.common.io.ByteStreams;
import mote4.scenegraph.Window;
import org.lwjgl.BufferUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

/**
 * File reading utilities.
 * @author Peter
 */
public class FileIO {
    /**
     * Returns a BufferedReader for a given file.
     * To read resource files, the path should begin with "/res/..."
     * @param filepath
     * @return
     * @throws NullPointerException 
     */
    public static BufferedReader getBufferedReader(String filepath) {
        try {
            return new BufferedReader(new InputStreamReader(getInputStream(filepath)));
        } catch (NullPointerException e) {
            e.printStackTrace();
            Window.destroy();
            return null;
        }
    }

    /**
     * Returns an InputStream for a given filepath.
     * @param filepath
     * @return 
     */
    public static InputStream getInputStream(String filepath) {
        return ClassLoader.class.getResourceAsStream(filepath);
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
}
