/*
 *
 * EGLTool.java
 * 
 * Created by Wuwang on 2016/11/11
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.util;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

/**
 * Description:
 */
public class EGLTool {

    private static Error err;

    private EGLTool(){}

    /**
     *
     * @param eglWidth
     * @param eglHeight
     * @return
     */
    public static GL10 createGLES(int eglWidth, int eglHeight){

        int[] attributes = new int[] {
            EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,      //离屏渲染
            //EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,      //前台渲染
            EGL10.EGL_RED_SIZE, 8,  //指定RGB中的R大小（bits）
            EGL10.EGL_GREEN_SIZE, 8, //指定G大小
            EGL10.EGL_BLUE_SIZE, 8,  //指定B大小
            EGL10.EGL_ALPHA_SIZE, 8, //指定Alpha大小，以上四项实际上指定了像素格式
            EGL10.EGL_DEPTH_SIZE, 16, //指定深度缓存(Z Buffer)大小
            EGL10.EGL_RENDERABLE_TYPE, 4, //指定渲染api类别, 如上一小节描述，这里或者是硬编码的4(EGL14.EGL_OPENGL_ES2_BIT)
            EGL10.EGL_NONE };  //总是以EGL10.EGL_NONE结尾

        int glAttrs[] = {
            0x3098, 2,              //0x3098是EGL14.EGL_CONTEXT_CLIENT_VERSION，但是4.2以前没有EGL14
            EGL10.EGL_NONE, };

        int pBufferAttrs[]={
            EGL10.EGL_WIDTH,eglWidth,
            EGL10.EGL_HEIGHT,eglHeight,
            EGL10.EGL_NONE
        };

        EGLContext mEGLContext;
        EGLConfig mEGLConfig;
        EGLSurface mEGLSurface;

        //获取EGL
        EGL10 mEGL=(EGL10)EGLContext.getEGL();

        //获取默认显示设备，一般为设备主屏幕
        EGLDisplay mDisplay=mEGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        //获取版本号，[0]为版本号，[1]为子版本号
        int[] versions=new int[2];
        mEGL.eglInitialize(mDisplay,versions);

        //获取EGL可用配置
        EGLConfig[] configs = new EGLConfig[1];
        int[] configNum = new int[1];
        mEGL.eglChooseConfig(mDisplay, attributes, configs, 1, configNum);
        mEGLConfig = configs[0];

        //创建EGLContext
        mEGLContext=mEGL.eglCreateContext(mDisplay,mEGLConfig,EGL10.EGL_NO_CONTEXT, glAttrs);
        if(mEGLContext==EGL10.EGL_NO_CONTEXT){
            err= Error.NO_CONTEXT;
            return null;
        }
        //获取创建后台绘制的Surface
        mEGLSurface=mEGL.eglCreatePbufferSurface(mDisplay,mEGLConfig,pBufferAttrs);
        if(mEGLSurface==EGL10.EGL_NO_SURFACE){
            int ec=mEGL.eglGetError();
            err= Error.NO_BUFFER;
            return null;
        }

        if(!mEGL.eglMakeCurrent(mDisplay,mEGLSurface,mEGLSurface,mEGLContext)){
            err= Error.MAKE_CURRENT;
            return null;
        }
        return (GL10)mEGLContext.getGL();
    }

    public static Error getErr(){
        Error e=err;
        err= Error.NO;
        return e;
    }


    public enum Error{
        NO(0,"正常"),
        NO_CONTEXT(1,"无法获取EGLContext"),
        NO_BUFFER(2,"创建buffer surface故障"),
        MAKE_CURRENT(3,"最后创建失败");

        private int state;
        private String msg;
        Error(int state,String msg){
            this.state=state;
            this.msg=msg;
        }

        public int asInt(){
            return state;
        }

        public String getMsg(){
            return msg;
        }

    }



}
