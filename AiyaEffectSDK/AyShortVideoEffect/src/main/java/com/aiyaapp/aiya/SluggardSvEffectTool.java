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
package com.aiyaapp.aiya;


import com.aiyaapp.aavt.gl.BaseFilter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * LazyShortVideoEffectTool
 *
 * @author wuwang
 * @version v1.0 2017:11:15 15:11
 */
public class SluggardSvEffectTool{

    private long threadId=-1;
    private HashMap<Class,BaseFilter> filters=new HashMap<>();
    private static SluggardSvEffectTool instance;

    private SluggardSvEffectTool(){

    }

    /**
     * 获取SluggardSvEffectTool单例
     * @return SluggardSvEffectTool
     */
    public static SluggardSvEffectTool getInstance(){
        if(instance==null){
            synchronized (SluggardSvEffectTool.class){
                if(instance==null){
                    instance=new SluggardSvEffectTool();
                }
            }
        }
        return instance;
    }



    /**
     * 处理一个纹理，并输出处理后的纹理
     * @param texture 输入纹理
     * @param width 输出纹理宽度
     * @param height 输出纹理高度
     * @param clazz 滤镜类型
     * @return 输出纹理
     */
    public int processTexture(int texture,int width, int height, Class<? extends BaseFilter> clazz){
        long nowThreadId=Thread.currentThread().getId();
        if(nowThreadId!=threadId){
            filters.clear();
            threadId=nowThreadId;
        }
        BaseFilter filter=filters.get(clazz);
        if(filter==null){
            Constructor<? extends BaseFilter> cons;
            try {
                cons=clazz.getConstructor();
                filter=cons.newInstance();
                filter.create();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        if(filter!=null){
            filters.put(clazz,filter);
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

    /**
     * GL线程销毁时，调用此方法，以便及时回收资源
     */
    public void onGlDestroy(){
        for (Map.Entry<Class, BaseFilter> classBaseFilterEntry : filters.entrySet()) {
            BaseFilter filter = classBaseFilterEntry.getValue();
            filter.destroy();
        }
        filters.clear();
    }

}
