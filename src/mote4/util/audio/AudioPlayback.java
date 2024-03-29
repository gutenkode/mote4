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

    private static boolean playSfx, playMusic, isMusicPlaying, isMusicPaused, stopOnFadeOut = false, isFadeDone = true;
    private static String currentMusic;
    private static float sfxVolume, musicVolume, musicFadeVolume, fadeStartGain, fadeEndGain;
    private static double fadeStartTime, fadeTransitionTime;

    private static List<Integer> sources;
    private static Map<String, Integer> loopingSfx;
    private static List<SongEntry> queuedSongs;
    private static SongEntry songToPlay;
    private static VorbisDecoder musicDecoder;

    private static double lastFade = -1;

    private static class SongEntry {
        public String name;
        public boolean loop;

        public VorbisDecoder decoder;

        public SongEntry(String name, boolean loop) {
            this.name = name;
            this.loop = loop;

            decoder = new VorbisDecoder(AudioLoader.vorbisMap.get(name), loop);
            decoder.update();
        }
    }

    static {
        sources = new ArrayList<>();
        loopingSfx = new HashMap<>();
        queuedSongs = new ArrayList<>();
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
        for (int source : loopingSfx.values()) {
            alSourcef(source, AL_GAIN, sfxVolume);
        }
        ErrorUtils.checkALError();
    }
    public static void setMusicVolume(float volume) {
        musicVolume = volume;
        if (musicDecoder != null) {
            musicDecoder.setVolume(musicVolume);
        }
        ErrorUtils.checkALError();
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
        if (!ALContext.isCreated())
            return;
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
        ErrorUtils.checkALError();
    }
    public static void stopAllLoopingSfx() {
        for (String name : loopingSfx.keySet())
            stopLoopingSfx(name);
        ErrorUtils.checkALError();
    }
    public static void unpauseAllLoopingSfx() {
        if (playSfx)
            for (int source : loopingSfx.values())
                alSourcePlay(source);
        ErrorUtils.checkALError();
    }

    public static void stopLoopingSfx(String name) {
        if (loopingSfx.containsKey(name)) {
            int source = loopingSfx.get(name);
            alSourceStop(source);
            alDeleteSources(source);
            loopingSfx.remove(name);
            ErrorUtils.checkALError();
        }
    }

    public static void setLoopingSfxPitch(String name, float pitch) {
        if (loopingSfx.containsKey(name)) {
            int source = loopingSfx.get(name);
            alSourcef(source, AL_PITCH, pitch);
            ErrorUtils.checkALError();
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
        if (!AudioLoader.vorbisMap.containsKey(name)) {
            throw new IllegalArgumentException("Vorbis file was not specified at runtime: '" + name + "'.");
        }

        if (name.equals(currentMusic) && songToPlay == null) {
            resumeMusic();
        }
        else {
            isMusicPaused = false; // hacky but probably not a problem
            currentMusic = name;

            songToPlay = new SongEntry(name, loop);
        }
    }

    private static void doPlayMusic() {
        if (songToPlay == null) {
            return;
        }

        if (musicDecoder != null) {
            musicDecoder.close();
        }

        musicDecoder = songToPlay.decoder;//new VorbisDecoder(AudioLoader.vorbisMap.get(name), loop);
        if (!musicDecoder.play()) {
            System.err.println("Music playback failed.");
            Window.destroy();
        }

        currentMusic = songToPlay.name;

        // even if music is disabled, songs will be loaded and ready to play
        isMusicPlaying = playMusic;
        isMusicPaused = false;
        if (!playMusic) {
            alSourcePause(musicDecoder.source);
        }

        // don't reset fade upon playing new music
        //fadeStartGain = 1;
        //fadeEndGain = 1;
        musicDecoder.setVolume(musicVolume);

        ErrorUtils.checkALError();

        songToPlay = null;
    }

    /**
     * Add a buffer to the play queue.
     * Queued tracks won't play if the current song is set to loop.
     */
    public static void queueMusic(String name, boolean loop) {
        queuedSongs.add(new SongEntry(name,loop));
    }

    /**
     * Stops playback and rewinds the decoder.
     */
    public static void stopMusic() {
        if (musicDecoder != null && songToPlay == null) {
            alSourceStop(musicDecoder.source);
            ErrorUtils.checkALError();
        }
        currentMusic = "";
        musicDecoder = null;
        isMusicPlaying = false;
        isMusicPaused = false;
    }

    public static void clearQueue() {
        for (SongEntry entry : queuedSongs) {
            entry.decoder.close();
        }
        queuedSongs.clear();
    }

    /**
     * Stops playback without rewinding.
     * Calling resumeMusic() or playMusic() with the
     * same song name will resume playing from this point.
     */
    public static void pauseMusic() {
        if (musicDecoder != null && songToPlay == null) {
            alSourcePause(musicDecoder.source);
            ErrorUtils.checkALError();
        }
        isMusicPlaying = false;
        isMusicPaused = true;
    }

    /**
     * If a music decoder exists, play it.
     */
    public static void resumeMusic() {
        if (playMusic)
            if (musicDecoder != null && songToPlay == null) {
                alSourcePlay(musicDecoder.source);
                ErrorUtils.checkALError();
                isMusicPlaying = true;
                isMusicPaused = false;
            }
    }

    public static String getCurrentMusic() {
        if (songToPlay != null)
            return songToPlay.name;
        return currentMusic;
    }

    /**
     * Create a volume fade for currently playing music.
     * If music is paused or not playing and endGain is 0, nothing will be done.
     * @param startGain The music will immediately be set to this level, scaled by the music volume.  A value of 1 is "full", independent of normal volume setting.
     * @param endGain The music will fade smoothly to this level.
     * @param time How long in seconds the transition should take.
     */
    public static void setMusicFade(float startGain, float endGain, double time, boolean stopOnFadeOut) {
        AudioPlayback.stopOnFadeOut = stopOnFadeOut;
        if (endGain == 0 && (isMusicPaused || !isMusicPlaying || !playMusic)) {
            if (stopOnFadeOut)
                stopMusic();
            else
                pauseMusic();
            return;
        }
        fadeStartGain = startGain;
        fadeEndGain = endGain;
        fadeStartTime = Window.time();
        fadeTransitionTime = time;
        musicFadeVolume = startGain;
        isFadeDone = false;
        if (isMusicPaused) {
            resumeMusic();
        }
        ErrorUtils.checkALError();
    }

    ////////////////////
    // Utility methods

    /**
     * Updates streaming music; called by the game loop.
     */
    public static void updateMusic() {
        if (songToPlay != null) {
            doPlayMusic();
        }

        if (musicDecoder != null)
        {
            if (!isMusicPlaying) {
                return;
            }

            musicFadeVolume = getMusicFade();
            if (musicFadeVolume != lastFade) {
                lastFade = musicFadeVolume;
                musicDecoder.setVolume(musicVolume * musicFadeVolume);
                ErrorUtils.checkALError();

                if (musicFadeVolume == 0 && fadeEndGain == 0 && isFadeDone) {
                    if (stopOnFadeOut)
                        stopMusic();
                    else
                        pauseMusic();
                    return;
                }
            }

            if (!musicDecoder.update()) {
                if (!queuedSongs.isEmpty())
                {
                    alSourceStop(musicDecoder.source);
                    musicDecoder.rewind();
                    isMusicPlaying = false;
                    isMusicPaused = false;
                    ErrorUtils.checkALError();

                    songToPlay = queuedSongs.remove(0);
                    doPlayMusic();
                }
                else {
                    stopMusic();
                }
                return;
            }
        }
    }

    private static void doUpdateLoop() {
        boolean loop = true;
        while (loop)
        {
            updateMusic();

            try {
                Thread.sleep(30);
                loop = !Thread.currentThread().isInterrupted();
            }
            catch (InterruptedException e) {
                loop = false;
            }
        }
    }

    private static Thread updateThread;
    public static void startUpdateThread() {
        if (updateThread != null && updateThread.isAlive())
            return;
        updateThread = new Thread(AudioPlayback::doUpdateLoop);
        updateThread.start();
    }

    public static void stopUpdateThread() {
        updateThread.interrupt();
        updateThread = null;
    }

    private static float getMusicFade() {
        double step = (Window.time()-fadeStartTime)/fadeTransitionTime;
        step = Math.max(0, Math.min(1, step));
        isFadeDone = step == 1;
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
