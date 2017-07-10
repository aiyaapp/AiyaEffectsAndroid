/*
 *
 * AiyaEffectFilter.java
 * 
 * Created by Wuwang on 2017/2/24
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.aiya.track;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import com.aiyaapp.camera.sdk.AiyaEffects;
import com.aiyaapp.camera.sdk.base.ISdkManager;
import com.aiyaapp.camera.sdk.base.Log;
import com.aiyaapp.camera.sdk.filter.AFilter;
import com.aiyaapp.camera.sdk.filter.EasyGlUtils;
import com.aiyaapp.camera.sdk.filter.GroupFilter;
import com.aiyaapp.camera.sdk.filter.NoFilter;
import com.aiyaapp.camera.sdk.filter.PrepareFilter;
import com.aiyaapp.camera.sdk.filter.ProcessFilter;
import javax.microedition.khronos.opengles.GL10;

/**
 *  SDK特效处理的主要流程实现，调用{@link PrepareFilter}及{@link ProcessFilter},
 *  并提供接口使用户能够在固定的流程上，添加自定义Filter以作其他处理。
 */
public class TrackEffectFilter extends AFilter {

    private float[] SM=new float[16];     //用于显示的变换矩阵

    private SurfaceTexture mSurfaceTexture;

//    private AFilter mBeauty;            //美颜滤镜
//    private AFilter mNoFilter;          //无滤镜
//    private AFilter mNowFilter;         //当前滤镜

    private AFilter mShowFilter;        //用于显示到界面上

    private float[] infos = new float[20];
    private float[] coordMatrix=new float[16];

    private TrackOnlyFilter mPreFilter;         //数据准备的Filter,track和保存

    private int textureIndex = 0;
    //创建离屏buffer
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[1];

    private int width=0,height=0;

    public static final int PARAMS_TYPE_FILTER = 0;

    public TrackEffectFilter(Resources res) {
        super(res);
        mPreFilter = new TrackOnlyFilter(res);

        mShowFilter = new NoFilter(res);

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
    }

    public SurfaceTexture getTexture() {
        return mSurfaceTexture;
    }

    @Override
    public int getOutputTexture() {
        return mPreFilter.getOutputTexture();
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        this.width = width;
        this.height = height;
        AiyaEffects.getInstance().set(ISdkManager.SET_IN_WIDTH,width);
        AiyaEffects.getInstance().set(ISdkManager.SET_IN_HEIGHT,height);
        AiyaEffects.getInstance().set(ISdkManager.SET_OUT_WIDTH,width);
        AiyaEffects.getInstance().set(ISdkManager.SET_OUT_HEIGHT,height);
        if(width>height&&width>320){
            mPreFilter.setTrackSize(320,320*height/width);
        }else if(height>width&&height>320){
            mPreFilter.setTrackSize(320*width/height,320);
        }else{
            mPreFilter.setTrackSize(width,height);
        }
        AiyaEffects.getInstance().set(ISdkManager.SET_ACTION,ISdkManager.ACTION_REFRESH_PARAMS_NOW);
        deleteFrameBuffer();
        GLES20.glGenFramebuffers(1,fFrame,0);
        EasyGlUtils.genTexturesWithParameter(1,fTexture,0,GLES20.GL_RGBA,width,height);
        mPreFilter.setSize(width,height);
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
        mPreFilter.draw();
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
