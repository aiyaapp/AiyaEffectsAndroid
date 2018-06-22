package com.aiyaapp.aiya;

import java.lang.ref.WeakReference;

/**
 * Created by aiya on 2017/9/20.
 */

public class WeakEventListener implements IEventListener {

    public WeakReference<IEventListener> mWeak;

    public WeakEventListener(IEventListener listener){
        this.mWeak=new WeakReference<>(listener);
    }

    public WeakEventListener(){}

    public void setEventListener(IEventListener listener){
        this.mWeak=new WeakReference<>(listener);
    }

    @Override
    public int onEvent(int type, int ret, String info) {
        if(mWeak!=null){
            IEventListener listener=mWeak.get();
            if(listener!=null){
                return listener.onEvent(type, ret, info);
            }
        }
        return 0;
    }
}
