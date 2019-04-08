package com.aiyaapp.aiya;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.util.Log;

import com.aiyaapp.aiya.gpuImage.AYGPUImageConstants;
import com.aiyaapp.aiya.gpuImage.AYGPUImageEGLContext;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFilter;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.AYGPUImageShortVideoFilter;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput.AYGPUImageTextureInput;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput.AYGPUImageTextureOutput;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_FRAMEBUFFER_BINDING;
import static android.opengl.GLES20.GL_RENDERBUFFER;
import static android.opengl.GLES20.GL_RENDERBUFFER_BINDING;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.GL_VERTEX_ATTRIB_ARRAY_ENABLED;
import static android.opengl.GLES20.GL_VIEWPORT;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindRenderbuffer;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glFinish;
import static android.opengl.GLES20.glGetIntegerv;
import static android.opengl.GLES20.glGetVertexAttribiv;
import static android.opengl.GLES20.glReadPixels;
import static android.opengl.GLES20.glViewport;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageRotateLeft;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageRotateRight;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.TAG;

public class AYShortVideoEffectHandler {

    private AYGPUImageEGLContext eglContext;
    private SurfaceTexture surfaceTexture;

    private AYGPUImageTextureInput textureInput;
    private AYGPUImageTextureOutput textureOutput;

    private AYGPUImageFilter commonInputFilter;
    private AYGPUImageFilter commonOutputFilter;

    private AYGPUImageShortVideoFilter shortVideoFilter;

    private boolean initCommonProcess = false;
    private boolean initProcess = false;

    private int[] bindingFrameBuffer = new int[1];
    private int[] bindingRenderBuffer = new int[1];
    private int[] viewPoint = new int[4];
    private int vertexAttribEnableArraySize = 5;
    private ArrayList<Integer> vertexAttribEnableArray = new ArrayList(vertexAttribEnableArraySize);

    public AYShortVideoEffectHandler(final Context context) {
        this(context, true);
    }

    public AYShortVideoEffectHandler(final Context context, boolean useCurrentEGLContext) {
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

                commonInputFilter = new AYGPUImageFilter(eglContext);
                commonOutputFilter = new AYGPUImageFilter(eglContext);

                shortVideoFilter = new AYGPUImageShortVideoFilter(eglContext);
            }
        });
    }

    public void setTypeOfShortVideo(AYGPUImageShortVideoFilter.AY_VIDEO_EFFECT_TYPE type) {
        if (shortVideoFilter != null) {
            shortVideoFilter.setType(type);
        }
    }

    public void setRotateMode(AYGPUImageConstants.AYGPUImageRotationMode rotateMode) {
        this.textureInput.setRotateMode(rotateMode);

        if (rotateMode == kAYGPUImageRotateLeft) {
            rotateMode = kAYGPUImageRotateRight;
        }else if (rotateMode == kAYGPUImageRotateRight) {
            rotateMode = kAYGPUImageRotateLeft;
        }

        this.textureOutput.setRotateMode(rotateMode);
    }

    private void commonProcess() {

        if (!initCommonProcess) {
            List<AYGPUImageFilter> filterChainArray = new ArrayList<AYGPUImageFilter>();

            if (shortVideoFilter != null) {
                filterChainArray.add(shortVideoFilter);
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
        }
    }

    public void processWithTexture(final int texture, final int width, final int height) {
        eglContext.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {

                saveOpenGLState();

                commonProcess();

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

    public Bitmap getCurrentImage(final int width, final int height) {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(width*height*4);

        eglContext.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                glFinish();
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
        textureInput.destroy();
        textureOutput.destroy();
        commonInputFilter.destroy();
        commonOutputFilter.destroy();

        if (shortVideoFilter != null) {
            shortVideoFilter.destroy();
            shortVideoFilter = null;
        }
        if (surfaceTexture != null) {
            surfaceTexture.release();
        }
        if (eglContext != null) {
            eglContext.destroyEGLWindow();
        }
    }
}
