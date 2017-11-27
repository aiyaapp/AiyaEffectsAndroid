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
package com.aiyaapp.aiya.panel;

import com.wuwang.aavt.gl.BaseFilter;

/**
 * EffectListener
 *
 * @author wuwang
 * @version v1.0 2017:11:09 10:07
 */
public class EffectListener {

    private EffectListener(){}

    /**
     * 滤镜更改监听
     */
    interface OnLookupFilterChangeListener{
        /**
         * 滤镜被更改时调用
         * @param key index
         * @param path 滤镜图片路径
         */
        void onLookUpFilterChanged(int key,String path);
    }

    interface OnEffectChangedListener{
        /**
         * 特效被更改时调用
         * @param key index
         * @param path 特效资源路径
         */
        void onEffectChanged(int key,String path);
    }

    interface OnBeautyChangedListener{
        /**
         * 美颜等级调节监听
         * @param key
         */
        void onBeautyChanged(int key);

        void onBeautyDegreeChanged(float degree);
    }

    interface OnShortVideoEffectChangedListener{
        /**
         * 短视频特效被更改时的监听
         * @param key index
         * @param name 短视频特效名称
         * @param clazz 特效滤镜类
         */
        void onShortVideoEffectChanged(int key,String name,Class<? extends BaseFilter> clazz);

    }

    public interface EffectFlinger extends OnLookupFilterChangeListener,OnEffectChangedListener,OnBeautyChangedListener,
    OnShortVideoEffectChangedListener{

    }

}
