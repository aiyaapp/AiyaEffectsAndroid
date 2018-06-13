package com.aiyaapp.aiya.shadereffect.filter;

/**
 * 抖动
 */
public class Shake extends NativeCoolFilter{

    public Shake() {
        super(CoolFilterFactory.TYPE_SHAKE);
        setConsecutiveFrame(7);
        setWaitTime(7);
        setPhantasmsScale(0.3f);
    }

    /**
     * 设置交替帧数间隔
     * @param frame 帧数，大于等于3
     */
    public void setConsecutiveFrame(int frame){
        _setConsecutiveFrame(frame);
    }

    /**
     * 设置放大参数
     * @param ratio 放大参数，0.0f-0.8f
     */
    public void setPhantasmsScale(float ratio){
        _setPhantasmsScale(ratio);
    }

    /**
     * 设置两侧抖动的间隔帧数
     * @param num 帧数
     */
    public void setWaitTime(int num){
        set("WaitTime",num);
    }

}
