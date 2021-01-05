package com.aiyaapp.aiya.cameraTool;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;

import com.aiyaapp.aiya.gpuImage.AYGLProgram;
import com.aiyaapp.aiya.gpuImage.AYGPUImageConstants;
import com.aiyaapp.aiya.gpuImage.AYGPUImageEGLContext;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFilter;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFramebuffer;

import java.io.IOException;
import java.nio.Buffer;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.*;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageNoRotation;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.needExchangeWidthAndHeightWithRotation;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.textureCoordinatesForRotation;

public class AYCameraPreviewWrap implements SurfaceTexture.OnFrameAvailableListener {

    private static final String kAYOESTextureFragmentShader = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "\n" +
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    private Camera mCamera;

    private AYGPUImageEGLContext eglContext;

    private SurfaceTexture surfaceTexture;

    private int oesTexture;

    private AYCameraPreviewListener previewListener;

    private AYGPUImageFramebuffer outputFramebuffer;

    private AYGPUImageConstants.AYGPUImageRotationMode rotateMode = kAYGPUImageNoRotation;

    private int inputWidth;
    private int inputHeight;

    private AYGLProgram filterProgram;

    private int filterPositionAttribute, filterTextureCoordinateAttribute;
    private int filterInputTextureUniform;

    private Buffer imageVertices = AYGPUImageConstants.floatArrayToBuffer(AYGPUImageConstants.imageVertices);

    public AYCameraPreviewWrap(Camera camera) {
        mCamera = camera;
    }

    public void startPreview(AYGPUImageEGLContext eglContext) {
        this.eglContext = eglContext;
        createGLEnvironment();

        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException ignored) {
        }

        Camera.Size s = mCamera.getParameters().getPreviewSize();
        inputWidth = s.width;
        inputHeight = s.height;

        setRotateMode(rotateMode);

        mCamera.startPreview();
   }

   public void stopPreview() {
       destroyGLContext();
       mCamera.stopPreview();
   }

    public void setPreviewListener(AYCameraPreviewListener previewListener) {
        this.previewListener = previewListener;
    }

    public void setRotateMode(AYGPUImageConstants.AYGPUImageRotationMode rotateMode) {
        this.rotateMode = rotateMode;

        if (needExchangeWidthAndHeightWithRotation(rotateMode)) {
            int temp = inputWidth;
            inputWidth = inputHeight;
            inputHeight = temp;
        }
    }

    private void createGLEnvironment() {
        eglContext.syncRunOnRenderThread(() -> {
            eglContext.makeCurrent();

            oesTexture = createOESTextureID();
            surfaceTexture = new SurfaceTexture(oesTexture);
            surfaceTexture.setOnFrameAvailableListener(AYCameraPreviewWrap.this);

            filterProgram = new AYGLProgram(AYGPUImageFilter.kAYGPUImageVertexShaderString, kAYOESTextureFragmentShader);
            filterProgram.link();

            filterPositionAttribute = filterProgram.attributeIndex("position");
            filterTextureCoordinateAttribute = filterProgram.attributeIndex("inputTextureCoordinate");
            filterInputTextureUniform = filterProgram.uniformIndex("inputImageTexture");
        });
    }

    @Override
    public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
        eglContext.syncRunOnRenderThread(() -> {
            eglContext.makeCurrent();

            if (EGL14.eglGetCurrentDisplay() != EGL14.EGL_NO_DISPLAY) {
                surfaceTexture.updateTexImage();

                glFinish();

                // 因为在shader中处理oes纹理需要使用到扩展类型, 必须要先转换为普通纹理再传给下一级
                renderToFramebuffer(oesTexture);

                if (previewListener != null) {
                    previewListener.cameraVideoOutput(outputFramebuffer.texture[0], inputWidth, inputHeight, surfaceTexture.getTimestamp());
                }
            }
        });
    }

    private void renderToFramebuffer(int oesTexture) {

        filterProgram.use();

        if (outputFramebuffer != null) {
            if (inputWidth != outputFramebuffer.width || inputHeight != outputFramebuffer.height) {
                outputFramebuffer.destroy();
                outputFramebuffer = null;
            }
        }

        if (outputFramebuffer == null) {
            outputFramebuffer = new AYGPUImageFramebuffer(inputWidth, inputHeight);
        }

        outputFramebuffer.activateFramebuffer();

        glClearColor(0, 0, 0, 0);
        glClear(GL_COLOR_BUFFER_BIT);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, oesTexture);

        glUniform1i(filterInputTextureUniform, 2);

        glEnableVertexAttribArray(filterPositionAttribute);
        glEnableVertexAttribArray(filterTextureCoordinateAttribute);

        glVertexAttribPointer(filterPositionAttribute, 2, GL_FLOAT, false, 0, imageVertices);
        glVertexAttribPointer(filterTextureCoordinateAttribute, 2, GL_FLOAT, false, 0, AYGPUImageConstants.floatArrayToBuffer(textureCoordinatesForRotation(rotateMode)));

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        glDisableVertexAttribArray(filterPositionAttribute);
        glDisableVertexAttribArray(filterTextureCoordinateAttribute);
    }

    private int createOESTextureID() {
        int[] texture = new int[1];
        glGenTextures(1, texture, 0);

        glBindTexture(GL_TEXTURE_EXTERNAL_OES, texture[0]);
        glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        return texture[0];
    }

    private void destroyGLContext() {
        eglContext.syncRunOnRenderThread(() -> {

            filterProgram.destroy();

            if (outputFramebuffer != null) {
                outputFramebuffer.destroy();
            }
        });
    }
}

