package com.aiyaapp.aiya;

import android.content.Context;

/**
 * AiyaEffects主要用于认证与注册。建议在应用初始化时调用认证。
 *
 * @author wuwang
 */

public class AiyaEffects {

    private static WeakEventListener mListener = new WeakEventListener();


    public static int init(Context context, String appKey) {
        return _init(context.getApplicationContext(), appKey);
    }


    /**
     * 设置SDK的事件监听器
     *
     * @param listener 事件监听器
     */
    public static void setEventListener(IEventListener listener) {
        mListener.setEventListener(listener);
        _setEventListener(mListener);
    }

    private static native void _setEventListener(IEventListener listener);

    private static native int _init(Context context, String appKey);

    public static void deInit() {
        _deInit();
    }

    private static native void _deInit();

    static {
        System.loadLibrary("AyCoreSdk");
        System.loadLibrary("AyCoreJni");
    }

}
