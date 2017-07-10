package com.aiyaapp.camera.sdk.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.AttributeSet;
import android.view.TextureView;
import com.aiyaapp.camera.sdk.AiyaEffects;
import com.aiyaapp.camera.sdk.base.ISdkManager;
import com.aiyaapp.camera.sdk.filter.AFilter;
import com.aiyaapp.camera.sdk.filter.EasyGlUtils;
import com.aiyaapp.camera.sdk.filter.NoFilter;
import com.aiyaapp.camera.sdk.filter.ProcessFilter;
import com.aiyaapp.sticker.sdk.R;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by aiya on 2017/6/26.
 */

public class EffectTextureView extends TextureView implements TextureView.SurfaceTextureListener,
    GLSurfaceView.Renderer{

    private GLEnvironment mEnv;
    private SurfaceTexture mTexture;
    private AiyaEffects mEffect;
    private ProcessFilter mEffectFilter;
    private AFilter mShowFilter;
    private int width,height;
    private float[] SM=new float[16];                       //用于绘制到屏幕上的变换矩阵

    private int[] tempTextures=new int[1];

    public EffectTextureView(Context context) {
        this(context,null);
    }

    public EffectTextureView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public EffectTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setOpaque(false);
        setKeepScreenOn(true);
        animate().scaleY(-1).withLayer();
        setSurfaceTextureListener(this);

        mEnv=new GLEnvironment(getContext());
        mEnv.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        mEnv.setPreserveEGLContextOnPause(true);
        mEnv.setEGLWindowSurfaceFactory(new GLEnvironment.EGLWindowSurfaceFactory() {
            @Override
            public EGLSurface createSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
                return egl.eglCreateWindowSurface(display,config,mTexture,null);
            }

            @Override
            public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
                egl.eglDestroySurface(display,surface);
            }
        });
        mEffect= AiyaEffects.getInstance();
        mEnv.setEGLContextClientVersion(2);
        mEnv.setRenderer(this);
        mEnv.setRenderMode(GLEnvironment.RENDERMODE_CONTINUOUSLY);
        mEffectFilter=new ProcessFilter(getResources());
        mShowFilter=new NoFilter(getResources()){
            @Override
            protected void onClear() {

            }
        };
        mEffectFilter.setTextureId(-1);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.mTexture=surface;
        mEnv.surfaceCreated(null);
        mEnv.surfaceChanged(null,0,width,height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mEnv.surfaceChanged(null,0,width,height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        this.mTexture=null;
        mEnv.surfaceDestroyed(null);
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mEffectFilter.create();
        mShowFilter.create();
        AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_FORCE_CLOSE,1);
        GLES20.glGenTextures(1,tempTextures,0);
    }

    private int getBitmapTexture(int x,int y){
        Bitmap bmp=BitmapFactory.decodeResource(getResources(), R.mipmap.test);
        int[] textures=new int[1];
        EasyGlUtils.genTexturesWithParameter(1,textures,0,GLES20.GL_RGBA,x,y);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bmp,0);
        bmp.recycle();
        return textures[0];
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width=width;
        this.height=height;
        //
        //MatrixUtils.getMatrix(SM,MatrixUtils.TYPE_CENTERCROP,
        //    720,1280,width,height);
        //mShowFilter.setMatrix(SM);
        //mEffectFilter.setSize(width, height);
        mShowFilter.setSize(width, height);

        AiyaEffects.getInstance().set(ISdkManager.SET_IN_WIDTH,width);
        AiyaEffects.getInstance().set(ISdkManager.SET_IN_HEIGHT,height);
        AiyaEffects.getInstance().set(ISdkManager.SET_OUT_WIDTH,width);
        AiyaEffects.getInstance().set(ISdkManager.SET_OUT_HEIGHT,height);
        if(width>height&&width>320){
            if(width>320){
                AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_WIDTH,320);
                AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_HEIGHT,320*height/width);
            }
        }else if(height>width&&height>320){
            if(height>320){
                AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_WIDTH,320*width/height);
                AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_HEIGHT,320);
            }
        }else{
            AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_WIDTH,width);
            AiyaEffects.getInstance().set(ISdkManager.SET_TRACK_HEIGHT,height);
        }
        AiyaEffects.getInstance().set(ISdkManager.SET_ACTION,ISdkManager.ACTION_REFRESH_PARAMS_NOW);
    }

    int index=0;

    @Override
    public void onDrawFrame(GL10 gl) {
        //EData.data.setDealStartTime(System.currentTimeMillis());
        //接收图像流，特效处理并输出一个texture
        GLES20.glClearColor(0,0,0,0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0,0,width,height);
        AiyaEffects.getInstance().track(null,null,index);
        AiyaEffects.getInstance().process(-1,index);
        //mEffectFilter.draw();
        index^=1;
        //mNoFilter.draw();
        //显示到屏幕上
        //GLES20.glViewport(0,0,width,height);
        //mShowFilter.setMatrix(SM);
        //mShowFilter.setTextureId(mEffectFilter.getOutputTexture());
        //mShowFilter.draw();
        //
        //EData.data.setDealEndTime(System.currentTimeMillis());
    }

    public void onResume(){
        mEnv.onResume();
    }

    public void onPause(){
        mEnv.onPause();
    }

}
