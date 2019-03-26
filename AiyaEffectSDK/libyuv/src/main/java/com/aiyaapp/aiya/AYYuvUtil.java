package com.aiyaapp.aiya;

import java.nio.ByteBuffer;

public class AYYuvUtil {

    static {
        System.loadLibrary("libyuv");
    }

    // bgra 转 yuv
    public static native void RGBA_To_I420(ByteBuffer bgra, ByteBuffer yuv, int width, int height);
}
