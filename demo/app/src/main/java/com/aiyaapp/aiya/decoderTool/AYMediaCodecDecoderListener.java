package com.aiyaapp.aiya.decoderTool;

import android.media.MediaFormat;

import java.nio.ByteBuffer;

public interface AYMediaCodecDecoderListener {

    void decoderOutputVideoFormat(int width, int height);

    void decoderOutputAudioFormat(int sampleRate, int channelCount);

    void decoderVideoOutput(int texture, int width, int height, long timestamp);

    void decoderAudioOutput(ByteBuffer byteBuffer, long timestamp);

    void decoderVideoEOS();

    void decoderAudioEOS();

}
