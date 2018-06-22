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
package com.aiyaapp.aiya.shadereffect.filter;

import android.content.Context;

import com.aiyaapp.aiya.AiyaShaderEffect;

/**
 * Cutscene
 *
 * @author wuwang
 * @version v1.0 2017:11:02 17:16
 */
public class Cutscene extends NativeCoolFilter{

    public Cutscene() {
        super(AiyaShaderEffect.TYPE_CUT_SCENE);
    }

    public void setDirection(final int direction){
        runAfterNativeObjCreated(new Runnable() {
            @Override
            public void run() {
                AiyaShaderEffect.nSet(id,"Direction",direction);
            }
        });
    }

    @Override
    public int draw(int texture, int width, int height,int flags) {
        return super.draw(texture, width, height,flags);
    }
}
