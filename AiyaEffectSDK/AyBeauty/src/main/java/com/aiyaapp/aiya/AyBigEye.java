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
        render = Create();
        InitGLResource(render);
    }

    /**
     释放opengl相关的资源
     */
    public void releaseGLResource() {
        if (render != 0) {
            DeinitGLResource(render);
            Destroy(render);
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
            Set(render, "Degree", intensity);
            updateIntensity = false;
        }

        if (updateFaceData) {
            SetFaceData(render, faceData);
            updateFaceData = false;
        } else {
            SetFaceData(render, 0);
        }

        Draw(render, texture, 0, 0, width, height);
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

    native long Create();

    native void Destroy(long render);

    native void InitGLResource(long render);

    native void DeinitGLResource(long render);

    native void Set(long render, String name, float value);

    native void SetFaceData(long render, long value);

    native void Draw(long render, int texture, int x, int y, int width, int height);
}
