/*
 *
 * Camera1Provider.java
 * 
 * Created by Wuwang on 2017/3/3
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.aiya.mvc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Rect;
import android.hardware.Camera;

import com.aiyaapp.camera.sdk.base.Log;
import com.aiyaapp.camera.sdk.base.Renderer;
import com.aiyaapp.camera.sdk.widget.AiyaController;
import com.aiyaapp.camera.sdk.widget.AiyaModel;

/**
 * Description: 使用{@link Camera}提供数据时的Model示例
 */
public class Camera1Model implements AiyaModel{

    private Camera mCamera;
    private CameraController mCameraController;
    private int cameraId=1;
    private AiyaController mController;

    public void setCameraController(CameraController controller){
        this.mCameraController=controller;
    }

    @Override
    public void attachToController(final AiyaController controller) {
        if (mCameraController==null){
            mCameraController=new CameraController();
        }
        this.mController=controller;
        controller.setRenderer(new Renderer() {
            @Override
            public void onDestroy() {
                mCameraController.release();
                mCamera=null;
            }

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                if(mCamera!=null){
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera=null;
                }
                mCamera=mCameraController.openCamera(cameraId);
                controller.setImageDirection(cameraId);
                controller.setDataSize(mCamera.getParameters().getPreviewSize().height,
                    mCamera.getParameters().getPreviewSize().width);
                mCameraController.setCameraCallback(mCamera);
                try {
                    mCamera.setPreviewTexture(controller.getTexture());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCameraController.preview(mCamera);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {

            }

            @Override
            public void onDrawFrame(GL10 gl) {
                mCameraController.dataRecycle();
            }
        });
    }

    public class CameraController{

        private Camera camera;
        private Camera.Size previewSize;
        private Queue<byte[]> dataPoll;

        public CameraController(){
            dataPoll=new ConcurrentLinkedQueue<>();
        }

        protected Camera.Size setSize(int cameraId,Camera.Parameters param){
            Camera.Size picSize = getPropPictureSize(param.getSupportedPictureSizes(), 1.778f,
                720);
            Camera.Size preSize = getPropPreviewSize(param.getSupportedPreviewSizes(), 1.778f,
                720);
            param.setPictureSize(picSize.width, picSize.height);
            param.setPreviewSize(preSize.width, preSize.height);
            return getPropPreviewSize(param.getSupportedPreviewSizes(),1.778f,720);
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
                Camera.Size size=setSize(cameraId,param);
                if(previewSize==null||!(size.width==previewSize.width&&size.height==previewSize
                    .height)){
                    dataPoll.clear();
                    for (int i=0;i<3;i++){
                        dataPoll.add(new byte[size.width*size.height*4]);
                    }
                }
                previewSize=size;
                setRecordHint(param);
                otherSetting(param);
                camera.setParameters(param);
            }
            return camera;
        }

        protected void onPreviewCallback(byte[] data,Camera camera){
            mController.requestRender();
        }

        public void setCameraCallback(Camera camera){
            for (int i=0;i<3;i++){
                byte[] dt=dataPoll.poll();
                if(dt!=null){
                    camera.addCallbackBuffer(dt);
                }else{
                    break;
                }
            }
            camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if(data!=null&&dataPoll!=null){
                        dataPoll.add(data);
                        Log.e("camera1","data poll size:"+dataPoll.size());
                    }
                    onPreviewCallback(data,camera);
                }
            });
        }

        public void dataRecycle(){
            byte[] dt=dataPoll.poll();
            if(dt!=null){
                camera.addCallbackBuffer(dt);
            }
        }

        public void preview(Camera camera){
            camera.startPreview();
        }

        protected void release(){
            if(camera!=null){
                try {
                    camera.setPreviewCallback(null);
                    camera.stopPreview();
                    camera.release();
                }catch (RuntimeException e){
                    e.printStackTrace();
                }
                camera=null;
            }
        }

        protected Camera.Size getPropPreviewSize(List<Camera.Size> list, float th, int minWidth){
            Collections.sort(list, sizeComparator);

            int i = 0;
            for(Camera.Size s:list){
                if((s.height >= minWidth) && equalRate(s, th)){
                    break;
                }
                i++;
            }
            if(i == list.size()){
                i = 0;
            }
            return list.get(i);
        }

        protected Camera.Size getPropPictureSize(List<Camera.Size> list, float th, int minWidth){
            Collections.sort(list, sizeComparator);
            int i = 0;
            for(Camera.Size s:list){
                if((s.height >= minWidth) && equalRate(s, th)){
                    break;
                }
                i++;
            }
            if(i == list.size()){
                i = 0;
            }
            return list.get(i);
        }

        private boolean equalRate(Camera.Size s, float rate){
            float r = (float)(s.width)/(float)(s.height);
            return Math.abs(r - rate) <= 0.03;
        }

        private Comparator<Camera.Size> sizeComparator=new Comparator<Camera.Size>(){
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                // TODO Auto-generated method stub
                if(lhs.height == rhs.height){
                    return 0;
                }
                else if(lhs.height > rhs.height){
                    return 1;
                }
                else{
                    return -1;
                }
            }
        };
    }

}
