package com.aiyaapp.aiya.shadereffect.filter;

import android.util.Log;

/**
 * {@link CoolFilterFactory} 为Shader 特效功能模块的实际入口类，它是所有的Shader特效的工厂类，旨在为诸多Shader特效提供统一的创建方式{@link CoolFilterFactory#createFilter(int)}或者{@link CoolFilterFactory#createFilter(Class)} 。使用者也可以直接去创建一个Shader特效，{@link CoolFilterFactory}并未做其他的特殊处理，只是将SDK能提供的Shader特效集中起来，让用户可以去更方便的选择。
 * Shader 特效功能模块当前支持的特效有：
 * {@link SpiritFreed} 灵魂出窍
 * {@link Shake} 抖动
 * {@link BlackMagic} 黑魔法
 * {@link VirtualMirror} 虚拟镜像
 * {@link Fluorescence} 荧光效果
 * {@link TimeTunnel} 时空隧道
 * {@link Dysphoria} 躁动
 * {@link FinalZelig} 终极变色
 * {@link SplitScreen} 动感分屏
 * {@link Hallucination} 幻觉特效
 * {@link Seventys } 70S特效
 * {@link RollUp} 炫酷转动
 * {@link FourScreen} 四分屏
 * {@link ThreeScreen} 三分屏
 * {@link BlackAndWhiteTwinkle} 黑白闪烁
 *
 *  不同的特效会有不同的参数设置，用户可以根据这些参数来修改特效呈现的效果。在一般情况下，用户无需设置这些参数。
 */

public class CoolFilterFactory{

    public static final int TYPE_NONE=0;            //  0——基本特效，直接绘制纹理
    public static final int TYPE_SPIRIT_FREED=1;    //  1——灵魂出窍
    public static final int TYPE_SHAKE=2;           //	2——抖动
    public static final int TYPE_BLACK_MAGIC=3;     //  3——Black Magic
    public static final int TYPE_VIRTUAL_MIRROR=4;  //  4——虚拟镜像
    public static final int TYPE_FLUORESCENCE=5;    //	5——荧光
    public static final int TYPE_TIME_TUNNEL=6;     //  6——时光隧道
    public static final int TYPE_DYSPHORIA =7;      //  7——躁动
    public static final int TYPE_FINAL_ZELIG=8;     //	8——终极变色
    public static final int TYPE_SPLIT_SCREEN=9;    //	9——动感分屏
    public static final int TYPE_HALLUCINATION=10;  //  10——幻觉
    public static final int TYPE_SEVENTYS=11;       //  11——70s
    public static final int TYPE_ROLL_UP=12;        //  12——炫酷转动
    public static final int TYPE_FOUR_SCREEN=13;    //  13——四分屏
    public static final int TYPE_THREE_SCREEN=14;   //  14——三分屏
    public static final int TYPE_BLACK_WHITE_TWINKLE=15;   //  15——黑白闪烁
    public static final int TYPE_CUT_SCENE=0x3000;   //  15——黑白闪烁

    public static final Class<None> CLA_NONE=None.class;                            //  0——基本特效，直接绘制纹理
    public static final Class<SpiritFreed> CLA_SPIRIT_FREED=SpiritFreed.class;      //  1——灵魂出窍
    public static final Class<Shake> CLA_SHAKE=Shake.class;                         //  2——抖动
    public static final Class<BlackMagic> CLA_BLACK_MAGIC=BlackMagic.class;         //  3——Black Magic
    public static final Class<VirtualMirror> CLA_VIRTUAL_MIRROR=VirtualMirror.class;  //  4——虚拟镜像
    public static final Class<Fluorescence> CLA_FLUORESCENCE=Fluorescence.class;    //	5——荧光
    public static final Class<TimeTunnel> CLA_TIME_TUNNEL=TimeTunnel.class;         //  6——时光隧道
    public static final Class<Dysphoria> CLA_DYSPHORIA =Dysphoria.class;            //  7——躁动
    public static final Class<FinalZelig> CLA_FINAL_ZELIG=FinalZelig.class;         //	8——终极变色
    public static final Class<SplitScreen> CLA_SPLIT_SCREEN=SplitScreen.class;      //  9——分屏
    public static final Class<Hallucination> CLA_HALLUCINATION=Hallucination.class; //  10——幻觉
    public static final Class<Seventys> CLA_SEVENTYS=Seventys.class;                //  11——70s
    public static final Class<RollUp> CLA_ROLL_UP=RollUp.class;                     //  12——炫酷转动
    public static final Class<FourScreen> CLA_FOUR_SCREEN=FourScreen.class;         //  13——四分屏
    public static final Class<ThreeScreen> CLA_THREE_SCREEN=ThreeScreen.class;      //  14——三分屏
    public static final Class<BlackAndWhiteTwinkle> CLA_BLACK_WHITE_TWINKLE=BlackAndWhiteTwinkle.class;      //  15——黑白闪烁
    public static final Class<Cutscene> CLA_CUT_SCENE=Cutscene.class;      //  15——黑白闪烁


    public static Class[] clazzs=new Class[]{
            CLA_NONE,CLA_SPIRIT_FREED,CLA_SHAKE,CLA_BLACK_MAGIC,CLA_VIRTUAL_MIRROR,CLA_FLUORESCENCE,
            CLA_TIME_TUNNEL,CLA_DYSPHORIA,CLA_FINAL_ZELIG,CLA_SPLIT_SCREEN,CLA_HALLUCINATION,
            CLA_SEVENTYS,CLA_ROLL_UP,CLA_FOUR_SCREEN,CLA_THREE_SCREEN,CLA_BLACK_WHITE_TWINKLE,CLA_CUT_SCENE
    };

    public static String[] FILTERS_NAME=new String[]{
            "无特效","灵魂出窍","抖动","黑魔法","虚拟镜像","荧光",
            "时光隧道","躁动","终极变色","动感分屏","幻觉","70s","炫酷转动",
            "四分屏","三分屏","黑白闪烁"
    };

    private CoolFilterFactory(){}

    public static <T> T createFilter(Class<T> type){
        try {
            return type.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static NativeCoolFilter createFilter(int type){
        if(type>=0&&type<clazzs.length){
            try {
                return (NativeCoolFilter)(clazzs[type].newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Log.d("NativeCoolFilter","create filter failed");
        return null;
    }

}


