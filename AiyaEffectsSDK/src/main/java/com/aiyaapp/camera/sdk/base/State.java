/*
 *
 * State.java
 * 
 * Created by Wuwang on 2016/11/8
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.base;

/**
 * Description:
 */
public enum  State {

    //正常状态的Code为0xF000-0xF800之间
    //0xF800-0xFF00之间表示为warming状态
    //其他状态为error状态

    RESOURCE_READY("资源准备完毕",0xF101),
    INIT_SUCCESS("初始化成功",0xF102),

    RESOURCE_FAILED("资源初始化失败",0xFF01),
    INIT_FAILED("初始化失败",0xFF02);

    private String msg;
    private int code;

    State(String msg,int code){
        this.msg=msg;
        this.code=code;
    }

    public int getCode(){
        return code;
    }

    public String getMsg(){
        return msg;
    }

    public boolean isCorrect(){
        return code>=0xF000&&code<0xF800;
    }

    public boolean isWarming(){
        return code>=0xF800&&code<0xFF00;
    }

    public boolean isError(){
        return code<0xF000||code>=0xFF00;
    }

}
