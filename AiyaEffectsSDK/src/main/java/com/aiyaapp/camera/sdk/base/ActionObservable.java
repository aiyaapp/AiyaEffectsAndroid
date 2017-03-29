/*
 *
 * StateHandler.java
 * 
 * Created by Wuwang on 2016/11/8
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.base;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class ActionObservable {

    private ArrayList<ActionObserver> mObservers;


    private Handler mHandler;

    public ActionObservable(){
        mObservers=new ArrayList<>();
        initHandler();
    }

    private void initHandler(){
        mHandler=new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                for (int i=0;i<mObservers.size();i++) {
                    mObservers.get(i).onAction((Event)msg.obj);
                }
            }
        };
    }

    public void notifyState(Event state){
        Message msg=mHandler.obtainMessage();
        msg.obj=state;
        mHandler.sendMessage(msg);
    }

    public void registerObserver(ActionObserver observer){
        if(!mObservers.contains(observer)){
            mObservers.add(observer);
        }
    }

    public void unRegisterObserver(ActionObserver observer){
        mObservers.remove(observer);
    }

    public void unRegisterAll(){
        mObservers.clear();
    }

}
