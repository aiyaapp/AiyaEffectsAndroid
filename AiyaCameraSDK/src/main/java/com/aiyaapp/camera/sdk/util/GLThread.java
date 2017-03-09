/*
 *
 * GLThread.java
 * 
 * Created by Wuwang on 2017/3/2
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.util;

import java.lang.ref.WeakReference;

import android.opengl.GLSurfaceView;

import com.aiyaapp.camera.sdk.base.Log;

/**
 * Description:
 */
public class GLThread extends Thread{

    private EGLHelper mEGLHelper;
    private WeakReference<RendererBackstage> mWeak;
    private boolean mSizeChanged=false;
    private int mWidth,mHeight;
    private static final GLThreadManager sGlThreadManager=new GLThreadManager();
    private int renderMode= GLSurfaceView.RENDERMODE_CONTINUOUSLY;

    private boolean mShouldExit=false;
    private boolean mRequestPause=false;
    private boolean mRequestRender=false;
    private boolean mRenderComplete=false;

    private boolean mExited=false;
    private boolean mPaused=false;
    private boolean mHaveEglSurface=false;
    private boolean mHaveEglContext=false;

    private final String TAG="GLThread";

    public GLThread(WeakReference<RendererBackstage> weak){
        super();
        this.mWeak=weak;
        mEGLHelper=new EGLHelper();
    }

    public void setWindowSize(int width,int height){
        this.mWidth=width;
        this.mHeight=height;
        mSizeChanged=true;
    }

    public void setRenderMode(int mode){
        synchronized (sGlThreadManager){
            this.renderMode=mode;
            sGlThreadManager.notifyAll();
        }
    }

    public void requestRender(){
        synchronized (sGlThreadManager){
            mRequestRender=true;
            sGlThreadManager.notifyAll();
            Log.e(TAG,"notifyAll");
        }
    }

    public void requestExit(){
        synchronized (sGlThreadManager){
            mShouldExit = true;
            sGlThreadManager.notifyAll();
            while (! mExited) {
                try {
                    sGlThreadManager.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void stopEglSurfaceLocked() {
        if (mHaveEglSurface) {
            mHaveEglSurface = false;
            mEGLHelper.destroySurface();
        }
    }

    private void stopEglContextLocked() {
        if (mHaveEglContext) {
            mEGLHelper.finish();
            mHaveEglContext = false;
            sGlThreadManager.notifyAll();
        }
    }

    public void onPause(){
        synchronized (sGlThreadManager){
            mRequestPause=true;
            sGlThreadManager.notifyAll();

            //保证pause命令能被正常执行完
            while ((! mExited) && (! mPaused)) {
                Log.e(TAG, "onPause waiting for mPaused.");
                try {
                    sGlThreadManager.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void onResume(){
        synchronized (sGlThreadManager){
            mRequestPause=false;
            mRequestRender=true;
            mRenderComplete=false;
            sGlThreadManager.notifyAll();
            while ((! mExited) && mPaused && (!mRenderComplete)){
                Log.e(TAG,"onResume waiting for !mPaused.");
                try {
                    sGlThreadManager.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void run() {
        setName("GLThread -"+System.currentTimeMillis());

        try {
            guardedRun();
//            RendererBackstage rend = mWeak.get();
//            if (rend != null&&rend.mRenderer!=null) {
//                rend.mRenderer.onDestroySurface(mEGLHelper.getGL());
//            }
            synchronized (sGlThreadManager){
                if(mShouldExit){
                    mExited=true;
                    Log.e(TAG,"should exit");
                    RendererBackstage rend=mWeak.get();
                    if(rend!=null&&rend.mRenderer!=null){
                        rend.mRenderer.onDestroy();
                    }
                    stopEglContextLocked();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void guardedRun() throws InterruptedException {
//        boolean surfaceCreated=false;
        boolean surfaceChanged=false;
        boolean createEglContext=false;
        boolean createEglSurface=false;
        Log.e(TAG,"start---");
        while (true){
            synchronized (sGlThreadManager){

                while (true){
                    if(mShouldExit){
                        return;
                    }

                    //处理Pause
                    boolean pausing=false;
                    if (mPaused!=mRequestPause){
                        Log.e(TAG,"mRequestPause:"+mRequestPause);
                        mPaused = mRequestPause;
                        pausing = mRequestPause;
                        sGlThreadManager.notifyAll();
                    }

                    //如果确定要暂停且EglSurface存在，释放掉EglSurface
                    if(pausing&&mHaveEglSurface){
                        Log.e(TAG,"destroy egl surface");
                        stopEglSurfaceLocked();
                    }

                    //如果确定要暂停且EglContext存在，RendererBackstage为空，或者在暂停时不用保存
                    //Context，则释放掉EglContext
                    if(pausing&&mHaveEglContext){
                        RendererBackstage rbk=mWeak.get();
                        if(rbk==null||!rbk.mPreserveEglContextOnPause){
                            Log.e(TAG,"destroy egl context");
                            stopEglContextLocked();
                        }
                    }

                    //如果绘制对象不存在，则释放掉EGL资源
                    RendererBackstage rend=mWeak.get();
                    if(rend==null){
                        mEGLHelper.finish();
                    }else if(rend.output==null&&mHaveEglSurface){
                        stopEglSurfaceLocked();
                    }

                    if(readyToDraw()){
                        Log.e(TAG,"ready to draw");
                        //不存在EglContext，就创建一个Context
                        if(!mHaveEglContext){
                            mEGLHelper.eglStart();
                            mHaveEglContext=true;
                            createEglContext=true;
                            sGlThreadManager.notifyAll();
                        }

                        //判断是否需要创建一个EglSurface
                        if(mHaveEglContext&&!mHaveEglSurface){
                            mHaveEglSurface=true;
                            createEglSurface=true;
                            surfaceChanged=true;
                        }

                        if(mHaveEglSurface){
                            mRequestRender=false;
                            sGlThreadManager.notifyAll();
                            Log.e(TAG,"break wait");
                            break;
                        }
                    }

                    Log.e(TAG,"wait --------------");
                    sGlThreadManager.wait();
                }

            }

            if(createEglSurface){
                Log.e(TAG,"eglCreateSurface");
                RendererBackstage rend=mWeak.get();
                if(rend!=null&&rend.output!=null){
                    mEGLHelper.eglCreateSurface(rend.output);
                }
                createEglSurface=false;
            }

            if(createEglContext){
                Log.e(TAG,"surface created");
                RendererBackstage rend=mWeak.get();
                if(rend!=null&&rend.mRenderer!=null){
                    rend.mRenderer.onSurfaceCreated(mEGLHelper.getGL(),mEGLHelper.getEglConfig());
                }
                createEglContext=false;
            }

            if(surfaceChanged){
                Log.e(TAG,"surface changed");
                RendererBackstage rend=mWeak.get();
                if(rend!=null&&rend.mRenderer!=null){
                    rend.mRenderer.onSurfaceChanged(mEGLHelper.getGL(),mWidth,mHeight);
                }
                surfaceChanged=false;
            }
            {
                RendererBackstage rend = mWeak.get();
                if (rend != null&&rend.mRenderer!=null) {
                    rend.mRenderer.onDrawFrame(mEGLHelper.getGL());
                    mEGLHelper.swap();
                }
            }
            Log.e(TAG,"draw finish");
        }
    }

    private boolean hasSurface(){
        return mWeak.get()!=null&&mWeak.get().output!=null;
    }

    private boolean readyToDraw(){
        return (!mPaused)&&mWidth>0&&mHeight>0&&hasSurface()
            &&(mRequestRender||renderMode==GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    private static class GLThreadManager{

    }

}
