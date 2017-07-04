/*
 *
 * TrackFilter.java
 * 
 * Created by Wuwang on 2016/12/21
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.filter;

import java.nio.ByteBuffer;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.aiyaapp.camera.sdk.AiyaEffects;
import com.aiyaapp.camera.sdk.base.ISdkManager;
import com.aiyaapp.camera.sdk.base.Log;

/**
 *  用于接收图像流，并做人脸特征点定位的类。该类必须与{@link ProcessFilter}配合使用
 *  当有图像输入时，该类会渲染一个用户设置的宽为{@link ISdkManager#SET_TRACK_WIDTH}，
 *  高为{@link ISdkManager#SET_TRACK_HEIGHT}的纹理，然后读取该纹理图像，用于人脸特征点定位。
 *  然后将原图像保存为纹理，并将上一次保存的纹理作为输出。
 */
public class PrepareFilter extends AFilter {

    private AiyaFilter mFilter;
    private int width=0;
    private int height=0;
    private float[] infos = new float[200];
    private int nowTextureIndex=0;

    private int fTextureSize = 2;
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[fTextureSize];

    private boolean isFirstDraw=true;

    //获取Track数据
    private ByteBuffer tBuffer;

    public PrepareFilter(Resources mRes) {
        super(mRes);
        mFilter=new AiyaFilter(mRes);
    }

    public void setCoordMatrix(float[] matrix){
        mFilter.setCoordMatrix(matrix);
    }

    @Override
    public void setFlag(int flag) {
        mFilter.setFlag(flag);
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
        long t=System.currentTimeMillis();
        GLES20.glReadPixels(0, 0, AiyaEffects.getInstance().get(ISdkManager.SET_TRACK_WIDTH),
            AiyaEffects.getInstance().get(ISdkManager.SET_TRACK_HEIGHT) , GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,tBuffer);
        Log.d("track read cost:"+(System.currentTimeMillis()-t));
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
        if(AiyaEffects.getInstance().isNeedTrack()){
            GLES20.glViewport(0,0, AiyaEffects.getInstance().get(ISdkManager.SET_TRACK_WIDTH),
                AiyaEffects.getInstance().get(ISdkManager.SET_TRACK_HEIGHT));
            mFilter.draw();
            AiyaEffects.getInstance().track(getTrackData(), infos, nowTextureIndex);
        }else{
            AiyaEffects.getInstance().track(null, infos, nowTextureIndex);
        }
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
