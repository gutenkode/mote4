package mote4.util.audio;

import mote4.util.ErrorUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 *
 * @author Peter
 */
public class ALContext {

    private static boolean created = false;
    private static long device, context;
    private static String lastDefaultDevice;

    /**
     * Creates the OpenAL context.  
     * This must be called before loading or using any audio utilities.
     */
    public static void initContext() {
        if (created)
            return;

        // Can call "alc" functions at any time
        lastDefaultDevice = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        device = alcOpenDevice(lastDefaultDevice);
        if (device == NULL)
            throw new IllegalStateException("Failed to open the default device.");
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);

        //ALUtils.printALCInfo(device, deviceCaps);

        context = alcCreateContext(device, (IntBuffer)null);
        if (context == NULL)
            throw new IllegalStateException("Failed to create an OpenAL context.");

        alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);

        //ALUtils.printALInfo();
        //ALUtils.printDevices();

        alListenerfv(AL_ORIENTATION, new float[] { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f });
        alListener3f(AL_POSITION, 0, 0, 0);
        alListener3f(AL_VELOCITY, 0, 0, 0);

        ErrorUtils.checkALError();
        created = true;
    }

    public static boolean isCreated() { return created; }

    public static void destroyContext() {
        if (created) {
            AudioPlayback.stopAllLoopingSfx();
            AudioPlayback.clear();

            alcSetThreadContext(NULL);
            alcDestroyContext(context);
            alcCloseDevice(device);

            System.out.println("OpenAL terminated.");
            created = false;
        }
    }

    public static String getCurrentDevice() {
        return lastDefaultDevice;
    }
}