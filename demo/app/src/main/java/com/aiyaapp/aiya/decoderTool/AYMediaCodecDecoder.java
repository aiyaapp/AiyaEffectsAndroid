package com.aiyaapp.aiya.decoderTool;

import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import com.aiyaapp.aiya.gpuImage.AYGLProgram;
import com.aiyaapp.aiya.gpuImage.AYGPUImageConstants;
import com.aiyaapp.aiya.gpuImage.AYGPUImageEGLContext;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFilter;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFramebuffer;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.microedition.khronos.opengles.GL11Ext;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_TEXTURE2;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glFinish;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexParameterf;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageNoRotation;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.noRotationTextureCoordinates;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.textureCoordinatesForRotation;

/**
 *
 * MediaCodec相关代码参考了Google Codec Sample
 * https://android.googlesource.com/platform/cts/+/jb-mr2-release/tests/tests/media/src/android/media/cts/DecoderTest.java
 *
 */
public class AYMediaCodecDecoder implements SurfaceTexture.OnFrameAvailableListener {

    private static final String kAYOESTextureFragmentShader = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "\n" +
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    private static final int TIMEOUT = 1000;

    private MediaExtractor videoExtractor;
    private MediaExtractor audioExtractor;

    // 编码器
    private MediaCodec videoDecoder;
    private MediaCodec audioDecoder;

    private AYMediaCodecDecoderListener decoderListener;

    private AYGPUImageEGLContext eglContext;

    private SurfaceTexture surfaceTexture;

    private Surface surface;

    private int oesTexture;

    private AYGPUImageFramebuffer outputFramebuffer;

    private AYGPUImageConstants.AYGPUImageRotationMode rotateMode = kAYGPUImageNoRotation;

    private int inputWidth;
    private int inputHeight;

    private AYGLProgram filterProgram;

    private int filterPositionAttribute, filterTextureCoordinateAttribute;
    private int filterInputTextureUniform;

    private Buffer imageVertices = AYGPUImageConstants.floatArrayToBuffer(AYGPUImageConstants.imageVertices);
    private Buffer textureCoordinates = AYGPUImageConstants.floatArrayToBuffer(AYGPUImageConstants.verticalFlipTextureCoordinates);

    // 视频解码中断时用到的锁
    private Boolean isDecoderAbort = false;
    private ReadWriteLock decoderAbortLock = new ReentrantReadWriteLock(true);

    private final Object decoderFrameSyncObject = new Object();     // guards decoderFrameAvailable
    private boolean decoderFrameAvailable;
    
    public AYMediaCodecDecoder(String path) throws IOException {
        videoExtractor = new MediaExtractor();
        audioExtractor = new MediaExtractor();

        videoExtractor.setDataSource(path);
        audioExtractor.setDataSource(path);
    }

    public AYMediaCodecDecoder(AssetFileDescriptor fileDescriptor) throws IOException {
        videoExtractor = new MediaExtractor();
        audioExtractor = new MediaExtractor();

        videoExtractor.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
        audioExtractor.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
    }

    public boolean configCodecAndStart(AYGPUImageEGLContext eglContext) {
        this.eglContext = eglContext;

        // 找到视频格式
        MediaFormat videoFormat = null;
        MediaFormat audioFormat = null;

        int videoTrack = 0;
        int audioTrack = 0;

        for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
            MediaFormat format = videoExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith("video")) {
                videoFormat = format;
                videoTrack = i;
            } else if (mime != null && mime.startsWith("audio")) {
                audioFormat = format;
                audioTrack = i;
            }
        }

        if (videoFormat == null) {
            Log.w(AYGPUImageConstants.TAG, "no exist video track");
            return false;
        }

        if (audioFormat == null) {
            Log.w(AYGPUImageConstants.TAG, "no exist audio track");
            return false;
        }

        Log.w(AYGPUImageConstants.TAG, "视频格式 : " + videoFormat);
        Log.w(AYGPUImageConstants.TAG, "音频格式 : " + audioFormat);

        // 创建MediaCodec硬解码器
        boolean hadError = false;

        createGLEnvironment();

        // 配置视频解码器
        try {
            videoDecoder = MediaCodec.createDecoderByType(videoFormat.getString(MediaFormat.KEY_MIME));
            videoDecoder.configure(videoFormat, surface, null, 0);
        } catch (Throwable e) {
            Log.w(AYGPUImageConstants.TAG, "video mediaCodec create error: " + e);
            hadError = true;
        } finally {
            if (videoDecoder != null && hadError) {
                videoDecoder.stop();
                videoDecoder.release();
                videoDecoder = null;
            }
        }

        if (hadError || videoDecoder == null) {
            return false;
        }

        inputWidth = videoFormat.getInteger(MediaFormat.KEY_WIDTH);
        inputHeight = videoFormat.getInteger(MediaFormat.KEY_HEIGHT);

        // 配置音频解码器
        try {
            audioDecoder = MediaCodec.createDecoderByType(audioFormat.getString(MediaFormat.KEY_MIME));
            audioDecoder.configure(audioFormat, null, null, 0);
        } catch (Throwable e) {
            Log.w(AYGPUImageConstants.TAG, "audio mediaCodec create error: " + e);
            hadError = true;
        } finally {
            if (audioDecoder != null && hadError) {
                audioDecoder.stop();
                audioDecoder.release();
                audioDecoder = null;
            }
        }

        if (hadError || audioDecoder == null) {
            return false;
        }

        if (decoderListener != null) {
            decoderListener.decoderVideoFormat(videoFormat);
            decoderListener.decoderAudioFormat(audioFormat);
        }

        videoDecoder.start();
        audioDecoder.start();

        videoExtractor.selectTrack(videoTrack);
        audioExtractor.selectTrack(audioTrack);

        Runnable videoDecodeRunnable = new Runnable() {
            @Override
            public void run() {
                // 等待编码器设置各种参数
                SystemClock.sleep(500);

                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

                boolean outputDone = false;
                boolean inputDone = false;

                while (!outputDone) {

                    decoderAbortLock.readLock().lock();

                    if (isDecoderAbort) {
                        Log.i(AYGPUImageConstants.TAG, "视频解码器强制中断");
                        decoderAbortLock.readLock().unlock();
                        return;
                    }

                    if (!inputDone) {

                        // 取一帧数据喂给解码器
                        int inputBufIndex = videoDecoder.dequeueInputBuffer(TIMEOUT);

                        if (inputBufIndex >= 0) {

                            ByteBuffer inputBuf;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                inputBuf = videoDecoder.getInputBuffer(inputBufIndex);
                            } else {
                                inputBuf = videoDecoder.getInputBuffers()[inputBufIndex];
                            }

                            int sampleSize = videoExtractor.readSampleData(inputBuf, 0);
                            long presentationTimeUs = 0;
                            if (sampleSize < 0) {
                                inputDone = true;
                                sampleSize = 0;
                            } else {
                                presentationTimeUs = videoExtractor.getSampleTime();
                            }

                            videoDecoder.queueInputBuffer(
                                    inputBufIndex,
                                    0,
                                    sampleSize,
                                    presentationTimeUs,
                                    inputDone ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);

                            if (!inputDone) {
                                // 前进到下一帧
                                videoExtractor.advance();
                            }
                        }
                    }

                    int outputBufIndex = videoDecoder.dequeueOutputBuffer(info, TIMEOUT);

                    if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        Log.d(AYGPUImageConstants.TAG, "视频解码器初始化完成");

                    } else if (outputBufIndex >= 0) {

                        // 最后一个输出
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            Log.i(AYGPUImageConstants.TAG, "视频解码器输出完成");
                            outputDone = true;
                        }

                        // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                        // to SurfaceTexture to convert to a texture.  The API doesn't guarantee
                        // that the texture will be available before the call returns, so we
                        // need to wait for the onFrameAvailable callback to fire.
                        videoDecoder.releaseOutputBuffer(outputBufIndex, info.size != 0);
                        if (info.size != 0) {
                            awaitNewImage();
                        }
                    }

                    decoderAbortLock.readLock().unlock();
                }

                decoderAbortLock.readLock().lock();

                if (videoDecoder != null) {
                    videoDecoder.stop();
                    videoDecoder.release();
                    videoDecoder = null;
                }

                if (videoExtractor != null) {
                    videoExtractor.release();
                    videoExtractor = null;
                }

                destroyGLContext();

                // 等待页面渲染完成后, 再回调EOS
                if (!isDecoderAbort) {
                    SystemClock.sleep(100);

                    if (decoderListener != null) {
                        decoderListener.decoderVideoEOS();
                    }
                }

                decoderAbortLock.readLock().unlock();
            }
        };

        Runnable audioDecodeRunnable = new Runnable() {
            @Override
            public void run() {
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

                boolean outputDone = false;
                boolean inputDone = false;

                while (!outputDone) {

                    decoderAbortLock.readLock().lock();

                    if (isDecoderAbort) {
                        Log.i(AYGPUImageConstants.TAG, "音频解码器强制中断");
                        decoderAbortLock.readLock().unlock();
                        return;
                    }

                    if (!inputDone) {

                        // 取一帧数据喂给解码器
                        int inputBufIndex = audioDecoder.dequeueInputBuffer(TIMEOUT);
                        if (inputBufIndex >= 0) {

                            ByteBuffer inputBuf;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                inputBuf = audioDecoder.getInputBuffer(inputBufIndex);
                            } else {
                                inputBuf = audioDecoder.getInputBuffers()[inputBufIndex];
                            }

                            int sampleSize = audioExtractor.readSampleData(inputBuf, 0);
                            long presentationTimeUs = 0;
                            if (sampleSize < 0) {
                                inputDone = true;
                                sampleSize = 0;
                            } else {
                                presentationTimeUs = audioExtractor.getSampleTime();
                            }

                            audioDecoder.queueInputBuffer(
                                    inputBufIndex,
                                    0,
                                    sampleSize,
                                    presentationTimeUs,
                                    inputDone ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);

                            if (!inputDone) {
                                // 前进到下一帧
                                audioExtractor.advance();
                            }
                        }
                    }

                    int outputBufIndex = audioDecoder.dequeueOutputBuffer(info, TIMEOUT);

                    if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        Log.d(AYGPUImageConstants.TAG, "音频解码器初始化完成");

                    } else if (outputBufIndex >= 0) {

                        // 最后一个输出
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            Log.i(AYGPUImageConstants.TAG, "音频解码器输出完成");
                            outputDone = true;
                        }

                        if (!outputDone) {
                            ByteBuffer outputBuf;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                outputBuf = audioDecoder.getOutputBuffer(outputBufIndex);
                            } else {
                                outputBuf = audioDecoder.getOutputBuffers()[outputBufIndex];
                            }

                            if (decoderListener != null) {
                                decoderListener.decoderAudioOutput(outputBuf, info.presentationTimeUs * 1000);
                            }
                        }

                        audioDecoder.releaseOutputBuffer(outputBufIndex, false);
                    }

                    decoderAbortLock.readLock().unlock();
                }

                decoderAbortLock.readLock().lock();

                if (audioDecoder != null) {
                    audioDecoder.stop();
                    audioDecoder.release();
                    audioDecoder = null;
                }

                if (audioExtractor != null) {
                    audioExtractor.release();
                    audioExtractor = null;
                }

                if (!isDecoderAbort) {
                    if (decoderListener != null) {
                        decoderListener.decoderAudioEOS();
                    }
                }

                decoderAbortLock.readLock().unlock();
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                videoDecodeRunnable.run();
                audioDecodeRunnable.run();
            }
        }).start();

        return true;
    }

    public void setDecoderListener(AYMediaCodecDecoderListener decoderListener) {
        this.decoderListener = decoderListener;
    }

    /**
     * 中断解码器
     */
    public void abortDecoder() {

        // 等待MediaCodec读锁释放
        decoderAbortLock.writeLock().lock();
        isDecoderAbort = true;

        if (videoDecoder != null) {
            videoDecoder.stop();
            videoDecoder.release();
            videoDecoder = null;
        }

        if (videoExtractor != null) {
            videoExtractor.release();
            videoExtractor = null;
        }

        if (audioDecoder != null) {
            audioDecoder.stop();
            audioDecoder.release();
            audioDecoder = null;
        }

        if (audioExtractor != null) {
            audioExtractor.release();
            audioExtractor = null;
        }

        destroyGLContext();

        decoderAbortLock.writeLock().unlock();
    }

    private void createGLEnvironment() {
        eglContext.syncRunOnRenderThread(() -> {
            eglContext.makeCurrent();

            oesTexture = createOESTextureID();
            surfaceTexture = new SurfaceTexture(oesTexture);
            surfaceTexture.setOnFrameAvailableListener(AYMediaCodecDecoder.this);

            surface = new Surface(surfaceTexture);

            filterProgram = new AYGLProgram(AYGPUImageFilter.kAYGPUImageVertexShaderString, kAYOESTextureFragmentShader);
            filterProgram.link();

            filterPositionAttribute = filterProgram.attributeIndex("position");
            filterTextureCoordinateAttribute = filterProgram.attributeIndex("inputTextureCoordinate");
            filterInputTextureUniform = filterProgram.uniformIndex("inputImageTexture");
        });
    }

    private void awaitNewImage() {
        final int TIMEOUT_MS = 500;
        synchronized (decoderFrameSyncObject) {
            while (!decoderFrameAvailable) {
                try {
                    // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                    // stalling the test if it doesn't arrive.
                    decoderFrameSyncObject.wait(TIMEOUT_MS);
                } catch (InterruptedException ie) {
                    // shouldn't happen
                    throw new RuntimeException(ie);
                }
            }
            decoderFrameAvailable = false;
        }
    }

    @Override
    public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
        synchronized (decoderFrameSyncObject) {
            decoderFrameAvailable = true;
            decoderFrameSyncObject.notifyAll();
        }
        
        eglContext.syncRunOnRenderThread(() -> {
            eglContext.makeCurrent();

            if (EGL14.eglGetCurrentDisplay() != EGL14.EGL_NO_DISPLAY) {
                surfaceTexture.updateTexImage();

                glFinish();

                // 因为在shader中处理oes纹理需要使用到扩展类型, 必须要先转换为普通纹理再传给下一级
                // 同时解码出来的画面要进行垂直翻转才是正常状态
                renderToFramebuffer(oesTexture);

                if (decoderListener != null) {
                    decoderListener.decoderVideoOutput(outputFramebuffer.texture[0], inputWidth, inputHeight, surfaceTexture.getTimestamp());
                }
            }
        });
    }

    private void renderToFramebuffer(int oesTexture) {

        filterProgram.use();

        if (outputFramebuffer != null) {
            if (inputWidth != outputFramebuffer.width || inputHeight != outputFramebuffer.height) {
                outputFramebuffer.destroy();
                outputFramebuffer = null;
            }
        }

        if (outputFramebuffer == null) {
            outputFramebuffer = new AYGPUImageFramebuffer(inputWidth, inputHeight);
        }

        outputFramebuffer.activateFramebuffer();

        glClearColor(0, 0, 0, 0);
        glClear(GL_COLOR_BUFFER_BIT);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, oesTexture);

        glUniform1i(filterInputTextureUniform, 2);

        glEnableVertexAttribArray(filterPositionAttribute);
        glEnableVertexAttribArray(filterTextureCoordinateAttribute);

        glVertexAttribPointer(filterPositionAttribute, 2, GL_FLOAT, false, 0, imageVertices);
        glVertexAttribPointer(filterTextureCoordinateAttribute, 2, GL_FLOAT, false, 0, textureCoordinates);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        glDisableVertexAttribArray(filterPositionAttribute);
        glDisableVertexAttribArray(filterTextureCoordinateAttribute);
    }

    private int createOESTextureID() {
        int[] texture = new int[1];
        glGenTextures(1, texture, 0);

        glBindTexture(GL_TEXTURE_EXTERNAL_OES, texture[0]);
        glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        return texture[0];
    }

    private void destroyGLContext() {
        eglContext.syncRunOnRenderThread(() -> {

            Log.d(AYGPUImageConstants.TAG, "释放解码器EGL环境");

            if (filterProgram != null) {
                filterProgram.destroy();
                filterProgram = null;
            }

            if (outputFramebuffer != null) {
                outputFramebuffer.destroy();
                outputFramebuffer = null;
            }

            if (surface != null) {
                surface.release();
                surface = null;
            }
        });
    }
}
