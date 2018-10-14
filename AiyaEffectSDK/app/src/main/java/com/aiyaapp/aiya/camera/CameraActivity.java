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

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.EGLSurface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.aiyaapp.aavt.av.CameraRecorder2;
import com.aiyaapp.aavt.gl.YuvOutputFilter;
import com.aiyaapp.aavt.media.RenderBean;
import com.aiyaapp.aavt.media.SurfaceShower;
import com.aiyaapp.aiya.DefaultEffectFlinger;
import com.aiyaapp.aiya.R;
import com.aiyaapp.aiya.panel.EffectController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp4";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

        initRecordView();

        findViewById(R.id.mIbFlip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecord.swithCamera();
            }
        });
    }

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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("wuwang", "onActivityResult:rq:" + requestCode + "/" + resultCode);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                Log.d("aiyaapp", "data:" + getRealFilePath(data.getData()));
                String dataPath = getRealFilePath(data.getData());
                if (dataPath != null && dataPath.endsWith(".json")) {
                    mFlinger.setEffect(dataPath);
                }
            }
        }
    }

    public String getRealFilePath(final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 拍照和录制
     */
    private CircularProgressView mCapture;
    private long maxTime = 30000;
    private long time;
    private int type = 0;     //0为录像，1为拍照
    private void initRecordView() {
        mExecutor = Executors.newSingleThreadExecutor();
        mCapture = (CircularProgressView) findViewById(R.id.mCapture);
        mCapture.setTotal((int) maxTime);
        mCapture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        recordFlag = false;
                        time = System.currentTimeMillis();
                        mCapture.postDelayed(captureTouchRunnable, 500);
                        break;
                    case MotionEvent.ACTION_UP:
                        recordFlag = false;
                        if (System.currentTimeMillis() - time < 500) {
                            mCapture.removeCallbacks(captureTouchRunnable);
                            if (System.currentTimeMillis() - time < 500) {
                                mCapture.removeCallbacks(captureTouchRunnable);
                                type = 1;
                                exportFlag = true;
                            }
                        }
                        break;
                }
                return false;
            }
        });



        mRecord.takePictureListener(new SurfaceShower.OnDrawEndListener() {
            @Override
            public void onDrawEnd(EGLSurface surface, final RenderBean bean) {
                if (exportFlag) {
                    mOutputFilter = new YuvOutputFilter(YuvOutputFilter.EXPORT_TYPE_NV21);
                    mOutputFilter.create();
                    mOutputFilter.sizeChanged(picX, picY);
                    mOutputFilter.setInputTextureSize(bean.sourceWidth, bean.sourceHeight);
                    tempBuffer = new byte[picX * picY * 3 / 2];

                    mOutputFilter.drawToTexture(bean.textureId);
                    mOutputFilter.getOutput(tempBuffer, 0, picX * picY * 3 / 2);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mBitmap != null) {
                                mBitmap.recycle();
                                mBitmap = null;
                            }
                            mBitmap = rawByteArray2RGBABitmap2(tempBuffer, picX, picY);
                            //保存图片
                            recordComplete(type, "", mBitmap);

                        }
                    });
                    exportFlag = false;
                }
            }
        });

    }

    /**
     * 拍照
     */
    private YuvOutputFilter mOutputFilter;
    private byte[] tempBuffer;
    private boolean exportFlag = false;
    private Bitmap mBitmap;
    private int picX = 720;
    private int picY = 1280;
    private ExecutorService mExecutor;

    public Bitmap rawByteArray2RGBABitmap2(byte[] data, int width, int height) {
        int frameSize = width * height;
        int[] rgba = new int[frameSize];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) data[i * width + j]));
                int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;
                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));
                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);
                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }
        }
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0, width, 0, 0, width, height);
        return bmp;
    }


    private boolean recordFlag;
    //录像的Runnable, 用于更新进度条和处理录制流程
    private Runnable captureTouchRunnable = new Runnable() {
        @Override
        public void run() {
            recordFlag = true;
            mExecutor.execute(recordRunnable);
        }
    };
    private long timeStep = 50;
    private Runnable recordRunnable = new Runnable() {

        @Override
        public void run() {
            type = 0;
            long timeCount = 0;
            try {
                mRecord.startRecord();
                while (timeCount <= maxTime && recordFlag) {
                    long start = System.currentTimeMillis();
                    mCapture.setProcess((int) timeCount);
                    long end = System.currentTimeMillis();
                    try {
                        Thread.sleep(timeStep - (end - start));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    timeCount += timeStep;
                }
                mRecord.stopRecord();
                if (timeCount < 1000) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCapture.setProcess(0);
                            mCapture.removeCallbacks(captureTouchRunnable);
                            type = 1;
                            exportFlag = true;
                        }
                    });
                } else {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recordComplete(type, tempPath, null);
                        }
                    }, 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };




    /**
     * 录制结束
     *
     * @param type
     * @param path
     */
    private void recordComplete(int type, String path, Bitmap bitmap) {
        mCapture.setProcess(0);
        //打开相册或图库
        if (type == 0) {//视频
            Intent v = new Intent(Intent.ACTION_VIEW);
            v.setDataAndType(Uri.parse(path), "video/mp4");
            if (v.resolveActivity(getPackageManager()) != null) {
                startActivity(v);
            } else {
                Toast.makeText(this,
                        "无法找到默认媒体软件打开:" + tempPath, Toast.LENGTH_SHORT).show();
            }
        } else {//打开图库
            Intent intent = new Intent(Intent.ACTION_VIEW);    //打开图片得启动ACTION_VIEW意图
            Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null));    //将bitmap转换为uri
            intent.setDataAndType(uri, "image/*");    //设置intent数据和图片格式
            startActivity(intent);

        }
    }


}