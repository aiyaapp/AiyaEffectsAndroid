/*
 *
 * AiyaCameraView.java
 * 
 * Created by Wuwang on 2016/11/18
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.widget;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.aiyaapp.camera.sdk.AiyaEffects;
import com.aiyaapp.camera.sdk.base.FrameCallback;
import com.aiyaapp.camera.sdk.base.ProcessCallback;
import com.aiyaapp.camera.sdk.base.TrackCallback;
import com.aiyaapp.camera.sdk.etest.EData;
import com.aiyaapp.camera.sdk.filter.AFilter;
import com.aiyaapp.camera.sdk.filter.AiyaEffectFilter;
import com.aiyaapp.camera.sdk.filter.EasyGlUtils;
import com.aiyaapp.camera.sdk.filter.MatrixUtils;
import com.aiyaapp.camera.sdk.filter.NoFilter;

/**
 * Description:
 */
@Deprecated
public class AiyaCameraView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private AiyaCamera mCamera;
    private AiyaEffectFilter mEffectFilter;
    private AFilter mShowFilter;

    private boolean isSetParm=false;
    private int dataWidth=0,dataHeight=0;

    private byte[][] cameraBuffer;
    private Queue<byte[]> mBytesQueue;

    private int cameraId=1;
    private AiyaEffects mEffect;
    private int width,height;
    private float[] SM=new float[16];                       //用于绘制到屏幕上的变换矩阵

    private boolean isRecord=false;                         //录像flag
    private boolean isShoot=false;                          //一次拍摄flag
    private FrameCallback mFrameCallback;                   //回调
    private int frameCallbackWidth, frameCallbackHeight;    //回调数据的宽高
    private ByteBuffer[] outPutBuffer = new ByteBuffer[3];  //用于存储回调数据的buffer
    private float[] callbackOM=new float[16];               //用于绘制回调缩放的矩阵
    private int indexOutput=0;                              //回调数据使用的buffer索引


    //创建离屏buffer，用于最后导出数据
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[1];

    private Runnable callbackIfCameraOpenFailed;

    public AiyaCameraView(Context context) {
        this(context,null);
    }

    public AiyaCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        mEffect= AiyaEffects.getInstance();
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        setPreserveEGLContextOnPause(true);
        setCameraDistance(100);
        mCamera=new AiyaCamera();
        IAiyaCamera.Config mConfig=new IAiyaCamera.Config();
        mConfig.minPreviewWidth=720;
        mConfig.minPictureWidth=720;
        mConfig.rate=1.778f;
        mCamera.setConfig(mConfig);

        mEffectFilter=new AiyaEffectFilter(getResources());
        mShowFilter=new NoFilter(getResources());

        mBytesQueue=new ConcurrentLinkedQueue<>();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isSetParm){
            open(cameraId);
        }
    }

    public void setCameraOpenFaildCallback(Runnable runnable){
        callbackIfCameraOpenFailed=runnable;
    }

    public AiyaCamera getCamera(){
        return mCamera;
    }

    @Override
    public void onPause() {
        super.onPause();
        mCamera.close();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mEffectFilter.create();
        if(!isSetParm){
            open(cameraId);
            stickerInit();
        }
        mEffectFilter.setSize(dataWidth,dataHeight);
        mShowFilter.create();
    }

    private void open(final int cameraId){
        mCamera.close();
        try {
            mCamera.open(cameraId);
            mEffectFilter.setFlag(cameraId);
            final Point previewSize=mCamera.getPreviewSize();
            dataWidth=previewSize.x;
            dataHeight=previewSize.y;
            //预览大小被更改时，回调不为空，需要重新计算最后的导出矩阵
            if(mFrameCallback!=null){
                setFrameCallback(frameCallbackWidth,frameCallbackHeight,mFrameCallback);
            }
            if(cameraBuffer==null){
                cameraBuffer=new byte[3][dataWidth*dataHeight*4];
            }
            for (int i=0;i<3;i++){
                mCamera.addBuffer(cameraBuffer[i]);
            }
            mCamera.setOnPreviewFrameCallbackWithBuffer(new AiyaCamera.PreviewFrameCallback() {

                @Override
                public void onPreviewFrame(byte[] bytes, int width, int height) {
                    EData.data.setCameraCallbackTime(System.currentTimeMillis());
                    if(isSetParm){
                        mBytesQueue.add(bytes);
                        requestRender();
                    }else{
                        mCamera.addBuffer(bytes);
                    }
                }
            });
            mCamera.setPreviewTexture(mEffectFilter.getTexture());
            mCamera.preview();
        }catch (Exception e){
            if(callbackIfCameraOpenFailed!=null){
                callbackIfCameraOpenFailed.run();
            }
        }
    }

    public void switchCamera(){
        cameraId=cameraId==0?1:0;
        open(cameraId);
    }

    public int getCameraId(){
        return cameraId;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width=width;
        this.height=height;
        MatrixUtils.getMatrix(SM,MatrixUtils.TYPE_CENTERCROP,dataWidth,dataHeight,width,height);
        mShowFilter.setMatrix(SM);
        mShowFilter.setSize(width, height);

        deleteFrameBuffer();
        GLES20.glGenFramebuffers(1,fFrame,0);
        EasyGlUtils.genTexturesWithParameter(1,fTexture,0,GLES20.GL_RGBA,dataWidth,dataHeight);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if(isSetParm){
            EData.data.setDealStartTime(System.currentTimeMillis());
            mEffectFilter.draw();

            //显示到屏幕上
            GLES20.glViewport(0,0,width,height);
            mShowFilter.setMatrix(SM);
            mShowFilter.setTextureId(mEffectFilter.getOutputTexture());
            mShowFilter.draw();
            EData.data.setDealEndTime(System.currentTimeMillis());
            byte[] data=mBytesQueue.poll();
            if(data!=null){
                mCamera.addBuffer(data);
            }

            callbackIfNeeded();
        }
    }

    //需要回调，则缩放图片到指定大小，读取数据并回调
    private void callbackIfNeeded() {
        if (mFrameCallback != null && (isRecord || isShoot)) {
            indexOutput = indexOutput++ >= 2 ? 0 : indexOutput;
            if (outPutBuffer[indexOutput] == null) {
                outPutBuffer[indexOutput] = ByteBuffer.allocate(frameCallbackWidth *
                    frameCallbackHeight*4);
            }
            GLES20.glViewport(0, 0, frameCallbackWidth, frameCallbackHeight);
            EasyGlUtils.bindFrameTexture(fFrame[0],fTexture[0]);
            mShowFilter.setMatrix(callbackOM);
            mShowFilter.draw();
            frameCallback();
            isShoot = false;
            EasyGlUtils.unBindFrameBuffer();
            mShowFilter.setMatrix(SM);
        }
    }

    //读取数据并回调
    private void frameCallback(){
        GLES20.glReadPixels(0, 0, frameCallbackWidth, frameCallbackHeight,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, outPutBuffer[indexOutput]);
        mFrameCallback.onFrame(outPutBuffer[indexOutput].array(),mEffectFilter.getTexture().getTimestamp());
    }

    public void onDestroy(){
        setPreserveEGLContextOnPause(false);
        onPause();
    }

    public void setFrameCallback(int width,int height,FrameCallback frameCallback){
        this.frameCallbackWidth =width;
        this.frameCallbackHeight = height;
        if (frameCallbackWidth > 0 && frameCallbackHeight > 0) {
            if(outPutBuffer!=null){
                outPutBuffer=new ByteBuffer[3];
            }
            MatrixUtils.getMatrix(callbackOM,MatrixUtils.TYPE_CENTERCROP,dataWidth, dataHeight, frameCallbackWidth,
                frameCallbackHeight);
            MatrixUtils.flip(callbackOM,false,true);
            this.mFrameCallback = frameCallback;
        } else {
            this.mFrameCallback = null;
        }
    }

    private void deleteFrameBuffer() {
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
        GLES20.glDeleteTextures(1, fTexture, 0);
    }

    public void setFairLevel(int level){
        mEffect.set(AiyaEffects.SET_BEAUTY_LEVEL,level);
    }

    public void setEffect(String effect){
        mEffect.setEffect(effect);
    }

    public void startRecord(){
        isRecord=true;
    }

    public void stopRecord(){
        isRecord=false;
    }

    public void takePhoto(){
        isShoot=true;
    }

    /**
     * 增加自定义滤镜
     * @param filter   自定义滤镜
     * @param isBeforeSticker 是否增加在贴纸之前
     */
    public void addFilter(AFilter filter,boolean isBeforeSticker){
        mEffectFilter.addFilter(filter,isBeforeSticker);
    }

    private void stickerInit(){
        if(!isSetParm&&dataWidth>0&&dataHeight>0) {
            isSetParm = true;
            mEffect.set(AiyaEffects.SET_IN_WIDTH,dataWidth);
            mEffect.set(AiyaEffects.SET_IN_HEIGHT,dataHeight);
            mEffect.setProcessCallback(mcallback);
            mEffect.setTrackCallback(mTrackCallback);
        }
    }

    private ByteBuffer[] mBuffer=new ByteBuffer[3];
    private int i=0;

    private ProcessCallback mcallback = new ProcessCallback() {

        @Override
        public void onFinished() {

        }
    };

    private float[] infos=new float[20];

    private TrackCallback mTrackCallback=new TrackCallback() {
        @Override
        public void onTrack(int trackCode,float[] info) {
            EData.data.setTrackCode(trackCode);
        }

    };

}
