/*
 *
 * AiyaEffect.java
 * 
 * Created by Wuwang on 2017/2/16
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk;

import java.io.File;

import android.content.Context;
import android.content.res.AssetManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.aiyaapp.camera.sdk.base.ISdkManager;

/**
 * Description:
 */
public class AiyaEffect {

    private static AiyaEffect instance;
    private AiyaCameraJni mAiyaJni;
    private boolean isSetParams=false;
    private String mEffect;
    private String mCurrentEffect;
    private AssetManager mAssetManager;

    private AiyaEffect(){
        mAiyaJni=new AiyaCameraJni();
    }

    public static AiyaEffect getInstance(){
        if(instance==null){
            synchronized (AiyaEffect.class){
                if(instance==null){
                    instance=new AiyaEffect();
                }
            }
        }
        return instance;
    }

    public void set(String key,Object value){
        mAiyaJni.set(key, value);
    }

    public void set(String key,int value){
        mAiyaJni.set(key, value);
    }

    public int init(Context context,String licensePath,String appKey){
        final String path=licensePath.substring(0,licensePath.lastIndexOf
            (File.separator)+1);
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context
            .TELEPHONY_SERVICE);
        String DEVICE_ID = tm.getDeviceId();
        if(DEVICE_ID==null)DEVICE_ID=android.os.Build.SERIAL;
        this.mAssetManager=context.getAssets();
        return mAiyaJni.init(context,path,licensePath,context.getPackageName(),DEVICE_ID,appKey);
    }

    public void process(int textureId,int width,int height){
        if(!isSetParams){
            isSetParams=true;
            mAiyaJni.setParameters(width,height,0,0,0,width,height,0,0,1);
            mAiyaJni.set(ISdkManager.SET_ASSETS_MANAGER,mAssetManager);
        }
        if(mEffect!=null){
            mCurrentEffect=mEffect;
            mEffect=null;
            mAiyaJni.setEffect(mCurrentEffect);
        }
        int i=mAiyaJni.processFrame(textureId,width,height,0);
        Log.e("a_eff","process->"+i);
    }

    public void setEffect(String effect){
        if(effect==null){
            mAiyaJni.set(ISdkManager.SET_EFFECT_ON,0);
        }else{
            mAiyaJni.set(ISdkManager.SET_EFFECT_ON,1);
        }
        mEffect=effect;
    }

    public void release(){
        mAiyaJni.release();
    }


}
