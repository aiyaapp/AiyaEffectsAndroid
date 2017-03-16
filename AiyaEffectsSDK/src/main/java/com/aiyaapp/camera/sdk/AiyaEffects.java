/*
 *
 * MultiThread.java
 * 
 * Created by Wuwang on 2016/11/16
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.telephony.TelephonyManager;

import com.aiyaapp.camera.sdk.base.Assets;
import com.aiyaapp.camera.sdk.base.ISdkManager;
import com.aiyaapp.camera.sdk.base.Log;
import com.aiyaapp.camera.sdk.base.Parameter;
import com.aiyaapp.camera.sdk.base.ProcessCallback;
import com.aiyaapp.camera.sdk.base.Rotation;
import com.aiyaapp.camera.sdk.base.State;
import com.aiyaapp.camera.sdk.base.StateObservable;
import com.aiyaapp.camera.sdk.base.StateObserver;
import com.aiyaapp.camera.sdk.base.TrackCallback;

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

    private StateObservable mObservable;
    private AiyaCameraJni mAiyaCameraJni;

    private HandlerThread mWorkThread;
    private Handler mWorkHandler;

    private Parameter input,output;
    private String nextEffect,currentEffect;

    private ProcessCallback mProcessCallback;
    private TrackCallback mTrackCallback;

    private Semaphore mSemaphore;

    private ExecutorService mTrackExecutor;

    private int mInWidth=720;
    private int mInHeight=1280;
    private int mOutWidth=720;
    private int mOutHeight=1280;

    private int mTrackWidth=180;
    private int mTrackHeight=320;
    private boolean isSetParam=false;

    private boolean isResourceReady=false;

    private Object assetManager;

    private AiyaEffects(){
        mObservable=new StateObservable();
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
    public void registerObserver(StateObserver observer){
        mObservable.registerObserver(observer);
    }

    @Override
    public void unRegisterObserver(StateObserver observer){
        mObservable.unRegisterObserver(observer);
    }

    private void cInit(){
        mSemaphore=new Semaphore(1,true);
        mAiyaCameraJni=new AiyaCameraJni();
        mWorkThread=new HandlerThread("Sdk Work Thread");
        mWorkThread.start();
        mWorkHandler=new Handler(mWorkThread.getLooper());
        mTrackExecutor= Executors.newFixedThreadPool(2);
    }

    private boolean prepareResource(Context context,String licensePath){
        Log.d("prepare Resource");
        Assets assets=new Assets(context,licensePath);
        return assets.doCopy();
    }

    @Override
    public void init(final Context context,final String licensePath,final String appKey) {
        Log.e("sdk init");
        assetManager=context.getAssets();
        cInit();
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.e("start prepare resource");
                boolean pb;
                final String path=licensePath.substring(0,licensePath.lastIndexOf
                    (File.separator)+1);
                Log.e("path -- >"+path);
                if(new File(licensePath).exists()){
                    pb=true;
                }else{
                    pb=prepareResource(context,path);
                }
                Log.e("prepare resource success:"+pb);
                if(pb){
                    mObservable.notifyState(State.RESOURCE_READY);
                    isResourceReady=true;
                    TelephonyManager tm = (TelephonyManager)context.getSystemService(Context
                        .TELEPHONY_SERVICE);
                    String DEVICE_ID = tm.getDeviceId();
                    if(DEVICE_ID==null)DEVICE_ID=android.os.Build.SERIAL;
                    Log.e("sticker jni init");
                    int state=mAiyaCameraJni.init(context,path,
                        licensePath,context.getPackageName(),DEVICE_ID,appKey);
                    Log.e("state="+state);
                    if(state==0){
                        mObservable.notifyState(State.INIT_SUCCESS);
                    }else{
                        mObservable.notifyState(State.INIT_FAILED);
                    }
                }else{
                    mObservable.notifyState(State.RESOURCE_FAILED);
                    isResourceReady=false;
                }
            }
        });
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

    @Override
    public void track(final byte[] trackData, final float[] info, final int trackIndex) {
//        Log.e("semaphore 1--> "+mSemaphore.availablePermits());
        if(isResourceReady){
            try {
                mSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mTrackExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    long start=System.currentTimeMillis();
                    int trackCode=mAiyaCameraJni.track(trackData,mTrackWidth,mTrackHeight,info,
                        trackIndex);
                    Log.e("track------------------------>"+(System.currentTimeMillis()-start));
                    if(mTrackCallback!=null){
                        mTrackCallback.onTrack(trackCode,info);
                    }
                    mSemaphore.release();
//                Log.e("semaphore 2--> "+mSemaphore.availablePermits());
                }
            });
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
            mAiyaCameraJni.processFrame(textureId,input.width,input.height,trackIndex);
            if(mProcessCallback!=null){
                mProcessCallback.onFinished();
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
