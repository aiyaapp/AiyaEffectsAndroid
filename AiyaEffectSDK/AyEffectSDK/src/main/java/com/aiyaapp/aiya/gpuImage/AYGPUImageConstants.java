package com.aiyaapp.aiya.gpuImage;

import android.graphics.PointF;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class AYGPUImageConstants {
    public static final String TAG = "AYGPUImage";

    public static float imageVertices[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f,  1.0f,
            1.0f,  1.0f,
    };

    public enum AYGPUImageRotationMode {
        kAYGPUImageNoRotation,
        kAYGPUImageRotateLeft,
        kAYGPUImageRotateRight,
        kAYGPUImageFlipVertical,
        kAYGPUImageFlipHorizontal,
        kAYGPUImageRotateRightFlipVertical,
        kAYGPUImageRotateRightFlipHorizontal,
        kAYGPUImageRotate180
    }

    public enum AYGPUImageContentMode {
        kAYGPUImageScaleToFill, //图像填充方式一:拉伸
        kAYGPUImageScaleAspectFit, //图像填充方式二:保持宽高比
        kAYGPUImageScaleAspectFill //图像填充方式三:保持宽高比同时填满整个屏幕
    }

    public static float noRotationTextureCoordinates[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };

    public static float rotateLeftTextureCoordinates[] = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
    };

    public static float rotateRightTextureCoordinates[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    public static float verticalFlipTextureCoordinates[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f,  0.0f,
            1.0f,  0.0f,
    };

    public static float horizontalFlipTextureCoordinates[] = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f,  1.0f,
            0.0f,  1.0f,
    };

    public static float rotateRightVerticalFlipTextureCoordinates[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    public static float rotateRightHorizontalFlipTextureCoordinates[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };

    public static float rotate180TextureCoordinates[] = {
            1.0f, 1.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };

    public static float[] textureCoordinatesForRotation(AYGPUImageRotationMode rotationMode) {
        switch(rotationMode) {
            case kAYGPUImageNoRotation: return noRotationTextureCoordinates;
            case kAYGPUImageRotateLeft: return rotateLeftTextureCoordinates;
            case kAYGPUImageRotateRight: return rotateRightTextureCoordinates;
            case kAYGPUImageFlipVertical: return verticalFlipTextureCoordinates;
            case kAYGPUImageFlipHorizontal: return horizontalFlipTextureCoordinates;
            case kAYGPUImageRotateRightFlipVertical: return rotateRightVerticalFlipTextureCoordinates;
            case kAYGPUImageRotateRightFlipHorizontal: return rotateRightHorizontalFlipTextureCoordinates;
            case kAYGPUImageRotate180: return rotate180TextureCoordinates;
            default:return noRotationTextureCoordinates;
        }
    }

    public static boolean needExchangeWidthAndHeightWithRotation(AYGPUImageRotationMode rotationMode) {
        switch(rotationMode)
        {
            case kAYGPUImageNoRotation: return false;
            case kAYGPUImageRotateLeft: return true;
            case kAYGPUImageRotateRight: return true;
            case kAYGPUImageFlipVertical: return false;
            case kAYGPUImageFlipHorizontal: return false;
            case kAYGPUImageRotateRightFlipVertical: return true;
            case kAYGPUImageRotateRightFlipHorizontal: return true;
            case kAYGPUImageRotate180: return false;
            default:return false;
        }
    }

    public static FloatBuffer floatArrayToBuffer(float[] array) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(array);
        floatBuffer.position(0);
        return floatBuffer;
    }

    public static PointF getAspectRatioInsideSize(PointF sourceSize, PointF boundingSize) {
        float sourceRatio = sourceSize.x / sourceSize.y;
        float boundingRatio = boundingSize.x / boundingSize.y;

        PointF destRatio = new PointF(0, 0);

        if (sourceRatio < boundingRatio) {
            destRatio.x = sourceRatio * boundingSize.y;
            destRatio.y = boundingSize.y;

        } else if (sourceRatio > boundingRatio) {
            destRatio.x = boundingSize.x;
            destRatio.y = boundingSize.x / sourceRatio;

        } else {
            destRatio = boundingSize;
        }

        return destRatio;
    }

}