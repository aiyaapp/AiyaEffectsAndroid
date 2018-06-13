/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aiyaapp.aavt.gl;

import android.content.res.Resources;
import android.opengl.GLES20;

/**
 * RollFilter 四分屏转动滤镜
 *
 * @author wuwang
 * @version v1.0 2017:10:31 11:54
 */
public class RollFilter extends LazyFilter {

    private int mXRollTime=10;
    private int mYRollTime=10;
    private int mFrameCount=0;

    public RollFilter(Resources resource) {
        super(resource);
    }

    public RollFilter(String vert, String frag) {
        super(vert, frag);
    }

    public void setRollTime(int xTime,int yTime){
        this.mXRollTime=xTime;
        this.mYRollTime=yTime;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        mFrameCount=0;
    }

    @Override
    protected void onDraw() {
        mFrameCount++;
        if(mFrameCount>=(mXRollTime+mYRollTime)){
            mFrameCount=0;
        }
        if(mFrameCount<mXRollTime){
            //todo x方向滚动
            int shift= (mFrameCount*mWidth/mXRollTime)/2;
            for (int i=0;i<3;i++){
                GLES20.glViewport(mWidth*i/2-shift,0,mWidth/2,mHeight/2);
                super.onDraw();
                GLES20.glViewport(mWidth*i/2+shift-mWidth/2,mHeight/2,mWidth/2,mHeight/2);
                super.onDraw();
            }
        }else{
            //todo y方向滚动
            int shift= (mHeight-(mFrameCount-mXRollTime)*mHeight/mYRollTime)/2;
            for (int i=0;i<3;i++){
                GLES20.glViewport(0,mHeight*i/2-shift,mWidth/2,mHeight/2);
                super.onDraw();
                GLES20.glViewport(mWidth/2,mHeight*i/2+shift-mHeight/2,mWidth/2,mHeight/2);
                super.onDraw();
            }
        }
    }

}
