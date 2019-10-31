package com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.aiyaapp.aiya.AyFaceTrack;
import com.aiyaapp.aiya.gpuImage.AYGLProgram;
import com.aiyaapp.aiya.gpuImage.AYGPUImageConstants;
import com.aiyaapp.aiya.gpuImage.AYGPUImageEGLContext;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFramebuffer;
import com.aiyaapp.aiya.gpuImage.AYGPUImageInput;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import static android.opengl.GLES20.*;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageNoRotation;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageFilter.kAYGPUImagePassthroughFragmentShaderString;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageFilter.kAYGPUImageVertexShaderString;

public class AYGPUImageTrackFilter implements AYGPUImageInput {

    private AYGPUImageEGLContext eglContext;

    private Buffer imageVertices = AYGPUImageConstants.floatArrayToBuffer(AYGPUImageConstants.imageVertices);

    protected AYGPUImageFramebuffer firstInputFramebuffer;
    protected AYGPUImageFramebuffer outputFramebuffer;

    protected AYGLProgram filterProgram;

    protected int filterPositionAttribute, filterTextureCoordinateAttribute;
    protected int filterInputTextureUniform;

    protected int outputWidth;
    protected int outputHeight;

    private AYGPUImageConstants.AYGPUImageRotationMode rotateMode = kAYGPUImageNoRotation;

    private Context context;
    private ByteBuffer bgraBuffer;

    public boolean trackResult;

    private boolean useDelay = false;
    private Semaphore semaphore = new Semaphore(1);

    public AYGPUImageTrackFilter(AYGPUImageEGLContext eglContext, Context context) {
        this.eglContext = eglContext;
        this.context = context;

        eglContext.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                filterProgram = new AYGLProgram(kAYGPUImageVertexShaderString, kAYGPUImagePassthroughFragmentShaderString);
                filterProgram.link();

                filterPositionAttribute = filterProgram.attributeIndex("position");
                filterTextureCoordinateAttribute = filterProgram.attributeIndex("inputTextureCoordinate");
                filterInputTextureUniform = filterProgram.uniformIndex("inputImageTexture");
                filterProgram.use();
            }
        });

        AyFaceTrack.Init(context);
    }

    public long faceData() {
        if (useDelay) {
            return AyFaceTrack.CacheFaceData();
        } else {
            return AyFaceTrack.FaceData();
        }
    }

    protected void renderToTexture(final Buffer vertices, final Buffer textureCoordinates) {

        eglContext.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                filterProgram.use();

                if (outputFramebuffer == null) {
                    outputFramebuffer = new AYGPUImageFramebuffer(outputWidth, outputHeight);
                }

                outputFramebuffer.activateFramebuffer();

                glClearColor(0, 0, 0, 0);
                glClear(GL_COLOR_BUFFER_BIT);

                glActiveTexture(GL_TEXTURE2);
                glBindTexture(GL_TEXTURE_2D, firstInputFramebuffer.texture[0]);

                glUniform1i(filterInputTextureUniform, 2);

                glEnableVertexAttribArray(filterPositionAttribute);
                glEnableVertexAttribArray(filterTextureCoordinateAttribute);

                glVertexAttribPointer(filterPositionAttribute, 2, GL_FLOAT, false, 0, vertices);
                glVertexAttribPointer(filterTextureCoordinateAttribute, 2, GL_FLOAT, false, 0, textureCoordinates);

                glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

                // 进行人脸识别
                bgraBuffer.rewind();
                glReadPixels(0, 0, outputWidth, outputHeight, GL_RGBA, GL_UNSIGNED_BYTE, bgraBuffer);

                if (useDelay) {
                    try {
                        semaphore.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    AyFaceTrack.UpdateCacheFaceData(); // 取出缓存数据

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // 正常处理
                            int result = AyFaceTrack.TrackWithBGRABuffer(bgraBuffer, outputWidth, outputHeight);
                            trackResult = result == 0;

                            semaphore.release();
                        }
                    }).start();
                } else {
                    int result = AyFaceTrack.TrackWithBGRABuffer(bgraBuffer, outputWidth, outputHeight);
                    trackResult = result == 0;
                }

                glDisableVertexAttribArray(filterPositionAttribute);
                glDisableVertexAttribArray(filterTextureCoordinateAttribute);
            }
        });
    }

    public void setRotateMode(AYGPUImageConstants.AYGPUImageRotationMode rotateMode) {
        this.rotateMode = rotateMode;
    }

    public void destroy() {
        eglContext.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                filterProgram.destroy();
                if (outputFramebuffer != null) {
                    outputFramebuffer.destroy();
                }
            }
        });

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        AyFaceTrack.Deinit();

        semaphore.release();
    }

    public void setOutputWidthAndHeight(int width, int height) {
        this.outputWidth = width;
        this.outputHeight = height;

        this.bgraBuffer = ByteBuffer.allocateDirect(width * height * 4);
    }

    @Override
    public void setInputSize(int width, int height) {
        if (outputWidth != width || outputHeight != height) {
            setOutputWidthAndHeight(160, (int) (height * 160.f / width));
        }
    }

    @Override
    public void setInputFramebuffer(AYGPUImageFramebuffer newInputFramebuffer) {
        firstInputFramebuffer = newInputFramebuffer;
    }

    @Override
    public void newFrameReady() {
        renderToTexture(imageVertices,  AYGPUImageConstants.floatArrayToBuffer(AYGPUImageConstants.textureCoordinatesForRotation(rotateMode)));
    }

    public void setUseDelay(boolean useDelay) {
        this.useDelay = useDelay;
    }
}
