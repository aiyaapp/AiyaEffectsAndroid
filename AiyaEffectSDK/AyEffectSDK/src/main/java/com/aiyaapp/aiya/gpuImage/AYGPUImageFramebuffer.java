package com.aiyaapp.aiya.gpuImage;

import android.util.Log;

import static android.opengl.GLES20.*;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.TAG;

public class AYGPUImageFramebuffer {

    public int width;
    public int height;
    private int minFilter;
    private int magFilter;
    private int wrapS;
    private int wrapT;
    private int internalFormat;
    private int format;
    private int type;

    private int[] framebuffer = new int[1];
    public int[] texture = new int[1];

    public AYGPUImageFramebuffer(int width, int height, int minFilter, int magFilter, int wrapS, int wrapT, int internalFormat, int format, int type) {
        this.width = width;
        this.height = height;
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        this.wrapS = wrapS;
        this.wrapT = wrapT;
        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;

        generateFramebuffer();

        Log.d(TAG, "创建一个 OpenGL frameBuffer " + framebuffer[0]);
    }

    public AYGPUImageFramebuffer(int width, int height) {
        this(width, height, GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE);
    }

    private void generateFramebuffer() {
        glGenFramebuffers(1, framebuffer, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer[0]);

        generateTexture();

        glBindTexture(GL_TEXTURE_2D, texture[0]);

        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture[0], 0);

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private void generateTexture() {
        glActiveTexture(GL_TEXTURE1);
        glGenTextures(1, texture, 0);
        glBindTexture(GL_TEXTURE_2D, texture[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);
        // This is necessary for non-power-of-two textures
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT);
    }

    public void destroy() {

        if (framebuffer[0] != 0){
            Log.d(TAG, "销毁一个 OpenGL frameBuffer " + framebuffer[0]);
            glDeleteFramebuffers(1, framebuffer, 0);
            framebuffer[0] = 0;
        }
        if (texture[0] != 0) {
            glDeleteTextures(1, texture, 0);
            texture[0] = 0;
        }
    }

    public void activateFramebuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer[0]);
        glViewport(0, 0, width, height);
    }
}