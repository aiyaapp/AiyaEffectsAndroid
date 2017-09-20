/*
 *
 * MultiThread.java
 * 
 * Created by Wuwang on 2016/11/16
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk;

import android.annotation.SuppressLint;
import com.aiyaapp.camera.sdk.etest.EData;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.telephony.TelephonyManager;

import com.aiyaapp.camera.sdk.base.Assets;
import com.aiyaapp.camera.sdk.base.ISdkManager;
import com.aiyaapp.camera.sdk.base.Log;
import com.aiyaapp.camera.sdk.base.Parameter;
import com.aiyaapp.camera.sdk.base.ProcessCallback;
import com.aiyaapp.camera.sdk.base.Rotation;
import com.aiyaapp.camera.sdk.base.Event;
import com.aiyaapp.camera.sdk.base.ActionObservable;
import com.aiyaapp.camera.sdk.base.ActionObserver;
import com.aiyaapp.camera.sdk.base.TrackCallback;
import com.aiyaapp.sticker.sdk.BuildConfig;

/**
 *  Sdk的核心接口，将工作线程、GL线程、人脸追踪线程分开，
 *  提高SDK对视频流的处理速度。
 */
public class AiyaEffects implements ISdkManager {

//        #define TRACK_STAT_OFF 0
//        #define TRACK_STAT_OK 1
//        #define TRACK_STAT_RECOVERING 2
//        #define TRACK_STAT_INIT 3

    private static AiyaEffects instance;

    private ActionObservable mObservable;
    private AiyaCameraJni mAiyaCameraJni;

    private HandlerThread mWorkThread;
    private Handler mWorkHandler;

    private Parameter input,output;
    private String nextEffect,currentEffect;

    private ProcessCallback mProcessCallback;
    private TrackCallback mTrackCallback;

    private ExecutorService mTrackExecutor;

    private int mInWidth=720;
    private int mInHeight=1280;
    private int mOutWidth=720;
    private int mOutHeight=1280;

    private int mTrackWidth=180;
    private int mTrackHeight=320;
    private boolean isSetParam=false;

    private int mMode=0;

    private int oxEye=0;
    private int thinFace=0;
    private int beautyLevel=0;
    private int beautyType=0;

    private boolean isResourceReady=false;
    private Semaphore mSemaphore;
    private boolean isBeautyNeedTrack=false;

    private Object assetManager;

    private String DEVICE_ID;

    private int forceCloseTrack=FALSE;

    private Event mProcessEvent=new Event(Event.PROCESS_END,Event.PROCESS_PLAY,"",null);
    private Event mInfoEvent=new Event(Event.PROCESS_ERROR,0,"",null);

    private AiyaEffects(){
        mObservable=new ActionObservable();
    }

    public static AiyaEffects getInstance(){
        if(instance==null){
            synchronized (AiyaEffects.class){
                if(instance==null){
                    instance=new AiyaEffects();
                }
            }
        }
        return instance;
    }

    @Override
    public void registerObserver(ActionObserver observer){
        mObservable.registerObserver(observer);
    }

    @Override
    public void unRegisterObserver(ActionObserver observer){
        mObservable.unRegisterObserver(observer);
    }

    private void cInit(){
        mSemaphore=new Semaphore(1,true);
        mAiyaCameraJni=new AiyaCameraJni();
        mWorkThread=new HandlerThread("Sdk Work Thread");
        mWorkThread.start();
        mWorkHandler=new Handler(mWorkThread.getLooper());
        mTrackExecutor= Executors.newFixedThreadPool(1);
    }

    private boolean prepareResource(Context context,String licensePath) {
        Assets assets = new Assets(context, licensePath);
        SharedPreferences sp = context.getSharedPreferences("AiyaSDKVersion", Context.MODE_PRIVATE);
        Log.d("last sdk version:"+sp.getString("v","none"));
        Log.d("now sdk version:"+BuildConfig.AiyaSDKVersionName);
        if (!sp.getString("v", "").equals(BuildConfig.AiyaSDKVersionName)) {
            assets.clearCache();
            sp.edit().putString("v", BuildConfig.AiyaSDKVersionName).apply();
        }
        return new File(licensePath).exists() || assets.doCopy();

    }

    @SuppressLint("HardwareIds")
    @Deprecated
    public void init(final Context context,final String configPath,final String appKey) {
        Log.e("sdk init");
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context
            .TELEPHONY_SERVICE);
        DEVICE_ID = tm.getDeviceId();
        if(DEVICE_ID==null)DEVICE_ID=android.os.Build.SERIAL;
        System.setProperty("ay.effects.debug","1");
        assetManager=context.getAssets();
        cInit();
        mWorkHandler.post(new Runnable() {
            @SuppressLint("HardwareIds")
            @Override
            public void run() {
                Log.e("start prepare resource");
                boolean pb=prepareResource(context,configPath);
                Log.e("prepare resource success:"+pb);
                if(pb){
                    mObservable.notifyState(new Event(Event.RESOURCE_READY,Event.RESOURCE_READY,"资源准备完成",null));
                    isResourceReady=true;
                    Log.e("sticker jni init");
                    int state=mAiyaCameraJni.init(context,configPath,
                        configPath,context.getPackageName(),DEVICE_ID,appKey);
                    Log.e("state="+state);
                    if(state==0){
                        mObservable.notifyState(new Event(Event.INIT_SUCCESS,Event.INIT_SUCCESS,"初始化成功",null));
                    }else{
                        mObservable.notifyState(new Event(Event.INIT_FAILED,state,"初始化失败",null));
                    }
                }else{
                    mObservable.notifyState(new Event(Event.RESOURCE_FAILED,Event.INIT_FAILED,"资源准备失败",null));
                    isResourceReady=false;
                }
            }
        });
    }

    public int beauty(int type, int texId, int width, int height, int level) {
        if (type>=ISdkManager.BEAUTY_WHITEN) {
            return mAiyaCameraJni.Whiten(texId, width, height, level, type);
        } else if (type>=ISdkManager.BEAUTY_SATURATE) {
            return mAiyaCameraJni.Saturate(texId, width, height, level, type);
        } else if (type>=ISdkManager.BEAUTY_SMOOTH) {
            return mAiyaCameraJni.Smooth(texId, width, height, level, type);
        }
        return -1;
    }

    @SuppressLint("HardwareIds")
    @Override
    public void init(final Context context,final String appKey){
        File cacheFilePath=context.getExternalFilesDir(null);
        if(cacheFilePath==null){
            cacheFilePath=context.getFilesDir();
        }
        init(context,cacheFilePath.getAbsolutePath()+"/config",appKey);
    }

    @Deprecated
    @Override
    public void setParameters(Parameter inputConfig, Parameter outputConfig) {
        refreshParams();
        if(inputConfig!=null){
            this.input=inputConfig;
        }
        if(outputConfig!=null){
            this.output=outputConfig;
        }
        Log.e("CameraJni.setParameters");
        mAiyaCameraJni.setParameters(input.width,input.height,input.format,input
                .rotation.asInt(),input.flip?1:0,output.width,output.height,output.format,
            output.rotation.asInt(),output.flip?1:0);
        set(SET_ASSETS_MANAGER,assetManager);
        currentEffect=null;
        setEffect(nextEffect);
        isSetParam=true;
    }

    @Override
    public void setEffect(String effectPath) {
        if(effectPath==null){
            currentEffect=null;
            set(SET_EFFECT_ON,0);
        }else{
            set(SET_EFFECT_ON,1);
        }
        this.nextEffect=effectPath;
    }

    private void refreshParams(){
        if(input==null){
            input=new Parameter();
        }
        input.width=mInWidth;
        input.height=mInHeight;
        input.rotation= Rotation.NORMAL;
        input.format=Parameter.FORMAT_RGBA;
        if(output==null){
            output=new Parameter();
        }
        output.width=mOutWidth;
        output.height=mOutHeight;
        output.rotation=Rotation.NORMAL;
        output.format=Parameter.FORMAT_RGBA;
    }

    @Override
    public void set(String key, int value) {
        switch (key){
            case SET_IN_WIDTH:
                mInWidth=value;
                isSetParam=false;
                break;
            case SET_IN_HEIGHT:
                mInHeight=value;
                isSetParam=false;
                break;
            case SET_OUT_WIDTH:
                mOutWidth=value;
                isSetParam=false;
                break;
            case SET_OUT_HEIGHT:
                mOutHeight=value;
                isSetParam=false;
                break;
            case SET_TRACK_WIDTH:
                mTrackWidth=value;
                break;
            case SET_TRACK_HEIGHT:
                mTrackHeight=value;
                break;
            case SET_BEAUTY_LEVEL:
                beautyLevel=value;
                mAiyaCameraJni.set(key,value);
                break;
            case SET_BEAUTY_TYPE:
                beautyType=value;
                mAiyaCameraJni.set(key,value);
                break;
            case SET_MODE:
                this.mMode=value;
                break;
            case SET_TRACK_FORCE_CLOSE:
                this.forceCloseTrack=value;
                break;
            case SET_OXEYE:
                oxEye=value;
                isBeautyNeedTrack=oxEye>0||thinFace>0;
                if (isBeautyNeedTrack&&beautyLevel==0){
                    mAiyaCameraJni.set(SET_BEAUTY_LEVEL,1);
                }
                mAiyaCameraJni.set(key, value);
                break;
            case SET_THIN_FACE:
                thinFace=value;
                isBeautyNeedTrack=oxEye>0||thinFace>0;
                if (isBeautyNeedTrack&&beautyLevel==0){
                    mAiyaCameraJni.set(SET_BEAUTY_LEVEL,1);
                }
                mAiyaCameraJni.set(key, value);
                break;
            case SET_ACTION:
                switch (value){
                    case ACTION_REFRESH_PARAMS_NOW:
                        setParameters(input,output);
                        break;
                }
                break;
            default:
                mAiyaCameraJni.set(key, value);
                break;
        }
    }

    @Override
    public void set(String key, Object obj) {
        if(key.equals(SET_ASSETS_MANAGER)){
            mAiyaCameraJni.set(key,obj);
        }
    }

    public boolean isNeedTrack(){
        return (currentEffect!=null||isBeautyNeedTrack)&&forceCloseTrack==FALSE;
    }

    @Override
    public void track(final byte[] trackData, final float[] info, final int trackIndex) {
        if(isResourceReady){
            mTrackExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if(!isNeedTrack()){
                        EData.data.setTrackCode(2);
                        mSemaphore.release();
                        return;
                    }
                    long start=System.currentTimeMillis();
                    int trackCode=mAiyaCameraJni.track(trackData,mTrackWidth,mTrackHeight,info,
                        trackIndex);
                    Log.d("track------------------------>"+(System.currentTimeMillis()-start));

                    if(mTrackCallback!=null){
                        mTrackCallback.onTrack(trackCode,info);
                    }
                    mSemaphore.release();
                }
            });
            try {
                mSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void process(int textureId, int trackIndex) {
        if(isResourceReady){
            if(!isSetParam){
                setParameters(input,output);
            }
            if(nextEffect!=null&&!nextEffect.equals(currentEffect)){
                mAiyaCameraJni.setEffect(nextEffect);
                currentEffect=nextEffect;
            }
            int ret= mAiyaCameraJni.processFrame(textureId,input.width,input.height,trackIndex);
            if(mProcessCallback!=null){
                mProcessCallback.onFinished();
            }
            if(ret==STATE_EFFECT_END){
                mProcessEvent.strTag=currentEffect;
                mObservable.notifyState(mProcessEvent);
            }else if(ret<0){
                mInfoEvent.intTag=ret;
                mInfoEvent.strTag="process error";
                mObservable.notifyState(mInfoEvent);
            }
            if(mMode==MODE_GIFT&&ret==STATE_EFFECT_END){
                setEffect(null);
            }
        }
    }


    @Override
    public void setProcessCallback(ProcessCallback callback) {
        this.mProcessCallback=callback;
    }

    @Override
    public void setTrackCallback(TrackCallback callback) {
        this.mTrackCallback=callback;
    }

    @Override
    public void stopEffect() {
        setEffect(null);
    }

    @Override
    public int get(String key) {
        switch (key){
            case SET_IN_WIDTH:
                return mInWidth;
            case SET_IN_HEIGHT:
                return mInHeight;
            case SET_OUT_WIDTH:
                return mOutWidth;
            case SET_OUT_HEIGHT:
                return mOutHeight;
            case SET_TRACK_WIDTH:
                return mTrackWidth;
            case SET_TRACK_HEIGHT:
                return mTrackHeight;
            case SET_MODE:
                return mMode;
            default:
               return -1;
        }
    }

    @Override
    public void release() {
        mObservable.unRegisterAll();
        if(mAiyaCameraJni!=null){
            mAiyaCameraJni.release();
        }
    }

}
