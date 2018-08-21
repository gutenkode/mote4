package mote4.util.audio;

import mote4.scenegraph.Window;
import mote4.util.ErrorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL10.alGetSourcei;

/**
 * Created by Peter on 5/31/17.
 */
public class AudioPlayback {

    private static boolean playSfx, playMusic, isMusicPlaying;
    private static String currentMusic;
    private static List<Integer> sources;
    private static Map<String, Integer> loopingSfx;
    private static VorbisDecoder musicDecoder;
    static {
        sources = new ArrayList<>();
        loopingSfx = new HashMap<>();
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

        ErrorUtils.checkALError();
    }

    /**
     * Will play a sfx effect on loop until told to stop.
     * @param name
     */
    public static void loopSfx(String name) {
        if (!playSfx)
            return;
        if (!AudioLoader.bufferMap.containsKey(name))
            throw new IllegalArgumentException("Could not find wav audio buffer '"+name+"'.");

        if (loopingSfx.containsKey(name))
            stopLoopingSfx(name);

        int source = createSource();
        loopingSfx.put(name, source);

        alSourcei(source, AL_BUFFER, AudioLoader.bufferMap.get(name));
        alSourcei(source, AL_LOOPING, AL_TRUE);
        alSourcef(source, AL_GAIN, 1f);
        alSourcef(source, AL_PITCH, 1f);
        alSourcePlay(source);

        ErrorUtils.checkALError();
    }

    public static void pauseAllLoopingSfx() {
        for (int source : loopingSfx.values())
            alSourcePause(source);
    }
    public static void unpauseAllLoopingSfx() {
        for (int source : loopingSfx.values())
            alSourcePlay(source);
    }

    public static void stopLoopingSfx(String name) {
        int source = loopingSfx.get(name);
        alSourceStop(source);
        alDeleteSources(source);
        loopingSfx.remove(name);
    }

    public static void setLoopingSfxPitch(String name, float pitch) {
        int source = loopingSfx.get(name);
        alSourcef(source, AL_PITCH, pitch);
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
            throw new IllegalArgumentException("Vorbis file was not specified at runtime: '"+name+"'.");

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

        ErrorUtils.checkALError();
    }

    /**
     * Stops playback and rewinds the decoder.
     */
    public static void stopMusic() {
        if (musicDecoder != null) {
            alSourceStop(musicDecoder.source);
            musicDecoder.rewind();
            isMusicPlaying = false;
        }
    }

    /**
     * Stops playback without rewinding.
     * Calling resumeMusic() or playMusic() with the
     * same song name will resume playing from this point.
     */
    public static void pauseMusic() {
        if (musicDecoder != null) {
            alSourcePause(musicDecoder.source);
            isMusicPlaying = false;
        }
    }

    /**
     * If a music decoder exists, play it.
     */
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
        ErrorUtils.checkALError();

        alSourcef (source, AL_PITCH, 1f );
        alSourcef (source, AL_GAIN, 1f );
        alSource3f(source, AL_POSITION, 0f, 0f, 0f );
        alSource3f(source, AL_VELOCITY, 0f, 0f, 0f );

        ErrorUtils.checkALError();
        return source;
    }

    /**
     * Deletes all currently loaded sources and buffers.
     */
    public static void clear() {
        if (ALContext.isCreated())
        {
            for (int i : sources)
                alDeleteSources(i);
            ErrorUtils.checkALError();
            sources.clear();

            for (int i : AudioLoader.bufferMap.values())
                alDeleteBuffers(i);
            ErrorUtils.checkALError();
            AudioLoader.bufferMap.clear();

            currentMusic = "";
            isMusicPlaying = false;
            if (musicDecoder != null) {
                musicDecoder.close();
                ErrorUtils.checkALError();
            }
        }
    }
}
