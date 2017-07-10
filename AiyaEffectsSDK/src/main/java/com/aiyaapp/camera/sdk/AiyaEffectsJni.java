package com.aiyaapp.camera.sdk;

import android.content.Context;
import android.telephony.TelephonyManager;
import com.aiyaapp.camera.sdk.base.Assets;
import com.aiyaapp.camera.sdk.base.Log;
import java.io.File;

/**
 * Created by aiya on 2017/7/9.
 */

public class AiyaEffectsJni {

    private static AiyaEffectsJni instance;

    private AiyaCameraJni mAiyaJni;

    private AiyaEffectsJni(){
        mAiyaJni=new AiyaCameraJni();
    }

    public static AiyaEffectsJni getInstance(){
        if(instance==null){
            synchronized(AiyaEffectsJni.class){
                if(instance==null){
                    instance=new AiyaEffectsJni();
                }
            }
        }
        return instance;
    }

    public boolean init(Context context,String appKey) {
        File cacheFilePath=context.getExternalFilesDir(null);
        if (cacheFilePath == null) {
            cacheFilePath=context.getFilesDir();
        }
        TelephonyManager tm=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String DEVICE_ID=tm.getDeviceId();
        if (DEVICE_ID == null) {
            DEVICE_ID=android.os.Build.SERIAL;
        }
        Log.e("start prepare resource");
        String configPath=cacheFilePath.getAbsolutePath() + "/config";
        boolean pb;
        if (new File(configPath).exists()) {
            pb=true;
        } else {
            Assets assets=new Assets(context);
            pb=assets.doCopy();
        }
        Log.e("prepare resource success:" + pb);
        return pb
            && mAiyaJni.init(context, configPath, configPath, context.getPackageName(), DEVICE_ID, appKey)
            == 0;
    }

    public void track(byte[] rgbabuffer, int width, int height, float[] outfdp,int trackIndex){
        mAiyaJni.track(rgbabuffer, width, height, outfdp, trackIndex);
    }

    public void setParameters(int width, int height, int format, int orientation, int flip,int outWidth, int outHeight, int outFormat, int outOrientation, int outFlip) {
        mAiyaJni.setParameters(width, height, format, orientation, flip, outWidth, outHeight, outFormat, outOrientation, outFlip);
    }

    public void set(String key,int value){
        mAiyaJni.set(key, value);
    }

    public void set(String key,Object obj){
        mAiyaJni.set(key, obj);
    }

    public void setEffect(String effectJson){
        mAiyaJni.setEffect(effectJson);
    }

    public int processFrame(int textureId,int width,int height,int trackIndex){
        return mAiyaJni.processFrame(textureId, width, height, trackIndex);
    }

    public void release() {
        mAiyaJni.release();
    }

}
