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
public class Event {

    //-----0x0000-0x8000为用户自定义事件----
    //-----0xE000-0xEFFF为正常使用中的事件----
    //0xE000-0xE100为Process事件
    //-----0xF000-0xFFFF为初始化时发生的事件-----
    //正常状态的Code为0xF000-0xF800之间
    //0xF800-0xFF00之间表示为warming状态
    //其他状态为error状态

    public static final int RESOURCE_READY=0xF101;
    public static final int INIT_SUCCESS=0xF102;
    public static final int RESOURCE_FAILED=0xFF01;
    public static final int INIT_FAILED=0xFF02;

    public static final int PROCESS_PLAY=0xE002;
    public static final int PROCESS_END=0xE004;
    public static final int PROCESS_ERROR=0xE008;

    public int eventType;
    public int intTag;
    public String strTag;
    public Object data;


    public Event(int eventType, int intTag, String strTag, Object data) {
        this.eventType = eventType;
        this.intTag = intTag;
        this.strTag = strTag;
        this.data = data;
    }

    public boolean isInitEvent(){
        return eventType>0xF000 && eventType<0xFFFF;
    }

    public boolean isProcessEvent(){
        return eventType>0xE001 && eventType<0xE100;
    }

}
