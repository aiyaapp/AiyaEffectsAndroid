package com.aiyaapp.aiya.shadereffect.filter;

/**
 * 终极变色
 */
public class FinalZelig extends NativeCoolFilter{

    public FinalZelig() {
        super(CoolFilterFactory.TYPE_FINAL_ZELIG);
    }

    /**
     * 设置交替帧数间隔
     * @param frame 帧数
     */
    public void setConsecutiveFrame(int frame){
        _setConsecutiveFrame(frame);
    }

}
