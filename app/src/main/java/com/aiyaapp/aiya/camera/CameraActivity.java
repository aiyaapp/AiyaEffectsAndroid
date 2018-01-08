/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aiyaapp.aiya.camera;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.aiyaapp.aavt.av.CameraRecorder2;
import com.aiyaapp.aiya.DefaultEffectFlinger;
import com.aiyaapp.aiya.R;
import com.aiyaapp.aiya.panel.EffectController;

/**
 * CameraActivity
 *
 * @author wuwang
 * @version v1.0 2017:11:09 09:31
 */
public class CameraActivity extends AppCompatActivity {

    private EffectController mEffectController;
    private DefaultEffectFlinger mFlinger;
    private View mContainer;
    private CameraRecorder2 mRecord;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        mRecord = new CameraRecorder2();
        mRecord.setOutputPath(tempPath);
        SurfaceView surface = (SurfaceView) findViewById(R.id.mSurface);
        surface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mRecord.open();
                mRecord.setSurface(holder.getSurface());
                mRecord.setPreviewSize(width, height);
                mRecord.startPreview();
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mRecord.stopPreview();
                mRecord.close();
            }
        });
        mContainer = findViewById(R.id.mEffectView);
        mFlinger = new DefaultEffectFlinger(getApplicationContext());
        mRecord.setRenderer(mFlinger);
        mEffectController = new EffectController(this, mContainer, mFlinger);

        findViewById(R.id.mShutter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecordOpen) { //停止拍摄
                    v.setSelected(false);
                    mRecord.stopRecord();
                    Toast.makeText(CameraActivity.this, "视频保存成功：" + tempPath, Toast.LENGTH_LONG).show();
                } else {
                    v.setSelected(true); //开始拍摄
                    mRecord.startRecord();
                    Toast.makeText(CameraActivity.this, "开始拍摄", Toast.LENGTH_SHORT).show();
                }
                isRecordOpen = !isRecordOpen;
            }
        });
    }

    private boolean isRecordOpen = false;

    private String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp4";


    public void onEffect(View view) {
        mEffectController.show();
    }

    public void onHidden(View view) {
        mEffectController.hide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFlinger != null) {
            mFlinger.release();
        }
    }
}
