package mote4.util.audio;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import mote4.scenegraph.Window;
import org.lwjgl.openal.*;

/**
 *
 * @author Peter
 */
public class ALContext {

    private static boolean created = false;

    /**
     * Creates the OpenAL context.  This must be called before loading or
     * using any audio utilities.
     */
    public static void initContext() {
        if (created)
            return;
        // Can call "alc" functions at any time
        long device = ALC10.alcOpenDevice((ByteBuffer)null);
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);

        long context = ALC10.alcCreateContext(device, (IntBuffer)null);
        ALC10.alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);

        AL10.alListenerfv(AL10.AL_ORIENTATION, new float[] { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f });
        AL10.alListener3f(AL10.AL_POSITION, 0, 0, 0);
        AL10.alListener3f(AL10.AL_VELOCITY, 0, 0, 0);

        created = true;
    }

    public static boolean isCreated() { return created; }
}