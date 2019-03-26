package com.aiyaapp.aiya;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.AYGPUImageBeautyFilter;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.AYGPUImageBigEyeFilter;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.AYGPUImageLookupFilter;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.AYGPUImageSlimFaceFilter;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.AYGPUImageTrackFilter;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput.AYGPUImageI420DataInput;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput.AYGPUImageI420DataOutput;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput.AYGPUImageTextureInput;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput.AYGPUImageTextureOutput;
import com.aiyaapp.aiya.gpuImage.AYGPUImageConstants;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFilter;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.*;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageRotateLeft;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageRotateRight;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageEGLContext.syncRunOnRenderThread;

public class AYEffectHandler {

    private AYGPUImageTextureInput textureInput;
    private AYGPUImageTextureOutput textureOutput;

    private AYGPUImageI420DataInput i420DataInput;
    private AYGPUImageI420DataOutput i420DataOutput;

    private AYGPUImageFilter commonInputFilter;
    private AYGPUImageFilter commonOutputFilter;

    private AYGPUImageLookupFilter lookupFilter;
    private AYGPUImageBeautyFilter beautyFilter;
    private AYGPUImageTrackFilter trackFilter;
    private AYGPUImageBigEyeFilter bigEyeFilter;
    private AYGPUImageSlimFaceFilter slimFaceFilter;

    private boolean initCommonProcess = false;
    private boolean initProcess = false;

    private int[] bindingFrameBuffer = new int[1];
    private int[] bindingRenderBuffer = new int[1];
    private int[] viewPoint = new int[4];
    private int vertexAttribEnableArraySize = 5;
    private ArrayList<Integer> vertexAttribEnableArray = new ArrayList(vertexAttribEnableArraySize);

    public AYEffectHandler(final Context context) {

        syncRunOnRenderThread(new Runnable(){
            @Override
            public void run() {
                textureInput = new AYGPUImageTextureInput();
                textureOutput = new AYGPUImageTextureOutput();

                i420DataInput = new AYGPUImageI420DataInput();
                i420DataOutput = new AYGPUImageI420DataOutput();

                commonInputFilter = new AYGPUImageFilter();
                commonOutputFilter = new AYGPUImageFilter();

                try {
                    Bitmap lookupBitmap = BitmapFactory.decodeStream(context.getAssets().open("lookup.png"));
                    lookupFilter = new AYGPUImageLookupFilter(lookupBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                beautyFilter = new AYGPUImageBeautyFilter(AyBeauty.AY_BEAUTY_TYPE.AY_BEAUTY_TYPE_5);
                bigEyeFilter = new AYGPUImageBigEyeFilter();
                slimFaceFilter = new AYGPUImageSlimFaceFilter();

                trackFilter = new AYGPUImageTrackFilter(context);
            }
        });
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

    public void setIntensityOfBeauty(float intensity) {
        if (beautyFilter != null) {
            beautyFilter.setIntensity(intensity);
        }
    }

    public void setIntensityOfSmooth(float intensity) {
        if (beautyFilter != null) {
            beautyFilter.setSmooth(intensity);
        }
    }

    public void setIntensityOfSaturation(float intensity) {
        if (beautyFilter != null) {
            beautyFilter.setSaturation(intensity);
        }
    }

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

            if (trackFilter != null) {
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
        }

        if (bigEyeFilter != null) {
            bigEyeFilter.setFaceData(trackFilter.faceData());
        }

        if (slimFaceFilter != null) {
            slimFaceFilter.setFaceData(trackFilter.faceData());
        }
    }

    public void processWithTexture(final int texture, final int width, final int height) {
        syncRunOnRenderThread(new Runnable() {
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

    public void processWithYUVData(final byte[] yData, final byte[] uData, final byte[] vData, final int width, final int height) {
        syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {

                saveOpenGLState();

                commonProcess();

                if (!initProcess) {
                    i420DataInput.addTarget(commonInputFilter);
                    commonOutputFilter.addTarget(i420DataOutput);
                    initProcess = true;
                }

                // 设置输出的Filter
                i420DataOutput.setOutputWithYUVData(yData, uData, vData, width, height, width);

                // 设置输入的Filter, 同时开始处理纹理数据
                i420DataInput.processWithYUV(yData, uData, vData, width, height, width);

                restoreOpenGLState();
            }
        });
    }

    public Bitmap getCurrentImage(final int width, final int height) {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(width*height*4);

        syncRunOnRenderThread(new Runnable() {
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
        textureInput.destroy();
        textureOutput.destroy();
        commonInputFilter.destroy();
        commonOutputFilter.destroy();

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
        if (trackFilter != null) {
            trackFilter.destroy();
        }
    }
}
