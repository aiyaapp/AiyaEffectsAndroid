package com.aiyaapp.aiya.track;

import android.content.res.Resources;
import android.graphics.PointF;
import android.opengl.GLES20;
import com.aiyaapp.camera.sdk.AiyaEffects;
import com.aiyaapp.camera.sdk.AiyaEffectsJni;
import com.aiyaapp.camera.sdk.base.ISdkManager;
import com.aiyaapp.camera.sdk.base.Log;
import com.aiyaapp.camera.sdk.base.TrackCallback;
import com.aiyaapp.camera.sdk.filter.AFilter;
import com.aiyaapp.camera.sdk.filter.AiyaFilter;
import com.aiyaapp.camera.sdk.filter.EasyGlUtils;
import com.aiyaapp.camera.sdk.filter.MatrixUtils;
import java.nio.ByteBuffer;

/**
 * Created by aiya on 2017/7/9.
 */

public class TrackOnlyFilter extends AFilter implements TrackCallback {

    private AiyaFilter mFilter;
    private PointFilter mPointFilter;
    private int width=0;
    private int height=0;

    private int trackWidth,trackHeight;
    private float[] infos = new float[200];
    private int nowTextureIndex=0;

    private int fTextureSize = 2;
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[fTextureSize];

    private boolean isFirstDraw=true;

    private float[] FlipOM=new float[16];

    //获取Track数据
    private ByteBuffer tBuffer;

    public TrackOnlyFilter(Resources mRes) {
        super(mRes);
        mFilter=new AiyaFilter(mRes);
        mPointFilter=new PointFilter(mRes);
        AiyaEffects.getInstance().setTrackCallback(this);
        MatrixUtils.flip(mFilter.getMatrix(),false,true);
        System.arraycopy(mFilter.getMatrix(),0,FlipOM,0,16);
    }

    public void setCoordMatrix(float[] matrix){
        mFilter.setCoordMatrix(matrix);
    }

    @Override
    public void setFlag(int flag) {
        mFilter.setFlag(flag);
    }

    @Override
    protected void initBuffer() {

    }

    public void setTrackSize(int width,int height){
        this.trackWidth=width;
        this.trackHeight=height;
    }

    @Override
    public void setMatrix(float[] matrix) {
        mFilter.setMatrix(matrix);
    }

    @Override
    public int getOutputTexture() {
        if (isFirstDraw){
            isFirstDraw=false;
            return fTexture[0];
        }
        return fTexture[nowTextureIndex];
    }

    private byte[] getTrackData() {
        long t=System.currentTimeMillis();
        GLES20.glReadPixels(0, 0,trackWidth,
            trackHeight, GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,tBuffer);
        Log.d("track read cost:"+(System.currentTimeMillis()-t));
        return tBuffer.array();
    }

    @Override
    public void draw() {
        boolean a=GLES20.glIsEnabled(GLES20.GL_DEPTH_TEST);
        if(a){
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        }
        mFilter.setTextureId(getTextureId());
        EasyGlUtils.bindFrameTexture(fFrame[0],fTexture[nowTextureIndex]);
        GLES20.glViewport(0,0,trackWidth,trackHeight);
        mFilter.setMatrix(OM);
        mFilter.draw();
        AiyaEffectsJni.getInstance().track(getTrackData(), trackWidth,trackHeight,infos,nowTextureIndex);
        EasyGlUtils.unBindFrameBuffer();
        GLES20.glViewport(0,0,width,height);
        mFilter.setMatrix(FlipOM);
        mFilter.draw();
        mPointFilter.setPoint(infos,0,138);
        mPointFilter.draw();
        if(a){
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }
        nowTextureIndex^=1;
    }

    @Override
    protected void onCreate() {
        mFilter.create();
        mPointFilter.create();
        //创建FrameBuffer和Texture
        nowTextureIndex=0;
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        mFilter.setSize(width, height);
        mPointFilter.setSize(width, height);
        if(this.width!=width||this.height!=height){
            this.width=width;
            this.height=height;
            deleteFrameBuffer();
            GLES20.glGenFramebuffers(1, fFrame, 0);
            EasyGlUtils.genTexturesWithParameter(fTextureSize, fTexture, 0,GLES20.GL_RGBA,width,height);
            if(tBuffer!=null){
                tBuffer.clear();
            }
            tBuffer = ByteBuffer.allocate(
                AiyaEffects.getInstance().get(ISdkManager.SET_TRACK_WIDTH)*
                    AiyaEffects.getInstance().get(ISdkManager.SET_TRACK_HEIGHT )* 4);
        }
    }

    private void deleteFrameBuffer() {
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
        GLES20.glDeleteTextures(fTextureSize, fTexture, 0);
    }

    @Override
    public void onTrack(int code, float[] info) {
        if(mPointFilter!=null){
            mPointFilter.setPoint(info,0,138);
        }
    }
}
