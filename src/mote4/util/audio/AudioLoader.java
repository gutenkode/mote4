package mote4.util.audio;

import mote4.scenegraph.Window;
import mote4.util.ErrorUtils;
import mote4.util.FileIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import static org.lwjgl.openal.AL10.*;

/**
 * Created by Peter on 12/6/16.
 */
public class AudioLoader {

    static HashMap<String, Integer> bufferMap;
    static HashMap<String, String> vorbisMap;
    static {
        bufferMap = new HashMap<>();
        vorbisMap = new HashMap<>();
    }

    /**
     * Load all audio files specified in an index file.
     * The index file must be in the res/audio directory.
     */
    public static void loadIndex(String filename) {
        try {
            BufferedReader br = FileIO.getBufferedReader("/res/audio/"+filename);
            String in;
            while((in = br.readLine()) != null) {
                if (in.isEmpty() || in.startsWith("#")) // skip empty lines or comments
                    continue;
                String[] keys = in.split("\t+");
                if (keys.length == 2 && keys[1].contains(".")) { // must have two keys
                    for (int i = 0; i < keys.length; i++) // remove whitespace
                        keys[i] = keys[i].trim();
                    String[] str = keys[1].split("\\."); // split the file extension
                    if (str[1].equals("wav"))
                        loadWav(str[0],keys[0]); // loadWav and loadOgg automatically append file extensions internally
                    else if (str[1].equals("ogg"))
                        loadOgg(str[0],keys[0]);
                    else
                        System.err.println("Invalid audio file format: "+keys[1]);
                } else
                    System.out.println("Invalid audio index line: "+in);
            }
        } catch (IOException e) {
            System.err.println("Error reading audio index file.");
            e.printStackTrace();
            Window.destroy();
        }
    }

    public static void loadWav(String filepath) {
        loadWav(filepath, filepath);
    }
    public static void loadWav(String filepath, String name) {
        int buffer = alGenBuffers();
        ErrorUtils.checkALError();

        WaveData waveFile = WaveData.create("res/audio/"+filepath+".wav");
        if (waveFile == null) {
            System.err.println("Couldn't find audio file: "+filepath);
            Window.destroy();
        } else {
            alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
            waveFile.dispose();
            bufferMap.put(name, buffer);
        }
    }

    public static void loadOgg(String filepath) {
        loadOgg(filepath, filepath);
    }
    public static void loadOgg(String filepath, String name) {
        // ogg files are loaded when they are played, so only store the filepath
        vorbisMap.put(name, "/res/audio/"+filepath+".ogg");
    }

    public static int getNumSongs() {
        return vorbisMap.size();
    }

    public static String[] getAllSongNames() {
        return vorbisMap.keySet().toArray(new String[0]);
    }
}
