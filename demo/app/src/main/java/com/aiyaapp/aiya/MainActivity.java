package com.aiyaapp.aiya;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button cameraBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        cameraBtn = findViewById(R.id.main_camera);
        cameraBtn.setOnClickListener(this);

        // 初始化License
        AYLicenseManager.initLicense(getApplicationContext(), "477de67d19ba39fb656a4806c803b552", new AyCore.OnResultCallback() {
            @Override
            public void onResult(int ret) {
                Log.d("哎吖科技", "License初始化结果 : " + ret);
            }
        });

        // copy数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                String dstPath = getExternalCacheDir() + "/aiya/effect";
                if (!new File(dstPath).exists()) {
                    AyFaceTrack.deleteFile(new File(dstPath));
                    AyFaceTrack.copyFileFromAssets("modelsticker", dstPath, getAssets());
                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

            enterNextPage();
        }
    }

    @Override
    public void onClick(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1001);
            } else {
                enterNextPage();
            }
        } else {
            enterNextPage();
        }
    }

    private void enterNextPage() {
        startActivity(new Intent(this, CameraActivity.class));
    }
}
