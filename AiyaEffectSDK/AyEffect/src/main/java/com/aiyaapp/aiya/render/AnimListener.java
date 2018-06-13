package com.aiyaapp.aiya.render;


/**
 * 特效动画监听器
 * @author wuwang
 */
public interface AnimListener{

    /**
     * 动画渲染事件通知
     * @param type 类型
     * @param ret 返回值
     * @param message 附加说明
     */
    void onAnimEvent(int type,int ret,String message);

}
