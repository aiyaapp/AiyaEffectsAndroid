package com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter;

import com.aiyaapp.aiya.gpuImage.AYGPUImageEGLContext;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFilter;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFramebuffer;

import java.nio.Buffer;

import static android.opengl.GLES20.*;

public class AYGPUImageDelayFilter extends AYGPUImageFilter {

    private AYGPUImageFramebuffer[] cacheFramebuffer = new AYGPUImageFramebuffer[2];
    private int cachePosition = 0;
    private long cacheExpireTime = 0;

    public AYGPUImageDelayFilter(AYGPUImageEGLContext context) {
        super(context);
    }

    @Override
    protected void renderToTexture(final Buffer vertices, final Buffer textureCoordinates) {
        context.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {
                filterProgram.use();

                int processPosition = cachePosition == 0 ? 1 : 0;

                outputFramebuffer = cacheFramebuffer[processPosition];

                if (outputFramebuffer != null) {
                    if (inputWidth != outputFramebuffer.width || inputHeight != outputFramebuffer.height) {
                        outputFramebuffer.destroy();
                        outputFramebuffer = null;
                    }
                }

                if (outputFramebuffer == null) {
                    outputFramebuffer = new AYGPUImageFramebuffer(inputWidth, inputHeight);
                    cacheFramebuffer[processPosition] = outputFramebuffer;
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

                // 使用缓存帧
                AYGPUImageFramebuffer framebuffer = cacheFramebuffer[cachePosition];

                if (framebuffer != null) {
                    if (inputWidth != framebuffer.width || inputHeight != framebuffer.height || cacheExpireTime < System.currentTimeMillis()) {
                        framebuffer.destroy();
                        framebuffer = null;
                        cacheFramebuffer[cachePosition] = null;
                    }
                }

                if (framebuffer != null) {
                    outputFramebuffer = framebuffer;
                }

                // 切换缓存位置
                cachePosition = processPosition;
                cacheExpireTime = System.currentTimeMillis() + 1000;
            }
        });
    }

    public void destroy() {
        super.destroy();

        context.syncRunOnRenderThread(new Runnable() {
            @Override
            public void run() {

                if (cacheFramebuffer[0] != null) {
                    cacheFramebuffer[0].destroy();
                }

                if (cacheFramebuffer[1] != null) {
                    cacheFramebuffer[1].destroy();
                }
            }
        });
    }
}
