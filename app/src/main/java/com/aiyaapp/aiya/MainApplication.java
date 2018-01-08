package com.aiyaapp.aiya;

import android.app.Application;

import com.aiyaapp.aiya.utils.CrashHandler;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by aiya on 2017/9/21.
 */

public class MainApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);

        // 异常处理，不需要处理时注释掉这两句即可！
        CrashHandler crashHandler = CrashHandler.getInstance();
        // 注册crashHandler
        crashHandler.init(getApplicationContext());
    }


}
