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
package com.aiyaapp.aavt.expend;

import com.aiyaapp.aavt.gl.BaseFilter;

import java.util.HashMap;
import java.util.Map;

/**
 * SluggardFilterTool 懒汉Filter工具，该工具用于快速使用{@link BaseFilter}及其子类来处理纹理，而无需关注框架中
 * 的滤镜处理流程。通常情况下，不推荐使用此工具类，推荐尽可能熟悉框架中的滤镜处理流程。然后自行根据业务逻辑，
 * 参照此类进行封装。
 *
 * @author wuwang
 * @version v1.0 2017:11:11 15:41
 */
public class SluggardFilterTool {

    private long threadId=-1;
    private HashMap<Class,BaseFilter> filters=new HashMap<>();

    /**
     * 处理一个纹理，并输出处理后的纹理
     * @param texture 输入纹理
     * @param width 输出纹理宽度
     * @param height 输出纹理高度
     * @param clazz 滤镜类型
     * @return 输出纹理
     */
    public int processTexture(int texture, int width, int height, Class<? extends BaseFilter> clazz){
        long nowThreadId=Thread.currentThread().getId();
        if(nowThreadId!=threadId){
            filters.clear();
            threadId=nowThreadId;
        }
        BaseFilter filter=filters.get(clazz);
        if(filter==null){
            try {
                filter=clazz.newInstance();
                filter.create();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            filters.put(clazz,filter);
        }
        if(filter!=null){
            filter.sizeChanged(width, height);
            return filter.drawToTexture(texture);
        }
        return -1;
    }

    /**
     * 处理一个纹理，并输出处理后的纹理
     * @param texture 输入纹理
     * @param width 输出纹理宽度
     * @param height 输出纹理高度
     * @param filter 滤镜实体
     * @return 输出纹理
     */
    public int processTexture(int texture,int width,int height,BaseFilter filter){
        long nowThreadId=Thread.currentThread().getId();
        if(nowThreadId!=threadId){
            filters.clear();
            threadId=nowThreadId;
        }
        if(filter!=null){
            Class clazz=filter.getClass();
            if(filters.get(clazz)==null){
                filters.put(clazz,filter);
                filter.create();
            }
            filter.sizeChanged(width, height);
            return filter.drawToTexture(texture);
        }
        return -1;
    }

    public void onGlDestroy(){
        for (Map.Entry<Class, BaseFilter> classBaseFilterEntry : filters.entrySet()) {
            BaseFilter filter = classBaseFilterEntry.getValue();
            filter.destroy();
        }
        filters.clear();
    }

}
