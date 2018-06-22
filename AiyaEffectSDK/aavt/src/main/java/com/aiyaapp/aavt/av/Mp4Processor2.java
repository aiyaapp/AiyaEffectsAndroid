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

import com.aiyaapp.aavt.media.Mp4Provider;
import com.aiyaapp.aavt.media.SurfaceEncoder;
import com.aiyaapp.aavt.media.SurfaceShower;
import com.aiyaapp.aavt.media.VideoSurfaceProcessor;
import com.aiyaapp.aavt.media.hard.IHardStore;
import com.aiyaapp.aavt.media.hard.StrengthenMp4MuxStore;

/**
 * Mp4Processor2 用于处理Mp4文件
 *
 * @author wuwang
 * @version v1.0 2017:10:26 18:48
 */
public class Mp4Processor2 {

    private VideoSurfaceProcessor mTextureProcessor;
    private Mp4Provider mMp4Provider;
    private SurfaceShower mShower;
    private SurfaceEncoder mSurfaceStore;
    private IHardStore mMuxer;

    public Mp4Processor2(){
        //用于视频混流和存储
        mMuxer=new StrengthenMp4MuxStore(true);

        //用于预览图像
        mShower=new SurfaceShower();
        mShower.setOutputSize(720,1280);

        //用于编码图像
        mSurfaceStore=new SurfaceEncoder();
        mSurfaceStore.setStore(mMuxer);

        //用于音频
//        mSoundRecord=new SoundRecorder(mMuxer);
        mMp4Provider=new Mp4Provider();
        mMp4Provider.setStore(mMuxer);

        //用于处理视频图像
        mTextureProcessor=new VideoSurfaceProcessor();
        mTextureProcessor.setTextureProvider(mMp4Provider);
        mTextureProcessor.addObserver(mShower);
        mTextureProcessor.addObserver(mSurfaceStore);
    }

    public void setSurface(Object surface){
        mShower.setSurface(surface);
    }

    public void setInputPath(String path){
        mMp4Provider.setInputPath(path);
    }

    public void setOutputPath(String path){
        mMuxer.setOutputPath(path);
    }

    public void setPreviewSize(int width,int height){
        mShower.setOutputSize(width,height);
    }

    public void open(){
        mTextureProcessor.start();
    }

    public void close(){
        mTextureProcessor.stop();
    }

    public void startPreview(){
        mShower.open();
    }

    public void stopPreview(){
        mShower.close();
    }

    public void startRecord(){
        mSurfaceStore.open();
//        mSoundRecord.start();
    }

    public void stopRecord(){
        mSurfaceStore.close();
//        mSoundRecord.stop();
    }

}
