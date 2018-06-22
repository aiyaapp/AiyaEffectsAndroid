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
package com.aiyaapp.aavt.gl;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * GroupFilter 滤镜组，将多个滤镜串联起来，合并成一个滤镜
 *
 * @author wuwang
 * @version v1.0 2017:10:31 11:53
 */
public class GroupFilter extends LazyFilter {

    private ArrayList<BaseFilter> mGroup;

    public GroupFilter(Resources resource) {
        super(resource);
    }

    public GroupFilter(){
        super();
    }

    @Override
    protected void initBuffer() {
        super.initBuffer();
        mGroup=new ArrayList<>();
    }

    public void addFilter(final BaseFilter filter){
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                filter.create();
                filter.sizeChanged(mWidth,mHeight);
                mGroup.add(filter);
            }
        });
    }

    public void addFilter(final int index,final BaseFilter filter){
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                filter.create();
                filter.sizeChanged(mWidth,mHeight);
                mGroup.add(index, filter);
            }
        });
    }

    public BaseFilter removeFilter(final int index){
        BaseFilter filter=mGroup.get(index);
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                BaseFilter filter=mGroup.remove(index);
                if(filter!=null){
                    filter.destroy();
                }
            }
        });
        return filter;
    }

    public void removeFilter(final BaseFilter filter){
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                mGroup.remove(filter);
            }
        });
    }

    public BaseFilter get(int index){
        return mGroup.get(index);
    }

    public Iterator<BaseFilter> iterator(){
        return mGroup.iterator();
    }

    public boolean isEmpty(){
        return mGroup.isEmpty();
    }

    @Override
    public void draw(int texture) {
        int tempTextureId=texture;
        for (int i=0;i<mGroup.size();i++){
            BaseFilter filter=mGroup.get(i);
            tempTextureId=filter.drawToTexture(tempTextureId);
        }
        super.draw(tempTextureId);
    }

}

