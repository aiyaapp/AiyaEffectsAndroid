package com.aiyaapp.aiya;

public class AyShortVideoEffect {

    static {
        System.loadLibrary("ShortVideo");
        System.loadLibrary("AiyaAe3dLib");
        System.loadLibrary("AiyaEffectLib");
        System.loadLibrary("AyShortVideoEffectJni");
    }

    private long render;

    private int type;

    private boolean initGL;

    private boolean initFourScreen;
    private boolean initThreeScreen;

    private boolean needReset;

    /**
     * 短视频特效
     */
    public AyShortVideoEffect(int type) {
        this.type = type;
    }

    /**
     * 初始化openGL相关的资源
     */
    public void initGLResource() {
        render = Create(this.type);

        if (13 == type) {
            setFloatValue("SubWindow", -1);
            setFloatValue("DrawGray", 1);

            initFourScreen = true;

        } else if (14 == type) {
            setFloatValue("SubWindow", -1);
            setFloatValue("DrawGray", 1);

            initThreeScreen = true;
        }
    }

    /**
     * 释放openGL相着的资源
     */
    public void releaseGLResource() {
        DeinitGLResource(render);
        Destroy(render);
    }

    /**
     * 设置参数
     */
    public void setFloatValue(String key, float value) {
        Set(render, key, value);
    }

    /**
     * 绘制特效
     */
    public void processWithTexture(int texture, int width, int height) {
        if (!initGL) {
            InitGLResource(render);

            initGL = true;
        }

        if (needReset) {
            Restart(render);

            if (13 == type) {
                setFloatValue("SubWindow", -1);
                setFloatValue("DrawGray", 1);

                initFourScreen = true;

            } else if (14 == type) {
                setFloatValue("SubWindow", -1);
                setFloatValue("DrawGray", 1);

                initThreeScreen = true;
            }

            needReset = false;
        }

        Draw(render, texture, 0, 0, width, height);

        if (initFourScreen) {
            setFloatValue("SubWindow", 0);
            setFloatValue("DrawGray", 0);

            initFourScreen = false;
        }

        if (initThreeScreen) {
            setFloatValue("SubWindow", 0);
            setFloatValue("DrawGray", 0);

            initThreeScreen = false;
        }
    }

    /**
     * 重置特效
     */
    public void reset() {
        needReset = true;
    }

    native long Create(int type);
    native void Destroy(long render);
    native void InitGLResource(long render);
    native void DeinitGLResource(long render);
    native void Restart(long render);
    native void Draw(long render, int texture, int x, int y, int width, int height);
    native void Set(long render, String key, float value);
}
