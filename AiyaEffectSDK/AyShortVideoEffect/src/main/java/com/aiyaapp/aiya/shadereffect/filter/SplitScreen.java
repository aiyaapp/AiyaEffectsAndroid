package com.aiyaapp.aiya.shadereffect.filter;

/**
 * 动感分屏
 */
public class SplitScreen extends NativeCoolFilter{

    public SplitScreen() {
        super(CoolFilterFactory.TYPE_SPLIT_SCREEN);
        setMaxSplitSize(3);
        setInterval(15);
    }

    /**
     * 设置分屏切换速度
     * @param frame 帧数
     */
    public void setInterval(int frame){
        set("Interval",frame);
    }

    /**
     * 设置最大分屏
     * @param maxSize 分屏size
     */
    public void setMaxSplitSize(int maxSize){
        set("MaxSplitSize",maxSize);
    }
}
