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

import com.aiyaapp.aavt.log.AvLog;
import com.aiyaapp.aiya.AiyaShaderEffect;

/**
 * SvCutSceneFilter 转场动画
 *
 * @author wuwang
 * @version v1.0 2017:11:07 16:25
 */
public class SvCutSceneFilter extends BaseShortVideoFilter {

    private int direction = 0;
    private long calls, total_time;

    public SvCutSceneFilter() {
        super(AiyaShaderEffect.TYPE_CUT_SCENE);
        calls = total_time = 0;
    }


    @Override
    protected void onSizeChanged(int width, int height) {
        super.onSizeChanged(width, height);
        AiyaShaderEffect.nSet(nativeObjId, "Direction", direction);
    }
    @Override
    public void draw(int texture) {
        if (nativeObjId > 0) {
            long start = System.currentTimeMillis();
            System.out.println("nativeObjId 1=" + nativeObjId);
            int count = AiyaShaderEffect.nDraw(nativeObjId, texture, 0, 0, mWidth, mHeight);
            System.out.println("count =" + count);
            if (count == 4) {
                AiyaShaderEffect.nRestart(nativeObjId);
            }
            calls++;
            total_time += (System.currentTimeMillis() - start);
            if(calls == 300) {
                Log.d("aiyaapp", "SvCutSceneFilter average cost time:" + total_time /count);
                calls = total_time = 0;
            }
            AvLog.d("ShortVideoFilter Draw cost time:" + (System.currentTimeMillis() - start));
        }
    }

}
