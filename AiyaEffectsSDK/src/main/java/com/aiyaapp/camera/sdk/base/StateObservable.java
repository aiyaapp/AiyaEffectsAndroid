/*
 *
 * StateHandler.java
 * 
 * Created by Wuwang on 2016/11/8
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.base;

import java.util.ArrayList;
import java.util.Observable;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class StateObservable{

    private ArrayList<StateObserver> mObservers;


    private Handler mHandler;

    public StateObservable(){
        mObservers=new ArrayList<>();
        initHandler();
    }

    private void initHandler(){
        mHandler=new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                for (StateObserver s:mObservers) {
                    s.onStateChange((State)msg.obj);
                }
            }
        };
    }

    public void notifyState(State state){
        Log.e("notifyState->"+state.getMsg());
        Message msg=mHandler.obtainMessage();
        msg.obj=state;
        mHandler.sendMessage(msg);
    }

    public void registerObserver(StateObserver observer){
        if(!mObservers.contains(observer)){
            mObservers.add(observer);
        }
    }

    public void unRegisterObserver(StateObserver observer){
        mObservers.remove(observer);
    }

    public void unRegisterAll(){
        mObservers.clear();
    }

}
