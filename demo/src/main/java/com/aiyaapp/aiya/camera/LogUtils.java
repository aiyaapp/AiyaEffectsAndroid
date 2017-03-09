/*
 *
 * Log.java
 * 
 * Created by Wuwang on 2016/11/8
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.aiya.camera;

/**
 * Description:
 */
public class LogUtils {

    private static final String tag="Reconfig";
    private static boolean isDebug=true;

    private LogUtils(){}

    public static void debug(boolean isDebug){
        LogUtils.isDebug=isDebug;
    }

    public static void e(String info){
        if(isDebug){
            android.util.Log.e(tag,info);
        }
    }

    public static void e(String tag,String info){
        if(isDebug){
            android.util.Log.e(tag,info);
        }
    }

    public static void d(String info){
        if(isDebug){
            android.util.Log.d(tag,info);
        }
    }

}
