package com.aiyaapp.aavt.gl;

import android.content.res.Resources;
import android.opengl.GLES20;

import java.nio.ByteBuffer;

/**
 * Created by aiya on 2017/9/23.
 */

public class WaterColorFilter extends BaseFilter {

    private int mGLWidth;
    private int mGLHeight;
    private int mGLNoise;

    private int mNoiseTextureId;


    public WaterColorFilter(Resources res){
        super(res,"shader/base.vert","shader/effect/water_color.frag");
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        mGLWidth= GLES20.glGetUniformLocation(mGLProgram,"uWidth");
        mGLHeight= GLES20.glGetUniformLocation(mGLProgram,"uHeight");
        mGLNoise= GLES20.glGetUniformLocation(mGLProgram,"uNoiseTexture");
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        super.onSizeChanged(width, height);
        mNoiseTextureId=createNoiseTexture(width,height);
    }

    @Override
    protected void onSetExpandData() {
        super.onSetExpandData();
        GLES20.glUniform1f(mGLWidth,mWidth);
        GLES20.glUniform1f(mGLHeight,mHeight);
    }

    @Override
    protected void onBindTexture(int textureId) {
        super.onBindTexture(textureId);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mNoiseTextureId);
        GLES20.glUniform1i(mGLNoise,1);
    }

    private int createNoiseTexture(int width,int height){
        int[] tempTexture=new int[1];
        GLES20.glGenTextures(1,tempTexture,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,tempTexture[0]);
        int length=width*height*3;
        byte[] data=new byte[length];
        for (int i=0;i<length;i++){
            data[i]= (byte) (Math.random()*8-4);
        }
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_RGB,width,height,0,GLES20.GL_RGB,GLES20.GL_UNSIGNED_BYTE,ByteBuffer.wrap(data));
        return tempTexture[0];
    }
}
