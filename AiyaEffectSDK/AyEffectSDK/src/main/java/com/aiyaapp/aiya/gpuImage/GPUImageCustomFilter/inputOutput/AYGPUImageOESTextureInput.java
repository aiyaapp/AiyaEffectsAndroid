package com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE2;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageNoRotation;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.needExchangeWidthAndHeightWithRotation;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageFilter.kAYGPUImagePassthroughFragmentShaderString;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageFilter.kAYGPUImageVertexShaderString;

import com.aiyaapp.aiya.gpuImage.AYGLProgram;
import com.aiyaapp.aiya.gpuImage.AYGPUImageConstants;
import com.aiyaapp.aiya.gpuImage.AYGPUImageEGLContext;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFramebuffer;
import com.aiyaapp.aiya.gpuImage.AYGPUImageInput;
import com.aiyaapp.aiya.gpuImage.AYGPUImageOutput;

import java.nio.Buffer;

public class AYGPUImageOESTextureInput extends AYGPUImageOutput {

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

    private AYGPUImageEGLContext context;

    private Buffer imageVertices = AYGPUImageConstants.floatArrayToBuffer(AYGPUImageConstants.imageVertices);

    protected AYGPUImageFramebuffer outputFramebuffer;

    protected AYGLProgram filterProgram;

    protected int filterPositionAttribute, filterTextureCoordinateAttribute;
    protected int filterInputTextureUniform;

    private AYGPUImageConstants.AYGPUImageRotationMode rotateMode = kAYGPUImageNoRotation;

    public AYGPUImageOESTextureInput(AYGPUImageEGLContext context) {
        this.context = context;
        context.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                filterProgram = new AYGLProgram(kAYGPUImageVertexShaderString, kAYOESTextureFragmentShader);
                filterProgram.link();

                filterPositionAttribute = filterProgram.attributeIndex("position");
                filterTextureCoordinateAttribute = filterProgram.attributeIndex("inputTextureCoordinate");
                filterInputTextureUniform = filterProgram.uniformIndex("inputImageTexture");
                filterProgram.use();
            }
        });
    }

    public void processWithOESTexture(final int texture, final int width, final int height) {
        context.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {

                int inputWidth = width;
                int inputHeight = height;

                if (needExchangeWidthAndHeightWithRotation(rotateMode)) {
                    int temp = width;
                    inputWidth = height;
                    inputHeight = temp;
                }

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
                glBindTexture(GL_TEXTURE_EXTERNAL_OES, texture);

                glUniform1i(filterInputTextureUniform, 2);

                glEnableVertexAttribArray(filterPositionAttribute);
                glEnableVertexAttribArray(filterTextureCoordinateAttribute);

                glVertexAttribPointer(filterPositionAttribute, 2, GL_FLOAT, false, 0, imageVertices);
                glVertexAttribPointer(filterTextureCoordinateAttribute, 2, GL_FLOAT, false, 0, AYGPUImageConstants.floatArrayToBuffer(AYGPUImageConstants.textureCoordinatesForRotation(rotateMode)));

                glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

                glDisableVertexAttribArray(filterPositionAttribute);
                glDisableVertexAttribArray(filterTextureCoordinateAttribute);

                for (AYGPUImageInput currentTarget : getTargets()) {
                    currentTarget.setInputSize(inputWidth, inputHeight);
                    currentTarget.setInputFramebuffer(outputFramebuffer);
                }

                for (AYGPUImageInput currentTarget : getTargets()) {
                    currentTarget.newFrameReady();
                }
            }
        });
    }

    public void setRotateMode(AYGPUImageConstants.AYGPUImageRotationMode rotateMode) {
        this.rotateMode = rotateMode;
    }

    public void destroy() {
        removeAllTargets();

        context.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                filterProgram.destroy();

                if (outputFramebuffer != null) {
                    outputFramebuffer.destroy();
                }
            }
        });
    }
}
