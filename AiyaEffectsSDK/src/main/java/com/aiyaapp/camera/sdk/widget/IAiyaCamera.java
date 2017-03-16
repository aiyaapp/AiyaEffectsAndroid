/*
 *
 * IAiyaCamera.java
 * 
 * Created by Wuwang on 2016/11/22
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.widget;

import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

/**
 * Description:
 */
@Deprecated
public interface IAiyaCamera {

    void open(int cameraId);

    void setPreviewTexture(SurfaceTexture texture);

    void setConfig(Config config);

    void setOnPreviewFrameCallback(PreviewFrameCallback callback);

    void preview();

    Point getPreviewSize();

    Point getPictureSize();

    boolean close();

    class Config{
        float rate=1.778f; //宽高比
        int minPreviewWidth;
        int minPictureWidth;
    }

    interface PreviewFrameCallback{
        void onPreviewFrame(byte[] bytes, int width, int height);
    }

    enum FlashMode{
        ON(1, Camera.Parameters.FLASH_MODE_ON),
        OFF(1, Camera.Parameters.FLASH_MODE_OFF),
        AUTO(2, Camera.Parameters.FLASH_MODE_AUTO),
        TORCH(4, Camera.Parameters.FLASH_MODE_TORCH),
        RED_EYE(5, Camera.Parameters.FLASH_MODE_RED_EYE);
        int type;
        String mode;
        FlashMode(int type,String mode){
            this.type=type;
            this.mode=mode;
        }

        @Override
        public String toString() {
            return mode;
        }

        public int value(){
            return type;
        }
    }

}
