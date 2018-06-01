package mote4.util;

import com.google.common.io.ByteStreams;
import mote4.scenegraph.Window;
import org.lwjgl.BufferUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * File reading utilities.
 * @author Peter
 */
public class FileIO {

    private static Module currentModule;
    static {
        currentModule = FileIO.class.getModule();
    }

    /**
     * Returns an InputStream for a given filepath.
     * This method is called by every other method in FileIO.
     * @param filepath
     * @return
     */
    public static InputStream getInputStream(String filepath) {
        try {
            InputStream is = new BufferedInputStream(currentModule.getResourceAsStream(filepath));
            if (is == null)
                throw new IllegalArgumentException("Could not open '" + filepath + "': InputStream is null");
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

    /**
     * Get the contents of a file as a ByteBuffer.
     * @param filepath
     * @return
     */
    public static ByteBuffer getByteBuffer(String filepath) {
        byte[] file = getByteArray(filepath);
        ByteBuffer buffer = BufferUtils.createByteBuffer(file.length);
        buffer.put(file);
        buffer.flip();
        return buffer;
    }

    /**
     * Get the contents of a file as an array of bytes.
     * @param filepath
     * @return
     */
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
     * Get the contents of a file as a single string.
     * Line breaks are appended between lines.
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

    /**
     * Get the contents of a file as a list of strings.
     * @param filepath
     * @return
     */
    public static List<String> getStringList(String filepath) {
        BufferedReader reader = getBufferedReader(filepath);
        List<String> strings = new ArrayList<>();
        String line;
        try {
            while((line = reader.readLine()) != null)
                strings.add(line);
            reader.close();
            return strings;
        } catch (IOException e) {
            e.printStackTrace();
            Window.destroy();
            return null;
        }
    }

    /**
     * Get the contents of a file as an array of strings.
     * @param filepath
     * @return
     */
    public static String[] getStringArray(String filepath) {
        BufferedReader reader = getBufferedReader(filepath);
        List<String> strings = new ArrayList<>();
        String line;
        try {
            while((line = reader.readLine()) != null)
                strings.add(line);
            reader.close();
            return strings.toArray(new String[strings.size()]);
        } catch (IOException e) {
            e.printStackTrace();
            Window.destroy();
            return null;
        }
    }
}
