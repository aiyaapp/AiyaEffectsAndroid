/*
 *
 * LoadActivity.java
 * 
 * Created by Wuwang on 2016/11/25
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.aiya;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.aiyaapp.aiya.camera.CameraActivity;
import com.aiyaapp.aiya.mvc.SurfaceHolderActivity;
import com.aiyaapp.aiya.mvc.TextureViewActivity;
import com.aiyaapp.aiya.util.PermissionUtils;
import com.aiyaapp.camera.sdk.AiyaEffects;
import com.aiyaapp.camera.sdk.base.Log;
import com.aiyaapp.camera.sdk.base.Event;
import com.aiyaapp.camera.sdk.base.ActionObserver;
import com.aiyaapp.camera.sdk.widget.CameraView;

/**
 * Description:
 */
public class LoadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionUtils.askPermission(this,new String[]{
            Manifest.permission.CAMERA,Manifest
            .permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE},10,mRunnable);
    }

    private Runnable mRunnable=new Runnable() {
        @Override
        public void run() {

            final ActionObserver observer=new ActionObserver() {
                @Override
                public void onAction(Event event) {
                    if(event.eventType== Event.RESOURCE_FAILED){
                        Log.e("resource failed");
                        AiyaEffects.getInstance().unRegisterObserver(this);
                    }else if(event.eventType== Event.RESOURCE_READY){
                        Log.e("resource ready");
                    }else if(event.eventType== Event.INIT_FAILED){
                        Log.e("init failed");
                        Toast.makeText(LoadActivity.this, "注册失败，请检查网络", Toast.LENGTH_SHORT)
                            .show();
                        AiyaEffects.getInstance().unRegisterObserver(this);
                    }else if(event.eventType== Event.INIT_SUCCESS){
                        Log.e("init success");
                        setContentView(R.layout.activity_load);
                        AiyaEffects.getInstance().unRegisterObserver(this);
                    }
                }
            };
            AiyaEffects.getInstance().registerObserver(observer);
            AiyaEffects.getInstance().init(LoadActivity.this,getExternalFilesDir(null)
                .getAbsolutePath()+"/config","");

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(requestCode == 10, grantResults, mRunnable, new Runnable() {
            @Override
            public void run() {
                finish();
                Toast.makeText(LoadActivity.this,"必要的权限未被允许",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.mCamera:
                startActivity(new Intent(this, CameraActivity.class));
                break;
            case R.id.mTexture:
                startActivity(new Intent(this, TextureViewActivity.class));
                break;
            case R.id.mHolder:
                startActivity(new Intent(this, SurfaceHolderActivity.class));
                break;
        }
    }
}
