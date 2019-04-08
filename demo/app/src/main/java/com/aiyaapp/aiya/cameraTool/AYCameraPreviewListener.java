package com.aiyaapp.aiya.cameraTool;

public interface AYCameraPreviewListener {
    void cameraCrateGLEnvironment();
    void cameraVideoOutput(int texture, int width, int height, long timeStamp);
    void cameraDestroyGLEnvironment();
}
