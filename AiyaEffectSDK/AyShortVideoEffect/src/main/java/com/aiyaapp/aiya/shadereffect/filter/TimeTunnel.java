package com.aiyaapp.aiya.shadereffect.filter;

/**
 * 时光隧道
 */
public class TimeTunnel extends NativeCoolFilter{

    public TimeTunnel() {
        super(CoolFilterFactory.TYPE_TIME_TUNNEL);
    }

    /**
     * 设置交替帧数间隔
     * @param frame 帧数
     */
    public void setConsecutiveFrame(int frame){
        _setConsecutiveFrame(frame);
    }

}
