package com.aiyaapp.aiya.gpuImage;

import java.nio.Buffer;

import static android.opengl.GLES20.*;

public class AYGPUImageFilter extends AYGPUImageOutput implements AYGPUImageInput{

    public static final String kAYGPUImageVertexShaderString = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "}";

    public static final String kAYGPUImagePassthroughFragmentShaderString = "" +
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "uniform sampler2D inputImageTexture;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    protected AYGPUImageEGLContext context;

    private Buffer imageVertices = AYGPUImageConstants.floatArrayToBuffer(AYGPUImageConstants.imageVertices);
    private Buffer textureCoordinates = AYGPUImageConstants.floatArrayToBuffer(AYGPUImageConstants.noRotationTextureCoordinates);

    protected AYGPUImageFramebuffer outputFramebuffer;
    protected AYGPUImageFramebuffer firstInputFramebuffer;

    protected AYGLProgram filterProgram;

    protected int filterPositionAttribute, filterTextureCoordinateAttribute;
    protected int filterInputTextureUniform;

    protected int inputWidth;
    protected int inputHeight;

    public AYGPUImageFilter(final AYGPUImageEGLContext context, final String vertexShaderString, final String fragmentShaderString) {
        this.context = context;
        context.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                filterProgram = new AYGLProgram(vertexShaderString, fragmentShaderString);
                filterProgram.link();

                filterPositionAttribute = filterProgram.attributeIndex("position");
                filterTextureCoordinateAttribute = filterProgram.attributeIndex("inputTextureCoordinate");
                filterInputTextureUniform = filterProgram.uniformIndex("inputImageTexture");
                filterProgram.use();
            }
        });
    }

    public AYGPUImageFilter(AYGPUImageEGLContext context) {
        this(context, kAYGPUImageVertexShaderString, kAYGPUImagePassthroughFragmentShaderString);
    }

    protected void renderToTexture(final Buffer vertices, final Buffer textureCoordinates) {
        context.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
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

    protected void informTargetsAboutNewFrame() {
        for (AYGPUImageInput currentTarget : getTargets()) {
            currentTarget.setInputSize(outputWidth(), outputHeight());
            currentTarget.setInputFramebuffer(outputFramebuffer);
        }

        for (AYGPUImageInput currentTarget : getTargets()) {
            currentTarget.newFrameReady();
        }
    }

    protected int outputWidth() {
        return inputWidth;
    }

    protected int outputHeight() {
        return inputHeight;
    }

    @Override
    public void setInputSize(int width, int height) {
        inputWidth = width;
        inputHeight = height;
    }

    @Override
    public void setInputFramebuffer(AYGPUImageFramebuffer newInputFramebuffer) {
        firstInputFramebuffer = newInputFramebuffer;
    }

    @Override
    public void newFrameReady() {
        renderToTexture(imageVertices, textureCoordinates);
        informTargetsAboutNewFrame();
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