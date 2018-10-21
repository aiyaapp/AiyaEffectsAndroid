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
package com.aiyaapp.aiya.filter;

import android.opengl.GLES20;
import android.util.Log;

import com.aiyaapp.aiya.AiyaShaderEffect;

/**
 * SvThreeScreenFilter 三分屏
 *
 * @author wuwang
 * @version v1.0 2017:11:07 15:52
 */
public class SvThreeScreenFilter extends BaseShortVideoFilter {

    private int[] tempTextures;
    private boolean hasInit=false;

    public SvThreeScreenFilter() {
        super(AiyaShaderEffect.TYPE_THREE_SCREEN);
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        super.onSizeChanged(width, height);
        AiyaShaderEffect.nSet(nativeObjId,"Interval",15);
    }

    public void setInterval(final int num){
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                AiyaShaderEffect.nSet(nativeObjId,"Interval",num);
            }
        });
    }

    public void setTextureInPos(int pos,int texture){
        if(pos==-1){
            tempTextures=null;
        }else if(pos>=0&&pos<3){
            if(tempTextures==null){
                tempTextures=new int[3];
            }
            tempTextures[pos]=texture;
        }
    }

    private void beforeDraw(int texture){
        if(tempTextures!=null){
            if(!hasInit){
                hasInit=true;
                for (int i=tempTextures.length-1;i>=0;i--){
                    AiyaShaderEffect.nSet(nativeObjId,"SubWindow",i);
                    AiyaShaderEffect.nSet(nativeObjId,"DrawGray",i!=0?1:0);
                    super.draw(tempTextures[i]);
                }
            }
        }else{
            if(!hasInit){
                hasInit=true;
                int[] buffer=new int[1];
                GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING,buffer,0);
                Log.d("aiyaapp","frameBuffer bind:"+buffer[0]);
                for (int i=0;i<3;i++){
                    AiyaShaderEffect.nSet(nativeObjId,"SubWindow",i);
                    AiyaShaderEffect.nSet(nativeObjId,"DrawGray",1);
                    super.draw(texture);
                }
                AiyaShaderEffect.nSet(nativeObjId,"SubWindow",0);
                AiyaShaderEffect.nSet(nativeObjId,"DrawGray",0);
                super.draw(texture);
            }
        }
    }

    @Override
    public void draw(int texture) {
        beforeDraw(texture);
        super.draw(texture);
    }

}
