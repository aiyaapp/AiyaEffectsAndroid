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

import android.util.Log;

import com.aiyaapp.aavt.gl.BaseFilter;
import com.aiyaapp.aavt.log.AvLog;
import com.aiyaapp.aiya.AiyaShaderEffect;


/**
 * BaseShortVideoFilter 短视频特效基类
 *
 * @author wuwang
 * @version v1.0 2017:11:07 09:20
 */
public abstract class BaseShortVideoFilter extends BaseFilter {

    protected long nativeObjId=0;
    private int type=0;
    private long count, total;
    public BaseShortVideoFilter(int type) {
        super(null,"none","none");
        this.type=type;
        count = total = 0;
    }

    @Override
    protected void onCreate() {

    }

    @Override
    public void sizeChanged(int width, int height) {
        if(mWidth!=width||mHeight!=height){
            nativeObjId=AiyaShaderEffect.nCreateNativeObj(type);
            if(nativeObjId>0){
                AiyaShaderEffect.nSet(nativeObjId,"WindowWidth",width);
                AiyaShaderEffect.nSet(nativeObjId,"WindowHeight",height);
                AiyaShaderEffect.nSet(nativeObjId,"FrameWidth",width);
                AiyaShaderEffect.nSet(nativeObjId,"FrameHeight",height);
                super.sizeChanged(width, height);
                AiyaShaderEffect.nGlInit(nativeObjId);
            }
        }
    }

    @Override
    public void draw(int texture) {
        if(nativeObjId>0){
            long start=System.currentTimeMillis();
            AiyaShaderEffect.nDraw(nativeObjId,texture,0,0,mWidth,mHeight);
            count++;
            total += (System.currentTimeMillis() - start);
            if(count == 300) {
                Log.d("aiyaapp", "ShortVideoFilter average cost time:" + total/count);
                count = total = 0;
            }
        }
    }

}
