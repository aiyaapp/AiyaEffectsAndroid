package com.aiyaapp.aiya.shadereffect.filter;

/**
 * 躁动特效
 */
public class Dysphoria extends NativeCoolFilter{

    public Dysphoria() {
        super(CoolFilterFactory.TYPE_DYSPHORIA);
    }

    /**
     * 设置交替帧数间隔
     * @param frame 帧数
     */
    public void setConsecutiveFrame(int frame){
        _setConsecutiveFrame(frame);
    }

}
