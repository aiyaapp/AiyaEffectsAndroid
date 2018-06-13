package com.aiyaapp.aiya;

import com.aiyaapp.aiya.render.AnimListener;

import java.lang.ref.WeakReference;

/**
 * Created by aiya on 2017/9/25.
 */

class WeakAnimListener implements AnimListener {

    private WeakReference<AnimListener> mWeak;

    WeakAnimListener(AnimListener listener){
        mWeak=new WeakReference<>(listener);
    }

    @Override
    public void onAnimEvent(int type, int ret, String message) {
        AnimListener listener=mWeak.get();
        if(listener!=null){
            listener.onAnimEvent(type, ret, message);
        }
    }
}
