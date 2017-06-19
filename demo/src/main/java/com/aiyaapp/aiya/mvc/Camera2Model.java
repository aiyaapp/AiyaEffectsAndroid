/*
 *
 * Camera2Model.java
 * 
 * Created by Wuwang on 2017/3/3
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.aiya.mvc;

import com.aiyaapp.camera.sdk.util.CamParaUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;

import com.aiyaapp.camera.sdk.base.Renderer;
import com.aiyaapp.camera.sdk.widget.AiyaController;
import com.aiyaapp.camera.sdk.widget.AiyaModel;
import com.aiyaapp.camera.sdk.base.Log;

/**
 * Description: 使用{@link android.hardware.camera2}中的API的Model示例
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Model implements AiyaModel {

    private CameraDevice mCameraDevice;
    private CameraController mCameraController;
    private static final Object lock=new Object();
    private AiyaController mController;
    private Context mContext;

    public Camera2Model(Context context){
        this.mContext=context;
    }

    public void setCameraController(CameraController cameraController){
        this.mCameraController=cameraController;
    }

    @Override
    public void attachToController(final AiyaController controller) {
        if(mCameraController==null){
            mCameraController=new CameraController(mContext);
        }
        this.mController=controller;
        controller.setRenderer(new Renderer() {
            @Override
            public void onDestroy() {
                mCameraController.release();
            }

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                mCameraController.start();
                mCameraController.setTarget(controller.getTexture());
                mCameraController.openCamera(1);
                controller.setDataSize(mCameraController.mPreviewSize.getHeight(),
                    mCameraController.mPreviewSize.getWidth());
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {

            }

            @Override
            public void onDrawFrame(GL10 gl) {

            }
        });
    }

    public class CameraController{

        private CameraManager mCameraManager;
        private CameraDevice mCameraDevice;
        private CameraCaptureSession mCaptureSession;
        private CaptureRequest.Builder mCaptureRequestBuilder;
        private SurfaceTexture mTexture;
        private HandlerThread mThread;
        private Handler mHandler;
        public Size mPreviewSize=new Size(720,1280);

        public CameraController(Context context){
            mCameraManager= (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
        }

        protected void start(){
            mThread=new HandlerThread("camera2 thread"+System.currentTimeMillis());
            mThread.start();
            mHandler=new Handler(mThread.getLooper());
        }

        protected Size setSize(int cameraId, StreamConfigurationMap map){
            List<Size> sizes=Arrays.asList(map.getOutputSizes(SurfaceTexture.class));
            return CamParaUtil.getInstance().getPropSize(sizes,1.778f,720);
        }

        public CameraDevice openCamera(int cameraId){
            try {
                CameraCharacteristics characteristics=
                    mCameraManager.getCameraCharacteristics(cameraId+"");
                //支持的STREAM CONFIGURATION
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics
                    .SCALER_STREAM_CONFIGURATION_MAP);
                mPreviewSize=setSize(cameraId,map);
                mCameraDevice=null;
                while (mCameraDevice==null){
                    mCameraManager.openCamera(cameraId+"",mDeviceStateCallback,mHandler);
                    synchronized (lock){
                        try {
                            Log.e("camera2","wait");
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (CameraAccessException | SecurityException e) {
                e.printStackTrace();
            }
            Log.e("camera2","reture device"+(mCameraDevice==null));
            return mCameraDevice;
        }

        public final void setTarget(SurfaceTexture texture){
            this.mTexture=texture;
        }

        private CameraDevice.StateCallback mDeviceStateCallback=new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice camera) {
                mCameraDevice=camera;
                synchronized (lock){
                    Log.e("camera2","lock.notifyAll()");
                    lock.notifyAll();
                }
                try {
                    Surface mSurface=new Surface(mTexture);
                    mTexture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());
                    mCaptureRequestBuilder=mCameraDevice.createCaptureRequest(CameraDevice
                        .TEMPLATE_RECORD);
                    mCaptureRequestBuilder.addTarget(mSurface);
                    mCameraDevice.createCaptureSession(Arrays.asList(mSurface),
                        mCaptureSessionStateCallback,mHandler);
                    Log.e("camera2","createCaptureSession");
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(CameraDevice camera) {
                synchronized (lock){
                    Log.e("camera2","onDisconnected");
                    lock.notifyAll();
                }
            }

            @Override
            public void onError(CameraDevice camera, int error) {
                synchronized (lock){
                    Log.e("camera2","open error");
                    lock.notifyAll();
                }
            }
        };

        private CameraCaptureSession.StateCallback mCaptureSessionStateCallback=
            new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mCaptureSession=session;
                    try {
                        session.setRepeatingRequest(mCaptureRequestBuilder.build(),
                            mCaptureSessionCaptureCallback,mHandler);
                        Log.e("camera2","setRepeatingRequest");
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            };

        private CameraCaptureSession.CaptureCallback mCaptureSessionCaptureCallback=
            new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Log.e("camera2","onCaptureCompleted");
                    onPreviewCallback(mCameraDevice,session);
                }

                @Override
                public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
                    super.onCaptureProgressed(session, request, partialResult);
                    Log.e("camera2","onCaptureProgressed");
                }
            };

        protected void onPreviewCallback(CameraDevice device,CameraCaptureSession session){
            mController.requestRender();
        }

        protected void release(){
            if(mCaptureSession!=null){
                mCaptureSession.close();
            }
            if(mCameraDevice!=null){
                mCameraDevice.close();
            }
            mHandler.getLooper().quitSafely();
        }

    }

}
