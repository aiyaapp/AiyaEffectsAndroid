package com.aiyaapp.aiya.recorderTool;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AYAudioRecorderWrap {

    private AYAudioRecorderListener audioRecorderListener;

    private AudioRecord audioRecord;
    private int bufferSize;

    private boolean isStop;

    private Lock lock = new ReentrantLock();

    public AYAudioRecorderWrap(AudioRecord audioRecord, int bufferSize) {
        this.audioRecord = audioRecord;
        this.bufferSize = bufferSize;
    }

    public void startRecording() {
        audioRecord.startRecording();
        isStop = false;

        new Thread() {
            @Override
            public void run() {
                ByteBuffer audioBuffer=ByteBuffer.allocateDirect(bufferSize);

                long readTotalSize = 0;

                while (true) {

                    lock.lock();

                    if (isStop) {
                        lock.unlock();
                        return;
                    }
                    audioBuffer.clear();

                    int readSize = audioRecord.read(audioBuffer, bufferSize);
                    if (readSize != AudioRecord.ERROR_INVALID_OPERATION) {

                        int perFrameSize = 1;
                        if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_FLOAT) {
                            perFrameSize = 4;
                        } else if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) {
                            perFrameSize = 2;
                        } else if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT) {
                            perFrameSize = 1;
                        }

                        readTotalSize += readSize;
                        float timeInterval = (float)readTotalSize / (float)(audioRecord.getChannelCount() * perFrameSize * audioRecord.getSampleRate());
                        long timestamp = (long) (timeInterval * 1000 * 1000 * 1000);

                        if (audioRecorderListener != null) {
                            audioRecorderListener.audioRecorderOutput(audioBuffer, timestamp);
                        }
                    }

                    lock.unlock();

                    // 休息一会, 释放锁资源
                    SystemClock.sleep(1);
                }
            }
        }.start();
    }

    public void stop() {
        lock.lock();

        audioRecord.stop();
        isStop = true;

        lock.unlock();
    }

    public void setAudioRecorderListener(AYAudioRecorderListener audioRecorderListener) {
        this.audioRecorderListener = audioRecorderListener;
    }
}

