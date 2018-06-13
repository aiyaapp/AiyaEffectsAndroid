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

import com.aiyaapp.aiya.AiyaShaderEffect;

/**
 * SvRollUpFilter 旋转分屏
 *
 * @author wuwang
 * @version v1.0 2017:11:07 15:42
 */
public class SvRollUpFilter extends BaseShortVideoFilter {

    public SvRollUpFilter() {
        super(AiyaShaderEffect.TYPE_ROLL_UP);
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        super.onSizeChanged(width, height);
        AiyaShaderEffect.nSet(nativeObjId,"SpliteSizeX",2);
        AiyaShaderEffect.nSet(nativeObjId,"SpliteSizeY",2);
        AiyaShaderEffect.nSet(nativeObjId,"ClockWise",1);
        AiyaShaderEffect.nSet(nativeObjId,"RollStepX",25);
        AiyaShaderEffect.nSet(nativeObjId,"RollStepY",40);
    }

}
