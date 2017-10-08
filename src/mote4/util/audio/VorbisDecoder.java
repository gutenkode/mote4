package mote4.util.audio;

import mote4.util.FileIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbisInfo;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static java.lang.Math.*;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.SOFTDirectChannels.AL_DIRECT_CHANNELS_SOFT;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/*
 * Pulled from the LWJGL demo on vorbis decoding.
 *
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
public class VorbisDecoder {

    private static final int BUFFER_SIZE = 1024 * 4;

    final ByteBuffer vorbis;

    final long handle;
    final int  channels;
    final int  sampleRate;
    final int  format;
    final int  source;

    final int   lengthSamples;
    final float lengthSeconds;
    final boolean loop;

    final ShortBuffer pcm;
    final IntBuffer   buffers;

    int samplesLeft;

    VorbisDecoder(String filePath, boolean loop) {
        vorbis = FileIO.getByteBuffer(filePath);

        this.loop = loop;

        source = alGenSources();
        alSourcei(source, AL_DIRECT_CHANNELS_SOFT, AL_TRUE);

        buffers = BufferUtils.createIntBuffer(2);
        alGenBuffers(buffers);

        IntBuffer error = BufferUtils.createIntBuffer(1);
        handle = stb_vorbis_open_memory(vorbis, error, null);
        if (handle == NULL) {
            throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
        }

        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            stb_vorbis_get_info(handle, info);
            //VorbisDecoder.printInfo(handle, info);
            this.channels = info.channels();
            this.sampleRate = info.sample_rate();
        }

        this.format = getFormat(channels);

        this.lengthSamples = stb_vorbis_stream_length_in_samples(handle);
        this.lengthSeconds = stb_vorbis_stream_length_in_seconds(handle);

        this.pcm = BufferUtils.createShortBuffer(BUFFER_SIZE);

        samplesLeft = lengthSamples;
    }

    private static void printInfo(long decoder, STBVorbisInfo info) {
        System.out.println("stream length, samples: " + stb_vorbis_stream_length_in_samples(decoder));
        System.out.println("stream length, seconds: " + stb_vorbis_stream_length_in_seconds(decoder));

        System.out.println();

        //stb_vorbis_get_info(decoder, info);

        System.out.println("channels = " + info.channels());
        System.out.println("sampleRate = " + info.sample_rate());
        System.out.println("maxFrameSize = " + info.max_frame_size());
        System.out.println("setupMemoryRequired = " + info.setup_memory_required());
        System.out.println("setupTempMemoryRequired() = " + info.setup_temp_memory_required());
        System.out.println("tempMemoryRequired = " + info.temp_memory_required());
    }

    private static int getFormat(int channels) {
        switch (channels) {
            case 1:
                return AL_FORMAT_MONO16;
            case 2:
                return AL_FORMAT_STEREO16;
            default:
                throw new UnsupportedOperationException("Unsupported number of channels: " + channels);
        }
    }

    private boolean stream(int buffer) {
        int samples = 0;

        while (samples < BUFFER_SIZE) {
            pcm.position(samples);
            int samplesPerChannel = stb_vorbis_get_samples_short_interleaved(handle, channels, pcm);
            if (samplesPerChannel == 0) {
                break;
            }

            samples += samplesPerChannel * channels;
        }

        if (samples == 0) {
            return false;
        }

        pcm.position(0);
        alBufferData(buffer, format, pcm, sampleRate);
        samplesLeft -= samples / channels;

        return true;
    }

    float getProgress() {
        return 1.0f - samplesLeft / (float)(lengthSamples);
    }

    float getProgressTime(float progress) {
        return progress * lengthSeconds;
    }

    void rewind() {
        stb_vorbis_seek_start(handle);
        samplesLeft = lengthSamples;
    }

    void skip(int direction) {
        seek(min(max(0, stb_vorbis_get_sample_offset(handle) + direction * sampleRate), lengthSamples));
    }

    void skipTo(float offset0to1) {
        seek(round(lengthSamples * offset0to1));
    }

    private void seek(int sample_number) {
        stb_vorbis_seek(handle, sample_number);
        samplesLeft = lengthSamples - sample_number;
    }

    boolean play() {
        for (int i = 0; i < buffers.limit(); i++) {
            if (!stream(buffers.get(i))) {
                return false;
            }
        }

        alSourceQueueBuffers(source, buffers);
        alSourcePlay(source);

        return true;
    }

    boolean update() {
        int processed = alGetSourcei(source, AL_BUFFERS_PROCESSED);

        for (int i = 0; i < processed; i++) {
            int buffer = alSourceUnqueueBuffers(source);

            if (!stream(buffer)) {
                boolean shouldExit = true;

                if (loop) {
                    rewind();
                    shouldExit = !stream(buffer);
                }

                if (shouldExit) {
                    return false;
                }
            }
            alSourceQueueBuffers(source, buffer);
        }

        if (processed == 2) {
            alSourcePlay(source);
        }

        return true;
    }

    void close() {
        stb_vorbis_close(handle);
        alDeleteSources(source);
        alDeleteBuffers(buffers);
    }
}
