/*
 *
 * GLRenderer.java
 * 
 * Created by Wuwang on 2017/3/2
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.util;

import java.lang.ref.WeakReference;

import android.opengl.GLSurfaceView;

import com.aiyaapp.camera.sdk.base.Renderer;
import com.aiyaapp.camera.sdk.util.GLThread;


/**
 * Description:
 */
public class RendererBackstage {

    private GLThread mGLThread;
    private int renderMode;
    private int mWidth,mHeight;
    boolean mPreserveEglContextOnPause;
    Object output;
    Renderer mRenderer;

    public RendererBackstage(){
        mPreserveEglContextOnPause=true;
        renderMode=GLSurfaceView.RENDERMODE_WHEN_DIRTY;
    }

    public void start(){
        mGLThread=new GLThread(new WeakReference<>(this));
        mGLThread.setRenderMode(renderMode);
        mGLThread.start();
    }

    public void setOutput(Object output){
        this.output=output;
        if(mGLThread!=null){
            mGLThread.requestRender();
        }
    }

    public void setSize(int width,int height){
        this.mWidth=width;
        this.mHeight=height;
        if(mGLThread!=null){
            mGLThread.setWindowSize(width,height);
            requestRender();
        }
    }

    public void onResume(){
        if(mGLThread!=null){
            mGLThread.onResume();
        }
    }

    public void onPause(){
        if(mGLThread!=null){
            mGLThread.onPause();
        }
    }

    public void onDestroy(){
        if(mGLThread!=null){
            mGLThread.requestExit();
        }
    }

    public void setPreserveEglContextOnPause(boolean preserveEglContextOnPause){
        this.mPreserveEglContextOnPause=preserveEglContextOnPause;
    }

    public void requestRender(){
        mGLThread.requestRender();
    }

    public void setRenderer(Renderer renderer){
        this.mRenderer=renderer;
    }

    public void setRenderMode(int mode){
        this.renderMode=mode;
        if(mGLThread!=null){
            mGLThread.setRenderMode(this.renderMode);
        }
    }

}
