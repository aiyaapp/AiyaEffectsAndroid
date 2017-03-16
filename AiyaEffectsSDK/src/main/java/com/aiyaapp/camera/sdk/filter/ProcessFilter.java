/*
 *
 * ProcessFilter.java
 * 
 * Created by Wuwang on 2016/12/21
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.filter;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.aiyaapp.camera.sdk.AiyaEffects;

/**
 * 用于处理用户通过视频或相机输入的图像流，给输入图像进行美颜处理、增加特效等。该类必须与
 * {@link PrepareFilter}配合使用。
 */
public class ProcessFilter extends AFilter {

    private int index=1;
    //创建离屏buffer
    private int[] fFrame = new int[1];
    private int[] fRender = new int[1];
    private int[] fTexture = new int[1];

    private int width;
    private int height;


    public ProcessFilter(Resources mRes) {
        super(mRes);
    }

    @Override
    protected void initBuffer() {

    }

    @Override
    protected void onCreate() {
        index=1;

    }

    @Override
    public void setInt(int type, int... params) {
        if(type==KEY_IN){
            index=params[0];
        }
        super.setInt(type, params);
    }

    @Override
    public int getOutputTexture() {
        return fTexture[0];
    }

    @Override
    public void draw() {
        boolean b=GLES20.glIsEnabled(GLES20.GL_CULL_FACE);
        if(b){
            GLES20.glDisable(GLES20.GL_CULL_FACE);
        }
        GLES20.glViewport(0,0,width,height);
        EasyGlUtils.bindFrameTexture(fFrame[0],fTexture[0]);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
            GLES20.GL_RENDERBUFFER, fRender[0]);
        AiyaEffects.getInstance().process(getTextureId(),index);
        EasyGlUtils.unBindFrameBuffer();
        if(b){
            GLES20.glEnable(GLES20.GL_CULL_FACE);
        }
        index^=1;
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        if(this.width!=width&&this.height!=height){
            this.width=width;
            this.height=height;
            deleteFrameBuffer();
            GLES20.glGenFramebuffers(1,fFrame,0);
            GLES20.glGenRenderbuffers(1,fRender,0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0]);
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER,fRender[0]);
            GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER,GLES20.GL_DEPTH_COMPONENT16,
                width, height);
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, fRender[0]);
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER,0);
            EasyGlUtils.genTexturesWithParameter(1,fTexture,0,GLES20.GL_RGBA,width,height);
        }
    }

    private void deleteFrameBuffer() {
        GLES20.glDeleteRenderbuffers(1, fRender, 0);
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
        GLES20.glDeleteTextures(1, fTexture, 0);
    }

}
