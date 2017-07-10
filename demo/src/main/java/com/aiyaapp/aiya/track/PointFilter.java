package com.aiyaapp.aiya.track;

import android.content.res.Resources;
import android.opengl.GLES20;
import com.aiyaapp.camera.sdk.filter.MatrixUtils;
import com.aiyaapp.camera.sdk.filter.NoFilter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by aiya on 2017/7/9.
 */

public class PointFilter extends NoFilter {

    private int length;

    public PointFilter(Resources mRes) {
        super(mRes);
        //MatrixUtils.flip(getMatrix(),false,true);
        MatrixUtils.scale(getMatrix(),2,2);
        MatrixUtils.translate(getMatrix(),-0.5f,-0.5f);
    }

    @Override
    protected void onClear() {

    }

    @Override
    protected void initBuffer() {
        ByteBuffer a=ByteBuffer.allocateDirect(800);
        a.order(ByteOrder.nativeOrder());
        mVerBuffer=a.asFloatBuffer();
        ByteBuffer b=ByteBuffer.allocateDirect(800);
        b.order(ByteOrder.nativeOrder());
        mTexBuffer=b.asFloatBuffer();
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/point.vert",
            "shader/point.frag");
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }

    public void setPoint(float[] buffer,int start,int length) {
        this.length=length;
        mVerBuffer.clear();
        mVerBuffer.put(buffer,start,length);
        mVerBuffer.position(0);
    }

    @Override
    protected void onDraw() {
        if(length>0){
            GLES20.glEnableVertexAttribArray(mHPosition);
            GLES20.glVertexAttribPointer(mHPosition,2, GLES20.GL_FLOAT, false, 0,mVerBuffer);
            GLES20.glEnableVertexAttribArray(mHCoord);
            GLES20.glVertexAttribPointer(mHCoord, 2, GLES20.GL_FLOAT, false, 0, mTexBuffer);
            GLES20.glDrawArrays(GLES20.GL_POINTS,0,length/2);
            GLES20.glDisableVertexAttribArray(mHPosition);
            GLES20.glDisableVertexAttribArray(mHCoord);
        }
    }

}
