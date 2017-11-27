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
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.aiyaapp.aiya.DefaultEffectFlinger;
import com.aiyaapp.aiya.R;
import com.aiyaapp.aiya.panel.EffectController;
import com.aiyaapp.aiya.panel.EffectListener;
import com.wuwang.aavt.av.CameraRecorder2;
import com.wuwang.aavt.core.Renderer;
import com.wuwang.aavt.gl.GroupFilter;
import com.wuwang.aavt.utils.MatrixUtils;

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
        mRecord=new CameraRecorder2();
        SurfaceView surface= (SurfaceView) findViewById(R.id.mSurface);
        surface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mRecord.setSurface(holder.getSurface());
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mRecord.setPreviewSize(width, height);
                mRecord.startPreview();
                mRecord.open();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mRecord.stopPreview();
                mRecord.close();
            }
        });

        mContainer=findViewById(R.id.mEffectView);
        mFlinger=new DefaultEffectFlinger(getApplicationContext());
        mRecord.setRenderer(mFlinger);
        mEffectController=new EffectController(this,mContainer,mFlinger);
    }

    public void onEffect(View view){
        mEffectController.show();
    }

    public void onHidden(View view){
        mEffectController.hide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mFlinger!=null){
            mFlinger.release();
        }
    }
}
