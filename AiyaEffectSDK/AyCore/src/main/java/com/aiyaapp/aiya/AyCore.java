package com.aiyaapp.aiya;

import android.content.Context;

public class AyCore {
    static {
        System.loadLibrary("AyCoreSdk");
        System.loadLibrary("BaseEffects");
        System.loadLibrary("AyCoreJni");
    }

    public static native void InitLicense(Context context, String key, OnResultCallback callback);

    public interface OnResultCallback {
        void onResult(int ret); //0表示成功, 其它表示失败
    }
}
