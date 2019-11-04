package com.aiyaapp.aiya;

public class AyEffect {

    static {
        System.loadLibrary("assimp");
        System.loadLibrary("gameplay");
        System.loadLibrary("ayeffects");
        System.loadLibrary("AyEffectJni");
    }

    /**
     * 特效播放中
     */
    public static final int MSG_STAT_EFFECTS_PLAY = 0x00020000;

    /**
     * 特效播放结束
     */
    public static final int MSG_STAT_EFFECTS_END = 0x00040000;

    /**
     * 特效播放开始
     */
    public static final int MSG_STAT_EFFECTS_START = 0x00080000;

    private long render;
    private String effectPath;
    private long faceData;
    private boolean enalbeVFilp;
    private boolean updateEffectPath;
    private boolean updateFaceData;
    private boolean updateVFlip;

    public void initGLResource() {
        render = Create();
    }

    public void releaseGLResource() {
        Destroy(render);
    }

    public void setCallback(OnEffectCallback callback) {
        Callback(render, callback);
    }

    public void processWithTexture(int texture, int width, int height) {
        if (updateEffectPath) {
            SetStickerPath(render, effectPath);
            updateEffectPath = false;
        }

        if (updateFaceData) {
            SetFaceData(render, faceData);
            updateFaceData =false;
        } else {
            SetFaceData(render, 0);
        }

        if (updateVFlip) {
            SetEnableVFlip(render, enalbeVFilp);
        }

        Draw(render, texture, width, height);
    }

    public void pauseProcess() {
        SetPause(render);
    }

    public void resumeProcess() {
        SetResume(render);
    }

    public void setEffectPath(String effectPath) {
        this.effectPath = effectPath;

        updateEffectPath = true;
    }

    public void setFaceData(long faceData) {
        this.faceData = faceData;

        updateFaceData = true;
    }

    public void setEnalbeVFilp(boolean enalbeVFilp) {
        this.enalbeVFilp = enalbeVFilp;

        updateVFlip = true;
    }

    native long Create();
    native void Destroy(long ayEffect);
    native void Callback(long ayEffect, OnEffectCallback callback);
    native void SetFaceData(long ayEffect, long value);
    native void SetStickerPath(long ayEffect, String ptah);
    native void SetEnableVFlip(long ayEffect,  boolean enable);
    native void SetPause(long ayEffect);
    native void SetResume(long ayEffect);
    native void Draw(long ayEffect, int texture, int width, int height);

    public interface OnEffectCallback {
        void aiyaEffectMessage(int type, int ret);
    }
}
