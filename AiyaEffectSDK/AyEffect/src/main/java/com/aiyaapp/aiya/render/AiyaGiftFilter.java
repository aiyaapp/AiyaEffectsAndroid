package com.aiyaapp.aiya.render;

import android.content.Context;
import android.opengl.GLES20;

import com.aiyaapp.aavt.gl.FrameBuffer;
import com.aiyaapp.aavt.gl.LazyFilter;
import com.aiyaapp.aavt.log.AvLog;
import com.aiyaapp.aiya.AiyaGiftEffect;
import com.aiyaapp.aiya.base.IComponent;

import java.nio.ByteBuffer;

/**
 * 特效滤镜，使用此类处理纹理，为一张纹理添加上特效
 *
 * @author wuwang
 */
public class AiyaGiftFilter extends LazyFilter {

    private AiyaGiftEffect mGift;
    private FrameBuffer mFrameBuffer;
    private boolean isHasTracker = false;
    private String mEffect;
    private ByteBuffer mTrackBuffer;
    private int[] mLastViewPort = new int[4];
    private int mTrackWidth, mTrackHeight;

    /**
     * 当前支持的IComponent为track模块中的AiyaTracker对象，或者为null
     *
     * @param context   context
     * @param component 附件组件
     */
    public AiyaGiftFilter(Context context, IComponent component) {
        super();
        mGift = new AiyaGiftEffect(context.getApplicationContext());
        if (component != null) {
            mGift.setTracker(component, 2);
            isHasTracker = true;
        }
    }


    public void setFaceDataID(final long id) {
        mGift.setFaceDataID(id);
        isHasTracker = true;

//        runOnGLThread(new Runnable() {
//            @Override
//            public void run() {
//          mGift.setFaceDataID(id);
//        isHasTracker = true;
//            }
//        });
    }

    public void setFaceData(IComponent tracker){
        mGift.setTracker(tracker, 2);
        isHasTracker = true;
    }


    /**
     * 设置礼物特效
     *
     * @param effect 特效配置文件路径
     */
    public void setEffect(String effect) {
        mGift.setEffect(effect);
        this.mEffect = effect;
    }

    /**
     * 设置特效动画的监听器
     *
     * @param listener 监听器
     */
    public void setAnimListener(AnimListener listener) {
        mGift.setEventListener(listener);
    }

    @Override
    protected void initBuffer() {
        super.initBuffer();
        mFrameBuffer = new FrameBuffer();
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        if (mEffect != null) {
            mGift.setEffect(mEffect);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        super.onSizeChanged(width, height);
        mFrameBuffer.destroyFrameBuffer();
        if (width > height && width > 320) {
            if (width > 320) {
                mTrackWidth = 320;
                mTrackHeight = 320 * height / width;
            }
        } else if (height > width && height > 320) {
            if (height > 320) {
                mTrackHeight = 320;
                mTrackWidth = 320 * width / height;
            }
        } else {
            mTrackWidth = width;
            mTrackHeight = height;
        }
        mGift.setTrackSize(mTrackWidth, mTrackHeight);
        mTrackBuffer = ByteBuffer.allocate(mTrackWidth * mTrackHeight * 4);
        AvLog.d("AiyaGiftFilter Size Changed:" + width + "/" + height);
    }

    private void drawGiftToFrame(int texture) {
        if (mEffect != null) {
            GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, mLastViewPort, 0);
            mFrameBuffer.bindFrameBuffer(mWidth, mHeight, isHasTracker);
            GLES20.glViewport(0, 0, mTrackWidth, mTrackHeight);
            super.draw(texture);
            GLES20.glReadPixels(0, 0, mTrackWidth, mTrackHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mTrackBuffer);
            GLES20.glViewport(mLastViewPort[0], mLastViewPort[1], mLastViewPort[2], mLastViewPort[3]);
            mGift.draw(texture, mWidth, mHeight, mTrackBuffer.array());
            mFrameBuffer.unBindFrameBuffer();
        } else {
            mFrameBuffer.bindFrameBuffer(mWidth, mHeight, isHasTracker);
            mGift.draw(texture, mWidth, mHeight);
            mFrameBuffer.unBindFrameBuffer();
        }
    }

    @Override
    public void draw(int texture) {
        drawGiftToFrame(texture);
        super.draw(mFrameBuffer.getCacheTextureId());
    }

    @Override
    public void destroy() {
        super.destroy();
        mGift.onDestroyGL();
    }

    /**
     * 注销礼物，注销后请勿再调用其他方法
     */
    public void release() {
        mGift.release();
    }

}
