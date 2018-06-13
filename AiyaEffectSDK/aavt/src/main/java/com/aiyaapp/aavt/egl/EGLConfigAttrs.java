package com.aiyaapp.aavt.egl;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGL10;

/*
 * Created by Wuwang on 2017/10/18
 */
public class EGLConfigAttrs {

    private int red=8;
    private int green=8;
    private int blue=8;
    private int alpha=8;
    private int depth=8;
    private int renderType=4;
    private int surfaceType= EGL10.EGL_WINDOW_BIT;

    private boolean makeDefault=false;

    public EGLConfigAttrs red(int red){
        this.red=red;
        return this;
    }

    public EGLConfigAttrs green(int green){
        this.green=green;
        return this;
    }

    public EGLConfigAttrs blue(int blue){
        this.blue=blue;
        return this;
    }

    public EGLConfigAttrs alpha(int alpha){
        this.alpha=alpha;
        return this;
    }

    public EGLConfigAttrs depth(int depth){
        this.depth=depth;
        return this;
    }

    public EGLConfigAttrs renderType(int type){
        this.renderType=type;
        return this;
    }

    public EGLConfigAttrs surfaceType(int type){
        this.surfaceType=type;
        return this;
    }

    public EGLConfigAttrs makeDefault(boolean def){
        this.makeDefault=def;
        return this;
    }

    public boolean isDefault(){
        return makeDefault;
    }

    int[] build(){
        return new int[] {
                EGL10.EGL_SURFACE_TYPE, surfaceType,      //渲染类型
                EGL10.EGL_RED_SIZE, red,  //指定RGB中的R大小（bits）
                EGL10.EGL_GREEN_SIZE, green, //指定G大小
                EGL10.EGL_BLUE_SIZE, blue,  //指定B大小
                EGL10.EGL_ALPHA_SIZE, alpha, //指定Alpha大小，以上四项实际上指定了像素格式
                EGL10.EGL_DEPTH_SIZE, depth, //指定深度缓存(Z Buffer)大小
                EGL10.EGL_RENDERABLE_TYPE, renderType, //指定渲染api类别, 如上一小节描述，这里或者是硬编码的4(EGL14.EGL_OPENGL_ES2_BIT)
                EGL10.EGL_NONE };  //总是以EGL14.EGL_NONE结尾
    }

    @Override
    public String toString() {
        return Arrays.toString(build());
    }
}
