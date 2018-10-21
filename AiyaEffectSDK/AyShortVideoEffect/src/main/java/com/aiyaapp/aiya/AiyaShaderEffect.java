/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aiyaapp.aiya;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * AiyaShortVideo 短视频对象工具类，请勿外部调用此类
 *
 * @author wuwang
 * @version v1.0 2017:11:02 14:27
 */
public class AiyaShaderEffect {

    private static long lastTimeStamp=System.currentTimeMillis();

    /**
     * 基本特效，直接绘制纹理
     */
    public static final int TYPE_NONE=0;

    /**
     * 灵魂出窍
     */
    public static final int TYPE_SPIRIT_FREED=1;

    /**
     * 抖动
     */
    public static final int TYPE_SHAKE=2;

    /**
     * 黑魔法
     */
    public static final int TYPE_BLACK_MAGIC=3;

    /**
     * 虚拟镜像
     */
    public static final int TYPE_VIRTUAL_MIRROR=4;

    /**
     * 荧光
     */
    public static final int TYPE_FLUORESCENCE=5;

    /**
     * 时光隧道
     */
    public static final int TYPE_TIME_TUNNEL=6;

    /**
     * 躁动
     */
    public static final int TYPE_DYSPHORIA =7;

    /**
     * 终极变色
     */
    public static final int TYPE_FINAL_ZELIG=8;

    /**
     * 动感分屏
     */
    public static final int TYPE_SPLIT_SCREEN=9;

    /**
     * 幻觉
     */
    public static final int TYPE_HALLUCINATION=10;

    /**
     * 70s
     */
    public static final int TYPE_SEVENTYS=11;

    /**
     * 炫酷转动
     */
    public static final int TYPE_ROLL_UP=12;

    /**
     * 四分屏
     */
    public static final int TYPE_FOUR_SCREEN=13;

    /**
     * 三分屏
     */
    public static final int TYPE_THREE_SCREEN=14;

    /**
     * 黑白闪烁
     */
    public static final int TYPE_BLACK_WHITE_TWINKLE=15;

    public static final int TYPE_CUT_SCENE=0x3000;

    public native static long nCreateNativeObj(int type);

    public native static void nDestroyNativeObj(long id);

    public native static int nSet(long id,String key,String value);

    public native static int nSet(long id,String key,float data);

    public native static int nSet(long id,String key,int texture,int w,int h);

    public native static int nSet(long id, Context context);

    public native static int nGlInit(long id);

    public native static int nGlDestroy(long id);

    public native static int nRestart(long id);

    public native static int nDraw(long id,int tex,int x,int y,int w,int h);

    /**
     * @return
     */
    public int getVersionCode(){
        return nVersionCode();
    }

    public String getVersionName(){
        return nVersionName();
    }

    private native static int nVersionCode();

    private native static String nVersionName();

    public static int release(){
        lastTimeStamp=System.currentTimeMillis();
        return 0;
    }

    public static long getLastTime(){
        return lastTimeStamp;
    }

    private AiyaShaderEffect(){}

    static {
        System.loadLibrary("AyCoreSdk");
        System.loadLibrary("AyCoreJni");
        System.loadLibrary("AiyaAe3dLib");
        System.loadLibrary("BaseEffects");
        System.loadLibrary("AiyaEffectLib");
        System.loadLibrary("AyShortVideoEffectJni");
    }

}
