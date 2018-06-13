package com.aiyaapp.aiya.shadereffect.filter;

/**
 * 分屏转动
 */

public class RollUp extends NativeCoolFilter {

    public RollUp() {
        super(CoolFilterFactory.TYPE_ROLL_UP);
        setSplitSizeX(2);
        setSplitSizeY(2);
        setClockWise(true);
        setRollStepX(25);
        setRollStepY(40);
    }

    /**
     * 设置X轴分屏个数
     * @param size 分屏个数
     */
    private void setSplitSizeX(int size){
        set("SpliteSizeX",size);
    }

    /**
     * 设置Y轴分屏个数
     * @param size 分屏个数
     */
    private void setSplitSizeY(int size){
        set("SpliteSizeY",size);
    }

    /**
     * 设置转动方向是否为顺时针
     * @param clockWise 是否为顺时针
     */
    public void setClockWise(boolean clockWise){
        set("ClockWise",clockWise?1:0);
    }

    /**
     * 设置X轴方向转动速度
     * @param step 每帧移动步长
     */
    public void setRollStepX(int step){
        set("RollStepX",step);
    }

    /**
     * 设置Y轴方向转动速度
     * @param step 每帧移动步长
     */
    public void setRollStepY(int step){
        set("RollStepY",step);
    }

}
