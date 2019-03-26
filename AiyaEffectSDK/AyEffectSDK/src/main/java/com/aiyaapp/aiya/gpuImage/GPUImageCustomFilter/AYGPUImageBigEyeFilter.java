package com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter;

import com.aiyaapp.aiya.AyBeauty;
import com.aiyaapp.aiya.AyBigEye;
import com.aiyaapp.aiya.AyFaceTrack;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFilter;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFramebuffer;

import java.nio.Buffer;

import static com.aiyaapp.aiya.gpuImage.AYGPUImageEGLContext.syncRunOnRenderThread;

public class AYGPUImageBigEyeFilter extends AYGPUImageFilter {

    private AyBigEye bigEye;

    private long faceData;

    public AYGPUImageBigEyeFilter() {
        super();

        syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                bigEye = new AyBigEye();
                bigEye.initGLResource();
            }
        });
    }

    @Override
    protected void renderToTexture(Buffer vertices, Buffer textureCoordinates) {
        syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
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

                //------------->绘制图像<--------------//
                bigEye.setFaceData(faceData);
                bigEye.processWithTexture(firstInputFramebuffer.texture[0], outputWidth(), outputHeight());
                //------------->绘制图像<--------------//
            }
        });
    }

    /**
     大眼强度 [0.0f, 1.0f]
     */
    public void setIntensity(float intensity) {
        bigEye.setIntensity(intensity);
    }

    public void setFaceData(long faceData) {
        this.faceData = faceData;
    }

    @Override
    public void destroy() {
        super.destroy();

        syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                bigEye.releaseGLResource();
            }
        });
    }
}
