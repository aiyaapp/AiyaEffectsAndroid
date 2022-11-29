package com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput;

import android.util.Log;

import com.aiyaapp.aiya.gpuImage.AYGLProgram;
import com.aiyaapp.aiya.gpuImage.AYGPUImageConstants;
import com.aiyaapp.aiya.gpuImage.AYGPUImageEGLContext;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFilter;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFramebuffer;
import com.aiyaapp.aiya.gpuImage.AYGPUImageInput;

import java.nio.Buffer;

import static android.opengl.GLES20.*;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageNoRotation;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.TAG;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageFilter.kAYGPUImagePassthroughFragmentShaderString;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageFilter.kAYGPUImageVertexShaderString;

public class AYGPUImageOESTextureOutput implements AYGPUImageInput {

    private AYGPUImageEGLContext context;
    private Buffer imageVertices = AYGPUImageConstants.floatArrayToBuffer(AYGPUImageConstants.imageVertices);

    protected AYGPUImageFramebuffer firstInputFramebuffer;

    protected AYGLProgram filterProgram;

    protected int filterPositionAttribute, filterTextureCoordinateAttribute;
    protected int filterInputTextureUniform;

    protected int inputWidth;
    protected int inputHeight;

    private AYGPUImageConstants.AYGPUImageRotationMode rotateMode = kAYGPUImageNoRotation;

    protected AYGPUImageFramebuffer outputFramebuffer;

    public AYGPUImageOESTextureOutput(AYGPUImageEGLContext context) {
        this.context = context;
        context.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                filterProgram = new AYGLProgram(kAYGPUImageVertexShaderString, kAYGPUImagePassthroughFragmentShaderString);
                filterProgram.link();

                filterPositionAttribute = filterProgram.attributeIndex("position");
                filterTextureCoordinateAttribute = filterProgram.attributeIndex("inputTextureCoordinate");
                filterInputTextureUniform = filterProgram.uniformIndex("inputImageTexture");
                filterProgram.use();
            }
        });
    }

    protected void renderToTexture(final Buffer vertices, final Buffer textureCoordinates) {
        context.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {

                filterProgram.use();

                outputFramebuffer.activateFramebuffer();

                glClearColor(0, 0, 0, 0);
                glClear(GL_COLOR_BUFFER_BIT);

                glActiveTexture(GL_TEXTURE2);
                glBindTexture(GL_TEXTURE_2D, firstInputFramebuffer.texture[0]);

                glUniform1i(filterInputTextureUniform, 2);

                glEnableVertexAttribArray(filterPositionAttribute);
                glEnableVertexAttribArray(filterTextureCoordinateAttribute);

                glVertexAttribPointer(filterPositionAttribute, 2, GL_FLOAT, false, 0, vertices);
                glVertexAttribPointer(filterTextureCoordinateAttribute, 2, GL_FLOAT, false, 0, textureCoordinates);

                glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

                glDisableVertexAttribArray(filterPositionAttribute);
                glDisableVertexAttribArray(filterTextureCoordinateAttribute);
            }
        });
    }

    public void setOutputWithOESTexture(int width, int height) {
        this.inputWidth = width;
        this.inputHeight = height;


        if (outputFramebuffer != null) {
            if (inputWidth != outputFramebuffer.width || inputHeight != outputFramebuffer.height) {
                outputFramebuffer.destroy();
                outputFramebuffer = null;
            }
        }

        if (outputFramebuffer == null) {
            outputFramebuffer = new AYGPUImageFramebuffer(inputWidth, inputHeight);
        }
    }

    public void setRotateMode(AYGPUImageConstants.AYGPUImageRotationMode rotateMode) {
        this.rotateMode = rotateMode;
    }

    public void destroy() {
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

    @Override
    public void setInputSize(int width, int height) {

    }

    @Override
    public void setInputFramebuffer(AYGPUImageFramebuffer newInputFramebuffer) {
        firstInputFramebuffer = newInputFramebuffer;
    }

    @Override
    public void newFrameReady() {
        renderToTexture(imageVertices,  AYGPUImageConstants.floatArrayToBuffer(AYGPUImageConstants.textureCoordinatesForRotation(rotateMode)));
    }

    public int getOutputTexture() {
        return outputFramebuffer.texture[0];
    }
}
