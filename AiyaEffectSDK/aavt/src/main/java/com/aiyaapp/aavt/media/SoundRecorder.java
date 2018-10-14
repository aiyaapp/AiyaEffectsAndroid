package com.aiyaapp.aavt.media;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.aiyaapp.aavt.log.AvLog;
import com.aiyaapp.aavt.media.hard.HardMediaData;
import com.aiyaapp.aavt.media.hard.IHardStore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

/*
 * Created by Wuwang on 2017/10/26
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class SoundRecorder {

    private AudioRecord mRecord;
    private int mRecordBufferSize = 0;
    private int mRecordSampleRate = 48000;   //音频采样率
    private int mRecordChannelConfig = AudioFormat.CHANNEL_IN_STEREO;   //音频录制通道,默认为立体声
    private int mRecordAudioFormat = AudioFormat.ENCODING_PCM_16BIT; //音频录制格式，默认为PCM16Bit
    private MediaCodec mAudioEncoder;
    private MediaConfig mConfig = new MediaConfig();
    private boolean isStarted = false;
    private IHardStore mStore;
    private static final int TIME_OUT = 1000;
    private int mAudioTrack = -1;
    private long startTime = 0;
    private boolean stopFlag = false;
    private Executors mExec;

    public SoundRecorder(IHardStore store) {
        this.mStore = store;
    }

    public void configure() {
    }

    public void start() {
        if (!isStarted) {
            stopFlag = false;

            mRecordBufferSize = AudioRecord.getMinBufferSize(mRecordSampleRate,
                    mRecordChannelConfig, mRecordAudioFormat) * 2;
            mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mRecordSampleRate, mRecordChannelConfig,
                    mRecordAudioFormat, mRecordBufferSize);
            mRecord.startRecording();
            try {
                MediaFormat format = convertAudioConfigToFormat(mConfig.mAudio);
                mAudioEncoder = MediaCodec.createEncoderByType(format.getString(MediaFormat.KEY_MIME));
                mAudioEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                mAudioEncoder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!stopFlag && !audioEncodeStep(false)) {};
                    audioEncodeStep(true);
                    Log.d("aiyaapp", "audio stop");
                    if (isStarted) {
                        mRecord.stop();
                        mRecord.release();
                        mRecord = null;
                    }
                    if (mAudioEncoder != null) {
                        mAudioEncoder.stop();
                        mAudioEncoder.release();
                        mAudioEncoder = null;
                    }
                    isStarted = false;
                }
            });
            thread.start();
            startTime = SystemClock.elapsedRealtimeNanos();
            isStarted = true;
        }
    }

    private synchronized boolean audioEncodeStep(boolean isEnd) {
        if (isStarted) {
            AvLog.d("audioEncodeStep");
            int inputIndex = mAudioEncoder.dequeueInputBuffer(TIME_OUT);
            if (inputIndex >= 0) {
                ByteBuffer buffer = CodecUtil.getInputBuffer(mAudioEncoder, inputIndex);
                buffer.clear();
                long time = (SystemClock.elapsedRealtimeNanos() - startTime) / 1000;
                int length = mRecord.read(buffer, mRecordBufferSize);
                if (length >= 0) {
                    mAudioEncoder.queueInputBuffer(inputIndex, 0, length, time,
                            isEnd ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
                }
            }
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            while (true) {
                int outputIndex = mAudioEncoder.dequeueOutputBuffer(info, TIME_OUT);
                if (outputIndex >= 0) {
                    if (mStore != null) {
                        mStore.addData(mAudioTrack, new HardMediaData(CodecUtil.getOutputBuffer(mAudioEncoder, outputIndex), info));
                    }
                    mAudioEncoder.releaseOutputBuffer(outputIndex, false);
                    if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                        AvLog.d("CameraRecorder get audio encode end of stream");
                        stop();
                        return true;
                    }
                } else if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break;
                } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    AvLog.d("get audio output format changed ->" + mAudioEncoder.getOutputFormat().toString());
                    mAudioTrack = mStore.addTrack(mAudioEncoder.getOutputFormat());
                }
            }
        }
        return false;
    }


    public void stop() {
        stopFlag = true;
    }

    public void setConfig(MediaConfig config) {this.mConfig = config;}


    protected MediaFormat convertAudioConfigToFormat(MediaConfig.Audio config) {
        MediaFormat format = MediaFormat.createAudioFormat(config.mime, config.sampleRate, config.channelCount);
        format.setInteger(MediaFormat.KEY_BIT_RATE, config.bitrate);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        return format;
    }


}
