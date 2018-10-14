package com.aiyaapp.aiya.shadereffect.filter;


import android.opengl.GLES20;
import android.util.Log;

import com.aiyaapp.aiya.AiyaShaderEffect;

/**
 * 四分屏特效
 */
public class FourScreen extends NativeCoolFilter {

    private int[] tempTextures;
    private int frameSize=45;

    private boolean hasInit=false;

    public FourScreen() {
        super(CoolFilterFactory.TYPE_FOUR_SCREEN);
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

    /**
     * 初始化时设置每个位置的纹理
     * @param pos 位置，从左到右，从上大小依次为0、1、2、3
     * @param texture 纹理ID
     */
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

    protected void beforeDraw(int texture,int width,int height,int flag){
        if(createTime< AiyaShaderEffect.getLastTime()){
            hasInit=false;
        }
        if(tempTextures!=null){
            if(!hasInit){
                hasInit=true;
                for (int i=tempTextures.length-1;i>=0;i--){
                    setSubWindow(i);
                    setDrawGray(i!=0);
                    super.draw(tempTextures[i],width,height,flag);
                }
            }
        }else{
            if(!hasInit){
                hasInit=true;
                int[] buffer=new int[1];
                GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING,buffer,0);
                Log.d("aiyaapp","frameBuffer bind:"+buffer[0]);
                for (int i=0;i<4;i++){
                    setSubWindow(i);
                    setDrawGray(true);
                    super.draw(texture,width,height,flag);
                }
                if(flag==0){
                    setSubWindow(0);
                    setDrawGray(false);
                    super.draw(texture,width,height,flag);
                }else {
                    setSubWindow(2);
                    setDrawGray(false);
                    super.draw(texture,width,height,flag);
                }
            }
        }
    }

    @Override
    public int draw(int texture, int width, int height) {
        beforeDraw(texture,width,height,0);
        return super.draw(texture, width, height);
    }

    @Override
    public int draw(int texture, int width, int height,int flag) {
        beforeDraw(texture,width,height,flag);
        return super.draw(texture, width, height,flag);
    }

}
