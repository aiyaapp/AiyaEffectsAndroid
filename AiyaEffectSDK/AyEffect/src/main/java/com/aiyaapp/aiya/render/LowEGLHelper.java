package com.aiyaapp.aiya.render;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * Created by aiya on 2017/9/16.
 */

class LowEGLHelper {

    private EGL10 mEgl;
    private EGLSurface mEGLSurface;
    private EGLContext mEGLContext;
    private EGLDisplay mEGLDisplay;
    private EGLConfig mEGLConfig;
    private EGLContext mShareEGLContext = EGL10.EGL_NO_CONTEXT;
    private boolean isDebug = true;

    private int mEglSurfaceType = EGL10.EGL_WINDOW_BIT;

    private Object mSurface;
    private Object mCopySurface;


    /**
     * @param type one of {@link EGL10#EGL_WINDOW_BIT}、{@link EGL10#EGL_PBUFFER_BIT}、{@link EGL10#EGL_PIXMAP_BIT}
     */
    public void setEGLSurfaceType(int type) {
        this.mEglSurfaceType = type;
    }

    public void setSurface(Object surface) {
        this.mSurface = surface;
    }

    public void setCopySurface(Object surface) {
        this.mCopySurface = surface;
    }


    /**
     * create the environment for OpenGLES
     *
     * @param eglWidth  width
     * @param eglHeight height
     */
    public boolean createGLES(int eglWidth, int eglHeight) {
        int[] attributes = new int[]{
                EGL10.EGL_SURFACE_TYPE, mEglSurfaceType,      //渲染类型
                EGL10.EGL_RED_SIZE, 8,  //指定RGB中的R大小（bits）
                EGL10.EGL_GREEN_SIZE, 8, //指定G大小
                EGL10.EGL_BLUE_SIZE, 8,  //指定B大小
                EGL10.EGL_ALPHA_SIZE, 8, //指定Alpha大小，以上四项实际上指定了像素格式
                EGL10.EGL_DEPTH_SIZE, 16, //指定深度缓存(Z Buffer)大小
                EGL10.EGL_RENDERABLE_TYPE, 4, //指定渲染api类别, 如上一小节描述，这里或者是硬编码的4(EGL14.EGL_OPENGL_ES2_BIT)
                EGL10.EGL_NONE};  //总是以EGL14.EGL_NONE结尾

        int glAttrs[] = {
                0x3098, 2,  //0x3098是EGL14.EGL_CONTEXT_CLIENT_VERSION，但是4.2以前没有EGL14
                EGL10.EGL_NONE
        };

        int bufferAttrs[] = {
                EGL10.EGL_WIDTH, eglWidth,
                EGL10.EGL_HEIGHT, eglHeight,
                EGL10.EGL_NONE
        };

        mEgl = (EGL10) EGLContext.getEGL();
        //获取默认显示设备，一般为设备主屏幕
        mEGLDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        //获取版本号，[0]为版本号，[1]为子版本号
        int[] versions = new int[2];
        mEgl.eglInitialize(mEGLDisplay, versions);
        log(mEgl.eglQueryString(mEGLDisplay, EGL10.EGL_VENDOR));
        log(mEgl.eglQueryString(mEGLDisplay, EGL10.EGL_VERSION));
        log(mEgl.eglQueryString(mEGLDisplay, EGL10.EGL_EXTENSIONS));
        //获取EGL可用配置
        EGLConfig[] configs = new EGLConfig[1];
        int[] configNum = new int[1];
        mEgl.eglChooseConfig(mEGLDisplay, attributes, configs, 1, configNum);
        if (configs[0] == null) {
            log("eglChooseConfig Error:" + mEgl.eglGetError());
            return false;
        }
        mEGLConfig = configs[0];
        //创建EGLContext
        mEGLContext = mEgl.eglCreateContext(mEGLDisplay, mEGLConfig, mShareEGLContext, glAttrs);
        if (mEGLContext == EGL10.EGL_NO_CONTEXT) {
            return false;
        }
        //获取创建后台绘制的Surface
        switch (mEglSurfaceType) {
            case EGL10.EGL_WINDOW_BIT:
                mEGLSurface = mEgl.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, mSurface, new int[]{EGL10.EGL_NONE});
                break;
            case EGL10.EGL_PIXMAP_BIT:
                break;
            case EGL10.EGL_PBUFFER_BIT:
                mEGLSurface = mEgl.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, bufferAttrs);
                break;
        }
        if (mEGLSurface == EGL10.EGL_NO_SURFACE) {
            log("eglCreateSurface Error:" + mEgl.eglGetError());
            return false;
        }

        if (!mEgl.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
            log("eglMakeCurrent Error:" + mEgl.eglQueryString(mEGLDisplay, mEgl.eglGetError()));
            return false;
        }
        log("gl environment create success");
        return true;
    }

    public EGLSurface createEGLWindowSurface(Object object) {
        return mEgl.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, object, new int[]{EGL10.EGL_NONE});
    }


    public void setShareEGLContext(EGLContext context) {
        this.mShareEGLContext = context;
    }

    public EGLContext getEGLContext() {
        return mEGLContext;
    }

    public boolean makeCurrent() {
        return mEgl.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
    }

    public boolean makeCurrent(EGLSurface surface) {
        return mEgl.eglMakeCurrent(mEGLDisplay, surface, surface, mEGLContext);
    }

    public boolean destroyGLES() {
        mEgl.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        mEgl.eglDestroySurface(mEGLDisplay, mEGLSurface);
        mEgl.eglDestroyContext(mEGLDisplay, mEGLContext);
        mEgl.eglTerminate(mEGLDisplay);
        log("gl destroy gles");
        return true;

    }

    public void destorySurface(EGLSurface surface) {
        mEgl.eglDestroySurface(mEGLDisplay, surface);
    }
    //    public void setPresentationTime(long time){
//        EGLExt.eglPresentationTimeANDROID(mEGLDisplay,mEGLSurface,time);
//    }
    //    public void setPresentationTime(EGLSurface surface,long time){
//        EGLExt.eglPresentationTimeANDROID(mEGLDisplay,surface,time);
//    }

    public boolean swapBuffers() {
        return mEgl.eglSwapBuffers(mEGLDisplay, mEGLSurface);
    }

    public boolean swapBuffers(EGLSurface surface) {
        return mEgl.eglSwapBuffers(mEGLDisplay, surface);
    }

    //创建视频数据流的OES TEXTURE
    public int createTextureID() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return texture[0];
    }

    private void log(String log) {
        if (isDebug) {
            Log.d("EGLHelper", log);
        }
    }

}
