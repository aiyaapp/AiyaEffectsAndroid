package com.aiyaapp.aiya;

public class AyBeauty {

    static {
        System.loadLibrary("Beauty");
        System.loadLibrary("Faceprocess");
        System.loadLibrary("ShortVideo");
        System.loadLibrary("AiyaAe3dLib");
        System.loadLibrary("AiyaEffectLib");
        System.loadLibrary("AyBeautyJni");
    }

    public enum AY_BEAUTY_TYPE {
        AY_BEAUTY_TYPE_0,
        AY_BEAUTY_TYPE_2,
        AY_BEAUTY_TYPE_3,
        AY_BEAUTY_TYPE_4,
        AY_BEAUTY_TYPE_5,
        AY_BEAUTY_TYPE_6
    }

    private AY_BEAUTY_TYPE type;

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
     亮度 [0.0f, 1.0f], 只适用于 0x1003, 0x1004, 0x1005, 0x1006
     */
    private float whiten;

    private long render;
    private boolean updateIntensity;
    private boolean updateSmooth;
    private boolean updateSaturation;
    private boolean updateWhiten;

    /**
     美颜类型取值 {0x1000, 0x1002, 0x1003, 0x1004, 0x1005, 0x1006}
     */
    public AyBeauty(AY_BEAUTY_TYPE type) {
        this.type = type;
    }

    /**
     初始化opengl相关的资源
     */
    public void initGLResource() {
        render = Create(getTypeValue(type));
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

        if (updateSmooth) {
            Set(render, "SmoothDegree", smooth);
            updateSmooth = false;
        }

        if (updateSaturation) {
            Set(render, "SaturateDegree", saturation);
            updateSaturation = false;
        }

        if (updateWhiten) {
            Set(render, "WhitenDegree", whiten);
            updateWhiten = false;
        }

        Draw(render, texture, 0, 0, width, height);
    }

    public AY_BEAUTY_TYPE getType() {
        return type;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;

        updateIntensity = true;
    }

    public float getSmooth() {
        return smooth;
    }

    public void setSmooth(float smooth) {
        this.smooth = smooth;

        updateSmooth = true;
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;

        updateSaturation = true;
    }

    public float getWhiten() {
        return whiten;
    }

    public void setWhiten(float whiten) {
        this.whiten = whiten;

        updateWhiten = true;
    }

    private int getTypeValue(AY_BEAUTY_TYPE type) {
        int _type = 0;
        switch (type) {
            case AY_BEAUTY_TYPE_0:
                _type = 0x1000;
                break;
            case AY_BEAUTY_TYPE_2:
                _type = 0x1002;
                break;
            case AY_BEAUTY_TYPE_3:
                _type = 0x1003;
                break;
            case AY_BEAUTY_TYPE_4:
                _type = 0x1004;
                break;
            case AY_BEAUTY_TYPE_5:
                _type = 0x1005;
                break;
            case AY_BEAUTY_TYPE_6:
                _type = 0x1006;
                break;
        }
        return _type;
    }

    native long Create(int type);

    native void Destroy(long render);

    native void InitGLResource(long render);

    native void DeinitGLResource(long render);

    native void Set(long render, String name, float value);

    native void Draw(long render, int texture, int x, int y, int width, int height);
}
