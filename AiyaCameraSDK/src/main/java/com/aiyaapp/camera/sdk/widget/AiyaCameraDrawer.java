/*
 *
 * AiyaCameraDrawer.java
 * 
 * Created by Wuwang on 2016/11/18
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.widget;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.aiyaapp.camera.sdk.base.FrameCallback;
import com.aiyaapp.camera.sdk.base.Log;
import com.aiyaapp.camera.sdk.filter.AFilter;
import com.aiyaapp.camera.sdk.filter.EasyGlUtils;
import com.aiyaapp.camera.sdk.filter.MatrixUtils;
import com.aiyaapp.camera.sdk.filter.GroupFilter;
import com.aiyaapp.camera.sdk.filter.NoFilter;
import com.aiyaapp.camera.sdk.filter.PrepareFilter;
import com.aiyaapp.camera.sdk.filter.ProcessFilter;

/**
 * Description:
 */
@Deprecated
public class AiyaCameraDrawer implements GLSurfaceView.Renderer {

    private float[] EM=new float[16];     //用于更改大小的变换矩阵
    private float[] SM=new float[16];     //用于显示的变换矩阵
    private float[] OM;     //用于后台绘制的变换矩阵

    private int width, height;
    private SurfaceTexture mSurfaceTexture;

//    private AFilter mBeauty;            //美颜滤镜
//    private AFilter mNoFilter;          //无滤镜
//    private AFilter mNowFilter;         //当前滤镜

    private AFilter mShowFilter;        //用于显示到界面上

    private FrameCallback mFrameCallback;   //每帧处理的回调
    private int frameCallbackWidth, frameCallbackHeight; //回调数据的宽高
    private boolean oneShotCallback = false;       //是否回调
    private boolean isKeepCallback = false;     //是否一直回调
    private ByteBuffer[] outPutBuffer = new ByteBuffer[3];
    private Queue<byte[]> cameraByteQueue;
    private int indexOutput = 0;
    private boolean update = false;
    private float[] infos = new float[20];

    private AFilter mPreFilter;         //数据准备的Filter,track和保存
    private AFilter mProcessFilter;
    private GroupFilter mBeFilter;
    private GroupFilter mAfFilter;

    private int textureIndex = 0;
    //创建离屏buffer
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[1];

    private int mPreviewWidth=0,mPreviewHeight=0;
    private boolean isPreviewSizeChanged=false;

    public static final int PARAMS_TYPE_FILTER = 0;

    public AiyaCameraDrawer(Resources res) {
        cameraByteQueue = new LinkedBlockingQueue<>();
//        mBeauty = new Beauty(res);
//        mNoFilter = new NoFilter(res);
//        mNowFilter = mNoFilter;

        mPreFilter = new PrepareFilter(res);
        mProcessFilter=new ProcessFilter(res);

        mShowFilter = new NoFilter(res);

        mBeFilter=new GroupFilter(res);
        mAfFilter=new GroupFilter(res);

        //必须传入上下翻转的矩阵
//        OM= MatrixUtils.getOriginalMatrix();
//        MatrixUtils.flip(OM,false,true);

    }

    public void addFilter(AFilter filter,boolean beforeTrack){
//        filter.setMatrix(OM);
        if(beforeTrack){
            mBeFilter.addFilter(filter);
        }else{
            mAfFilter.addFilter(filter);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        int texture = createTextureID();
        mSurfaceTexture = new SurfaceTexture(texture);
        mPreFilter.create();
        mPreFilter.setTextureId(texture);
        mProcessFilter.create();
//        mBeauty.create();
//        mNoFilter.create();
        mShowFilter.create();
        mBeFilter.create();
        mAfFilter.create();
    }

    public SurfaceTexture getTexture() {
        return mSurfaceTexture;
    }

    public void update(byte[] bytes) {
        cameraByteQueue.add(bytes);
        this.update = true;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        cameraByteQueue.clear();
        this.width = width;
        this.height = height;
        if(this.isPreviewSizeChanged){
            isPreviewSizeChanged=false;
            deleteFrameBuffer();
            GLES20.glGenFramebuffers(1,fFrame,0);
            EasyGlUtils.genTexturesWithParameter(1,fTexture,0,GLES20.GL_RGBA,mPreviewWidth,mPreviewHeight);
            mPreFilter.setSize(mPreviewWidth,mPreviewHeight);
            mBeFilter.setSize(mPreviewWidth,mPreviewHeight);
            mAfFilter.setSize(mPreviewWidth,mPreviewHeight);
            mProcessFilter.setSize(mPreviewWidth,mPreviewHeight);
        }
        MatrixUtils.getShowMatrix(SM,mPreviewWidth, mPreviewHeight, width, height);
        MatrixUtils.flip(SM,false,true);
        if (frameCallbackWidth != 0 && frameCallbackHeight != 0) {
            MatrixUtils.getShowMatrix(EM,mPreviewWidth, mPreviewHeight, frameCallbackWidth,
                frameCallbackHeight);
            MatrixUtils.flip(EM,false,true);
        }
        mShowFilter.setMatrix(SM);

//        if (OM == null) {
//            OM = MatrixUtils.getOriginalMatrix();
//            MatrixUtils.flip(OM, false, true);
//        }
//        mBeauty.setMatrix(OM);
//        mNoFilter.setMatrix(OM);
    }

    public void setParams(int type,int ... params){

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (update) {
            update = false;
            mSurfaceTexture.updateTexImage();
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
        GLES20.glViewport(0, 0, width, height);
        mShowFilter.setTextureId(mAfFilter.getOutputTexture());
        mShowFilter.draw();
        Log.e("beauty------------------------>"+(System.currentTimeMillis()-start));

        callbackIfNeeded();

        byte[] d = cameraByteQueue.poll();
        if (mCamera != null && d != null) {
            mCamera.addCallbackBuffer(d);
        }
    }

    /**
     * 设置帧数据回调
     * @param width 希望得到的图片宽度
     * @param height 希望得到的图片高度
     * @param frameCallback 回调
     */
    public void setFrameCallback(int width, int height, FrameCallback frameCallback) {
        this.frameCallbackWidth =width;
        this.frameCallbackHeight = height;
        if (frameCallbackWidth > 0 && frameCallbackHeight > 0) {
            if(outPutBuffer!=null){
                outPutBuffer=new ByteBuffer[3];
            }
            MatrixUtils.getShowMatrix(EM,mPreviewWidth, mPreviewHeight, frameCallbackWidth,frameCallbackHeight);
            MatrixUtils.flip(EM,false,true);
            this.mFrameCallback = frameCallback;
        } else {
            this.mFrameCallback = null;
        }
    }

    /**
     * 设置是否开启对每一帧数据的捕获
     * @param isKeepCallback
     */
    public void setKeepCallback(boolean isKeepCallback){
        this.isKeepCallback=isKeepCallback;
    }

    /**
     * 设置是否去捕获下一帧数据
     * @param oneShotCallback
     */
    public void setOneShotCallback(boolean oneShotCallback){
        this.oneShotCallback=oneShotCallback;
    }

    private Camera mCamera;

    public void setCamera(Camera camera) {
        this.mCamera = camera;
    }

    public void setPreviewSize(int width,int height){
        if(this.mPreviewWidth!=width||this.mPreviewHeight!=height){
            this.mPreviewWidth=width;
            this.mPreviewHeight=height;
            this.isPreviewSizeChanged=true;
        }
    }

    //需要回调，则缩放图片到指定大小，读取数据并回调
    private void callbackIfNeeded() {
        if (mFrameCallback != null && (oneShotCallback || isKeepCallback)) {
            indexOutput = indexOutput++ >= 2 ? 0 : indexOutput;
            if (outPutBuffer[indexOutput] == null) {
                outPutBuffer[indexOutput] = ByteBuffer.allocate(frameCallbackWidth *
                    frameCallbackHeight*4);
            }
            GLES20.glViewport(0, 0, frameCallbackWidth, frameCallbackHeight);
            EasyGlUtils.bindFrameTexture(fFrame[0],fTexture[0]);
            mShowFilter.setMatrix(EM);
            mShowFilter.draw();
            frameCallback();
            oneShotCallback = false;
            EasyGlUtils.unBindFrameBuffer();
            mShowFilter.setMatrix(SM);
        }
    }

    //读取数据并回调
    private void frameCallback(){
        GLES20.glReadPixels(0, 0, frameCallbackWidth, frameCallbackHeight,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, outPutBuffer[indexOutput]);
        mFrameCallback.onFrame(outPutBuffer[indexOutput].array(),mSurfaceTexture.getTimestamp());
    }

    //根据摄像头设置纹理映射坐标
    public void setCameraId(int id) {
        mPreFilter.setFlag(id);
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
