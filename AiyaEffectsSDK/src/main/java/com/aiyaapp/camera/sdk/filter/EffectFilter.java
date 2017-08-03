/*
 *
 * EffectFilter.java
 * 
 * Created by Wuwang on 2017/3/15
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.filter;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.aiyaapp.camera.sdk.AiyaEffects;
import com.aiyaapp.camera.sdk.base.ISdkManager;
import com.aiyaapp.camera.sdk.base.Log;

import java.util.Arrays;

/**
 *  增加特效的Filter，相比{@link AiyaEffectFilter},EffectFilter是以textureid为输入，
 *  默认会将最终结果渲染到外部指定的FrameBuffer或者WindowBuffer上。如果调用{@link #setFlag(int)},
 *  可控制输出到内部的outputTextureId上。EffectFilter的处理，不关注外部数据源时相机、视频还是图片。
 */
public class EffectFilter extends AFilter {

    private float[] FlipOM= Arrays.copyOf(MatrixUtils.getOriginalMatrix(),16);     //用于显示的变换矩阵

    private AFilter mShowFilter;        //用于显示到界面上

    private float[] infos = new float[20];

    private AFilter mTrackFilter;         //数据准备的Filter,track和保存
    private AFilter mProcessFilter;
    private GroupFilter mPreProcessFilter;
    private GroupFilter mPostProcessFilter;

    private int textureIndex = 0;
    //创建离屏buffer
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[1];

    private int width=0,height=0;

    private int[] outBindFrameBuffer=new int[1];
    private int[] outBindRenderBuffer=new int[1];

    private boolean isExportByOutputTexturteId=false;

    public EffectFilter(Resources res) {
        super(res);
        mTrackFilter = new TrackFilter(res);
        mProcessFilter=new ProcessFilter(res);

        mShowFilter = new NoFilter(res);

        mPreProcessFilter=new GroupFilter(res);
        mPostProcessFilter=new GroupFilter(res);
        MatrixUtils.flip(FlipOM,false,true);
    }

    public void addFilter(AFilter filter,boolean beforeProcess){
        if(beforeProcess){
            mPreProcessFilter.addFilter(filter);
        }else{
            mPostProcessFilter.addFilter(filter);
        }
    }

    public void removeFilter(AFilter filter){
        mPreProcessFilter.removeFilter(filter);
        mPostProcessFilter.removeFilter(filter);
    }

    /**
     * flag为1时，EffectFilter通过outputTextureId向外面提供数据，
     * flag为0时，EffectFilter通过draw直接将数据渲染到外部指定的地方。
     * **/
    @Override
    public void setFlag(int flag) {
        if(flag==0){
            isExportByOutputTexturteId=false;
            mTrackFilter.setMatrix(OM);
        }else{
            isExportByOutputTexturteId=true;
            mTrackFilter.setMatrix(FlipOM);
        }
    }

    @Override
    public int getOutputTexture() {
        return mPostProcessFilter.getOutputTexture();
    }

    @Override
    protected void onCreate() {
        mTrackFilter.create();
        mProcessFilter.create();
        mShowFilter.create();
        mPreProcessFilter.create();
        mPostProcessFilter.create();
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
            if(width>320){
                AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_WIDTH,320);
                AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_HEIGHT,320*height/width);
            }
        }else if(height>width&&height>320){
            if(height>320){
                AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_WIDTH,320*width/height);
                AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_HEIGHT,320);
            }
        }else{
            AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_WIDTH,width);
            AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_HEIGHT,height);
        }
        AiyaEffects.getInstance().set(ISdkManager.SET_ACTION,ISdkManager.ACTION_REFRESH_PARAMS_NOW);
        deleteFrameBuffer();
        GLES20.glGenFramebuffers(1,fFrame,0);
        EasyGlUtils.genTexturesWithParameter(1,fTexture,0,GLES20.GL_RGBA,width,height);
        mTrackFilter.setSize(width,height);
        mPreProcessFilter.setSize(width,height);
        mPostProcessFilter.setSize(width,height);
        mProcessFilter.setSize(width,height);
        mShowFilter.setSize(width,height);
    }

    @Override
    public float[] getMatrix() {
        return mShowFilter.getMatrix();
    }

    @Override
    public void setMatrix(float[] matrix) {
        mShowFilter.setMatrix(matrix);
    }

    //SDK 特效处理的主要流程主要在此处
    //TrackFilter(mTrackFilter)封装SDK的track方法，同普通Filter类似，接收textureId作为输入
    //GroupFilter(mPreProcessFilter和mPostProcessFilter)用于提供在ProcessFilter前后增加滤镜(包括水印在类)的支持。
    //当用户没有添加自定义滤镜时，GroupFilter以输入直接作为输出，不影响性能。
    //ProcessFilter(mProcessFilter)封装SDK的process方法，会绘制出原图及贴图。
    //在流程中Filter是以前一个Filter的输出作为输入进行绘制的，输入输出都是TextureId。
    @Override
    public void draw() {
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING,outBindFrameBuffer,0);
        GLES20.glGetIntegerv(GLES20.GL_RENDERBUFFER_BINDING,outBindRenderBuffer,0);
        boolean isDepthEnable=GLES20.glIsEnabled(GLES20.GL_DEPTH_TEST);
        boolean isBlendEnable=GLES20.glIsEnabled(GLES20.GL_BLEND);
        boolean isCullFaceEnable=GLES20.glIsEnabled(GLES20.GL_CULL_FACE);
        //在离屏Buffer中绘制track的数据，并交给track处理
        long start=System.currentTimeMillis();
        mTrackFilter.setTextureId(getTextureId());
        mTrackFilter.draw();
        Log.d("track read------------------------>"+(System.currentTimeMillis()-start));
        start=System.currentTimeMillis();
        mPreProcessFilter.setTextureId(mTrackFilter.getOutputTexture());
        mPreProcessFilter.draw();

        Log.d("before filter------------------------>"+(System.currentTimeMillis()-start));
        start=System.currentTimeMillis();
        //获取缓存的texture绘制并处理
        mProcessFilter.setTextureId(mPreProcessFilter.getOutputTexture());
        mProcessFilter.draw();
        Log.d("process------------------------>"+(System.currentTimeMillis()-start));
        start=System.currentTimeMillis();
        mPostProcessFilter.setTextureId(mProcessFilter.getOutputTexture());
        mPostProcessFilter.draw();
        Log.d("after filter------------------------>"+(System.currentTimeMillis()-start));
        Log.d("show data index->" + textureIndex);
        //显示出刚才绘制的内容
        if(outBindFrameBuffer[0]!=0){
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,outBindFrameBuffer[0]);
        }
        if(outBindFrameBuffer[0]!=0){
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER,outBindRenderBuffer[0]);
        }
        changeGLState(GLES20.GL_DEPTH_TEST,isDepthEnable);
        changeGLState(GLES20.GL_BLEND,isBlendEnable);
        changeGLState(GLES20.GL_CULL_FACE,isCullFaceEnable);
        if(!isExportByOutputTexturteId){
            GLES20.glViewport(0, 0, width, height);
            mShowFilter.setTextureId(mPostProcessFilter.getOutputTexture());
            mShowFilter.draw();
        }
    }

    private void changeGLState(int key,boolean isEnable){
        if(isEnable){
            GLES20.glEnable(key);
        }else{
            GLES20.glDisable(key);
        }
    }


    private void deleteFrameBuffer() {
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
        GLES20.glDeleteTextures(1, fTexture, 0);
    }

}
