package mote4.util.audio;

import mote4.util.ErrorUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;

import java.nio.IntBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.ALC11.ALC_DEFAULT_ALL_DEVICES_SPECIFIER;
import static org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 *
 * @author Peter
 */
public class ALContext {

    private static boolean created = false;
    private static long device, context;
    private static String lastDefaultDevice, currentDevice;

    /**
     * Creates the OpenAL context on the default device.
     * This must be called before loading or using any audio utilities.
     */
    public static void initContext() {
        // Can call "alc" functions at any time
        initContext(alcGetString(0, ALC_DEFAULT_ALL_DEVICES_SPECIFIER));
    }

    private static void initContext(String newDevice) {
        if (created)
            return;

        lastDefaultDevice = alcGetString(0, ALC_DEFAULT_ALL_DEVICES_SPECIFIER);
        currentDevice = newDevice;
        device = alcOpenDevice(currentDevice);
        if (device == NULL)
            throw new IllegalStateException("Failed to open audio device.");
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);

        //ALUtils.printALCInfo(device, deviceCaps);

        context = alcCreateContext(device, (IntBuffer)null);
        if (context == NULL)
            throw new IllegalStateException("Failed to create an OpenAL context.");

        alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);

        //ALUtils.printALInfo();
        //ALUtils.printDevices(EnumerateAllExt.ALC_ALL_DEVICES_SPECIFIER, "playback");

        alListenerfv(AL_ORIENTATION, new float[] { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f });
        alListener3f(AL_POSITION, 0, 0, 0);
        alListener3f(AL_VELOCITY, 0, 0, 0);

        ErrorUtils.checkALError();
        created = true;
    }

    public static boolean isCreated() { return created; }

    public static void destroyContext() {
        ErrorUtils.checkALError();
        if (created) {
            created = false;
            AudioPlayback.stopAllLoopingSfx();
            AudioPlayback.stopMusic();
            AudioPlayback.clearQueue();
            AudioPlayback.clear();

            ErrorUtils.checkALError();

            alcMakeContextCurrent(NULL);
            alcSetThreadContext(NULL);
            alcDestroyContext(context);
            alcCloseDevice(device);

            ErrorUtils.checkALError();

            System.out.println("OpenAL terminated.");
        }
    }

    public static String getLastDefaultDevice() {
        return lastDefaultDevice;
    }

    public static String getCurrentDevice() {
        return currentDevice;
    }

    public static long getCurrentDeviceID() {
        return device;
    }

    /**
     * Destroys and recreates the current audio context on a new device.
     * This WILL unload all sfx and music, which MUST be handled by the game.
     * @param newDevice
     */
    public static void switchToDevice(String newDevice) {
        ErrorUtils.checkALError();

        if (!created)
            return;
        destroyContext();
        initContext(newDevice);

        ErrorUtils.checkALError();
    }
}