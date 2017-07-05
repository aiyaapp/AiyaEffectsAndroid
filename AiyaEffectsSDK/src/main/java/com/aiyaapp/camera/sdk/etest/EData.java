/*
 *
 * EData.java
 * 
 * Created by Wuwang on 2016/11/18
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.etest;

/**
 * Description:
 */
public class EData {

    public static d data=new d();

    public static class d{
        private long drawTime=0;
        private float fps;
        private int dealTime;
        private int trackCode=-1;
        private float cameraFps;

        private long renderRequestTime=0;
        private int renderTime;

        private long cameraTime=0;

        public void setCameraCallbackTime(long cameraTime){
            if(this.cameraTime>0&&cameraTime-this.cameraTime!=0){
                cameraFps=1000/(cameraTime-this.cameraTime);
            }
            this.cameraTime=cameraTime;
        }

        public void setDealStartTime(long dealStartTime){
            if(drawTime>0){
                fps=1000/(dealStartTime-drawTime);
            }
            this.drawTime=dealStartTime;
        }

        public void setDealEndTime(long dealEndTime){
            dealTime= (int)(dealEndTime-drawTime);
        }

        /**
         * 绘制的FPS
         * @return
         */
        public float getFps(){
            return fps;
        }

        /**
         * 处理时长
         * @return
         */
        public int getDealTime(){
            return dealTime;
        }

        public void setTrackCode(int trackCode){
            this.trackCode=trackCode;
        }

        /**
         * 当前track的状态
         * @return
         */
        public int getTrackCode(){
            return trackCode;
        }

        public void setRequestRenderTime(long time){
            this.renderRequestTime=time;
        }

        /**
         * Camera回调的FPS
         * @return
         */
        public float getCameraFps(){
            return cameraFps;
        }

        public void setResponseRenderTime(long time){
            if(time>=renderRequestTime){
                renderTime= (int)(time-renderRequestTime);
                this.renderRequestTime=0;
            }
        }

        public int getResonseTime(){
            return renderTime;
        }
    }

}
