package com.aiyaapp.aiya;

public class AyBigEye {

    static {
        System.loadLibrary("AyBeautyJni");
    }

    /**
     大眼强度 [0.0f, 1.0f]
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
        render = AYSDK_AiyaBigEyeEffect_Create();
        AYSDK_AiyaBigEyeEffect_InitGLResource(render);
    }

    /**
     释放opengl相关的资源
     */
    public void releaseGLResource() {
        if (render != 0) {
            AYSDK_AiyaBigEyeEffect_DeinitGLResource(render);
            AYSDK_AiyaBigEyeEffect_Destroy(render);
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
            AYSDK_AiyaBigEyeEffect_Set(render, "Degree", intensity);
            updateIntensity = false;
        }

        if (updateFaceData) {
            AYSDK_AiyaBigEyeEffect_SetFaceData(render, faceData);
            updateFaceData = false;
        } else {
            AYSDK_AiyaBigEyeEffect_SetFaceData(render, 0);
        }

        AYSDK_AiyaBigEyeEffect_Draw(render, texture, 0, 0, width, height);
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

    native long AYSDK_AiyaBigEyeEffect_Create();

    native void AYSDK_AiyaBigEyeEffect_Destroy(long render);

    native void AYSDK_AiyaBigEyeEffect_InitGLResource(long render);

    native void AYSDK_AiyaBigEyeEffect_DeinitGLResource(long render);

    native void AYSDK_AiyaBigEyeEffect_Set(long render, String name, float value);

    native void AYSDK_AiyaBigEyeEffect_SetFaceData(long render, long value);

    native void AYSDK_AiyaBigEyeEffect_Draw(long render, int texture, int x, int y, int width, int height);
}
