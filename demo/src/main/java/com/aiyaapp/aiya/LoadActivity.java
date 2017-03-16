/*
 *
 * LoadActivity.java
 * 
 * Created by Wuwang on 2016/11/25
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.aiya;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.aiyaapp.aiya.camera.CameraActivity;
import com.aiyaapp.aiya.mvc.SurfaceHolderActivity;
import com.aiyaapp.aiya.mvc.TextureViewActivity;
import com.aiyaapp.camera.sdk.AiyaEffects;
import com.aiyaapp.camera.sdk.base.Log;
import com.aiyaapp.camera.sdk.base.State;
import com.aiyaapp.camera.sdk.base.StateObserver;

/**
 * Description:
 */
public class LoadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final StateObserver observer=new StateObserver() {
            @Override
            public void onStateChange(State state) {
                if(state==State.RESOURCE_FAILED){
                    Log.e("resource failed");
                }else if(state==State.RESOURCE_READY){
                    Log.e("resource ready");
                }else if(state==State.INIT_FAILED){
                    Log.e("init failed");
                    Toast.makeText(LoadActivity.this, "注册失败，请检查网络", Toast.LENGTH_SHORT)
                        .show();
                    AiyaEffects.getInstance().unRegisterObserver(this);
                }else if(state==State.INIT_SUCCESS){
                    Log.e("init success");
                    setContentView(R.layout.activity_load);
                    AiyaEffects.getInstance().unRegisterObserver(this);
                }
            }
        };
        AiyaEffects.getInstance().registerObserver(observer);
        AiyaEffects.getInstance().init(LoadActivity.this,getExternalFilesDir(null)
            .getAbsolutePath()+"/146-563-918-415-578-677-783-748-043-705-956.vlc","");
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
