package com.aiyaapp.aavt.gl;

import android.content.res.Resources;
import android.opengl.GLES20;

/**
 * Created by wuwang on 2017/10/1.
 */

public class Faltung3x3Filter extends BaseFilter {

    private float[] mFaltung;
    private int mGLFaltung;

    public static final float[] FILTER_SHARPEN=new float[]{0,-1,0,-1,5,-1,0,-1,0};
    public static final float[] FILTER_BORDER=new float[]{0,1,0,1,-4,1,0,1,0};
    public static final float[] FILTER_CAMEO=new float[]{2,0,2,0,0,0,3,0,-6};

    public Faltung3x3Filter(Resources res,float[] faltung){
        super(res,"shader/base.vert","shader/func/faltung3x3.frag");
        shaderNeedTextureSize(true);
        this.mFaltung=faltung;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        mGLFaltung= GLES20.glGetUniformLocation(mGLProgram,"uFaltung");
    }

    @Override
    protected void onSetExpandData() {
        super.onSetExpandData();
        GLES20.glUniformMatrix3fv(mGLFaltung,1,false,mFaltung,0);
    }
}
