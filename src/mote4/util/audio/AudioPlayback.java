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

    private static boolean playSfx, playMusic, isMusicPlaying, isMusicPaused;
    private static String currentMusic;
    private static float sfxVolume, musicVolume, musicFadeVolume, fadeStartGain, fadeEndGain;
    private static double fadeStartTime, fadeTransitionTime;

    private static List<Integer> sources;
    private static Map<String, Integer> loopingSfx;
    private static VorbisDecoder musicDecoder;

    static {
        sources = new ArrayList<>();
        loopingSfx = new HashMap<>();
        currentMusic = "";
        playSfx = playMusic = true;
        isMusicPlaying = false;
        isMusicPaused = false;
        sfxVolume = 1;
        musicVolume = 1;
        musicFadeVolume = 1;
    }

    ////////////////////
    // Config methods

    public static void enableSfx(boolean enable) {
        playSfx = enable;
        if (!playSfx)
            pauseAllLoopingSfx();
    }
    public static void enableMusic(boolean enable) {
        playMusic = enable;
        if (!playMusic)
            pauseMusic(); // automatically pause music, but don't automatically start playing it
    }
    public static boolean isSfxEnabled() { return playSfx; }
    public static boolean isMusicEnabled() { return playMusic; }
    public static boolean isMusicPlaying() { return isMusicPlaying; }
    public static boolean isIsMusicPaused() { return isMusicPaused; }

    public static void setSfxVolume(float volume) {
        sfxVolume = volume;
        for (int source : loopingSfx.values())
            alSourcef(source, AL_GAIN, sfxVolume);
    }
    public static void setMusicVolume(float volume) {
        musicVolume = volume;
        if (musicDecoder != null) {
            musicDecoder.setVolume(musicVolume);
        }
    }
    public static float getSfxVolume() { return sfxVolume; }
    public static float getMusicVolume() { return musicVolume; }

    ////////////////////
    // Sfx methods

    /**
     * Play an audio buffer as sfx.
     * Sfx will always play their full length without interruption, and not loop.
     * @param name
     */
    public static void playSfx(String name) {
        playSfx(name,1f,1f);
    }
    public static void playSfx(String name, float gain, float pitch) {
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
        alSourcef(source, AL_GAIN, sfxVolume *gain);
        alSourcef(source, AL_PITCH, pitch);
        alSourcePlay(source);

        ErrorUtils.checkALError();
    }

    /**
     * Will play a sfx effect on loop until told to stop.
     * @param name
     */
    public static void loopSfx(String name) { loopSfx(name,1); }
    public static void loopSfx(String name, float gain) {
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
        alSourcef(source, AL_GAIN, sfxVolume *gain);
        alSourcef(source, AL_PITCH, 1f);
        alSourcePlay(source);

        ErrorUtils.checkALError();
    }


    ////////////////////
    // Looping sfx methods

    public static void pauseAllLoopingSfx() {
        for (int source : loopingSfx.values())
            alSourcePause(source);
    }
    public static void stopAllLoopingSfx() {
        for (String name : loopingSfx.keySet())
            stopLoopingSfx(name);
    }
    public static void unpauseAllLoopingSfx() {
        if (playSfx)
            for (int source : loopingSfx.values())
                alSourcePlay(source);
    }

    public static void stopLoopingSfx(String name) {
        if (loopingSfx.containsKey(name)) {
            int source = loopingSfx.get(name);
            alSourceStop(source);
            alDeleteSources(source);
            loopingSfx.remove(name);
        }
    }

    public static void setLoopingSfxPitch(String name, float pitch) {
        if (loopingSfx.containsKey(name)) {
            int source = loopingSfx.get(name);
            alSourcef(source, AL_PITCH, pitch);
        }
    }

    ////////////////////
    // Music methods

    /**
     * Play an audio buffer as music.
     * Only one music track can play at a time.
     * @param name
     */
    public static void playMusic(String name, boolean loop) {
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
            // even if music is disabled, songs will be loaded and ready to play
            if (!playMusic) {
                alSourcePause(musicDecoder.source);
                isMusicPlaying = false;
            }

            // reset all music volume and fade parameters upon new music
            fadeStartGain = 1;
            fadeEndGain = 1;
            musicDecoder.setVolume(musicVolume);
        }
        currentMusic = name;
        isMusicPlaying = true;
        isMusicPaused = false;

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
            isMusicPaused = false;
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
            isMusicPaused = true;
        }
    }

    /**
     * If a music decoder exists, play it.
     */
    public static void resumeMusic() {
        if (playMusic)
            if (musicDecoder != null) {
                alSourcePlay(musicDecoder.source);
                isMusicPlaying = true;
                isMusicPaused = false;
            }
    }

    public static String getCurrentMusic() { return currentMusic; }

    /**
     * Create a volume fade for currently playing music. Music is NOT paused, even if target gain is 0.
     * @param startGain The music will immediately be set to this level, scaled by the music volume.  A value of 1 is "full", independent of normal volume setting.
     * @param endGain The music will fade smoothly to this level.
     * @param time How long in seconds the transition should take.
     */
    public static void setMusicFade(float startGain, float endGain, double time) {
        fadeStartGain = startGain;
        fadeEndGain = endGain;
        fadeStartTime = Window.time();
        fadeTransitionTime = time;
        musicFadeVolume = startGain;
        if (isMusicPaused) {
            resumeMusic();
        }
    }

    ////////////////////
    // Utility methods

    /**
     * Updates streaming music; called by the game loop.
     */
    public static void updateMusic() {
        // TODO support fading music in/out
        if (musicDecoder != null) {

            musicFadeVolume = getMusicFade();
            musicDecoder.setVolume(musicVolume * musicFadeVolume);


            if (!isMusicPlaying) {
                return;
            }

            if (musicFadeVolume == 0) {
                pauseMusic();
                return;
            }

            if (!musicDecoder.update()) {
                stopMusic();
            }
        }
    }

    private static float getMusicFade() {
        double step = (Window.time()-fadeStartTime)/fadeTransitionTime;
        step = Math.max(0, Math.min(1, step));
        return fadeStartGain *(float)(1-step) + fadeEndGain *(float)step;
    }

    public static float getCurrentFadeTarget() {
        return fadeEndGain;
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
