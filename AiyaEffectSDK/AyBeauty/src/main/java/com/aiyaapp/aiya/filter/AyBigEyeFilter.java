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
import com.aiyaapp.aiya.AiyaBeauty;


/**
 * AyBigEyeFilter
 *
 * @author wuwang
 * @version v1.0 2017:11:29 14:32
 */
public class AyBigEyeFilter extends BaseFilter {

    private long nativeId;
    private long faceId;
    private float degree=0.0f;
    private long count, total;
    public AyBigEyeFilter() {
        super(null, "", "");
        count = total = 0;
    }



    public void setDegree(final float degree){
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                AiyaBeauty.nSet(nativeId,"Degree",degree);
            }
        });
    }

    public void setFaceDataID(long id){
        this.faceId=id;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        nativeId= AiyaBeauty.nCreateNativeObj(AiyaBeauty.BIG_EYE);
        AiyaBeauty.nGlInit(nativeId);
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        super.onSizeChanged(width, height);
        AiyaBeauty.nSet(nativeId,"FrameWidth",width);
        AiyaBeauty.nSet(nativeId,"FrameHeight",height);
    }

    @Override
    public void draw(int texture) {
        onTaskExec();
        long start=System.currentTimeMillis();
        if(faceId!=0){
            AiyaBeauty.nSet(nativeId,"FaceData",faceId);
        }
        AiyaBeauty.nDraw(nativeId,texture,0,0,mWidth,mHeight);
        count++;
        total += (System.currentTimeMillis() - start);
        if(count == 300) {
            Log.d("aiyaapp", "AyBigEyeFilter average cost time:" + total/count);
            count = total = 0;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        AiyaBeauty.nGlDestroy(nativeId);
        AiyaBeauty.nDestroyNativeObj(nativeId);
    }

}
