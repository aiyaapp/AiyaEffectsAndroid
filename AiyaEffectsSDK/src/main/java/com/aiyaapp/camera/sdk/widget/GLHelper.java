package com.aiyaapp.camera.sdk.widget;

import android.opengl.GLUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import com.aiyaapp.camera.sdk.base.Log;
import com.aiyaapp.camera.sdk.base.Renderer;
import javax.microedition.khronos.egl.EGL;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by aiya on 2017/6/19.
 */

public class GLHelper {

    private final static Object mLock=new Object();

    private EGL10 mEgl;
    private EGLContext mEglContext;
    private EGLDisplay mEglDisplay;
    private EGLConfig mEglConfig;
    private EGLSurface mEglSurface;

    private int mWidth,mHeight;

    private final int EGL_CONTEXT_CLIENT_VERSION=12440;
    private final int EGL_OPENGL_ES2_BIT=4;

    private Object mSurface;
    private Renderer mRenderer;

    private Thread mThread;

    private boolean isCreated=false;
    private boolean isRequestRender=true;

    public GLHelper(){
        startThread();
    }

    private void startThread(){
        mThread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    guardedRun();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        mThread.start();
    }

    private boolean created(){
        if(!isCreated){
            mEgl= (EGL10)EGLContext.getEGL();
            mEglDisplay=mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            int[] minor=new int[2];
            if(!mEgl.eglInitialize(mEglDisplay,minor)){
                throw new RuntimeException("eglInitialize failed :"+ eglError());
            }
            int[] configAttribs = {
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,      //前台渲染
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

            if(mSurface!=null){
                mEglSurface=mEgl.eglCreateWindowSurface(mEglDisplay,mEglConfig,mSurface,null);
            }
            if(mEglSurface==null||mEglSurface==EGL10.EGL_NO_SURFACE){
                return false;
            }
            //mEgl.eglCreatePbufferSurface(mEglDisplay,mEglConfig,)

            if(!mEgl.eglMakeCurrent(mEglDisplay,mEglSurface,mEglSurface,mEglContext)){
                Log.e("wuwang","eglMakeCurrent error:"+mEgl.eglGetError());
                return false;
            }
            if(mRenderer!=null){
                mRenderer.onSurfaceCreated((GL10) mEglContext.getGL(),mEglConfig);
            }
            isCreated=true;
        }
        return true;
    }

    private void change(){

    }

    private void destroy(){

    }

    private void draw(){
        if(mRenderer!=null){
            mRenderer.onDrawFrame((GL10) mEglContext.getGL());
        }
    }

    private String eglError(){
        return GLUtils.getEGLErrorString(mEgl.eglGetError());
    }

    private void guardedRun() throws InterruptedException {



        while (true){
            synchronized(mLock){
                mLock.wait();
            }
            if(created()&&isRequestRender){
                change();
                draw();
                isRequestRender=false;
            }
        }
    }

    public void setSurface(Object surface){
        this.mSurface=surface;
    }

    public void setRenderer(Renderer renderer){
        this.mRenderer=renderer;
    }

    public void requestRender(){
        Log.e("wuwang","glHelper try request render");
        synchronized(mLock){
            Log.e("wuwang","glHelper request render");
            isRequestRender=true;
            mLock.notifyAll();
        }
    }

    public void glCreate(){

    }

    public void glChangeSize(int width,int height){
        this.mWidth=width;
        this.mHeight=height;
    }

    public void glDraw(){

    }

    public void glDestroy(){

    }

    public void glPause(){

    }

    public void glResume(){

    }


}
