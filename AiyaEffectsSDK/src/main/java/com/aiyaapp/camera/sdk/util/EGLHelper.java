/*
 *
 * EglHelper.java
 * 
 * Created by Wuwang on 2017/3/2
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.util;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLUtils;

/**
 * Description:
 */
public class EGLHelper {

    private EGL10 mEgl;
    private EGLContext mEglContext;
    private EGLDisplay mEglDisplay;
    private EGLConfig mEglConfig;
    private EGLSurface mEglSurface;
    private GL10 mGL;

    private final int EGL_CONTEXT_CLIENT_VERSION=12440;
    private final int EGL_OPENGL_ES2_BIT=4;

    public void eglStart(){
        mEgl= (EGL10)EGLContext.getEGL();
        mEglDisplay=mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int[] minor=new int[2];
        if(!mEgl.eglInitialize(mEglDisplay,minor)){
            throw new RuntimeException("eglInitialize failed :"+ eglError());
        }
        int[] configAttribs = {
            EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,
            EGL10.EGL_ALPHA_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL10.EGL_NONE
        };
        int []numConfigs = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        if (!mEgl.eglChooseConfig(mEglDisplay, configAttribs, configs, 1, numConfigs)) {
            throw new RuntimeException("eglChooseConfig failed : " +eglError());
        }
        mEglConfig=configs[0];
        int[] contextAttribs = {
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL10.EGL_NONE
        };
        mEglContext = mEgl.eglCreateContext(mEglDisplay, mEglConfig, EGL10.EGL_NO_CONTEXT,
            contextAttribs);
    }

    public void eglCreateSurface(Object nativeWindow){
        mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, mEglConfig ,nativeWindow, null);
        if (mEglSurface == EGL10.EGL_NO_SURFACE || mEglContext == EGL10.EGL_NO_CONTEXT) {
            throw new RuntimeException("eglCreateWindowSurface failed : " +eglError());
        }
        if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed : " +eglError());
        }

        mGL = (GL10)mEglContext.getGL();
    }

     private void eglInit(Object nativeWindow){
        eglStart();
        eglCreateSurface(nativeWindow);
    }

    public void swap(){
        if(mEgl!=null&&mEglDisplay!=null&&mEglDisplay!=EGL10.EGL_NO_DISPLAY
            &&mEglSurface!=null&&mEglSurface!=EGL10.EGL_NO_SURFACE){
            mEgl.eglSwapBuffers(mEglDisplay,mEglSurface);
        }
    }

    public GL10 getGL(){
        return mGL;
    }

    public EGLConfig getEglConfig(){
        return mEglConfig;
    }

    private String eglError(){
        return GLUtils.getEGLErrorString(mEgl.eglGetError());
    }

    public void destroySurface() {
        destroySurfaceImp();
    }

    private void destroySurfaceImp() {
        if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {
            mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_CONTEXT);
            mEgl.eglDestroySurface(mEglDisplay,mEglSurface);
            mEglSurface = null;
        }
    }

    public void finish() {
        if (mEglContext != null) {
            mEgl.eglDestroyContext(mEglDisplay,mEglContext);
            mEglContext = null;
        }
        if (mEglDisplay != null) {
            mEgl.eglTerminate(mEglDisplay);
            mEglDisplay = null;
        }
    }

}
