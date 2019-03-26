package com.aiyaapp.aiya;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.aiyaapp.aiya.gift.GiftActivity;
import com.aiyaapp.aiya.panel.PermissionUtils;

import java.io.File;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "aiyaapp";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionUtils.askPermission(this, new String[]{ Manifest.permission.CAMERA }, 10001, start);
    }

    private Runnable start = new Runnable() {
        @Override
        public void run() {
            setContentView(R.layout.activity_main);
            AiyaEffects.setEventListener(new IEventListener() {
                @Override
                public int onEvent(int i, int i1, String s) {
                    Log.d(TAG, "MSG(type/ret/info):" + i + "/" + i1 + "/" + s);
                    return 0;
                }
            });

            AyCore.initLicense(getApplicationContext(), "477de67d19ba39fb656a4806c803b552", new AyCore.OnResultCallback() {
                @Override
                public void onResult(int ret) {
                    Log.d("wangyang", "License初始化结果 : " + ret);
                }
            });

//            int id = AiyaEffects.init(getApplicationContext(), "477de67d19ba39fb656a4806c803b552");
//            Log.d(TAG, "id:" + id);
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(requestCode == 10001, grantResults, start, new Runnable() {
            @Override
            public void run() {
                finish();
                Toast.makeText(MainActivity.this, "必要的权限未被允许", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mBtnGift:
                startActivity(new Intent(this, GiftActivity.class));
                break;
            default:
                startActivity(new Intent(this, CameraActivity.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
