package com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter;

import com.aiyaapp.aiya.AySlimFace;
import com.aiyaapp.aiya.gpuImage.AYGPUImageEGLContext;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFilter;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFramebuffer;

import java.nio.Buffer;

public class AYGPUImageSlimFaceFilter extends AYGPUImageFilter {

    private AySlimFace slimFace;

    private long faceData;

    public AYGPUImageSlimFaceFilter(AYGPUImageEGLContext context) {
        super(context);

        context.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                slimFace = new AySlimFace();
                slimFace.initGLResource();
            }
        });
    }

    @Override
    protected void renderToTexture(Buffer vertices, Buffer textureCoordinates) {
        context.syncRunOnRenderThread(new Runnable() {
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
                slimFace.setFaceData(faceData);
                slimFace.processWithTexture(firstInputFramebuffer.texture[0], outputWidth(), outputHeight());
                //------------->绘制图像<--------------//
            }
        });
    }

    /**
     大眼强度 [0.0f, 1.0f]
     */
    public void setIntensity(float intensity) {
        slimFace.setIntensity(intensity);
    }

    public void setFaceData(long faceData) {
        this.faceData = faceData;
    }

    @Override
    public void destroy() {
        super.destroy();

        context.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                slimFace.releaseGLResource();
            }
        });
    }
}
