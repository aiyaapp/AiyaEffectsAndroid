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

import com.aiyaapp.aavt.log.AvLog;
import com.aiyaapp.aiya.AiyaShaderEffect;

/**
 * SvSpiritFreedFilter 灵魂出窍
 *
 * @author wuwang
 * @version v1.0 2017:11:07 15:47
 */
public class SvSpiritFreedFilter extends BaseShortVideoFilter{

    public SvSpiritFreedFilter() {
        super(AiyaShaderEffect.TYPE_SPIRIT_FREED);
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        super.onSizeChanged(width, height);
        AvLog.d("nativeObjId","id->"+nativeObjId);
        AiyaShaderEffect.nSet(nativeObjId,"MaxScalingRatio",0.6f);
        AiyaShaderEffect.nSet(nativeObjId,"LastTime",8);
        AiyaShaderEffect.nSet(nativeObjId,"WaitTime",6);
        AiyaShaderEffect.nSet(nativeObjId,"ShadowAlpha",0.15f);
    }

}
