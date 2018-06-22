package com.aiyaapp.aavt.av;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Bundle;
import android.view.Surface;

import com.aiyaapp.aavt.core.Renderer;
import com.aiyaapp.aavt.egl.EGLConfigAttrs;
import com.aiyaapp.aavt.egl.EGLContextAttrs;
import com.aiyaapp.aavt.egl.EglHelper;
import com.aiyaapp.aavt.log.AvLog;
import com.aiyaapp.aavt.media.WrapRenderer;
import com.aiyaapp.aavt.utils.GpuUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;


/**
 * MP4处理工具，暂时只用于处理图像。
 * 4.4的手机不支持video/mp4v-es格式的视频流，MediaMuxer混合无法stop，5.0以上可以
 *
 */
@Deprecated
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Mp4Processor {

    private final int TIME_OUT=1000;

    private String mInputPath;                  //输入路径
    private String mOutputPath;                 //输出路径

    private MediaCodec mVideoDecoder;           //视频解码器
    private MediaCodec mVideoEncoder;           //视频编码器
    //private MediaCodec mAudioDecoder;           //音频解码器
    //private MediaCodec mAudioEncoder;           //音频编码器
    private MediaExtractor mExtractor;          //音视频分离器
    private MediaMuxer mMuxer;                  //音视频混合器
    private EglHelper mEGLHelper;               //GL环境创建的帮助类
    private MediaCodec.BufferInfo mVideoDecoderBufferInfo;  //用于存储当前帧的视频解码信息
    //private MediaCodec.BufferInfo mAudioDecoderBufferInfo;  //用于存储当前帧的音频解码信息
    private MediaCodec.BufferInfo mVideoEncoderBufferInfo;  //用于存储当前帧的视频编码信息
    private MediaCodec.BufferInfo mAudioEncoderBufferInfo;  //用于纯粹当前帧的音频编码信息

    private int mAudioEncoderTrack=-1;     //解码音轨
    private int mVideoEncoderTrack=-1;     //解码视轨
    private int mAudioDecoderTrack=-1;     //编码音轨
    private int mVideoDecoderTrack=-1;     //编码视轨

    //private String mAudioMime;
    //private String mVideoMime;

    private int mInputVideoWidth=0;     //输入视频的宽度
    private int mInputVideoHeight=0;    //输入视频的高度

    private int mOutputVideoWidth=0;    //输出视频的宽度
    private int mOutputVideoHeight=0;   //输出视频的高度
    private int mVideoTextureId;        //原始视频图像的纹理
    private SurfaceTexture mVideoSurfaceTexture;    //用于接收原始视频的解码的图像流

    private boolean isRenderToWindowSurface;        //是否渲染到用户设置的WindowBuffer上，用于测试
    private Surface mOutputSurface;                 //视频输出的Surface

    private Thread mDecodeThread;
    private Thread mGLThread;
    private boolean mCodecFlag=false;
    private boolean isVideoExtractorEnd=false;
    private boolean isAudioExtractorEnd=false;
    private boolean isStarted=false;
    private WrapRenderer mRenderer;
    private boolean mGLThreadFlag=false;
    private Semaphore mSem;
    private Semaphore mDecodeSem;

    private final Object Extractor_LOCK=new Object();
    private final Object MUX_LOCK=new Object();
    private final Object PROCESS_LOCK=new Object();

    private OnProgressListener mProgressListener;

    private boolean isUserWantToStop=false;
    private long mVideoStopTimeStamp=0;     //视频停止时的时间戳，用于外部主动停止处理时，音频截取

    private long mTotalVideoTime=0;     //视频的总时长

    public Mp4Processor(){
        mEGLHelper=new EglHelper();
        mVideoDecoderBufferInfo=new MediaCodec.BufferInfo();
        //mAudioDecoderBufferInfo=new MediaCodec.BufferInfo();
        mVideoEncoderBufferInfo=new MediaCodec.BufferInfo();
        mAudioEncoderBufferInfo=new MediaCodec.BufferInfo();
    }


    /**
     * 设置用于处理的MP4文件
     * @param path 文件路径
     */
    public void setInputPath(String path){
        this.mInputPath=path;
    }

    /**
     * 设置处理后的mp4存储的位置
     * @param path 文件路径
     */
    public void setOutputPath(String path){
        this.mOutputPath=path;
    }

    /**
     * 设置直接渲染到指定的Surface上,测试用
     * @param surface 渲染的位置
     */
    public void setOutputSurface(Surface surface){
        this.mOutputSurface=surface;
        this.isRenderToWindowSurface=surface!=null;
    }

    /**
     * 设置用户处理接口
     * @param renderer 处理接口
     */
    public void setRenderer(Renderer renderer){
        mRenderer=new WrapRenderer(renderer);
    }

    public int getVideoSurfaceTextureId(){
        return mVideoTextureId;
    }

    public SurfaceTexture getVideoSurfaceTexture(){
        return mVideoSurfaceTexture;
    }

    /**
     * 设置输出Mp4的图像大小，默认为输出大小
     * @param width 视频图像宽度
     * @param height 视频图像高度
     */
    public void setOutputSize(int width,int height){
        this.mOutputVideoWidth=width;
        this.mOutputVideoHeight=height;
    }

    public void setOnCompleteListener(OnProgressListener listener){
        this.mProgressListener=listener;
    }

    private boolean prepare() throws IOException {
        //todo 获取视频旋转信息，并做出相应处理
        synchronized (PROCESS_LOCK){
            int videoRotation=0;
            MediaMetadataRetriever mMetRet=new MediaMetadataRetriever();
            mMetRet.setDataSource(mInputPath);
            mExtractor=new MediaExtractor();
            mExtractor.setDataSource(mInputPath);
            int count=mExtractor.getTrackCount();
            //解析Mp4
            for (int i=0;i<count;i++){
                MediaFormat format=mExtractor.getTrackFormat(i);
                String mime=format.getString(MediaFormat.KEY_MIME);
                AvLog.d("extractor format-->"+mExtractor.getTrackFormat(i));
                if(mime.startsWith("audio")){
                    mAudioDecoderTrack=i;
                    //todo 暂时不对音频处理，后续需要对音频处理时再修改这个
                    /*mAudioDecoder=MediaCodec.createDecoderByType(mime);
                    mAudioDecoder.configure(format,null,null,0);
                    if(!isRenderToWindowSurface){
                        Log.e("wuwang", format.toString());
                        MediaFormat audioFormat=MediaFormat.createAudioFormat(mime,
                                format.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                                format.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
                        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                                format.getInteger(MediaFormat.KEY_AAC_PROFILE));
                        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE,
                                Integer.valueOf(mMetRet.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)));
                        mAudioEncoder=MediaCodec.createEncoderByType(mime);
                        mAudioEncoder.configure(audioFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
                    }*/
                }else if(mime.startsWith("video")){
                    //5.0以下，不能解析mp4v-es //todo 5.0以上也可能存在问题，目前还不知道原因
//                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP&&mime.equals(MediaFormat.MIMETYPE_VIDEO_MPEG4)) {
//                        return false;
//                    }
                    mVideoDecoderTrack=i;

                    MediaFormat originFormat=mExtractor.getTrackFormat(mVideoDecoderTrack);
                    int frameRate=originFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
                    frameRate=frameRate==0?24:frameRate;

                    mTotalVideoTime=Long.valueOf(mMetRet.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    String rotation=mMetRet.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                    if(rotation!=null){
                        videoRotation=Integer.valueOf(rotation);
                    }
                    if(videoRotation==90||videoRotation==270){
                        mInputVideoHeight=format.getInteger(MediaFormat.KEY_WIDTH);
                        mInputVideoWidth=format.getInteger(MediaFormat.KEY_HEIGHT);
                    }else{
                        mInputVideoWidth=format.getInteger(MediaFormat.KEY_WIDTH);
                        mInputVideoHeight=format.getInteger(MediaFormat.KEY_HEIGHT);
                    }
                    AvLog.d("createDecoder");
                    mVideoDecoder= MediaCodec.createDecoderByType(mime);
                    AvLog.d("createDecoder end");
                    mVideoTextureId= GpuUtils.createTextureID(true);
                    mVideoSurfaceTexture=new SurfaceTexture(mVideoTextureId);
                    mVideoDecoder.configure(format,new Surface(mVideoSurfaceTexture),null,0);
                    if(!isRenderToWindowSurface){
                        if(mOutputVideoWidth==0||mOutputVideoHeight==0){
                            mOutputVideoWidth=mInputVideoWidth;
                            mOutputVideoHeight=mInputVideoHeight;
                        }
                        MediaFormat videoFormat= MediaFormat.createVideoFormat(/*mime*/"video/avc",mOutputVideoWidth,mOutputVideoHeight);
                        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE,mOutputVideoHeight*mOutputVideoWidth*5);
                        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
                        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, frameRate*10);
                        mVideoEncoder= MediaCodec.createEncoderByType(/*mime*/"video/avc");
                        mVideoEncoder.configure(videoFormat,null,null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                        mOutputSurface=mVideoEncoder.createInputSurface();
                        Bundle bundle=new Bundle();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            bundle.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE,mOutputVideoHeight*mOutputVideoWidth*5);
                            mVideoEncoder.setParameters(bundle);
                        }
                    }
                }
            }
            if(!isRenderToWindowSurface){
                //如果用户没有设置渲染到指定Surface，就需要导出视频，暂时不对音频做处理
                mMuxer=new MediaMuxer(mOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//                mMuxer.setOrientationHint(videoRotation);
                AvLog.d("video rotation:"+videoRotation);
                //如果mp4中有音轨
                if(mAudioDecoderTrack>=0){
                    MediaFormat format=mExtractor.getTrackFormat(mAudioDecoderTrack);
                    AvLog.d("audio track-->"+format.toString());

                    mAudioEncoderTrack=mMuxer.addTrack(format);
                }
            }
        }
        return true;
    }

    public boolean start() throws IOException {
        synchronized (PROCESS_LOCK){
            if(!isStarted){
                if(!prepare()){
                    AvLog.d("prepare failed");
                    return false;
                }

                isUserWantToStop=false;

                isVideoExtractorEnd=false;
                isVideoExtractorEnd=false;
                mGLThreadFlag=true;
                mVideoDecoder.start();
                //mAudioDecoder.start();
                if(!isRenderToWindowSurface){
                    //mAudioEncoder.start();
                    mVideoEncoder.start();
                }

                mGLThread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        glRunnable();
                    }
                });
                mGLThread.start();

                mCodecFlag=true;
                mDecodeThread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //视频处理
                        if(mVideoDecoderTrack>=0){
                            AvLog.d("videoDecodeStep start");
                            codecNum=0;
                            while (mCodecFlag&&!videoDecodeStep()){};
                            AvLog.d("videoDecodeStep end--FrameNum="+codecNum);
                            mGLThreadFlag=false;
                            try {
                                mSem.release();
                                mGLThread.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        //将原视频中的音频复制到新视频中
                        if(mAudioDecoderTrack>=0&&mVideoEncoderTrack>=0){
                            ByteBuffer buffer= ByteBuffer.allocate(1024*32);
                            while (mCodecFlag&&!audioDecodeStep(buffer)){};
                            buffer.clear();
                        }

                        AvLog.d("codec thread_finish");
                        mCodecFlag=false;
                        avStop();
                        //todo 判断是用户取消了的情况
                        if(mProgressListener!=null){
                            mProgressListener.onComplete(mOutputPath);
                        }
                    }
                });
                mDecodeThread.start();
                isStarted=true;
            }
        }
        return true;
    }

    /**
     * 等待解码线程执行完毕，异步线程同步等待
     */
    public void waitProcessFinish() throws InterruptedException {
        if(mDecodeThread!=null&&mDecodeThread.isAlive()){
            mDecodeThread.join();
        }
    }

    private boolean audioDecodeStep(ByteBuffer buffer){
        boolean isTimeEnd=false;
        buffer.clear();
        synchronized (Extractor_LOCK){
            mExtractor.selectTrack(mAudioDecoderTrack);
            int length=mExtractor.readSampleData(buffer,0);
            if(length!=-1){
                int flags=mExtractor.getSampleFlags();
                mAudioEncoderBufferInfo.size=length;
                mAudioEncoderBufferInfo.flags=flags;
                mAudioEncoderBufferInfo.presentationTimeUs=mExtractor.getSampleTime();
                mAudioEncoderBufferInfo.offset=0;
                AvLog.d("audio sampleTime="+mAudioEncoderBufferInfo.presentationTimeUs);
                isTimeEnd=mExtractor.getSampleTime()>=mVideoStopTimeStamp;
                mMuxer.writeSampleData(mAudioEncoderTrack,buffer,mAudioEncoderBufferInfo);
            }
            isAudioExtractorEnd=!mExtractor.advance();
        }
        return isAudioExtractorEnd||isTimeEnd;
    }

    //视频解码到SurfaceTexture上，以供后续处理。返回值为是否是最后一帧视频
    private int codecNum=0;
    private boolean videoDecodeStep(){
        int mInputIndex=mVideoDecoder.dequeueInputBuffer(TIME_OUT);
        if(mInputIndex>=0){
            ByteBuffer buffer=getInputBuffer(mVideoDecoder,mInputIndex);
            buffer.clear();
            synchronized (Extractor_LOCK) {
                mExtractor.selectTrack(mVideoDecoderTrack);
                int ret = mExtractor.readSampleData(buffer, 0);
                if (ret != -1) {
                    mVideoStopTimeStamp=mExtractor.getSampleTime();
                    AvLog.d("mVideoStopTimeStamp:"+mVideoStopTimeStamp);
                    mVideoDecoder.queueInputBuffer(mInputIndex, 0, ret, mVideoStopTimeStamp, mExtractor.getSampleFlags());
                }
                isVideoExtractorEnd = !mExtractor.advance();
            }
        }
        while (true){
            int mOutputIndex=mVideoDecoder.dequeueOutputBuffer(mVideoDecoderBufferInfo,TIME_OUT);
            if(mOutputIndex>=0){
                try {
                    AvLog.d(" mDecodeSem.acquire ");
                    if(!isUserWantToStop){
                        mDecodeSem.acquire();
                    }
                    AvLog.d(" mDecodeSem.acquire end ");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                codecNum++;
                mVideoDecoder.releaseOutputBuffer(mOutputIndex,true);
                mSem.release();

            }else if(mOutputIndex== MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                //MediaFormat format=mVideoDecoder.getOutputFormat();
            }else if(mOutputIndex== MediaCodec.INFO_TRY_AGAIN_LATER){
                break;
            }
        }
        return isVideoExtractorEnd||isUserWantToStop;
    }

    private boolean videoEncodeStep(boolean isEnd){
        if(isEnd){
            mVideoEncoder.signalEndOfInputStream();
        }
        while (true){
            int mOutputIndex=mVideoEncoder.dequeueOutputBuffer(mVideoEncoderBufferInfo,TIME_OUT);
            AvLog.d("videoEncodeStep-------------------mOutputIndex="+mOutputIndex+"/"+mVideoEncoderBufferInfo.presentationTimeUs);
            if(mOutputIndex>=0){
                ByteBuffer buffer=getOutputBuffer(mVideoEncoder,mOutputIndex);
                if(mVideoEncoderBufferInfo.size>0){
                    mMuxer.writeSampleData(mVideoEncoderTrack,buffer,mVideoEncoderBufferInfo);
                }
                mVideoEncoder.releaseOutputBuffer(mOutputIndex,false);
            }else if(mOutputIndex== MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                MediaFormat format=mVideoEncoder.getOutputFormat();
                AvLog.d("video format -->"+format.toString());
                mVideoEncoderTrack=mMuxer.addTrack(format);
                mMuxer.start();
                synchronized (MUX_LOCK){
                    MUX_LOCK.notifyAll();
                }
            }else if(mOutputIndex== MediaCodec.INFO_TRY_AGAIN_LATER){
                break;
            }
        }
        return false;
    }

    private void glRunnable(){
        mSem=new Semaphore(0);
        mDecodeSem=new Semaphore(1);
        boolean ret=mEGLHelper.createGLESWithSurface(new EGLConfigAttrs(),new EGLContextAttrs(),mOutputSurface);
        if(!ret){
            return;
        }
        if(mRenderer==null){
            mRenderer=new WrapRenderer(null);
        }
        mRenderer.create();
        mRenderer.sizeChanged(mOutputVideoWidth,mOutputVideoHeight);
        int frameNum=0;
        while (mGLThreadFlag){
            try {
                AvLog.d(" mSem.acquire ");
                mSem.acquire();
                AvLog.d(" mSem.acquire end");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(mGLThreadFlag){
                mVideoSurfaceTexture.updateTexImage();
                //todo 带有rotation的视频，还需要处理
                mVideoSurfaceTexture.getTransformMatrix(mRenderer.getTextureMatrix());
                mRenderer.draw(mVideoTextureId);
                mEGLHelper.setPresentationTime(mEGLHelper.getDefaultSurface(),mVideoDecoderBufferInfo.presentationTimeUs*1000);
                if(!isRenderToWindowSurface){
                    frameNum++;
                    videoEncodeStep(false);
                }
                mEGLHelper.swapBuffers(mEGLHelper.getDefaultSurface());
            }
            if(mProgressListener!=null){
                mProgressListener.onProgress(getTotalVideoTime()*1000L,mVideoDecoderBufferInfo.presentationTimeUs);
            }
            mDecodeSem.release();
        }
        AvLog.d("Encode Frame num-----:"+frameNum);
        if(!isRenderToWindowSurface){
            videoEncodeStep(true);
        }
        mRenderer.destroy();
        mEGLHelper.destroyGLES(mEGLHelper.getDefaultSurface(),mEGLHelper.getDefaultContext());
    }

    public long getPresentationTime(){
        return mVideoDecoderBufferInfo.presentationTimeUs*1000;
    }

    public long getTotalVideoTime(){
        return mTotalVideoTime;
    }

    private void avStop(){
        if(isStarted){
            if(mVideoDecoder!=null){
                mVideoDecoder.stop();
                mVideoDecoder.release();
                mVideoDecoder=null;
            }
            if(!isRenderToWindowSurface&&mVideoEncoder!=null){
                mVideoEncoder.stop();
                mVideoEncoder.release();
                mVideoEncoder=null;
            }
            if(!isRenderToWindowSurface){
                if(mMuxer!=null&&mVideoEncoderTrack>=0){
                    try {
                        mMuxer.stop();
                    }catch (IllegalStateException e){
                        e.printStackTrace();
                    }
                }
                if(mMuxer!=null){
                    try {
                        mMuxer.release();
                    }catch (IllegalStateException e){
                        e.printStackTrace();
                    }
                    mMuxer=null;
                }
            }
            if(mExtractor!=null){
                mExtractor.release();
            }
            isStarted=false;
            mVideoEncoderTrack=-1;
            mVideoDecoderTrack=-1;
            mAudioEncoderTrack=-1;
            mAudioDecoderTrack=-1;
        }
    }

    public boolean stop() throws InterruptedException {
        synchronized (PROCESS_LOCK){
            if(isStarted){
                if(mCodecFlag){
                    mDecodeSem.release();
                    isUserWantToStop=true;
                    if(mDecodeThread!=null&&mDecodeThread.isAlive()){
                        AvLog.d("try to stop decode thread");
                        mDecodeThread.join();
                        AvLog.d("decode thread stoped");
                    }
                    isUserWantToStop=false;
                }
            }
        }
        return true;
    }

    public boolean release() throws InterruptedException {
        synchronized (PROCESS_LOCK){
            if(mCodecFlag){
                stop();
            }
        }
        return true;
    }

    private ByteBuffer getInputBuffer(MediaCodec codec, int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return codec.getInputBuffer(index);
        }else{
            return codec.getInputBuffers()[index];
        }
    }

    private ByteBuffer getOutputBuffer(MediaCodec codec, int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return codec.getOutputBuffer(index);
        }else{
            return codec.getOutputBuffers()[index];
        }
    }

    public interface OnProgressListener{
        void onProgress(long max, long current);
        void onComplete(String path);
    }

}
