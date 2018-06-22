package com.aiyaapp.aiya.shadereffect.filter;

/**
 * 黑白闪烁特效
 */
public class BlackAndWhiteTwinkle extends NativeCoolFilter {

    public BlackAndWhiteTwinkle() {
        super(CoolFilterFactory.TYPE_BLACK_WHITE_TWINKLE);
        setTwinkleTime(25);
        setTotalTime(50);
    }

    /**
     * 设置黑白闪烁的时间
     * @param time 大于0，小于总时间
     */
    public void setTwinkleTime(int time){
        set("TwinkleTime",time);
    }

    /**
     * 设置总时间
     * @param time 黑白闪烁+不闪烁的时间
     */
    public void setTotalTime(int time){
        set("TotalTime",time);
    }

}
