package com.aiyaapp.aiya.shadereffect.filter;

/**
 * 荧光特效
 */
public class Fluorescence extends NativeCoolFilter{

    private boolean isFirstDraw=true;

    public Fluorescence() {
        super(CoolFilterFactory.TYPE_FLUORESCENCE);
    }

    public void setConsecutiveFrame(int frame){
        _setConsecutiveFrame(frame);
    }

    /**
     * 设置Frame的大小，一般不用设置
     * @param width 宽度
     * @param height 高度
     */
    public void setFrameSize(int width,int height){
        isFirstDraw=false;
        _setFrameWidth(width);
        _setFrameHeight(height);
    }

    @Override
    public int draw(int texture, int width, int height) {
        if(isFirstDraw){
            setFrameSize(width,height);
        }
        return super.draw(texture, width, height);
    }
}
