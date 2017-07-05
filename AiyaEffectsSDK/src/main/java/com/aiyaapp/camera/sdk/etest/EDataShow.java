/*
 *
 * EDataShow.java
 * 
 * Created by Wuwang on 2016/11/18
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.etest;

import java.util.Locale;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Debug;
import android.os.Process;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.aiyaapp.camera.sdk.base.Log;
import com.aiyaapp.camera.sdk.util.DisplayUtil;

/**
 * Description:
 */
public class EDataShow extends SurfaceView implements Runnable,SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private Paint mPaint;
    private Thread mThread;
    private boolean flag=false;

    private final int dtime=500;
    private Debug.MemoryInfo mInfo;
    private final String OUT_LOG="memoryUseage:%04d cpuUsage:%.3f fps:%02f";

    public EDataShow(Context context) {
        this(context,null);
    }

    public EDataShow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        mHolder=getHolder();
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        mPaint=new Paint();
        mPaint.setTextSize(DisplayUtil.sp2px(getContext(),12));
        mPaint.setColor(0xFFFF8800);
        mPaint.setStrokeWidth(2);
        mPaint.setAntiAlias(true);
        mInfo=getRunningInfo(getContext());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        flag=true;
        mThread=new Thread(this);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        flag=false;
    }

    @Override
    public void run() {
        while (flag){
            long startTime=System.currentTimeMillis();
            myDraw();
            long deltaTime=System.currentTimeMillis()-startTime;
            if(deltaTime<dtime){
                try {
                    cpuRate=AppOsUtils.getProcessCpuRate(dtime-deltaTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void myDraw(){
        mInfo=getRunningInfo(getContext());
        memSize=mInfo!=null?mInfo.getTotalPss()/1024:-1;
        try {
            mCanvas=mHolder.lockCanvas();
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mCanvas.drawText(String.format(Locale.CHINA,"Fps=%.1f，Dtm=%03d，Cma=%.1f",EData.data
                .getFps(),
                EData.data.getDealTime(),EData.data.getCameraFps()),40,40, mPaint);
            try {
                mCanvas.drawText(String.format(Locale.CHINA,"Mem=%04dM，Cpu=%.3f%%,track:%b",memSize,
                    cpuRate,EData.data.getTrackCode()==0),
                    40,70,mPaint);
            }catch (Exception e){
                e.printStackTrace();
            }
            Log.e("log_analyse",String.format(Locale.CHINA,OUT_LOG,memSize,cpuRate,EData.data
                .getFps()));
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(mCanvas!=null){
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    private int memSize;
    private float cpuRate;
    private ActivityManager mActivityManager;
    private Debug.MemoryInfo getRunningInfo(Context context) {
        if(mActivityManager==null){
            mActivityManager = (ActivityManager)context.getSystemService(Context
                .ACTIVITY_SERVICE);
            //获得系统里正在运行的所有进程
        }
        int mpid = Process.myPid();
        Debug.MemoryInfo[] info=mActivityManager.getProcessMemoryInfo(new int[]{mpid});
        if(info!=null&&info.length>0){
            return info[0];
        }
        return null;
    }

}
