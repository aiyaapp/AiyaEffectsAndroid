package com.aiyaapp.aiya.recorderTool;

import java.nio.ByteBuffer;

public interface AYAudioRecorderListener {
    void audioRecorderOutput(ByteBuffer byteBuffer, long timestamp);
}
