package com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.inputOutput;

import android.util.Log;

import com.aiyaapp.aiya.gpuImage.AYGLProgram;
import com.aiyaapp.aiya.gpuImage.AYGPUImageConstants;
import com.aiyaapp.aiya.gpuImage.AYGPUImageEGLContext;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFramebuffer;
import com.aiyaapp.aiya.gpuImage.AYGPUImageInput;
import com.aiyaapp.aiya.gpuImage.AYGPUImageOutput;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageNoRotation;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.TAG;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.needExchangeWidthAndHeightWithRotation;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageFilter.kAYGPUImageVertexShaderString;

public class AYGPUImageI420DataInput extends AYGPUImageOutput {

    private static final String kAYRGBConversionFragmentShaderString = "" +
            "varying highp vec2 textureCoordinate;\n" +
            "uniform sampler2D yTexture;\n" +
            "uniform sampler2D uTexture;\n" +
            "uniform sampler2D vTexture;\n" +
            "uniform mediump mat3 colorConversionMatrix;\n" +
            "void main()\n" +
            "{\n" +
            "    mediump vec3 yuv;\n" +
            "    lowp vec3 rgb;\n" +
            "    yuv.x = texture2D(yTexture, textureCoordinate).r;\n" +
            "    yuv.y = texture2D(uTexture, textureCoordinate).r - 0.5;\n" +
            "    yuv.z = texture2D(vTexture, textureCoordinate).r - 0.5;\n" +
            "    rgb = colorConversionMatrix * yuv;\n" +
            "    gl_FragColor = vec4(rgb, 1);\n" +
            "}";

    public static float kImageVertices[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f,  1.0f,
            1.0f,  1.0f,
    };

    private static final float[] kAYColorConversion601FullRangeDefault = {
            1.000f,        1.000f,       1.000f,
            0.000f,       -0.343f,       1.765f,
            1.400f,       -0.711f,       0.000f
    };

    private AYGPUImageEGLContext context;

    private Buffer imageVertices = AYGPUImageConstants.floatArrayToBuffer(kImageVertices);

    protected AYGPUImageFramebuffer outputFramebuffer;

    protected AYGLProgram filterProgram;

    protected int filterPositionAttribute, filterTextureCoordinateAttribute;

    protected int yTextureUniform, uTextureUniform, vTextureUniform;

    protected int colorConversionUniform;

    protected int[] inputYTexture = {0}, inputUTexture = {0}, inputVTexture = {0};

    private AYGPUImageConstants.AYGPUImageRotationMode rotateMode = kAYGPUImageNoRotation;

    public AYGPUImageI420DataInput(AYGPUImageEGLContext context) {
        this.context = context;
        context.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                filterProgram = new AYGLProgram(kAYGPUImageVertexShaderString, kAYRGBConversionFragmentShaderString);
                filterProgram.link();

                filterPositionAttribute = filterProgram.attributeIndex("position");
                filterTextureCoordinateAttribute = filterProgram.attributeIndex("inputTextureCoordinate");
                yTextureUniform = filterProgram.uniformIndex("yTexture");
                uTextureUniform = filterProgram.uniformIndex("uTexture");
                vTextureUniform = filterProgram.uniformIndex("vTexture");
                colorConversionUniform = filterProgram.uniformIndex("colorConversionMatrix");
                filterProgram.use();
            }
        });
    }

    public void processWithYUV(final byte[] yuvData, final int width, final int height, final int lineSize) {
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

                if (inputYTexture[0] == 0) {
                    glGenTextures(1, inputYTexture, 0);
                    glBindTexture(GL_TEXTURE_2D, inputYTexture[0]);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
                    glBindTexture(GL_TEXTURE_2D, 0);
                }

                if (inputUTexture[0] == 0) {
                    glGenTextures(1, inputUTexture, 0);
                    glBindTexture(GL_TEXTURE_2D, inputUTexture[0]);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
                    glBindTexture(GL_TEXTURE_2D, 0);
                }

                if (inputVTexture[0] == 0) {
                    glGenTextures(1, inputVTexture, 0);
                    glBindTexture(GL_TEXTURE_2D, inputVTexture[0]);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
                    glBindTexture(GL_TEXTURE_2D, 0);
                }

                ByteBuffer yBuffer = ByteBuffer.allocate(lineSize*height);
//                yBuffer.order(ByteOrder.BIG_ENDIAN);
                yBuffer.put(yuvData, 0, lineSize*height);
                yBuffer.position(0);
                glActiveTexture(GL_TEXTURE1);
                glBindTexture(GL_TEXTURE_2D, inputYTexture[0]);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, lineSize, height, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, yBuffer);
                glUniform1i(yTextureUniform, 1);

                ByteBuffer uBuffer = ByteBuffer.allocate(lineSize*height/4);
//                uBuffer.order(ByteOrder.BIG_ENDIAN);
                uBuffer.put(yuvData, lineSize*height, lineSize*height/4);
                uBuffer.position(0);
                glActiveTexture(GL_TEXTURE2);
                glBindTexture(GL_TEXTURE_2D, inputUTexture[0]);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, lineSize / 2, height / 2, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, uBuffer);
                glUniform1i(uTextureUniform, 2);

                ByteBuffer vBuffer = ByteBuffer.allocate(lineSize*height/4);
//                vBuffer.order(ByteOrder.BIG_ENDIAN);
                vBuffer.put(yuvData, lineSize*height+lineSize*height/4, lineSize*height/4);
                vBuffer.position(0);
                glActiveTexture(GL_TEXTURE3);
                glBindTexture(GL_TEXTURE_2D, inputVTexture[0]);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, lineSize / 2, height / 2, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, vBuffer);
                glUniform1i(vTextureUniform, 3);

                glUniformMatrix3fv(colorConversionUniform, 1, false, AYGPUImageConstants.floatArrayToBuffer(kAYColorConversion601FullRangeDefault));

                glEnableVertexAttribArray(filterPositionAttribute);
                glEnableVertexAttribArray(filterTextureCoordinateAttribute);

                float[] textureCoordinates = AYGPUImageConstants.textureCoordinatesForRotation(rotateMode);

                // 处理lineSize != width
                for (int x = 0; x < textureCoordinates.length; x = x + 2) {
                    if (textureCoordinates[x] == 1) {
                        textureCoordinates[x] = (float)width / (float)lineSize;
                    }
                }

                glVertexAttribPointer(filterPositionAttribute, 2, GL_FLOAT, false, 0, imageVertices);
                glVertexAttribPointer(filterTextureCoordinateAttribute, 2, GL_FLOAT, false, 0, AYGPUImageConstants.floatArrayToBuffer(textureCoordinates));

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

                if (inputYTexture[0] != 0) {
                    glDeleteTextures(1, inputYTexture, 0);
                    inputYTexture[0] = 0;
                }

                if (inputUTexture[0] != 0) {
                    glDeleteTextures(1, inputUTexture, 0);
                    inputUTexture[0] = 0;
                }

                if (inputVTexture[0] != 0) {
                    glDeleteTextures(1, inputVTexture, 0);
                    inputVTexture[0] = 0;
                }
            }
        });
    }
}
