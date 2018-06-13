/*
 *
 * PermissionUtils.java
 * 
 * Created by Wuwang on 2016/11/14
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.aiya.panel;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

/**
 * Description:
 */
public class PermissionUtils {


    public static void askPermission(Activity context, String[] permissions,int req, Runnable
        runnable){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            int result= 0;
            for(String a:permissions){
                result+=ActivityCompat.checkSelfPermission(context,a);
            }
            if(result== PackageManager.PERMISSION_GRANTED){
                runnable.run();
            }else{
                ActivityCompat.requestPermissions(context,permissions,req);
            }
        }else{
            runnable.run();
        }
    }

    public static void onRequestPermissionsResult(boolean isReq,int[] grantResults,Runnable
        okRun,Runnable deniRun){
        if(isReq){
            boolean b=true;
            for (int a:grantResults){
                b&=(a==PackageManager.PERMISSION_GRANTED);
            }
            if (grantResults.length > 0&&b) {
                okRun.run();
            } else {
                deniRun.run();
            }
        }
    }

}
