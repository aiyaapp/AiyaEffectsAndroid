/*
 *
 * TrackFilter.java
 * 
 * Created by Wuwang on 2017/3/15
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.filter;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.aiyaapp.camera.sdk.AiyaEffects;
import com.aiyaapp.camera.sdk.base.ISdkManager;

import java.nio.ByteBuffer;

/**
 * Description:
 */
public class TrackFilter extends AFilter{

    private NoFilter mFilter;
    private int width=0;
    private int height=0;
    private float[] infos = new float[20];
    private int nowTextureIndex=0;

    private int fTextureSize = 2;
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[fTextureSize];

    private boolean isFirstDraw=true;

    //获取Track数据
    private ByteBuffer tBuffer;

    public TrackFilter(Resources mRes) {
        super(mRes);
        mFilter=new NoFilter(mRes);
    }

    @Override
    protected void initBuffer() {

    }

    @Override
    public void setMatrix(float[] matrix) {
        mFilter.setMatrix(matrix);
    }

    @Override
    public int getOutputTexture() {
        if (isFirstDraw){
            isFirstDraw=false;
            return fTexture[0];
        }
        return fTexture[nowTextureIndex];
    }

    private byte[] getTrackData() {
        GLES20.glReadPixels(0, 0, AiyaEffects.getInstance().get(ISdkManager.SET_TRACK_WIDTH),
                AiyaEffects.getInstance().get(ISdkManager.SET_TRACK_HEIGHT) , GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,tBuffer);
        return tBuffer.array();
    }

    @Override
    public void draw() {
        boolean a=GLES20.glIsEnabled(GLES20.GL_DEPTH_TEST);
        if(a){
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        }
        mFilter.setTextureId(getTextureId());
        EasyGlUtils.bindFrameTexture(fFrame[0],fTexture[nowTextureIndex]);
        GLES20.glViewport(0,0, AiyaEffects.getInstance().get(ISdkManager.SET_TRACK_WIDTH),
                AiyaEffects.getInstance().get(ISdkManager.SET_TRACK_HEIGHT));
        mFilter.draw();
        AiyaEffects.getInstance().track(getTrackData(), infos, nowTextureIndex);
        GLES20.glViewport(0,0,width,height);
        mFilter.draw();
        EasyGlUtils.unBindFrameBuffer();
        if(a){
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }
        nowTextureIndex^=1;
    }

    @Override
    protected void onCreate() {
        mFilter.create();
        //创建FrameBuffer和Texture
        nowTextureIndex=0;
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        mFilter.setSize(width, height);
        if(this.width!=width||this.height!=height){
            this.width=width;
            this.height=height;
            deleteFrameBuffer();
            GLES20.glGenFramebuffers(1, fFrame, 0);
            EasyGlUtils.genTexturesWithParameter(fTextureSize, fTexture, 0,GLES20.GL_RGBA,width,height);
            if(tBuffer!=null){
                tBuffer.clear();
            }
            tBuffer = ByteBuffer.allocate(
                    AiyaEffects.getInstance().get(ISdkManager.SET_TRACK_WIDTH)*
                            AiyaEffects.getInstance().get(ISdkManager.SET_TRACK_HEIGHT )* 4);
        }
    }

    private void deleteFrameBuffer() {
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
        GLES20.glDeleteTextures(fTextureSize, fTexture, 0);
    }

}
