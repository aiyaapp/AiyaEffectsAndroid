package com.aiyaapp.aiya.shadereffect.filter;

/**
 * 灵魂出窍
 */
public class SpiritFreed extends NativeCoolFilter{

    public SpiritFreed() {
        super(CoolFilterFactory.TYPE_SPIRIT_FREED);
        _setPhantasmsScale(0.6f);
        _setConsecutiveFrame(8);
        setWaitTime(6);
        setShadowAlpha(0.15f);
    }

    /**
     * 设置交替帧数间隔
     * @param frame 帧数，大于等于3
     */
    public void setConsecutiveFrame(int frame){
        _setConsecutiveFrame(frame);
    }

    /**
     * 设置停留帧
     * @param frame 帧数
     */
    public void setWaitTime(int frame){
        set("WaitTime",frame);
    }

    /**
     * 设置影子的透明度
     * @param alpha 透明度
     */
    public void setShadowAlpha(float alpha){
        set("ShadowAlpha",alpha);
    }

    /**
     * 设置放大参数
     * @param ratio 放大参数，0.0f-0.8f
     */
    public void setPhantasmsScale(float ratio){
        _setPhantasmsScale(ratio);
    }

}
