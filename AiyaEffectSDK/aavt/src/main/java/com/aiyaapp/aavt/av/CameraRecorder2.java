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
package com.aiyaapp.aavt.av;

import com.aiyaapp.aavt.core.Renderer;
import com.aiyaapp.aavt.media.CameraProvider;
import com.aiyaapp.aavt.media.ITextureProvider;
import com.aiyaapp.aavt.media.SoundRecorder;
import com.aiyaapp.aavt.media.SurfaceEncoder;
import com.aiyaapp.aavt.media.SurfaceShower;
import com.aiyaapp.aavt.media.VideoSurfaceProcessor;
import com.aiyaapp.aavt.media.av.AvException;
import com.aiyaapp.aavt.media.hard.IHardStore;
import com.aiyaapp.aavt.media.hard.StrengthenMp4MuxStore;

/**
 * CameraRecorder2 相机预览及录制工具类
 *
 * @author wuwang
 * @version v1.0 2017:10:26 18:35
 */
public class CameraRecorder2 {

    private VideoSurfaceProcessor mTextureProcessor;
    private ITextureProvider mCameraProvider;
    private SurfaceShower mShower;
    private SurfaceEncoder mSurfaceStore;
    private IHardStore mMuxer;
    private SoundRecorder mSoundRecord;

    public CameraRecorder2() {
        //用于视频混流和存储
        mMuxer = new StrengthenMp4MuxStore(true);

        //用于预览图像
        mShower = new SurfaceShower();
        mShower.setOutputSize(720, 1280);

        //用于编码图像
        mSurfaceStore = new SurfaceEncoder();
        mSurfaceStore.setStore(mMuxer);
        //用于音频
        mSoundRecord = new SoundRecorder(mMuxer);
        //用于处理视频图像
        mTextureProcessor = new VideoSurfaceProcessor();
        mTextureProcessor.setTextureProvider(mCameraProvider = new CameraProvider());
        mTextureProcessor.addObserver(mShower);
        mTextureProcessor.addObserver(mSurfaceStore);
    }


    public void setRenderer(Renderer renderer) {
        mTextureProcessor.setRenderer(renderer);
    }


    /**
     * 设置预览对象，必须是{@link android.view.Surface}、{@link android.graphics.SurfaceTexture}或者
     * {@link android.view.TextureView}
     *
     * @param surface 预览对象
     */
    public void setSurface(Object surface) {
        mShower.setSurface(surface);
    }

    /**
     * 设置录制的输出路径
     *
     * @param path 输出路径
     */
    public void setOutputPath(String path) {
        mMuxer.setOutputPath(path);
    }

    /**
     * 设置预览大小
     *
     * @param width  预览区域宽度
     * @param height 预览区域高度
     */
    public void setPreviewSize(int width, int height) {
        mShower.setOutputSize(width, height);
    }

    /**
     * 打开数据源
     */
    public void open() {
        mTextureProcessor.start();
    }

    /**
     * 关闭数据源
     */
    public void close() {
        mTextureProcessor.stop();
        stopRecord();
    }

    /**
     * 打开预览
     */
    public void startPreview() {
        mShower.open();
    }

    /**
     * 关闭预览
     */
    public void stopPreview() {
        mShower.close();
    }

    /**
     * 开始录制
     */
    public void startRecord() {
        mSurfaceStore.open();
        mSoundRecord.start();
    }


    /**
     * 拍照
     */
    public void takePictureListener(SurfaceShower.OnDrawEndListener listener) {
        if (listener == null) return;
        mShower.setOnDrawEndListener(listener);

    }


    public void swithCamera() {
        mCameraProvider.swithCamera();
    }


    /**
     * 关闭录制
     */
    public void stopRecord() {
        mSoundRecord.stop();
        mSurfaceStore.close();
        try {
            mMuxer.close();
        } catch (AvException e) {
            e.printStackTrace();
        }
    }

}
