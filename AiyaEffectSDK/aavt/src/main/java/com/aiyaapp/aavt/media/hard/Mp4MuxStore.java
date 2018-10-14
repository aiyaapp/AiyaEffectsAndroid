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
package com.aiyaapp.aavt.media.hard;

import android.annotation.TargetApi;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;

import com.aiyaapp.aavt.media.av.AvException;

import java.io.IOException;

/**
 * Mp4MuxStore
 *
 * @author wuwang
 * @version v1.0 2017:10:28 17:48
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Mp4MuxStore implements IHardStore {

    private MediaMuxer mMuxer;
    private final Object LOCK=new Object();
    private String mPath;
    private int mAudioTrack=-1;
    private int mVideoTrack=-1;
    private boolean isMuxStart=false;
    private boolean waiAudio=true;

    public Mp4MuxStore(boolean waitAudio){
        this.waiAudio=waitAudio;
    }

    @Override
    public int addTrack(MediaFormat mediaFormat) {
        synchronized (LOCK){
            if(!isMuxStart){
                if(mAudioTrack==-1&&mVideoTrack==-1){
                    try {
                        mMuxer=new MediaMuxer(mPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(mMuxer!=null){
                    int ret=-1;
                    String mime=mediaFormat.getString(MediaFormat.KEY_MIME);
                    if(mime.startsWith("audio")){
                        ret = mMuxer.addTrack(mediaFormat);
                        mAudioTrack=ret;
                    }else if(mime.startsWith("video")){
                        ret = mMuxer.addTrack(mediaFormat);
                        mVideoTrack=ret;
                        if(mAudioTrack<0&&waiAudio){
                            try {
                                LOCK.wait(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        mMuxer.start();
                        isMuxStart=true;
                        Log.d("aiyaapp","add video track:"+ret);
                    }
                    return ret;
                }
            }
        }
        return -1;
    }

    @Override
    public int addData(int track, HardMediaData hardMediaData) {
        boolean canMux=isMuxStart&&(track==mAudioTrack||track==mVideoTrack);
        if(canMux){
            mMuxer.writeSampleData(track,hardMediaData.data,hardMediaData.info);
            return 0;
        }
        return -1;
    }

    @Override
    public void setOutputPath(String path) {
        this.mPath=path;
    }

    @Override
    public void close() throws AvException {
        synchronized (LOCK){
            try {
                if(isMuxStart){
                    isMuxStart=false;
                    mAudioTrack=-1;
                    mVideoTrack=-1;
                    mMuxer.stop();
                    mMuxer.release();
                    mMuxer=null;
                }
            }catch (IllegalStateException e){
                throw new AvException("close muxer failed!",e);
            }

        }
    }

}
