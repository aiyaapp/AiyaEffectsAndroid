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
 * BeautyFilter 美颜滤镜
 *
 * @author wuwang
 * @version v1.0 2017:11:14 16:34
 */
public class AyBeautyFilter extends BaseFilter {

    private long nativeId;
    private int beautyType = AiyaBeauty.TYPE1;
    private long count, total;
    public AyBeautyFilter(int type) {
        super(null, "", "");
        if (type != 0) {
            this.beautyType = type;
        }
        count = total = 0;
    }

    @Override
    protected void initBuffer() {

    }

    /**
     * 设置美颜程度
     *
     * @param degree 美颜程度，0-1之间
     */
    public void setDegree(final float degree) {
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                AvLog.d("degree", "degree->" + degree);
                AiyaBeauty.nSet(nativeId, "Degree", degree);
            }
        });
    }
    /**
     * 设置磨皮系数
     *
     * @param degree
     */
    public void setSmoothDegree(final float degree) {
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                AvLog.d("SmoothDegree", "SmoothDegree->" + degree);
                AiyaBeauty.nSet(nativeId, "SmoothDegree", degree);
            }
        });

    }

    /**
     * 设置红润度系数
     * @param degree
     */
    public void setSaturateDegree(final float degree) {
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                AvLog.d("SaturateDegree", "SaturateDegree->" + degree);
                AiyaBeauty.nSet(nativeId, "SaturateDegree", degree);
            }
        });

    }


    /**
     * 设置美白系数
     *
     * @param degree
     */
    public void setBrightenDegree(final float degree) {
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                AvLog.d("WhitenDegree", "WhitenDegree->" + degree);
                AiyaBeauty.nSet(nativeId, "WhitenDegree", degree);
            }
        });

    }


    @Override
    protected void onCreate() {
        super.onCreate();
        nativeId = AiyaBeauty.nCreateNativeObj(beautyType);
        AiyaBeauty.nGlInit(nativeId);
    }


    @Override
    protected void onSizeChanged(int width, int height) {
        super.onSizeChanged(width, height);
        AiyaBeauty.nSet(nativeId, "FrameWidth", width);
        AiyaBeauty.nSet(nativeId, "FrameHeight", height);
    }

    @Override
    public void draw(int texture) {
        onTaskExec();
        long start = System.currentTimeMillis();
        AiyaBeauty.nDraw(nativeId, texture, 0, 0, mWidth, mHeight);
        count++;
        total += (System.currentTimeMillis() - start);
        if(count == 300) {
            Log.d("aiyaapp", "AyBeautyFilter average cost time:" + total/count);
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
