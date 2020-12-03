package com.aiyaapp.aiya;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        findViewById(R.id.main_camera).setOnClickListener(this);
        findViewById(R.id.main_animation).setOnClickListener(this);
        findViewById(R.id.main_recorder).setOnClickListener(this);

        // 初始化License
        AYLicenseManager.initLicense(getApplicationContext(), "477de67d19ba39fb656a4806c803b552", ret -> Log.d("哎吖科技", "License初始化结果 : " + ret));

        // copy数据
        new Thread(() -> {
            String dstPath = getCacheDir().getPath() + File.separator + "effect";
            if (!new File(dstPath).exists()) {
                AyFaceTrack.deleteFile(new File(dstPath));
                AyFaceTrack.copyFileFromAssets("effect", dstPath, getAssets());
            }
            dstPath = getCacheDir().getPath() + File.separator + "style";
            if (!new File(dstPath).exists()) {
                AyFaceTrack.deleteFile(new File(dstPath));
                AyFaceTrack.copyFileFromAssets("style", dstPath, getAssets());
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

            enterCameraNextPage();

        } else if (requestCode == 1002) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

            enterRecorderPage();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.main_camera) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 1001);
                } else {
                    enterCameraNextPage();
                }
            } else {
                enterCameraNextPage();
            }

        } else if (view.getId() == R.id.main_animation) {
            enterAnimationPage();

        } else if (view.getId() == R.id.main_recorder) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 1002);
                } else {
                    enterRecorderPage();
                }
            } else {
                enterRecorderPage();
            }

        }

    }

    private void enterCameraNextPage() {
        startActivity(new Intent(this, CameraActivity.class));
    }

    private void enterAnimationPage() {
        startActivity(new Intent(this, AnimationActivity.class));
    }

    private void enterRecorderPage() {
        startActivity(new Intent(this, RecorderActivity.class));
    }
}
