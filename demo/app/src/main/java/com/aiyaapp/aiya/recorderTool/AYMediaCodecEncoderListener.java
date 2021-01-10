package com.aiyaapp.aiya.recorderTool;

import android.media.MediaFormat;

public interface AYMediaCodecEncoderListener {

    void encoderOutputVideoFormat(MediaFormat format);

    void encoderOutputAudioFormat(MediaFormat format);
}
