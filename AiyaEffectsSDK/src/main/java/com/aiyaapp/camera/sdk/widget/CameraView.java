/*
 *
 * AiyaCameraView2.java
 * 
 * Created by Wuwang on 2017/3/1
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.widget;

import android.util.Log;
import com.aiyaapp.camera.sdk.util.CamParaUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
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
public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private AiyaEffects mEffect;
    private AiyaEffectFilter mEffectFilter;
    private AFilter mShowFilter;
    private static CameraController mDefaultCameraController=new CameraController();
    private CameraController mCameraController=mDefaultCameraController;
    private int width,height;
    private AtomicBoolean isParamSet=new AtomicBoolean(false);
    private int cameraId=1;
    private int dataWidth,dataHeight;
    private float[] SM=new float[16];                       //用于绘制到屏幕上的变换矩阵
    private float[] callbackOM=new float[16];               //用于绘制回调缩放的矩阵
    private boolean isRecord=false;                         //录像flag
    private boolean isShoot=false;                          //一次拍摄flag
    private ByteBuffer[] outPutBuffer = new ByteBuffer[3];  //用于存储回调数据的buffer
    private FrameCallback mFrameCallback;                   //回调
    private int frameCallbackWidth, frameCallbackHeight;    //回调数据的宽高
    private int indexOutput=0;                              //回调数据使用的buffer索引

    //创建离屏buffer，用于最后导出数据
    private int[] mExportFrame = new int[1];
    private int[] mExportTexture = new int[1];

    private Camera mCamera;

    public CameraView(Context context) {
        this(context,null);
    }

    public CameraView(Context context, AttributeSet attrs) {
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

        mEffectFilter=new AiyaEffectFilter(getResources());
        mShowFilter=new NoFilter(getResources());
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isParamSet.get()){
            openCamera(cameraId);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mCamera!=null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera=null;
        }
    }

    public void addFilter(AFilter filter,boolean isBeforeProcess){
        mEffectFilter.addFilter(filter,isBeforeProcess);
    }

    public void removeFilter(AFilter filter){
        mEffectFilter.removeFilter(filter);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mEffectFilter.create();
        if(!isParamSet.get()){
            openCamera(cameraId);
            if(mFrameCallback!=null){
                setFrameCallback(frameCallbackWidth,frameCallbackHeight,mFrameCallback);
            }
            mEffectFilter.setSize(dataWidth,dataHeight);
        }
        mShowFilter.create();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width=width;
        this.height=height;

        MatrixUtils.getMatrix(SM,MatrixUtils.TYPE_CENTERCROP,
            dataWidth,dataHeight,width,height);
        mShowFilter.setMatrix(SM);
        mShowFilter.setSize(width, height);

        deleteFrameBuffer();
        GLES20.glGenFramebuffers(1,mExportFrame,0);
        EasyGlUtils.genTexturesWithParameter(1,mExportTexture,0,GLES20.GL_RGBA,dataWidth,dataHeight);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if(isParamSet.get()){
            EData.data.setDealStartTime(System.currentTimeMillis());
            //接收图像流，特效处理并输出一个texture
            mEffectFilter.draw();

            //显示到屏幕上
            GLES20.glViewport(0,0,width,height);
            mShowFilter.setMatrix(SM);
            mShowFilter.setTextureId(mEffectFilter.getOutputTexture());
            mShowFilter.draw();

            EData.data.setDealEndTime(System.currentTimeMillis());

            //相机回调数据循环利用
            mCameraController.dataRecycle();

            callbackIfNeeded();
        }
    }

    public void onDestroy(){
        setPreserveEGLContextOnPause(false);
        onPause();
    }

    public void switchCamera(){
        cameraId=cameraId==0?1:0;
        openCamera(cameraId);
    }

    public int getCameraId(){
        return cameraId;
    }

    public void setCameraController(CameraController controller){
        this.mCameraController=controller;
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

    //需要回调，则缩放图片到指定大小，读取数据并回调
    private void callbackIfNeeded() {
        if (mFrameCallback != null && (isRecord || isShoot)) {
            indexOutput = indexOutput++ >= 2 ? 0 : indexOutput;
            if (outPutBuffer[indexOutput] == null) {
                outPutBuffer[indexOutput] = ByteBuffer.allocate(frameCallbackWidth *
                    frameCallbackHeight*4);
            }
            GLES20.glViewport(0, 0, frameCallbackWidth, frameCallbackHeight);
            EasyGlUtils.bindFrameTexture(mExportFrame[0],mExportTexture[0]);
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

    private void openCamera(int cameraId){
        if(mCamera!=null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera=null;
        }
        mCamera=mCameraController.openCamera(cameraId);
        mEffectFilter.setFlag(cameraId);
        dataWidth=mCamera.getParameters().getPreviewSize().height;
        dataHeight=mCamera.getParameters().getPreviewSize().width;
        mCameraController.setCameraCallback(this,mCamera,isParamSet);
        try {
            mCamera.setPreviewTexture(mEffectFilter.getTexture());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCameraController.preview(mCamera);
        isParamSet.set(false);
        updateSdkParams();
    }

    private void updateSdkParams(){
        if(!isParamSet.get()&&dataWidth>0&&dataHeight>0) {
            isParamSet.set(true);
            mEffect.set(AiyaEffects.SET_IN_WIDTH,dataWidth);
            mEffect.set(AiyaEffects.SET_IN_HEIGHT,dataHeight);
            mEffect.setProcessCallback(mcallback);
            mEffect.setTrackCallback(mTrackCallback);
        }
    }

    private void deleteFrameBuffer() {
        GLES20.glDeleteFramebuffers(1, mExportFrame, 0);
        GLES20.glDeleteTextures(1, mExportTexture, 0);
    }

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

    public static class CameraController{

        private Camera camera;
        private byte[][] cameraBuffers;
        private Queue<byte[]> mByteQueue=new ConcurrentLinkedQueue<>();

        protected void setSize(int cameraId,Camera.Parameters param){
            Camera.Size picSize = CamParaUtil.getInstance().getPropSize(
                param.getSupportedPictureSizes(), 1.778f,720);
            Camera.Size preSize = CamParaUtil.getInstance().getPropSize(
                param.getSupportedPreviewSizes(), 1.778f,720);
            param.setPictureSize(picSize.width, picSize.height);
            param.setPreviewSize(preSize.width, preSize.height);
            Log.d("AiyaCamera","Preview Size:"+preSize.width+"/"+preSize.height);
        }

        protected void otherSetting(Camera.Parameters param){
            if (param.getMaxNumFocusAreas() > 0) {
                Rect areaRect1 = new Rect(-50, -50, 50, 50);
                List<Camera.Area> focusAreas = new ArrayList<>();
                focusAreas.add(new Camera.Area(areaRect1, 1000));
                param.setFocusAreas(focusAreas);
            }
            // if the camera support setting of metering area.
            if (param.getMaxNumMeteringAreas() > 0) {
                List<Camera.Area> meteringAreas = new ArrayList<>();
                Rect areaRect1 = new Rect(-100, -100, 100, 100);
                meteringAreas.add(new Camera.Area(areaRect1, 1000));
                param.setMeteringAreas(meteringAreas);
            }

//            Log.e("wuwang","camera isVideoStabilizationSupported:"+param
//                .isVideoStabilizationSupported());
//            param.setVideoStabilization(true);        //无效
//            param.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);   //无效
//            List<int[]> al=param.getSupportedPreviewFpsRange();
//            for (int[] a:al){
//                Log.e("previewSize","size->"+a[0]+"/"+a[1]);
//            }
//            param.setPreviewFpsRange(30000,30000);
        }

        protected void setRecordHint(Camera.Parameters param){
            param.setRecordingHint(true);
            param.set("video-size", param.getPreviewSize().width + "x" + param.getPreviewSize().height);
        }

        public Camera openCamera(int cameraId){
            camera=Camera.open(cameraId);
            if(camera!=null) {
                Camera.Parameters param = camera.getParameters();
                setSize(cameraId,param);
                setRecordHint(param);
                otherSetting(param);
                camera.setParameters(param);
            }
            return camera;
        }

        public void setCameraCallback(final GLSurfaceView view,Camera camera,
                                      final AtomicBoolean isParamSet){
            mByteQueue.clear();
            int dataSize=camera.getParameters().getPreviewSize().width*
                camera.getParameters().getPreviewSize().height*4;
            if(cameraBuffers==null||cameraBuffers[0].length!=dataSize){
                cameraBuffers=new byte[3][dataSize];
            }
            for (int i=0;i<3;i++){
                camera.addCallbackBuffer(cameraBuffers[i]);
            }
            camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if(isParamSet.get()){
                        mByteQueue.add(data);
                        view.requestRender();
                    }else{
                        camera.addCallbackBuffer(data);
                    }
                }
            });
        }

        public void preview(Camera camera){
            camera.startPreview();
        }

        public void dataRecycle(){
            byte[] data=mByteQueue.poll();
            if(data!=null){
                camera.addCallbackBuffer(data);
            }
        }

        private static boolean equalRate(Camera.Size s, float rate){
            float r = (float)(s.width)/(float)(s.height);
            return Math.abs(r - rate) <= 0.03;
        }
    }
}
