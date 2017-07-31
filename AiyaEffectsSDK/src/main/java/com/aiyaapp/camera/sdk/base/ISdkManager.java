/*
 *
 * SdkInterface.java
 * 
 * Created by Wuwang on 2016/11/8
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.base;

import android.content.Context;

/**
 * Description:
 */
public interface ISdkManager {

    String SET_BEAUTY_LEVEL="beauty_level";     //美颜等级，1-6，不在范围内表示关闭美颜
    String SET_BEAUTY_TYPE="beauty_type";       //美颜类型，0,1,4,5
    String SET_EFFECT_ON="effects_on";          //特效开关1开0关
    String SET_OXEYE="SetBigEyeScale";          //大眼，0-100
    String SET_THIN_FACE="SetSlimFaceScale";    //瘦脸，0-100

    String SET_EFFECT_FLIP="EnableVFlip";       //effect是否翻转

    String SET_TRACK_FORCE_CLOSE="track_force_close";   //人脸捕获强制关闭

    String SET_IN_WIDTH="in_width";             //处理图片的输入宽度
    String SET_IN_HEIGHT="in_height";           //处理图片的输入高度
    String SET_TRACK_WIDTH="track_width";       //track图片的宽度
    String SET_TRACK_HEIGHT="track_height";     //track图片的高度

    String SET_OUT_WIDTH="out_width";           //输出宽度
    String SET_OUT_HEIGHT="out_height";         //输出高度

    String SET_MODE="effect_mode";              //设置特效显示模式

    String SET_ACTION="set_action";             //动作

    String SET_ASSETS_MANAGER="assets_manager"; //AssetsManager

    String SET_LOG="LogLevel";

    int ACTION_REFRESH_PARAMS_NOW=1;            //刷新params,需要在GL线程中执行

    int STATE_EFFECT_END=0x00040000;            //特效播放结束
    int STATE_EFFECT_PLAY=0x00020000;           //特效播放中

    int MODE_ORNAMENT=0;                        //饰品
    int MODE_GIFT=1;                            //礼物

    int BEAUTY_TYPE_SUPER =1;
    int BEAUTY_TYPE_SNAKE =2;
    int BEAUTY_TYPE_MASK  =3;
    int BEAUTY_TYPE_SUPER2P =4;
    int BEAUTY_TYPE_DXLB  =5;
    int BEAUTY_TYPE_B612   =6;
    int BEAUTY_TYPE_FACECUT=7;
    int BEAUTY_TYPE_NORMAL=8;

    int TRUE=1;
    int FALSE=0;
//
//    String SET_EFFECT="effect_path";            //设置贴纸效果


    int BEAUTY_SMOOTH=0x0010;
    int BEAUTY_SATURATE=0x0020;
    int BEAUTY_WHITEN=0x0030;

    /**
     * SDK 初始化
     * @param context 上下文
     * @param appKey appKey
     */
    void init(final Context context,  String appKey);

    /**
     * 设置参数配置，必须在GL环境中调用
     * @param inputConfig  输入参数
     * @param outputConfig 输出参数
     */
    @Deprecated
    void setParameters(Parameter inputConfig, Parameter outputConfig);

    /**
     * 设置贴纸特效路径
     * @param effectPath 贴纸特效的绝对路径
     */
    void setEffect(String effectPath);

    /**
     * 设置参数
     * @param key 参数名
     * @param value 参数值
     */
    void set(String key, int value);

    void set(String key, Object obj);

    /**
     * 人脸追踪，必须在GL环境中调用
     * @param trackData 需要追踪的原始图片数据
     * @param info 追踪结果，69个特征点，每两个浮点表示一个特征点，info长度200
     */
    void track(byte[] trackData, float[] info, int trackIndex);

    /**
     * 处理图片数据，必须在GL环境中调用
     * @param textureId   纹理Id
     * @param trackIndex
     */
    void process(int textureId, int trackIndex);

    /**
     * 设置图片数据处理回调
     * @param callback 回调
     */
    void setProcessCallback(ProcessCallback callback);

    /**
     * 设置人脸捕捉回调
     * @param callback
     */
    void setTrackCallback(TrackCallback callback);

    /**
     * 注册状态观察者
     * @param observer
     */
    void registerObserver(ActionObserver observer);

    /**
     * 删除已注册的状态观察者
     * @param observer
     */
    void unRegisterObserver(ActionObserver observer);

    /**
     * 停止贴纸特效
     */
    @Deprecated
    void stopEffect();

    /**
     * 获取参数
     * @param key 参数的KEy
     * @return 参数值
     */
    int get(String key);

    /**
     * 释放SDK资源
     */
    void release();

}
