package com.aiyaapp.aiya;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.util.Log;

import com.aiyaapp.aiya.gpuImage.AYGPUImageEGLContext;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFramebuffer;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.AYGPUImageBeautyFilter;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.AYGPUImageBigEyeFilter;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.AYGPUImageDelayFilter;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.AYGPUImageEffectFilter;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.AYGPUImageEffectPlayFinishListener;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.AYGPUImageLookupFilter;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.AYGPUImageSlimFaceFilter;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.AYGPUImageTrackFilter;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput.AYGPUImageBGRADataInput;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput.AYGPUImageBGRADataOutput;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput.AYGPUImageI420DataInput;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput.AYGPUImageI420DataOutput;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput.AYGPUImageOESTextureInput;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput.AYGPUImageOESTextureOutput;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput.AYGPUImageTextureInput;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput.AYGPUImageTextureOutput;
import com.aiyaapp.aiya.gpuImage.AYGPUImageConstants;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFilter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.*;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageRotateLeft;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageRotateRight;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.TAG;

public class AYEffectHandler {

    private AYGPUImageEGLContext eglContext;
    private SurfaceTexture surfaceTexture;

    protected AYGPUImageTextureInput textureInput;
    protected AYGPUImageTextureOutput textureOutput;

    protected AYGPUImageOESTextureInput oesTextureInput;
    protected AYGPUImageOESTextureOutput oesTextureOutput;

    protected AYGPUImageI420DataInput i420DataInput;
    protected AYGPUImageI420DataOutput i420DataOutput;

    protected AYGPUImageBGRADataInput bgraDataInput;
    protected AYGPUImageBGRADataOutput bgraDataOutput;

    protected AYGPUImageFilter commonInputFilter;
    protected AYGPUImageFilter commonOutputFilter;

    protected AYGPUImageDelayFilter delayFilter;
    protected AYGPUImageLookupFilter lookupFilter;
    protected AYGPUImageBeautyFilter beautyFilter;
    protected AYGPUImageTrackFilter trackFilter;
    protected AYGPUImageBigEyeFilter bigEyeFilter;
    protected AYGPUImageSlimFaceFilter slimFaceFilter;
    protected AYGPUImageEffectFilter effectFilter;

    protected boolean initCommonProcess = false;
    protected boolean initProcess = false;

    private int[] bindingFrameBuffer = new int[1];
    private int[] bindingRenderBuffer = new int[1];
    private int[] viewPoint = new int[4];
    private int vertexAttribEnableArraySize = 5;
    private ArrayList<Integer> vertexAttribEnableArray = new ArrayList(vertexAttribEnableArraySize);

    public AYEffectHandler(final Context context) {
        this(context, true);
    }

    public AYEffectHandler(final Context context, boolean useCurrentEGLContext) {

        eglContext = new AYGPUImageEGLContext();
        if (useCurrentEGLContext) {
            if (EGL14.eglGetCurrentContext() == null) {
                surfaceTexture = new SurfaceTexture(0);
                eglContext.initWithEGLWindow(surfaceTexture);
            } else {
                Log.d(TAG, "不需要初始化EGL环境");
            }
        } else {
            surfaceTexture = new SurfaceTexture(0);
            eglContext.initWithEGLWindow(surfaceTexture);
        }

        eglContext.syncRunOnRenderThread(new Runnable(){
            @Override
            public void run() {
                textureInput = new AYGPUImageTextureInput(eglContext);
                textureOutput = new AYGPUImageTextureOutput(eglContext);

                oesTextureInput = new AYGPUImageOESTextureInput(eglContext);
                oesTextureOutput = new AYGPUImageOESTextureOutput(eglContext);

                i420DataInput = new AYGPUImageI420DataInput(eglContext);
                i420DataOutput = new AYGPUImageI420DataOutput(eglContext);

                bgraDataInput = new AYGPUImageBGRADataInput(eglContext);
                bgraDataOutput = new AYGPUImageBGRADataOutput(eglContext);

                commonInputFilter = new AYGPUImageFilter(eglContext);
                commonOutputFilter = new AYGPUImageFilter(eglContext);

                delayFilter = new AYGPUImageDelayFilter(eglContext);

                try {
                    Bitmap lookupBitmap = BitmapFactory.decodeStream(context.getAssets().open("lookup.png"));
                    lookupFilter = new AYGPUImageLookupFilter(eglContext,lookupBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                beautyFilter = new AYGPUImageBeautyFilter(eglContext, AyBeauty.AY_BEAUTY_TYPE.AY_BEAUTY_TYPE_3);
                bigEyeFilter = new AYGPUImageBigEyeFilter(eglContext);
                slimFaceFilter = new AYGPUImageSlimFaceFilter(eglContext);

                trackFilter = new AYGPUImageTrackFilter(eglContext, context);

                effectFilter = new AYGPUImageEffectFilter(eglContext);
            }
        });
    }

    public void setEffectPath(String effectPath) {
        File file = new File(effectPath);
        if (!file.exists() && !effectPath.equals("")) {
            Log.e("AYEffect", "无效的特效资源路径");
            return;
        }
        if (effectFilter != null) {
            effectFilter.setEffectPath(effectPath);
        }
    }

    public void setEffectPlayCount(int effectPlayCount) {
        if (effectFilter != null) {
            effectFilter.setEffectPlayCount(effectPlayCount);
        }
    }

    public void setEffectPlayFinishListener(AYGPUImageEffectPlayFinishListener effectPlayFinishListener) {
        if (effectFilter != null) {
            effectFilter.setEffectPlayFinishListener(effectPlayFinishListener);
        }
    }

    public void pauseEffect() {
        if (effectFilter != null) {
            effectFilter.pause();
        }
    }

    public void resumeEffect() {
        if (effectFilter != null) {
            effectFilter.resume();
        }
    }

    public void setStyle(Bitmap lookup) {
        if (lookupFilter != null) {
            lookupFilter.setLookup(lookup);
        }
    }

    public void setIntensityOfStyle(float intensity) {
        if (lookupFilter != null) {
            lookupFilter.setIntensity(intensity);
        }
    }

    // 需要保证在初始化线程设置
    public void setBeautyType(AyBeauty.AY_BEAUTY_TYPE type) {
        if (beautyFilter != null) {
            beautyFilter.setType(type);
        }
    }

    // 只支持 AY_BEAUTY_TYPE_2
    public void setIntensityOfBeauty(float intensity) {
        if (beautyFilter != null) {
            beautyFilter.setIntensity(intensity);
        }
    }

    // 不支持 AY_BEAUTY_TYPE_2
    public void setIntensityOfSmooth(float intensity) {
        if (beautyFilter != null) {
            beautyFilter.setSmooth(intensity);
        }
    }

    // 不支持 AY_BEAUTY_TYPE_2
    public void setIntensityOfSaturation(float intensity) {
        if (beautyFilter != null) {
            beautyFilter.setSaturation(intensity);
        }
    }

    // 不支持 AY_BEAUTY_TYPE_2
    public void setIntensityOfWhite(float intensity) {
        if (beautyFilter != null) {
            beautyFilter.setWhiten(intensity);
        }
    }

    public void setIntensityOfBigEye(float intensity) {
        if (bigEyeFilter != null) {
            bigEyeFilter.setIntensity(intensity);
        }
    }

    public void setIntensityOfSlimFace(float intensity) {
        if (slimFaceFilter != null) {
            slimFaceFilter.setIntensity(intensity);
        }
    }

    public void setRotateMode(AYGPUImageConstants.AYGPUImageRotationMode rotateMode) {
        this.textureInput.setRotateMode(rotateMode);
        this.oesTextureInput.setRotateMode(rotateMode);
        this.i420DataInput.setRotateMode(rotateMode);
        this.bgraDataInput.setRotateMode(rotateMode);

        if (rotateMode == kAYGPUImageRotateLeft) {
            rotateMode = kAYGPUImageRotateRight;
        }else if (rotateMode == kAYGPUImageRotateRight) {
            rotateMode = kAYGPUImageRotateLeft;
        }

        this.textureOutput.setRotateMode(rotateMode);
        this.oesTextureOutput.setRotateMode(rotateMode);
        this.i420DataOutput.setRotateMode(rotateMode);
        this.bgraDataOutput.setRotateMode(rotateMode);
    }

    protected void commonProcess(boolean useDelay) {

        if (!initCommonProcess) {
            List<AYGPUImageFilter> filterChainArray = new ArrayList<AYGPUImageFilter>();

            if (useDelay && delayFilter != null) {
                filterChainArray.add(delayFilter);
            }

            if (lookupFilter != null) {
                filterChainArray.add(lookupFilter);
            }

            if (beautyFilter != null) {
                filterChainArray.add(beautyFilter);
            }

            if (bigEyeFilter != null) {
                filterChainArray.add(bigEyeFilter);
            }

            if (slimFaceFilter != null) {
                filterChainArray.add(slimFaceFilter);
            }

            if (effectFilter != null) {
                filterChainArray.add(effectFilter);
            }

            if (trackFilter != null) {
                trackFilter.setUseDelay(useDelay);
                commonInputFilter.addTarget(trackFilter);
            }

            if (filterChainArray.size() > 0) {
                commonInputFilter.addTarget(filterChainArray.get(0));
                for (int x = 0; x < filterChainArray.size() - 1; x++) {
                    filterChainArray.get(x).addTarget(filterChainArray.get(x+1));
                }
                filterChainArray.get(filterChainArray.size()-1).addTarget(commonOutputFilter);

            }else {
                commonInputFilter.addTarget(commonOutputFilter);
            }

            initCommonProcess = true;

            if (bigEyeFilter != null && trackFilter != null) {
                bigEyeFilter.setFaceData(trackFilter.faceData());
            }

            if (slimFaceFilter != null && trackFilter != null) {
                slimFaceFilter.setFaceData(trackFilter.faceData());
            }

            if (effectFilter != null && trackFilter != null) {
                effectFilter.setFaceData(trackFilter.faceData());
            }
        }
    }

    public void processWithTexture(final int texture, final int width, final int height) {
        eglContext.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                eglContext.makeCurrent();

                saveOpenGLState();

                commonProcess(false);

                if (!initProcess) {
                    textureInput.addTarget(commonInputFilter);
                    commonOutputFilter.addTarget(textureOutput);
                    initProcess = true;
                }

                // 设置输出的Filter
                textureOutput.setOutputWithBGRATexture(texture, width, height);

                // 设置输入的Filter, 同时开始处理纹理数据
                textureInput.processWithBGRATexture(texture, width, height);

                restoreOpenGLState();
            }
        });
    }

    public int processWithOESTexture(final int oesTexture, final int width, final int height) {
        final int[] texture = new int[1];
        eglContext.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                eglContext.makeCurrent();

                saveOpenGLState();

                commonProcess(false);

                if (!initProcess) {
                    oesTextureInput.addTarget(commonInputFilter);
                    commonOutputFilter.addTarget(oesTextureOutput);
                    initProcess = true;
                }

                // 设置输出的Filter
                oesTextureOutput.setOutputWithOESTexture(width, height);

                // 设置输入的Filter, 同时开始处理纹理数据
                oesTextureInput.processWithOESTexture(oesTexture, width, height);

                texture[0] = oesTextureOutput.getOutputTexture();

                restoreOpenGLState();
            }
        });

        return texture[0];
    }

    public void processWithYUVData(final byte[] yuvData, final int width, final int height) {
        eglContext.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                eglContext.makeCurrent();

                saveOpenGLState();

                commonProcess(true);

                if (!initProcess) {
                    i420DataInput.addTarget(commonInputFilter);
                    commonOutputFilter.addTarget(i420DataOutput);
                    initProcess = true;
                }

                // 设置输出的Filter
                i420DataOutput.setOutputWithYUVData(yuvData, width, height, width);

                // 设置输入的Filter, 同时开始处理纹理数据
                i420DataInput.processWithYUV(yuvData, width, height, width);

                restoreOpenGLState();
            }
        });
    }

    public void processWithBGRAData(final byte[] bgraData, final int width, final int height) {
        eglContext.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                eglContext.makeCurrent();

                saveOpenGLState();

                commonProcess(false);

                if (!initProcess) {
                    bgraDataInput.addTarget(commonInputFilter);
                    commonOutputFilter.addTarget(bgraDataOutput);
                    initProcess = true;
                }

                // 设置输出的Filter
                bgraDataOutput.setOutputWithBGRAData(bgraData, width, height, width);

                // 设置输入的Filter, 同时开始处理纹理数据
                bgraDataInput.processWithBGRAData(bgraData, width, height, width);

                restoreOpenGLState();
            }
        });
    }

    public Bitmap getCurrentImage(final int width, final int height) {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(width*height*4);

        eglContext.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                glReadPixels(0,0,width,height,GL_RGBA, GL_UNSIGNED_BYTE, byteBuffer);
            }
        });
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(byteBuffer);
        Matrix matrix = new Matrix();
        matrix.setScale(1, -1);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        return bitmap;
    }

    private void saveOpenGLState() {
        // 获取当前绑定的FrameBuffer
        glGetIntegerv(GL_FRAMEBUFFER_BINDING, bindingFrameBuffer, 0);

        // 获取当前绑定的RenderBuffer
        glGetIntegerv(GL_RENDERBUFFER_BINDING, bindingRenderBuffer, 0);

        // 获取viewpoint
        glGetIntegerv(GL_VIEWPORT, viewPoint, 0);

        // 获取顶点数据
        vertexAttribEnableArray.clear();
        for (int x = 0 ; x < vertexAttribEnableArraySize; x++) {
            int[] vertexAttribEnable = new int[1];
            glGetVertexAttribiv(x, GL_VERTEX_ATTRIB_ARRAY_ENABLED, vertexAttribEnable, 0);
            if (vertexAttribEnable[0] != 0) {
                vertexAttribEnableArray.add(x);
            }
        }
    }

    private void restoreOpenGLState() {
        // 还原当前绑定的FrameBuffer
        glBindFramebuffer(GL_FRAMEBUFFER, bindingFrameBuffer[0]);

        // 还原当前绑定的RenderBuffer
        glBindRenderbuffer(GL_RENDERBUFFER, bindingRenderBuffer[0]);

        // 还原viewpoint
        glViewport(viewPoint[0], viewPoint[1], viewPoint[2], viewPoint[3]);

        // 还原顶点数据
        for (int x = 0 ; x < vertexAttribEnableArray.size(); x++) {
            glEnableVertexAttribArray(vertexAttribEnableArray.get(x));
        }
    }

    public void destroy() {
        if (eglContext != null) {
            eglContext.syncRunOnRenderThread(new Runnable() {
                @Override
                public void run() {
                    eglContext.makeCurrent();

                    textureInput.destroy();
                    textureOutput.destroy();
                    oesTextureInput.destroy();
                    oesTextureOutput.destroy();
                    i420DataInput.destroy();
                    i420DataOutput.destroy();
                    bgraDataInput.destroy();
                    bgraDataOutput.destroy();
                    commonInputFilter.destroy();
                    commonOutputFilter.destroy();

                    if (delayFilter != null) {
                        delayFilter.destroy();
                    }

                    if (lookupFilter != null) {
                        lookupFilter.destroy();
                    }
                    if (beautyFilter != null) {
                        beautyFilter.destroy();
                    }
                    if (bigEyeFilter != null) {
                        bigEyeFilter.destroy();
                    }
                    if (slimFaceFilter != null) {
                        slimFaceFilter.destroy();
                    }
                    if (effectFilter != null) {
                        effectFilter.destroy();
                    }
                    if (trackFilter != null) {
                        trackFilter.destroy();
                    }
                    if (surfaceTexture != null) {
                        surfaceTexture.release();
                    }

                    eglContext.destroyEGLWindow();
                }
            });
        }
    }
}
