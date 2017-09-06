package mote4.util.audio;

import mote4.scenegraph.Window;

import java.util.ArrayList;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL10.alGetSourcei;

/**
 * Created by Peter on 5/31/17.
 */
public class AudioPlayback {

    private static boolean playSfx, playMusic, isMusicPlaying;
    private static String currentMusic;
    private static ArrayList<Integer> sources;
    private static VorbisDecoder musicDecoder;
    static {
        sources = new ArrayList<>();
        currentMusic = "";
        playSfx = playMusic = true;
        isMusicPlaying = false;
    }

    public static void enableSfx(boolean enable) { playSfx = enable; }
    public static void enableMusic(boolean enable) { playMusic = enable; } // TODO same as other todo in playMusic()
    public static boolean isSfxEnabled() { return playSfx; }
    public static boolean isMusicEnabled() { return playMusic; }

    /**
     * Play an audio buffer as sfx.
     * Sfx will always play their full length without interruption, and not loop.
     * @param name
     */
    public static void playSfx(String name) {
        if (!playSfx)
            return;
        if (!AudioLoader.bufferMap.containsKey(name))
            throw new IllegalArgumentException("Could not find wav audio buffer '"+name+"'.");

        int source = -1;
        int listInd = 0;
        boolean sourceFound = false;
        /*
         * This method will find an open source from the source list, bind the
         * buffer to it, and then play the source without looping.  If no free
         * source is found, it will create a new one and add it to the list.
         */
        while (listInd < sources.size() && !sourceFound)
        {
            source = sources.get(listInd);
            if (alGetSourcei(source, AL_SOURCE_STATE) != AL_PLAYING)
                sourceFound = true;
            else
                listInd++;
        }
        if (!sourceFound) {
            source = createSource();
            sources.add(source);
        }

        alSourcei(source, AL_BUFFER, AudioLoader.bufferMap.get(name));
        alSourcei(source, AL_LOOPING, AL_FALSE);
        alSourcef(source, AL_GAIN, 1f);
        alSourcef(source, AL_PITCH, 1f);
        alSourcePlay(source);
    }

    /**
     * Play an audio buffer as music.
     * Only one music track can play at a time.
     * @param name
     */
    public static void playMusic(String name, boolean loop) {
        if (!playMusic) // TODO toggling music should simply start/pause a loaded audio buffer, instead of not loading music
            return;
        if (!AudioLoader.vorbisMap.containsKey(name))
            throw new IllegalArgumentException("Could not find vorbis file '"+name+"'.");

        if (name.equals(currentMusic)) {
            resumeMusic();
        } else {
            if (musicDecoder != null) {
                musicDecoder.close();
            }
            musicDecoder = new VorbisDecoder(AudioLoader.vorbisMap.get(name), loop);
            if (!musicDecoder.play()) {
                System.err.println("Music playback failed.");
                Window.destroy();
            }
        }
        currentMusic = name;
        isMusicPlaying = true;
    }

    public static void stopMusic() {
        if (musicDecoder != null) {
            alSourceStop(musicDecoder.source);
            isMusicPlaying = false;
        }
    }
    public static void pauseMusic() {
        if (musicDecoder != null) {
            alSourcePause(musicDecoder.source);
            isMusicPlaying = false;
        }
    }
    public static void resumeMusic() {
        if (musicDecoder != null) {
            alSourcePlay(musicDecoder.source);
            isMusicPlaying = true;
        }
    }

    /**
     * Updates streaming music; called by the game loop.
     */
    public static void updateMusic() {
        // TODO support fading music in/out
        if (musicDecoder != null) {
            if (isMusicPlaying)
                musicDecoder.update();
        }
    }

    private static int createSource() {
        int source = alGenSources();

        if (alGetError() != AL_NO_ERROR)
            throw new RuntimeException("Error creating OpenAL source.");

        alSourcef (source, AL_PITCH, 1f );
        alSourcef (source, AL_GAIN, 1f );
        alSource3f(source, AL_POSITION, 0f, 0f, 0f );
        alSource3f(source, AL_VELOCITY, 0f, 0f, 0f );

        if (alGetError() != AL_NO_ERROR)
            throw new RuntimeException("Error creating OpenAL source.");

        return source;
    }

    /**
     * Deletes all currently loaded sources and buffers.
     */
    public static void clear() {
        if (ALContext.isCreated()) {
            for (int i : AudioLoader.bufferMap.values())
                alDeleteBuffers(i);
            AudioLoader.bufferMap.clear();
            for (int i : sources)
                alDeleteSources(i);
            sources.clear();

            currentMusic = "";
            isMusicPlaying = false;
            if (musicDecoder != null) {
                musicDecoder.close();
            }
        }
    }
}
