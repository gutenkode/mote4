package mote4.util.audio;

import mote4.scenegraph.Window;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Peter on 12/6/16.
 */
public class Audio {

    private static HashMap<String, Integer> bufferMap;
    private static ArrayList<Integer> sources;

    static {
        bufferMap = new HashMap<>();
        sources = new ArrayList<>();
    }

    public static void loadWav(String filepath) {
        loadWav(filepath, filepath);
    }
    public static void loadWav(String filepath, String name) {
        int buffer = AL10.alGenBuffers();

        //if (AL10.alGetError() != AL10.AL_NO_ERROR)
        //    throw new RuntimeException("error loading sound");
        WaveData waveFile = WaveData.create("res/audio/"+filepath+".wav");
        if (waveFile == null) {
            System.err.println("Could not find audio file: "+filepath);
            Window.destroy();
        } else {
            AL10.alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
            waveFile.dispose();
            bufferMap.put(name, buffer);
        }
    }
    public static void loadOgg(String filepath) {
        loadOgg(filepath, filepath);
    }
    public static void loadOgg(String filepath, String name) {
        throw new UnsupportedOperationException("Ogg file loading not yet implemented.");
    }

    /**
     * This method will find an open source from the source list, bind the
     * buffer to it, and then play the source without looping.  If no free
     * source is found, it will create a new one and add it to the list.
     * This method ensures every sfx requested can play in its entirety while
     * limiting the number of sources created to minimum.
     * @param name
     */
    public static void playSfx(String name) {
        int source = -1;
        int listInd = 0;
        boolean sourceFound = false;
        while (listInd < sources.size() && !sourceFound)
        {
            source = sources.get(listInd);
            if (AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING)
                sourceFound = true;
            else
                listInd++;
        }
        if (!sourceFound)
            source = createSource();

        AL10.alSourcei (source, AL10.AL_BUFFER, bufferMap.get(name));
        AL10.alSourcei(source, AL10.AL_LOOPING, AL10.AL_FALSE);
        AL10.alSourcePlay(source);
    }

    /**
     * Creates a source and adds it to the list of sources.
     * @return
     */
    private static int createSource() {
        int source = AL10.alGenSources();

        if (AL10.alGetError() != AL10.AL_NO_ERROR)
            throw new RuntimeException("Error creating OpenAL source.");

        //AL10.alSourcei (source, AL10.AL_BUFFER, buffer);
        AL10.alSourcef (source, AL10.AL_PITCH, 1.0f );
        AL10.alSourcef (source, AL10.AL_GAIN, 1.0f );
        AL10.alSource3f(source, AL10.AL_POSITION, 0.0f, 0.0f, 0.0f );
        AL10.alSource3f(source, AL10.AL_VELOCITY, 0.0f, 0.0f, 0.0f );

        if (AL10.alGetError() != AL10.AL_NO_ERROR)
            throw new RuntimeException("Error creating OpenAL source.");

        sources.add(source);
        return source;
    }

    /**
     * Deletes all sources and buffers.
     */
    public static void clear() {
        if (ALContext.isCreated()) {
            for (int i : bufferMap.values())
                AL10.alDeleteBuffers(i);
            for (int i : sources)
                AL10.alDeleteSources(i);
        }
    }
}
