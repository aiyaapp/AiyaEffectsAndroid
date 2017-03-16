/*
 *
 * FrameCallback.java
 * 
 * Created by Wuwang on 2016/11/19
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.base;

/**
 * Description:
 */
public interface FrameCallback {

    void onFrame(byte[] bytes, long time);

}
