package com.aiyaapp.aavt.media;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.view.Surface;

import com.aiyaapp.aavt.log.AvLog;
import com.aiyaapp.aavt.media.av.AvException;
import com.aiyaapp.aavt.media.hard.HardMediaData;
import com.aiyaapp.aavt.media.hard.IHardStore;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

/**
 * @author wuwang
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class Mp4Provider implements ITextureProvider {

    private final String tag=getClass().getSimpleName();
    private String mPath;
    private MediaExtractor mExtractor;
    private MediaCodec mVideoDecoder;
    private int mVideoDecodeTrack=-1;
    private int mAudioDecodeTrack=-1;
    private Point mVideoSize=new Point();
    private Semaphore mFrameSem;
    private static final int TIME_OUT=1000;
    private final Object Extractor_LOCK=new Object();
    private long mVideoStopTimeStamp;
    private boolean isVideoExtractorEnd=false;
    private boolean isUserWantToStop=false;
    private Semaphore mDecodeSem;

    private boolean videoProvideEndFlag=false;

    private IHardStore mStore;

    private long nowTimeStamp=-1;
    private MediaCodec.BufferInfo videoDecodeBufferInfo=new MediaCodec.BufferInfo();
    private int mAudioEncodeTrack=-1;
    private long mVideoTotalTime=-1;

    public Mp4Provider(){

    }

    public void setInputPath(String path){
        this.mPath=path;
    }

    private boolean extractMedia(){
        if(mPath==null||!new File(mPath).exists()){
            //文件不存在
            return false;
        }
        try {
            MediaMetadataRetriever mMetRet=new MediaMetadataRetriever();
            mMetRet.setDataSource(mPath);
            mVideoTotalTime=Long.valueOf(mMetRet.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            mExtractor=new MediaExtractor();
            mExtractor.setDataSource(mPath);
            int trackCount=mExtractor.getTrackCount();
            for (int i=0;i<trackCount;i++){
                MediaFormat format=mExtractor.getTrackFormat(i);
                String mime=format.getString(MediaFormat.KEY_MIME);
                if(mime.startsWith("audio")){
                    mAudioDecodeTrack=i;
                }else if(mime.startsWith("video")){
                    mVideoDecodeTrack=i;
                    int videoRotation=0;
                    String rotation=mMetRet.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                    if(rotation!=null){
                        videoRotation=Integer.valueOf(rotation);
                    }
                    if(videoRotation%180!=0){
                        mVideoSize.y=format.getInteger(MediaFormat.KEY_WIDTH);
                        mVideoSize.x=format.getInteger(MediaFormat.KEY_HEIGHT);
                    }else{
                        mVideoSize.x=format.getInteger(MediaFormat.KEY_WIDTH);
                        mVideoSize.y=format.getInteger(MediaFormat.KEY_HEIGHT);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void setStore(IHardStore store){
        this.mStore=store;
    }

    private boolean videoDecodeStep(){
        int mInputIndex=mVideoDecoder.dequeueInputBuffer(TIME_OUT);
        if(mInputIndex>=0){
            ByteBuffer buffer= CodecUtil.getInputBuffer(mVideoDecoder,mInputIndex);
            buffer.clear();
            synchronized (Extractor_LOCK) {
                mExtractor.selectTrack(mVideoDecodeTrack);
                int ret = mExtractor.readSampleData(buffer, 0);
                if (ret != -1) {
                    mVideoStopTimeStamp=mExtractor.getSampleTime();
                    mVideoDecoder.queueInputBuffer(mInputIndex, 0, ret, mVideoStopTimeStamp, mExtractor.getSampleFlags());
                    isVideoExtractorEnd = false;
                }else{
                    //可以用!mExtractor.advance，但是貌似会延迟一帧。readSampleData 返回 -1 也表示没有更多数据了
                    isVideoExtractorEnd = true;
                }
                mExtractor.advance();
            }
        }
        while (true){
            int mOutputIndex=mVideoDecoder.dequeueOutputBuffer(videoDecodeBufferInfo,TIME_OUT);
            if(mOutputIndex>=0){
                try {
                    if(!isUserWantToStop){
                        mDecodeSem.acquire();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                nowTimeStamp=videoDecodeBufferInfo.presentationTimeUs;
                mVideoDecoder.releaseOutputBuffer(mOutputIndex,true);
                mFrameSem.release();
            }else if(mOutputIndex== MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){

            }else if(mOutputIndex== MediaCodec.INFO_TRY_AGAIN_LATER){
                break;
            }
        }
        return isVideoExtractorEnd||isUserWantToStop;
    }

    public long getMediaDuration(){
        return mVideoTotalTime;
    }

    private void startDecodeThread(){
        Thread mDecodeThread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (!videoDecodeStep()){}
                if(videoDecodeBufferInfo.flags!=MediaCodec.BUFFER_FLAG_END_OF_STREAM){
                    AvLog.d(tag,"video ------------------ end");
                    videoProvideEndFlag=true;
//                    try {
//                        mDecodeSem.acquire();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    //释放最后一帧的信号
                    videoDecodeBufferInfo.flags=MediaCodec.BUFFER_FLAG_END_OF_STREAM;
                    mFrameSem.release();
                }
                mVideoDecoder.stop();
                mVideoDecoder.release();
                mVideoDecoder=null;
                AvLog.d(tag,"audioStart");
                audioDecodeStep();
                AvLog.d(tag,"audioStop");
                mExtractor.release();
                mExtractor=null;
                try {
                    mStore.close();
                } catch (AvException e) {
                    e.printStackTrace();
                }
            }
        });
        mDecodeThread.start();
    }

    private boolean isOpenAudio=true;
    private boolean audioDecodeStep(){
        ByteBuffer buffer=ByteBuffer.allocate(1024*64);
        boolean isTimeEnd=false;
        if(isOpenAudio){
            buffer.clear();
            mExtractor.selectTrack(mAudioDecodeTrack);
            MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();
            while (true){
                int length=mExtractor.readSampleData(buffer,0);
                if(length!=-1){
                    int flags=mExtractor.getSampleFlags();
                    boolean isAudioEnd=mExtractor.getSampleTime()>mVideoStopTimeStamp;
                    info.size=length;
                    info.flags=isAudioEnd?MediaCodec.BUFFER_FLAG_END_OF_STREAM:flags;
                    info.presentationTimeUs=mExtractor.getSampleTime();
                    info.offset=0;
                    AvLog.d(tag,"audio sampleTime= "+info.presentationTimeUs+"/"+mVideoStopTimeStamp);
                    isTimeEnd=mExtractor.getSampleTime()>mVideoStopTimeStamp;
                    AvLog.d(tag,"is End= "+isAudioEnd );
                    mStore.addData(mAudioEncodeTrack,new HardMediaData(buffer,info));
                    if(isAudioEnd){
                        break;
                    }
                }else{
                    AvLog.d(tag,"is End= "+true );
                    info.size=0;
                    info.flags=MediaCodec.BUFFER_FLAG_END_OF_STREAM;
                    mStore.addData(mAudioEncodeTrack,new HardMediaData(buffer,info));
                    isTimeEnd=true;
                    break;
                }
                mExtractor.advance();
            }
        }
        return isTimeEnd;
    }

    @Override
    public Point open(SurfaceTexture surface) {
        try {
            if(!extractMedia()){
                return new Point(0,0);
            }
            mFrameSem=new Semaphore(0);
            mDecodeSem=new Semaphore(1);
            videoProvideEndFlag=false;
            isUserWantToStop=false;
            mAudioEncodeTrack=mStore.addTrack(mExtractor.getTrackFormat(mAudioDecodeTrack));
            MediaFormat format=mExtractor.getTrackFormat(mVideoDecodeTrack);
            mVideoDecoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));
            mVideoDecoder.configure(format,new Surface(surface),null,0);
            mVideoDecoder.start();
            startDecodeThread();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mVideoSize;
    }

    @Override
    public void close() {
        isUserWantToStop=true;
    }


    @Override
    public void swithCamera() {

    }

    @Override
    public boolean frame() {
        try {
            mFrameSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mDecodeSem.release();
        return videoProvideEndFlag;
    }

    @Override
    public long getTimeStamp() {
        return nowTimeStamp;
    }

    @Override
    public boolean isLandscape() {
        return false;
    }

}
