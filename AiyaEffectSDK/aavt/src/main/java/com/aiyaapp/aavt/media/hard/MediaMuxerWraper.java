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
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.aiyaapp.aavt.log.AvLog;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * MediaMuxerWraper
 *
 * @author wuwang
 * @version v1.0 2017:11:08 11:07
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MediaMuxerWraper{

    private final String tag=getClass().getSimpleName();
    private MediaMuxer mMuxer;
    private BlockingQueue<HardMediaData> datas;
    private Recycler<HardMediaData> recycler;
    private boolean isStarted=false;
    private int indexCount=0;
    private final Object Lock=new Object();
    private ExecutorService mExec;

    public MediaMuxerWraper(String path, int format) throws IOException {
        mMuxer=new MediaMuxer(path,format);
        datas=new LinkedBlockingQueue<>(30);
        recycler=new Recycler<>();
        ThreadFactory factory= Executors.defaultThreadFactory();
        mExec=new ThreadPoolExecutor(1,1,1,TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>(16),factory);
    }

    private void muxRun(){
        while (isStarted){
            try {
                HardMediaData data=datas.poll(1, TimeUnit.SECONDS);
                AvLog.d(tag,"get HardMediaData from the queue");
                synchronized (Lock){
                    if(isStarted){
                        mMuxer.writeSampleData(data.index, data.data, data.info);
                        recycler.put(data.index,data);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        AvLog.d(tag,"end -->");
        mMuxer.stop();
        mMuxer.release();
        datas.clear();
        recycler.clear();
    }

    public void release(){
        stop();
    }

    public void start(){
        synchronized (Lock){
            if(!isStarted){
                mMuxer.start();
                isStarted=true;
                mExec.execute(new Runnable() {
                    @Override
                    public void run() {
                        muxRun();
                    }
                });
            }
        }
    }

    public void stop(){
        synchronized (Lock){
            indexCount=0;
            isStarted=false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setLocation(float latitude, float longitude){
        mMuxer.setLocation(latitude, longitude);
    }

    public void writeSampleData(int trackIndex, @NonNull ByteBuffer byteBuf,
                                @NonNull MediaCodec.BufferInfo bufferInfo){
        HardMediaData hmd=recycler.poll(trackIndex);
        if(hmd==null){
            ByteBuffer buffer=ByteBuffer.allocate(byteBuf.capacity());
            MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();
            hmd=new HardMediaData(buffer,info);
            AvLog.d(tag,"buffer Size->"+buffer.capacity());
        }
        AvLog.i(tag,"buffer Size->"+hmd.data.capacity()+"/data size:"+bufferInfo.size);
        hmd.data.position(0);
        hmd.data.put(byteBuf);
        hmd.info.set(bufferInfo.offset,bufferInfo.size,bufferInfo.presentationTimeUs,bufferInfo.flags);
        hmd.index=trackIndex;
        datas.offer(hmd);
    }

    public int addTrack(@NonNull MediaFormat format){
        indexCount++;
        AvLog.d(tag,"addTrack  -->"+indexCount);
        return mMuxer.addTrack(format);
    }

    public void setOrientationHint(int degrees){
        mMuxer.setOrientationHint(degrees);
    }

}
