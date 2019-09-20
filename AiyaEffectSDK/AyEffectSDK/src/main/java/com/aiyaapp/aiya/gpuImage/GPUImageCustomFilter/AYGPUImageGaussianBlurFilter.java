package com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter;

import android.annotation.SuppressLint;

import com.aiyaapp.aiya.gpuImage.AYGLProgram;
import com.aiyaapp.aiya.gpuImage.AYGPUImageEGLContext;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFilter;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFramebuffer;

import java.nio.Buffer;
import java.util.Locale;

import static android.opengl.GLES20.*;

public class AYGPUImageGaussianBlurFilter extends AYGPUImageFilter {

    private int verticalPassTexelWidthOffsetUniform;
    private int verticalPassTexelHeightOffsetUniform;

    private AYGPUImageFramebuffer secondOutputFramebuffer;

    private float blurRadiusInPixels;

    private boolean needResetProgram = false;
    private String newGaussianBlurVertexShader;
    private String newGaussianBlurFragmentShader;

    public AYGPUImageGaussianBlurFilter(AYGPUImageEGLContext context) {
        super(context);
    }

    private static String vertexShaderForOptimizedBlurOfRadius(int blurRadius, float sigma) {
        // First, generate the normal Gaussian weights for a given sigma
        float[] standardGaussianWeights = new float[blurRadius + 1];
        float sumOfWeights = 0.0f;
        for (int currentGaussianWeightIndex = 0; currentGaussianWeightIndex < blurRadius + 1; currentGaussianWeightIndex++) {

            standardGaussianWeights[currentGaussianWeightIndex] = (float) ((1.0 / Math.sqrt(2.0 * Math.PI * Math.pow(sigma, 2.0))) * Math.exp(-Math.pow(currentGaussianWeightIndex, 2.0) / (2.0 * Math.pow(sigma, 2.0))));

            if (currentGaussianWeightIndex == 0) {
                sumOfWeights += standardGaussianWeights[currentGaussianWeightIndex];
            } else {
                sumOfWeights += 2.0 * standardGaussianWeights[currentGaussianWeightIndex];
            }
        }

        // Next, normalize these weights to prevent the clipping of the Gaussian curve at the end of the discrete samples from reducing luminance
        for (int currentGaussianWeightIndex = 0; currentGaussianWeightIndex < blurRadius + 1; currentGaussianWeightIndex++) {
            standardGaussianWeights[currentGaussianWeightIndex] = standardGaussianWeights[currentGaussianWeightIndex] / sumOfWeights;
        }

        // From these weights we calculate the offsets to read interpolated values from
        int numberOfOptimizedOffsets = Math.min(blurRadius / 2 + (blurRadius % 2), 7);
        float[] optimizedGaussianOffsets = new float[numberOfOptimizedOffsets];

        for (int currentOptimizedOffset = 0; currentOptimizedOffset < numberOfOptimizedOffsets; currentOptimizedOffset++) {
            float firstWeight = standardGaussianWeights[currentOptimizedOffset * 2 + 1];
            float secondWeight = standardGaussianWeights[currentOptimizedOffset * 2 + 2];

            float optimizedWeight = firstWeight + secondWeight;

            optimizedGaussianOffsets[currentOptimizedOffset] = (firstWeight * (currentOptimizedOffset * 2 + 1) + secondWeight * (currentOptimizedOffset * 2 + 2)) / optimizedWeight;
        }

        StringBuilder shaderString = new StringBuilder();

        // Header
        shaderString.append(String.format(Locale.ENGLISH,
                "attribute vec4 position;\n" +
                "attribute vec4 inputTextureCoordinate;\n" +
                "\n" +
                "uniform float texelWidthOffset;\n" +
                "uniform float texelHeightOffset;\n" +
                "\n" +
                "varying vec2 blurCoordinates[%d];\n" +
                "\n" +
                "void main()\n " +
                "{\n" +
                "  gl_Position = position;\n" +
                "\n" +
                "  vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);\n", (long) (1 + (numberOfOptimizedOffsets * 2))));

        // Inner offset loop
        shaderString.append("blurCoordinates[0] = inputTextureCoordinate.xy;\n");
        for (int currentOptimizedOffset = 0; currentOptimizedOffset < numberOfOptimizedOffsets; currentOptimizedOffset++) {
            shaderString.append(String.format(Locale.ENGLISH,
                "blurCoordinates[%d] = inputTextureCoordinate.xy + singleStepOffset * %f;\n" +
                "blurCoordinates[%d] = inputTextureCoordinate.xy - singleStepOffset * %f;\n",
                    (long)((currentOptimizedOffset * 2) + 1),
                    optimizedGaussianOffsets[currentOptimizedOffset],
                    (long)((currentOptimizedOffset * 2) + 2),
                    optimizedGaussianOffsets[currentOptimizedOffset]));

        }

        // Footer
        shaderString.append("}\n");

        return shaderString.toString();
    }

    @SuppressLint("DefaultLocale")
    private static String fragmentShaderForOptimizedBlurOfRadius(int blurRadius, float sigma) {

        // First, generate the normal Gaussian weights for a given sigma
        float[] standardGaussianWeights = new float[blurRadius + 1];
        float sumOfWeights = 0.0f;
        for (int currentGaussianWeightIndex = 0; currentGaussianWeightIndex < blurRadius + 1; currentGaussianWeightIndex++) {

            standardGaussianWeights[currentGaussianWeightIndex] = (float) ((1.0 / Math.sqrt(2.0 * Math.PI * Math.pow(sigma, 2.0))) * Math.exp(-Math.pow(currentGaussianWeightIndex, 2.0) / (2.0 * Math.pow(sigma, 2.0))));

            if (currentGaussianWeightIndex == 0) {
                sumOfWeights += standardGaussianWeights[currentGaussianWeightIndex];
            } else {
                sumOfWeights += 2.0 * standardGaussianWeights[currentGaussianWeightIndex];
            }
        }

        // Next, normalize these weights to prevent the clipping of the Gaussian curve at the end of the discrete samples from reducing luminance
        for (int currentGaussianWeightIndex = 0; currentGaussianWeightIndex < blurRadius + 1; currentGaussianWeightIndex++) {
            standardGaussianWeights[currentGaussianWeightIndex] = standardGaussianWeights[currentGaussianWeightIndex] / sumOfWeights;
        }

        // From these weights we calculate the offsets to read interpolated values from
        int numberOfOptimizedOffsets = Math.min(blurRadius / 2 + (blurRadius % 2), 7);
        int trueNumberOfOptimizedOffsets = blurRadius / 2 + (blurRadius % 2);

        StringBuilder shaderString = new StringBuilder();

        // Header
        shaderString.append(String.format(Locale.ENGLISH,
            "uniform sampler2D inputImageTexture;\n" +
            "uniform highp float texelWidthOffset;\n" +
            "uniform highp float texelHeightOffset;\n" +
            "\n" +
            "varying highp vec2 blurCoordinates[%d];\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "  lowp vec4 sum = vec4(0.0);\n", (long)(1 + (numberOfOptimizedOffsets * 2))));

        // Inner texture loop
        shaderString.append(String.format(Locale.ENGLISH,"sum += texture2D(inputImageTexture, blurCoordinates[0]) * %f;\n", standardGaussianWeights[0]));

        for (int currentBlurCoordinateIndex = 0; currentBlurCoordinateIndex < numberOfOptimizedOffsets; currentBlurCoordinateIndex++) {
            float firstWeight = standardGaussianWeights[currentBlurCoordinateIndex * 2 + 1];
            float secondWeight = standardGaussianWeights[currentBlurCoordinateIndex * 2 + 2];
            float optimizedWeight = firstWeight + secondWeight;

            shaderString.append(String.format(Locale.ENGLISH,"sum += texture2D(inputImageTexture, blurCoordinates[%d]) * %f;\n", (long)((currentBlurCoordinateIndex * 2) + 1), optimizedWeight));
            shaderString.append(String.format(Locale.ENGLISH,"sum += texture2D(inputImageTexture, blurCoordinates[%d]) * %f;\n", (long)((currentBlurCoordinateIndex * 2) + 2), optimizedWeight));
        }

        // If the number of required samples exceeds the amount we can pass in via varyings, we have to do dependent texture reads in the fragment shader
        if (trueNumberOfOptimizedOffsets > numberOfOptimizedOffsets) {
            shaderString.append("highp vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);\n");

            for (int currentOverlowTextureRead = numberOfOptimizedOffsets; currentOverlowTextureRead < trueNumberOfOptimizedOffsets; currentOverlowTextureRead++) {
                float firstWeight = standardGaussianWeights[currentOverlowTextureRead * 2 + 1];
                float secondWeight = standardGaussianWeights[currentOverlowTextureRead * 2 + 2];

                float optimizedWeight = firstWeight + secondWeight;
                float optimizedOffset = (firstWeight * (currentOverlowTextureRead * 2 + 1) + secondWeight * (currentOverlowTextureRead * 2 + 2)) / optimizedWeight;

                shaderString.append(String.format(Locale.ENGLISH,"sum += texture2D(inputImageTexture, blurCoordinates[0] + singleStepOffset * %f) * %f;\n", optimizedOffset, optimizedWeight));
                shaderString.append(String.format(Locale.ENGLISH,"sum += texture2D(inputImageTexture, blurCoordinates[0] - singleStepOffset * %f) * %f;\n", optimizedOffset, optimizedWeight));
            }
        }

        // Footer
        shaderString.append(
                "gl_FragColor = sum;\n" +
                "}\n");

        return shaderString.toString();
    }

    // inputRadius for Core Image's CIGaussianBlur is really sigma in the Gaussian equation, so I'm using that for my blur radius, to be consistent
    public void setBlurRadiusInPixels(float newValue) {
        // 7.0 is the limit for blur size for hardcoded varying offsets

        if (Math.round(newValue) != blurRadiusInPixels) {
            blurRadiusInPixels = Math.round(newValue); // For now, only do integral sigmas

            int calculatedSampleRadius = 0;
            if (blurRadiusInPixels >= 1) { // Avoid a divide-by-zero error here

                // Calculate the number of pixels to sample from by setting a bottom limit for the contribution of the outermost pixel
                float minimumWeightToFindEdgeOfSamplingArea = 1.0f / 256.0f;
                calculatedSampleRadius = (int) Math.floor(Math.sqrt(-2.0 * Math.pow(blurRadiusInPixels, 2.0) * Math.log(minimumWeightToFindEdgeOfSamplingArea * Math.sqrt(2.0 * Math.PI * Math.pow(blurRadiusInPixels, 2.0)))));
                calculatedSampleRadius += calculatedSampleRadius % 2; // There's nothing to gain from handling odd radius sizes, due to the optimizations I use
            }

            newGaussianBlurVertexShader = AYGPUImageGaussianBlurFilter.vertexShaderForOptimizedBlurOfRadius(calculatedSampleRadius, blurRadiusInPixels);
            newGaussianBlurFragmentShader = AYGPUImageGaussianBlurFilter.fragmentShaderForOptimizedBlurOfRadius(calculatedSampleRadius, blurRadiusInPixels);

            needResetProgram = true;
        }
    }

    @Override
    protected void renderToTexture(final Buffer vertices, final Buffer textureCoordinates) {
        context.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                if (needResetProgram) {
                    needResetProgram = false;

                    filterProgram.destroy();

                    filterProgram = new AYGLProgram(newGaussianBlurVertexShader, newGaussianBlurFragmentShader);
                    filterProgram.link();

                    filterPositionAttribute = filterProgram.attributeIndex("position");
                    filterTextureCoordinateAttribute = filterProgram.attributeIndex("inputTextureCoordinate");
                    filterInputTextureUniform = filterProgram.uniformIndex("inputImageTexture");
                    verticalPassTexelWidthOffsetUniform = filterProgram.uniformIndex("texelWidthOffset");
                    verticalPassTexelHeightOffsetUniform = filterProgram.uniformIndex("texelHeightOffset");
                    filterProgram.use();
                }

                filterProgram.use();

                if (secondOutputFramebuffer != null) {
                    if (inputWidth != secondOutputFramebuffer.width || inputHeight != secondOutputFramebuffer.height) {
                        secondOutputFramebuffer.destroy();
                        secondOutputFramebuffer = null;
                    }
                }

                if (secondOutputFramebuffer == null) {
                    secondOutputFramebuffer = new AYGPUImageFramebuffer(inputWidth, inputHeight);
                }

                secondOutputFramebuffer.activateFramebuffer();

                glClearColor(0, 0, 0, 0);
                glClear(GL_COLOR_BUFFER_BIT);

                glActiveTexture(GL_TEXTURE2);
                glBindTexture(GL_TEXTURE_2D, firstInputFramebuffer.texture[0]);
                glUniform1i(filterInputTextureUniform, 2);

                glUniform1f(verticalPassTexelWidthOffsetUniform, 1.0f / inputWidth);
                glUniform1f(verticalPassTexelHeightOffsetUniform, 0);

                glEnableVertexAttribArray(filterPositionAttribute);
                glEnableVertexAttribArray(filterTextureCoordinateAttribute);

                glVertexAttribPointer(filterPositionAttribute, 2, GL_FLOAT, false, 0, vertices);
                glVertexAttribPointer(filterTextureCoordinateAttribute, 2, GL_FLOAT, false, 0, textureCoordinates);

                glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

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

                glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                glActiveTexture(GL_TEXTURE2);
                glBindTexture(GL_TEXTURE_2D, secondOutputFramebuffer.texture[0]);
                glUniform1i(filterInputTextureUniform, 2);

                glUniform1f(verticalPassTexelWidthOffsetUniform, 0);
                glUniform1f(verticalPassTexelHeightOffsetUniform, 1.0f / inputHeight);

                glVertexAttribPointer(filterPositionAttribute, 2, GL_FLOAT, false, 0, vertices);
                glVertexAttribPointer(filterTextureCoordinateAttribute, 2, GL_FLOAT, false, 0, textureCoordinates);

                glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

                glDisableVertexAttribArray(filterPositionAttribute);
                glDisableVertexAttribArray(filterTextureCoordinateAttribute);
            }
        });
    }
}
