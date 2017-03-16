/*
 *
 * Parameter.java
 * 
 * Created by Wuwang on 2016/11/8
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.base;

/**
 * Description:
 */
public class Parameter {

    public static final int FORMAT_NV21 = 0;
    public static final int FORMAT_YV12 = 1;
    public static final int FORMAT_ARGB = 6;
    public static final int FORMAT_RGBA = 7;
    public static final int FORMAT_RGB = 8;

    /**
     * 视频帧的宽度
     */
    public int width;
    /**
     * 视频帧的高度
     */
    public int height;

    /**
     * 格式
     */
    public int format;

    /**
     * 旋转角度，前置摄像头旋转角度为270，后置摄像头为90
     */
    public Rotation rotation;
    /**
     * 是否翻转，如果是前置摄像头，flip为true，后置摄像头为false
     */
    public boolean flip;

    public float trackScale=1.0f;

}
