package com.aiyaapp.aiya.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import com.aiyaapp.aavt.gl.BaseFilter;
import com.aiyaapp.aavt.gl.FrameBuffer;
import com.aiyaapp.aavt.gl.LazyFilter;
import com.aiyaapp.aavt.utils.MatrixUtils;
import com.aiyaapp.aiya.AiyaGiftEffect;
import com.aiyaapp.aiya.Const;

import javax.microedition.khronos.egl.EGLSurface;

/**
 * 礼物特效组件
 * @author wuwang
 */
public class AiyaEffectTextureView extends TextureView {

    private LowEGLHelper mEglHelper;
    private SurfaceTextureListener mSurfaceTextureListener;
    private Thread mThread;
    private boolean mGLThreadFlag=false;
    private int mTimeCell;

    private int mWidth=0,mHeight=0;

    private String effect;
    private AnimListener mAnimListener;

    private AiyaGiftEffect mGift;
    private FrameBuffer mFrameBuffer;
    private BaseFilter mBaseFilter;
    private int mLoopLimit=1;
    private int mLoopCount=0;

    private SurfaceTexture mTempSurfaceTexture;
    private int mTempTextureId;
    private SurfaceTexture mOutputSurfaceTexture;
    private EGLSurface mWindowSurface=null;
    private boolean mReCreateFlag=false;

    private boolean isForbidChangeSizeWhenRecreate=false;

    private boolean mPauseIfSurfaceDestroyed=false;

    private final Object SURFACE_LOCK=new Object();

    public AiyaEffectTextureView(Context context) {
        this(context,null);
    }

    public AiyaEffectTextureView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AiyaEffectTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setOpaque(false);
        setKeepScreenOn(true);
        setFrameRate(60);
        mEglHelper=new LowEGLHelper();
        super.setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
                synchronized (SURFACE_LOCK){
                    if(mPauseIfSurfaceDestroyed){
                        mGift.resume();
                    }
                    if(!isForbidChangeSizeWhenRecreate||mWidth==0||mHeight==0){
                        mWidth=width;
                        mHeight=height;
                    }
                    MatrixUtils.getMatrix(mBaseFilter.getVertexMatrix(),MatrixUtils.TYPE_CENTERCROP,720,1280,
                            mWidth,mHeight);
                    mOutputSurfaceTexture=surface;
                    mReCreateFlag=true;
                    SURFACE_LOCK.notifyAll();
                }
                if(mSurfaceTextureListener!=null){
                    mSurfaceTextureListener.onSurfaceTextureAvailable(surface, width, height);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                synchronized (SURFACE_LOCK){
                    if(!isForbidChangeSizeWhenRecreate||mWidth==0||mHeight==0){
                        mWidth=width;
                        mHeight=height;
                    }
                    MatrixUtils.getMatrix(mBaseFilter.getVertexMatrix(),MatrixUtils.TYPE_CENTERCROP,720,1280,
                            mWidth,mHeight);
                    mOutputSurfaceTexture=surface;
                }
                if(mSurfaceTextureListener!=null){
                    mSurfaceTextureListener.onSurfaceTextureSizeChanged(surface, width, height);
                }
            }
            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                if(mSurfaceTextureListener!=null){
                    mSurfaceTextureListener.onSurfaceTextureDestroyed(surface);
                }
                synchronized (SURFACE_LOCK){
                    mOutputSurfaceTexture=null;
                    if(mPauseIfSurfaceDestroyed&&mGift!=null){
                        mGift.pause();
                    }
                }

                return false;
            }
            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                if(mSurfaceTextureListener!=null){
                    mSurfaceTextureListener.onSurfaceTextureUpdated(surface);
                }
            }
        });
        mBaseFilter=new LazyFilter(){
            @Override
            protected void onClear() {
                GLES20.glClearColor(0, 0, 0, 0);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            }
        };
        startGLThread(720,1280);
    }

    /**
     * 设置SurfaceTextureListener，覆盖{@link TextureView#setSurfaceTextureListener(SurfaceTextureListener)},
     * 如果有监听TextureView中SurfaceTexture创建销毁的需求，可以调用此方法。不要操作SurfaceTexture。
     * @param listener
     */
    @Override
    public void setSurfaceTextureListener(SurfaceTextureListener listener) {
        this.mSurfaceTextureListener=listener;
    }

    /**
     * 按照Surface第一次创建时的大小设置渲染区域的大小。
     * 当Surface重建时，不改变渲染区域的大小，
     * @param forbid 禁止大小跟随Surface更改
     */
    public void forbidChangeSizeWhenSurfaceRecreate(boolean forbid){
        this.isForbidChangeSizeWhenRecreate=forbid;
    }

    /**
     * 当Surface被销毁时（比如活动切入后台），暂停特效的播放
     * @param pause 是否暂停
     */
    public void pauseIfSurfaceDestroyed(boolean pause){
        this.mPauseIfSurfaceDestroyed=pause;
    }

    /**
     * 设置礼物特效，并开始礼物特效的渲染
     * @param effect 礼物特效路径，assets中路径，需加上'assets/'前缀。其他位置填入绝对路径。
     */
    public void setEffect(String effect){
        synchronized (SURFACE_LOCK){
            this.effect=effect;
            if(mGift!=null){
                mGift.setEffect(effect);
            }
            if(this.effect!=null){
                SURFACE_LOCK.notifyAll();
            }
        }
    }

    /**
     * 重新初始化并启动环境，通常无需调用
     */
    public void reInit(){
        stopGLThread();
        startGLThread(720,1280);
    }

    /**
     * 销毁环境，可在页面销毁时调用
     */
    public void release(){
        stopGLThread();
    }

//    public void pauseIfSurfaceDestroyed(boolean pause){
//        this.mPauseIfSurfaceDestroyed=pause;
//    }


    @Override
    protected void finalize() throws Throwable {
        stopGLThread();
        super.finalize();
    }

    public void setLoop(int loop){
        this.mLoopLimit=loop;
    }

    private void startGLThread(final int width,final int height){
        mThread=new Thread(new Runnable() {
            @Override
            public void run() {
                mGLThreadFlag=true;
                mTempTextureId=mEglHelper.createTextureID();
                mTempSurfaceTexture=new SurfaceTexture(mTempTextureId);
                mEglHelper.setSurface(mTempSurfaceTexture);
                boolean ret=mEglHelper.createGLES(width,height);
                if(!ret&&mAnimListener!=null){
                    mAnimListener.onAnimEvent(Const.MSG_TYPE_ERROR,AiyaGiftEffect.MSG_ERROR_GL_ENVIRONMENT,"create gl environment failed");
                }
                onGlCreate(width, height);
                while (mGLThreadFlag){

                    long start=System.currentTimeMillis();
                    synchronized (SURFACE_LOCK){
                        if(effect==null||(mPauseIfSurfaceDestroyed&&mOutputSurfaceTexture==null)){
                            try {
                                SURFACE_LOCK.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    mEglHelper.makeCurrent();
                    onGlDrawToTempSurface(width,height);
                    mEglHelper.swapBuffers();
                    synchronized (SURFACE_LOCK){
                        if(mReCreateFlag&&mOutputSurfaceTexture!=null){
                            mReCreateFlag=false;
                            if(mWindowSurface!=null){
                                mEglHelper.destorySurface(mWindowSurface);
                                mWindowSurface=null;
                            }
                            mWindowSurface=mEglHelper.createEGLWindowSurface(mOutputSurfaceTexture);
                        }
                        if(mWindowSurface!=null){
                            mEglHelper.makeCurrent(mWindowSurface);
                            onGlDrawToWindowSurface(mWidth,mHeight);
                            mEglHelper.swapBuffers(mWindowSurface);
                        }
                    }

                    long time=System.currentTimeMillis()-start;
                    if(time<mTimeCell&&mGLThreadFlag){
                        try {
                            Thread.sleep(mTimeCell-time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                onGlDestroy();
                mEglHelper.destroyGLES();
            }
        });
        mThread.start();
    }

    private void onGlCreate(int width,int height){
        GLES20.glClearColor(0,0,0,0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
        mGift=new AiyaGiftEffect(getContext());
        if(mAnimListener!=null){
            mGift.setEventListener(mAnimListener);
        }
        if(effect!=null){
            mGift.setEffect(effect);
        }
        mFrameBuffer=new FrameBuffer();
        mBaseFilter.create();
    }

    private void onGlDrawToTempSurface(int width,int height){
        mFrameBuffer.bindFrameBuffer(width,height);
        GLES20.glClearColor(0,0,0,0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
        int ret=mGift.draw(-1,width,height);
        if(ret==AiyaGiftEffect.MSG_STAT_EFFECTS_END){
            if(mLoopLimit>0){
                mLoopCount++;
                if(mLoopLimit==mLoopCount){
                    mLoopCount=0;
                    mGift.setEffect(null);
                    effect=null;
//                    if(mAnimListener!=null){
//                        mAnimListener.onAnimEvent(Const.MSG_TYPE_INFO,AiyaGiftEffect.MSG_STAT_EFFECTS_END,"effect end");
//                    }
                }
            }
        }
        mFrameBuffer.unBindFrameBuffer();
    }

    private void onGlDrawToWindowSurface(int width,int height){
        GLES20.glViewport(0,0,width,height);
        mBaseFilter.draw(mFrameBuffer.getCacheTextureId());
    }

    private void onGlDestroy(){
        mFrameBuffer.destroyFrameBuffer();
        mGift.release();
        mGift=null;
    }

    /**
     * 设置渲染最大帧率
     * @param rate 帧数
     */
    public void setFrameRate(int rate){
        mTimeCell=1000/rate;
    }

    /**
     * 设置特效动画监听器，监听器事件回调在渲染线程
     * @param listener 监听器
     */
    public void setAnimListener(AnimListener listener){
        this.mAnimListener=listener;
        if(mGift!=null){
            mGift.setEventListener(listener);
        }
    }

    private void stopGLThread(){
        mGLThreadFlag=false;
        if(mThread!=null&&mThread.isAlive()){
            synchronized (SURFACE_LOCK){
                SURFACE_LOCK.notifyAll();
            }
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d("aiyaapp","GL finish");
        mThread=null;
    }

}
