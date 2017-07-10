/*
 *
 * Log.java
 * 
 * Created by Wuwang on 2016/11/8
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.base;

import com.aiyaapp.camera.sdk.AiyaEffects;

/**
 * Description:
 */
public class Log {

    private static final String tag="AiyaCameraEffect";
    private static boolean isDebug=true;

    private Log(){}

    public static void debug(boolean isDebug){
        Log.isDebug=isDebug;
        AiyaEffects.getInstance().set(ISdkManager.SET_LOG,isDebug?0x03:0x07);
    }

    public static void e(String info){
        if(isDebug){
            android.util.Log.e(tag,info);
        }
    }

    public static void e(String tag,String info){
        if (isDebug){
            android.util.Log.e(tag,info);
        }
    }

    public static void d(String info){
        if(isDebug){
            android.util.Log.d(tag,info);
        }
    }

    public static void d(String tag,String info){
        if(isDebug){
            android.util.Log.d(tag,info);
        }
    }


}
