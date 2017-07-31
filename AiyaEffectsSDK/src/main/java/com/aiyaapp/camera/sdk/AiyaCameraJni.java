package com.aiyaapp.camera.sdk;

import android.content.Context;
import android.util.Log;

/**
 * 核心功能的JNI接口
 */
final class AiyaCameraJni {
    private static final String TAG = "AiyaCameraJni";

    public int init(Context context,String configPath,String licensePath,String appId,String
        hwId,String appKey) {
        if(context == null) {
            throw new IllegalArgumentException("context is null");
        }
        return nSdkInit(context,configPath,licensePath,appId,hwId,appKey);
    }

    public void setParameters(int width, int height, int format, int orientation, int flip,int outWidth, int outHeight, int outFormat, int outOrientation, int outFlip) {
        Log.d(TAG,"setParameters width : " + width + "  height : " + height + " orientation : " + orientation + " flip : " + flip);
        nSetParameters(width, height,format, orientation, flip,outWidth, outHeight,  outFormat, outOrientation, outFlip);
    }

    public void set(String key,int value){
        nConfig(key,value);
    }

    public void set(String key,Object obj){
        nControl(key,obj);
    }

    public void setEffect(String effectJson){
        nSetEffect(effectJson);
    }

    public int processFrame(int textureId,int width,int height,int trackIndex){
        return nProcessFrame(textureId,width,height,trackIndex);
    }

    //outfdp 长度为19
    public int track(byte[] rgbabuffer, int width, int height, float[] outfdp,int trackIndex){
        return nTrack(rgbabuffer,width,height,outfdp,trackIndex);
    }

    public void release() {
        nRelease();
    }
    //磨皮接口，当前type只支持0x0010
    public native int Smooth(int texId, int width, int height, int level, int type);

    //红润接口，当前type只支持0x0020
    public native int Saturate(int texId, int width, int height, int level, int type);

    //美白接口，当前type只支持0x0030
    public native int Whiten(int texId, int width, int height, int level, int type);

    private native void nInitLicense(Object context,String licensePath);
    private native void nSetParameters(int width, int height, int format, int orientation, int flip,int outWidth, int outHeight, int outFormat, int outOrientation, int outFlip);
    private native void nSetEffect(String effectJson);
    private native int  nProcessFrame(int textureId,int width,int height,int trackIndex);
    private native int nSdkInit(Object context, String configPath, String licensePath,String
        appId,String hwId,String appKey);
    private native void nConfig(String key,int value);
    private native void nRelease();
    private native int nTrack(byte[] rgbabuffer, int width, int height, float[] outfdp,int
        trackIndex);
    private native void nControl(String key,Object obj);

    static {
        System.loadLibrary("simd");
        System.loadLibrary("aftk");
        System.loadLibrary("assimp");
        System.loadLibrary("ayeffects");
        System.loadLibrary("AiyaJniWrapper");
    }
}
