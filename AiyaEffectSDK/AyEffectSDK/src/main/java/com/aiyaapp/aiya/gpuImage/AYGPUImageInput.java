package com.aiyaapp.aiya.gpuImage;

public interface AYGPUImageInput {
    void setInputSize(int width, int height);
    void setInputFramebuffer(AYGPUImageFramebuffer newInputFramebuffer);
    void newFrameReady();
}
