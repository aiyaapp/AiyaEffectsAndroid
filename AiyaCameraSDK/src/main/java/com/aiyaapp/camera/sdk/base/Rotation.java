/*
 *
 * Rotation.java
 * 
 * Created by Wuwang on 2016/11/8
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.base;

/**
 * Description:
 */
public enum Rotation {

    NORMAL(0),
    ROTATION_90(90),
    ROTATION_180(180),
    ROTATION_270(270);

    private int angle;

    Rotation(int angle){
        this.angle=angle;
    }

    public int asInt() {
        return angle;
    }

    public static Rotation fromInt(int rotation) {
        for(Rotation s:Rotation.values()){
            if(rotation==s.angle)return s;
        }
        throw new IllegalStateException(rotation+"不存在，只能为0、90、180、270");
    }

}
