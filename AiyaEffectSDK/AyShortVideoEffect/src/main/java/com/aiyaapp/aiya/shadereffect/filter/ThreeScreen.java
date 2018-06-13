package com.aiyaapp.aiya.shadereffect.filter;

import com.aiyaapp.aiya.AiyaShaderEffect;

/**
 * 三分屏效果
 */
public class ThreeScreen extends NativeCoolFilter {

    private int[] tempTextures;
    private int frameSize=15;

    private boolean hasInit=false;

    public ThreeScreen() {
        super(CoolFilterFactory.TYPE_THREE_SCREEN);
        setInterval(frameSize);
    }

    /**
     * 设置显示的位置
     * @param pos -1到3，-1表示所有窗口，0绘制到左下，1绘制到左上，2绘制到右下，3绘制到右上
     */
    public void setSubWindow(int pos){
        set("SubWindow",pos);
    }

    /**
     * 设置是否绘制为灰度图像
     * @param isGray true or false
     */
    public void setDrawGray(boolean isGray){
        set("DrawGray",isGray?1:0);
    }

    /**
     * 设置窗口切换帧数间隔
     * @param interval 帧数
     */
    public void setInterval(int interval){
        set("Interval",interval);
    }

    public void setTextureInPos(int pos,int texture){
        if(pos==-1){
            tempTextures=null;
        }else if(pos>=0&&pos<4){
            if(tempTextures==null){
                tempTextures=new int[4];
            }
            tempTextures[pos]=texture;
        }

    }

    protected void beforeDraw(int texture,int width,int height){
        if(createTime< AiyaShaderEffect.getLastTime()){
            hasInit=false;
        }
        if(tempTextures!=null){
            if(!hasInit){
                hasInit=true;
                for (int i=tempTextures.length;i>=0;i--){
                    setSubWindow(i);
                    setDrawGray(i!=0);
                    super.draw(tempTextures[i],width,height,0);
                }
            }
        }else{
            if(!hasInit){
                hasInit=true;
                for (int i=3;i>=0;i--){
                    setSubWindow(i);
                    setDrawGray(i!=0);
                    super.draw(texture,width,height,0);
                }
            }
        }
    }

    @Override
    public int draw(int texture, int width, int height, int flag) {
        beforeDraw(texture,width,height);
        return super.draw(texture, width, height, flag);
    }
}
