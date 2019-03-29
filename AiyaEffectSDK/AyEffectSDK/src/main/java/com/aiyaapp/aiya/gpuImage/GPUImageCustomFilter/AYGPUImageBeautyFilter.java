package com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter;

import com.aiyaapp.aiya.AyBeauty;
import com.aiyaapp.aiya.gpuImage.AYGPUImageEGLContext;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFilter;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFramebuffer;

import java.nio.Buffer;

public class AYGPUImageBeautyFilter extends AYGPUImageFilter {

    /**
     美颜算法类型
     */
    private AyBeauty.AY_BEAUTY_TYPE type;

    /**
     美颜强度 [0.0f, 1.0f], 只适用于 0x1002
     */
    private float intensity;

    /**
     磨皮 [0.0f, 1.0f], 只适用于 0x1000, 0x1003, 0x1004, 0x1005, 0x1006
     */
    private float smooth;

    /**
     饱和度 [0.0f, 1.0f], 只适用于 0x1000, 0x1003, 0x1004, 0x1005
     */
    private float saturation;

    /**
     美白 [0.0f, 1.0f], 只适用于 0x1003, 0x1004, 0x1005, 0x1006
     */
    private float whiten;

    private AyBeauty beauty;

    public AYGPUImageBeautyFilter(AYGPUImageEGLContext context, AyBeauty.AY_BEAUTY_TYPE type) {
        super(context);
        this.type = type;

        context.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                beauty = new AyBeauty(AYGPUImageBeautyFilter.this.type);
                beauty.initGLResource();
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
                beauty.processWithTexture(firstInputFramebuffer.texture[0], outputWidth(), outputHeight());
                //------------->绘制图像<--------------//
            }
        });
    }

    public void setType(final AyBeauty.AY_BEAUTY_TYPE type) {
        this.type = type;

        context.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                if (beauty != null) {
                    beauty.releaseGLResource();
                }

                beauty = new AyBeauty(type);
                beauty.initGLResource();
            }
        });
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;

        if (beauty.getType() != AyBeauty.AY_BEAUTY_TYPE.AY_BEAUTY_TYPE_2) { // 只有类型2能设置强度
            this.intensity = 0;
            return;
        }

        beauty.setIntensity(intensity);
    }

    public void setSmooth(float smooth) {
        this.smooth = smooth;

        if (beauty.getType() == AyBeauty.AY_BEAUTY_TYPE.AY_BEAUTY_TYPE_2) { // 类型2没有磨皮
            this.smooth = 0;
            this.intensity = smooth;
            beauty.setIntensity(this.intensity);
            return;
        }

        beauty.setSmooth(smooth);
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;

        if (beauty.getType() == AyBeauty.AY_BEAUTY_TYPE.AY_BEAUTY_TYPE_2) { // 类型2没有磨皮
            this.saturation = 0;
            this.intensity = saturation;
            beauty.setIntensity(this.intensity);
            return;
        }

        beauty.setSaturation(saturation);
    }

    public void setWhiten(float whiten) {
        this.whiten = whiten;

        if (beauty.getType() == AyBeauty.AY_BEAUTY_TYPE.AY_BEAUTY_TYPE_2) { // 类型2没有磨皮
            this.whiten = 0;
            this.intensity = whiten;
            beauty.setIntensity(this.intensity);
            return;
        }

        beauty.setWhiten(whiten);
    }

    @Override
    public void destroy() {
        super.destroy();

        context.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                beauty.releaseGLResource();
            }
        });
    }
}
