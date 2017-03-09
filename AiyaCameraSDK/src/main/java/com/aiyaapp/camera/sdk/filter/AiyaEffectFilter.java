/*
 *
 * AiyaEffectFilter.java
 * 
 * Created by Wuwang on 2017/2/24
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.filter;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.aiyaapp.camera.sdk.AiyaCameraEffect;
import com.aiyaapp.camera.sdk.base.ISdkManager;
import com.aiyaapp.camera.sdk.base.Log;

/**
 *  SDK特效处理的主要流程实现，调用{@link PrepareFilter}及{@link ProcessFilter},
 *  并提供接口使用户能够在固定的流程上，添加自定义Filter以作其他处理。
 */
public class AiyaEffectFilter extends AFilter {

    private float[] SM=new float[16];     //用于显示的变换矩阵
    private float[] OM;     //用于后台绘制的变换矩阵

    private SurfaceTexture mSurfaceTexture;

//    private AFilter mBeauty;            //美颜滤镜
//    private AFilter mNoFilter;          //无滤镜
//    private AFilter mNowFilter;         //当前滤镜

    private AFilter mShowFilter;        //用于显示到界面上

    private float[] infos = new float[20];
    private float[] coordMatrix=new float[16];

    private PrepareFilter mPreFilter;         //数据准备的Filter,track和保存
    private AFilter mProcessFilter;
    private GroupFilter mBeFilter;
    private GroupFilter mAfFilter;

    private int textureIndex = 0;
    //创建离屏buffer
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[1];

    private int width=0,height=0;

    public static final int PARAMS_TYPE_FILTER = 0;

    public AiyaEffectFilter(Resources res) {
        super(res);
        mPreFilter = new PrepareFilter(res);
        mProcessFilter=new ProcessFilter(res);

        mShowFilter = new NoFilter(res);

        mBeFilter=new GroupFilter(res);
        mAfFilter=new GroupFilter(res);

    }

    public void addFilter(AFilter filter,boolean beforeProcess){
        filter.setMatrix(OM);
        if(beforeProcess){
            mBeFilter.addFilter(filter);
        }else{
            mAfFilter.addFilter(filter);
        }
    }

    @Override
    public void setFlag(int flag) {
        super.setFlag(flag);
        mPreFilter.setFlag(flag);
    }

    @Override
    protected void onCreate() {
        int texture = createTextureID();
        mSurfaceTexture = new SurfaceTexture(texture);
        mPreFilter.create();
        mPreFilter.setTextureId(texture);
        mProcessFilter.create();
        mShowFilter.create();
        mBeFilter.create();
        mAfFilter.create();
    }

    public SurfaceTexture getTexture() {
        return mSurfaceTexture;
    }

    @Override
    public int getOutputTexture() {
        if(mAfFilter==null)return -1;
        return mAfFilter.getOutputTexture();
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        this.width = width;
        this.height = height;
        AiyaCameraEffect.getInstance().set(ISdkManager.SET_IN_WIDTH,width);
        AiyaCameraEffect.getInstance().set(ISdkManager.SET_IN_HEIGHT,height);
        AiyaCameraEffect.getInstance().set(ISdkManager.SET_OUT_WIDTH,width);
        AiyaCameraEffect.getInstance().set(ISdkManager.SET_OUT_HEIGHT,height);
        if(width>height&&width>320){
            if(width>320){
                AiyaCameraEffect.getInstance().set(ISdkManager.SET_TRACK_WIDTH,320);
                AiyaCameraEffect.getInstance().set(ISdkManager.SET_TRACK_HEIGHT,320*height/width);
            }
        }else if(height>width&&height>320){
            if(height>320){
                AiyaCameraEffect.getInstance().set(ISdkManager.SET_TRACK_WIDTH,320*width/height);
                AiyaCameraEffect.getInstance().set(ISdkManager.SET_TRACK_HEIGHT,320);
            }
        }else{
            AiyaCameraEffect.getInstance().set(ISdkManager.SET_TRACK_WIDTH,width);
            AiyaCameraEffect.getInstance().set(ISdkManager.SET_TRACK_HEIGHT,height);
        }
        AiyaCameraEffect.getInstance().set(ISdkManager.SET_ACTION,ISdkManager.ACTION_REFRESH_PARAMS_NOW);
        deleteFrameBuffer();
        GLES20.glGenFramebuffers(1,fFrame,0);
        EasyGlUtils.genTexturesWithParameter(1,fTexture,0,GLES20.GL_RGBA,width,height);
        mPreFilter.setSize(width,height);
        mBeFilter.setSize(width,height);
        mAfFilter.setSize(width,height);
        mProcessFilter.setSize(width,height);
    }

    @Override
    public float[] getMatrix() {
        return mPreFilter.getMatrix();
    }

    @Override
    public void setMatrix(float[] matrix) {
        mPreFilter.setMatrix(matrix);
    }

    //SDK 特效处理的主要流程主要在此处
    //PrepareFilter(mPreFilter)封装SDK的track方法，提供SurfaceTexture做为数据源的输出，也是它的输入。
    //GroupFilter(mBeFilter和mAfFilter)用于提供在ProcessFilter前后增加滤镜(包括水印在类)的支持。
    //当用户没有添加自定义滤镜时，GroupFilter以输入直接作为输出，不影响性能。
    //ProcessFilter(mProcessFilter)封装SDK的process方法，会绘制出原图及贴图。
    //在流程中Filter是以前一个Filter的输出作为输入进行绘制的，输入输出都是TextureId。
    @Override
    public void draw() {

        if(getTexture()!=null){
            getTexture().updateTexImage();
            //此处不可缺，否则，在部分手机上，会出现绿边或其他类似现象。
            getTexture().getTransformMatrix(coordMatrix);
            mPreFilter.setCoordMatrix(coordMatrix);
        }

        //在离屏Buffer中绘制track的数据，并交给track处理
        long start=System.currentTimeMillis();
        mPreFilter.draw();
        Log.e("track read------------------------>"+(System.currentTimeMillis()-start));
        start=System.currentTimeMillis();
        mBeFilter.setTextureId(mPreFilter.getOutputTexture());
        mBeFilter.draw();

        Log.e("before filter------------------------>"+(System.currentTimeMillis()-start));
        start=System.currentTimeMillis();
        //获取缓存的texture绘制并处理
        mProcessFilter.setTextureId(mBeFilter.getOutputTexture());
        mProcessFilter.draw();
        Log.e("process------------------------>"+(System.currentTimeMillis()-start));
        start=System.currentTimeMillis();
        mAfFilter.setTextureId(mProcessFilter.getOutputTexture());
        mAfFilter.draw();
        Log.e("after filter------------------------>"+(System.currentTimeMillis()-start));

        Log.e("show data index->" + textureIndex);
        //显示出刚才绘制的内容
//        GLES20.glViewport(0, 0, width, height);
//        mShowFilter.setTextureId(mAfFilter.getOutputTexture());
//        mShowFilter.draw();
        Log.e("beauty------------------------>"+(System.currentTimeMillis()-start));
    }

    //创建显示摄像头原始数据的OES TEXTURE
    private int createTextureID() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }


    private void deleteFrameBuffer() {
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
        GLES20.glDeleteTextures(1, fTexture, 0);
    }

}
