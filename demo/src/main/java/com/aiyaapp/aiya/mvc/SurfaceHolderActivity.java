/*
 *
 * SurfaceHolderActivity.java
 * 
 * Created by Wuwang on 2017/3/6
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.aiya.mvc;

import android.Manifest;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.aiyaapp.aiya.EffectSelectActivity;
import com.aiyaapp.aiya.R;
import com.aiyaapp.aiya.util.PermissionUtils;
import com.aiyaapp.camera.sdk.base.FrameCallback;
import com.aiyaapp.camera.sdk.base.Log;
import com.aiyaapp.camera.sdk.filter.WaterMarkFilter;
import com.aiyaapp.camera.sdk.widget.AiyaController;
import com.aiyaapp.camera.sdk.widget.AiyaModel;

/**
 * Description:
 */
public class SurfaceHolderActivity extends EffectSelectActivity implements FrameCallback {

    private int bmpWidth=720,bmpHeight=1280;

    private SurfaceView mSurfaceView;

    private AiyaModel mAiyaModel;
    private AiyaController mAiyaController;

    private Camera1Model mCamera1Model;
    private Camera2Model mCamera2Model;

    private SurfaceHolder mNowHolder;
    private int mWidth,mHeight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionUtils.askPermission(this,new String[]{Manifest.permission.CAMERA,Manifest
            .permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE},10,mRunnable);

    }


    private Runnable mRunnable=new Runnable() {
        @Override
        public void run() {
            setContentView(R.layout.activity_surfaceholder);
            initData();
            modelInit();
            mAiyaController=new AiyaController(SurfaceHolderActivity.this);

            mAiyaController.setFrameCallback(bmpWidth,bmpHeight,SurfaceHolderActivity.this);
            mAiyaModel=mCamera1Model;
//            mAiyaModel=mCamera2Model;     //使用Camera2
            mSurfaceView= (SurfaceView)findViewById(R.id.mSurfaceView);
            mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    mNowHolder=holder;
                    mAiyaController.surfaceCreated(holder);
                    mAiyaModel.attachToController(mAiyaController);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    mWidth=width;
                    mHeight=height;
                    mAiyaController.surfaceChanged(width, height);
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mAiyaController.surfaceDestroyed();
                    mNowHolder=null;
                }
            });
        }
    };

    private void modelInit(){
        mCamera1Model=new Camera1Model();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            mCamera2Model=new Camera2Model(this);
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()){
            case R.id.mShutter:
                mAiyaController.takePhoto();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAiyaController!=null){
            Log.e("textureview","resume---->");
            mAiyaController.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAiyaController!=null){
            Log.e("textureview","pause---->");
            mAiyaController.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAiyaController!=null){
            mAiyaController.destroy();
        }
    }


    @Override
    public void onFrame(byte[] bytes, long time) {
        saveBitmapAsync(bytes,bmpWidth,bmpHeight);
    }

}
