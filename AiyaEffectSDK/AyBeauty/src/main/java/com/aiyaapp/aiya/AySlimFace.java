package com.aiyaapp.aiya;

public class AySlimFace {

    static {
        System.loadLibrary("AyBeautyJni");
    }

    /**
     瘦脸强度 [0.0f, 1.0f]
     */
    private float intensity;

    /**
     人脸数据
     */
    private long faceData;

    private long render;
    private boolean updateIntensity;
    private boolean updateFaceData;

    /**
     初始化opengl相关的资源
     */
    public void initGLResource() {
        render = AYSDK_AiyaSlimFaceEffect_Create();
        AYSDK_AiyaSlimFaceEffect_InitGLResource(render);
    }

    /**
     释放opengl相关的资源
     */
    public void releaseGLResource() {
        if (render != 0) {
            AYSDK_AiyaSlimFaceEffect_DeinitGLResource(render);
            AYSDK_AiyaSlimFaceEffect_Destroy(render);
        }
    }

    /**
     * 绘制特效
     * @param texture 纹理数据
     * @param width 宽度
     * @param height 高度
     */
    public void processWithTexture(int texture, int width, int height) {
        if (updateIntensity) {
            AYSDK_AiyaSlimFaceEffect_Set(render, "Degree", intensity);
            updateIntensity = false;
        }

        if (updateFaceData) {
            AYSDK_AiyaSlimFaceEffect_SetFaceData(render, faceData);
            updateFaceData = false;
        } else {
            AYSDK_AiyaSlimFaceEffect_SetFaceData(render, 0);
        }

        AYSDK_AiyaSlimFaceEffect_Draw(render, texture, 0, 0, width, height);
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;

        updateIntensity = true;
    }

    public void setFaceData(long faceData) {
        this.faceData = faceData;

        updateFaceData = true;
    }

    native long AYSDK_AiyaSlimFaceEffect_Create();

    native void AYSDK_AiyaSlimFaceEffect_Destroy(long render);

    native void AYSDK_AiyaSlimFaceEffect_InitGLResource(long render);

    native void AYSDK_AiyaSlimFaceEffect_DeinitGLResource(long render);

    native void AYSDK_AiyaSlimFaceEffect_Set(long render, String name, float value);

    native void AYSDK_AiyaSlimFaceEffect_SetFaceData(long render, long value);

    native void AYSDK_AiyaSlimFaceEffect_Draw(long render, int texture, int x, int y, int width, int height);
}
