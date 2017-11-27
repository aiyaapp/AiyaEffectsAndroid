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

import android.content.Context;
import android.opengl.GLES20;

import com.aiyaapp.aiya.filter.AyBeautyFilter;
import com.aiyaapp.aiya.filter.SvCutSceneFilter;
import com.aiyaapp.aiya.panel.EffectListener;
import com.aiyaapp.aiya.render.AiyaGiftFilter;
import com.aiyaapp.aiya.render.AnimListener;
import com.wuwang.aavt.core.Renderer;
import com.wuwang.aavt.gl.BaseFilter;
import com.wuwang.aavt.gl.GroupFilter;
import com.wuwang.aavt.gl.LazyFilter;
import com.wuwang.aavt.log.AvLog;
import com.wuwang.aavt.utils.MatrixUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * DefaultEffectFlinger
 *
 * @author wuwang
 * @version v1.0 2017:11:09 10:43
 */
public class DefaultEffectFlinger implements EffectListener.EffectFlinger,Renderer {

    private AiyaGiftFilter mEffectFilter;
    private Context mContext;
    private AyBeautyFilter mAiyaBeautyFilter;
    private SluggardSvEffectTool mSvTool=SluggardSvEffectTool.getInstance();
    private LinkedList<Runnable> mTask=new LinkedList<>();
    private Class<? extends BaseFilter> mNowSvClazz;
    private BaseFilter mShowFilter;
    private float mBeautyDegree=0.0f;

    private int mWidth,mHeight;

    public DefaultEffectFlinger(Context context){
        this.mContext=context;
        mEffectFilter=new AiyaGiftFilter(mContext,new AiyaTracker(mContext));
        mEffectFilter.setAnimListener(new AnimListener() {
            @Override
            public void onAnimEvent(int i, int i1, String s) {
                AvLog.d("EffectFlingerInfo","-->"+i+"/"+i1+s);
            }
        });
        mShowFilter=new LazyFilter();
//        MatrixUtils.flip(mFilter.getVertexMatrix(),false,true);
    }

    public void runOnRender(Runnable runnable){
        mTask.add(runnable);
    }

    @Override
    public void onLookUpFilterChanged(int key, String path) {

    }

    @Override
    public void onEffectChanged(int key, String path) {
        if(key==0){
            mEffectFilter.setEffect(null);
        }else{
            mEffectFilter.setEffect(path);
        }
    }

    @Override
    public void onBeautyChanged(int key) {
        runOnRender(() -> {
            if(mAiyaBeautyFilter!=null){
                mAiyaBeautyFilter.destroy();
            }
            mAiyaBeautyFilter=new AyBeautyFilter(key);
            mAiyaBeautyFilter.create();
            mAiyaBeautyFilter.sizeChanged(mWidth,mHeight);
            mAiyaBeautyFilter.setDegree(mBeautyDegree);
        });

    }

    @Override
    public void onBeautyDegreeChanged(float degree) {
        runOnRender(()->{
            if(mAiyaBeautyFilter!=null){
                this.mBeautyDegree=degree;
                mAiyaBeautyFilter.setDegree(degree);
            }
        });
    }

    @Override
    public void onShortVideoEffectChanged(int key, String name, Class<? extends BaseFilter> clazz) {
        runOnRender(() -> {
            if(key==0){
                mNowSvClazz=null;
            }else{
                mNowSvClazz=clazz;
            }
        });
    }

    @Override
    public void create() {
        mEffectFilter.create();
        mShowFilter.create();
    }

    @Override
    public void sizeChanged(int width, int height) {
        this.mWidth=width;
        this.mHeight=height;
        mEffectFilter.sizeChanged(width, height);
        mShowFilter.sizeChanged(width, height);
    }

    @Override
    public void draw(int texture) {
        while (!mTask.isEmpty()){
            mTask.removeFirst().run();
        }
        //礼物特效处理
        texture=mEffectFilter.drawToTexture(texture);

        //美颜处理
        if(mAiyaBeautyFilter!=null){
            texture=mAiyaBeautyFilter.drawToTexture(texture);
        }

        //短视频特效处理
        if(mNowSvClazz!=null){
            texture=mSvTool.processTexture(texture,mWidth,mHeight,mNowSvClazz);
        }
        mShowFilter.draw(texture);
    }

    @Override
    public void destroy() {
        AvLog.d("wuwang","-->flinger destroy");
        mEffectFilter.destroy();
        mShowFilter.destroy();
        mSvTool.onGlDestroy();
    }

    public void release(){
        if(mEffectFilter!=null){
            mEffectFilter.release();
        }
    }

}
