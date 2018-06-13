package com.aiyaapp.aiya.shadereffect.filter;

/**
 * 黑魔法
 */
public class BlackMagic extends NativeCoolFilter{

    private boolean isFirstDraw=true;

    public BlackMagic() {
        super(CoolFilterFactory.TYPE_BLACK_MAGIC);
        setScale(1.0f);
    }

    /**
     * 设置图像大小，一般不用设置
     * @param width 宽度
     * @param height 高度
     */
    public void setFrameSize(int width,int height){
        _setFrameWidth(width);
        _setFrameHeight(height);
        isFirstDraw=false;
    }

    /**
     * 设置放大倍数，一般不用设置
     * @param scale 放大倍数
     */
    public void setScale(float scale){
        _setScale(scale);
    }

    @Override
    public int draw(int texture, int width, int height, int flag) {
        if(isFirstDraw){
            setFrameSize(width,height);
        }
        return super.draw(texture, width, height,flag);
    }
}
