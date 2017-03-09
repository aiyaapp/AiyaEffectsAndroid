/*
 *
 * TextureViewActivity.java
 * 
 * Created by Wuwang on 2017/3/4
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.aiya.mvc;

import android.Manifest;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.TextureView;

import com.aiyaapp.aiya.EffectSelectActivity;
import com.aiyaapp.aiya.R;
import com.aiyaapp.aiya.util.PermissionUtils;
import com.aiyaapp.camera.sdk.widget.AiyaController;
import com.aiyaapp.camera.sdk.widget.AiyaModel;
import com.aiyaapp.camera.sdk.base.Log;

/**
 * Description:
 */
public class TextureViewActivity extends EffectSelectActivity {

    private TextureView mTextureView;

    private AiyaModel mAiyaModel;
    private AiyaController mAiyaController;

    private Camera1Model mCamera1Model;
    private Camera2Model mCamera2Model;

    private SurfaceTexture mNowTexture;
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
            setContentView(R.layout.activity_textureview);
            initData();
            modelInit();
            mAiyaController=new AiyaController(TextureViewActivity.this);
            mAiyaModel=mCamera1Model;
            //mAiyaModel=mCamera2Model;
            mTextureView= (TextureView)findViewById(R.id.mTextureView);
            mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    mNowTexture=surface;
                    mWidth=width;
                    mHeight=height;
                    mAiyaController.surfaceCreated(surface);
                    mAiyaModel.attachToController(mAiyaController);
                    mAiyaController.surfaceChanged(width, height);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    Log.e("textureview","onSurfaceTextureSizeChanged---->");
                    mAiyaController.surfaceChanged(width, height);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    mNowTexture=null;
                    Log.e("textureview","onSurfaceTextureDestroyed---->");
                    mAiyaController.surfaceDestroyed();
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
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

}
