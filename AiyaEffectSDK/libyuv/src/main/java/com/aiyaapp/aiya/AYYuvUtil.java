package com.aiyaapp.aiya;

import java.nio.ByteBuffer;

public class AYYuvUtil {

    static {
        System.loadLibrary("libyuv");
    }

    // bgra 转 yuv
    public static native void RGBA_To_I420(ByteBuffer bgra, ByteBuffer yuv, int width, int height);

    // yuv 转 bgra
    public static native void I420_To_RGBA(ByteBuffer yuv, ByteBuffer bgra, int width, int height);
}
