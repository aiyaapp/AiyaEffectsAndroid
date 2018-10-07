/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aiyaapp.aiya;

import android.content.Context;

import com.aiyaapp.aavt.core.Renderer;
import com.aiyaapp.aavt.gl.BaseFilter;
import com.aiyaapp.aavt.gl.LazyFilter;
import com.aiyaapp.aavt.log.AvLog;

import com.aiyaapp.aiya.filter.AyBeautyFilter;
import com.aiyaapp.aiya.filter.AyBigEyeFilter;
import com.aiyaapp.aiya.filter.AyThinFaceFilter;
import com.aiyaapp.aiya.filter.AyTrackFilter;

import com.aiyaapp.aiya.panel.EffectListener;
import com.aiyaapp.aiya.render.AiyaGiftFilter;
import com.aiyaapp.aiya.render.AnimListener;

import java.util.LinkedList;

/**
 * DefaultEffectFlinger
 *
 * @author wuwang
 * @version v1.0 2017:11:09 10:43
 */
public class DefaultEffectFlinger implements EffectListener.EffectFlinger, Renderer {

    private AiyaGiftFilter mEffectFilter;
    private Context mContext;
    private AyBeautyFilter mAiyaBeautyFilter;
    private SluggardSvEffectTool mSvTool = SluggardSvEffectTool.getInstance();

    private LinkedList<Runnable> mTask = new LinkedList<>();
    private Class<? extends BaseFilter> mNowSvClazz;
    private BaseFilter mShowFilter;
    private AyTrackFilter mTrackFilter;

    private AyBigEyeFilter mBigEyeFilter;
    private AyThinFaceFilter mThinFaceFilter;

    private float mBeautyDegree = 0.0f;
    private float mBigEyeDegree = 0.0f;
    private float mThinFaceDegree = 0.0f;
    private float mSmoothDegree = 0.0f;
    private float mSaturateDegree = 0.0f;
    private float mBrightenDegree = 0.0f;
    private int mWidth, mHeight;


    public DefaultEffectFlinger(Context context) {
        this.mContext = context;
        mEffectFilter = new AiyaGiftFilter(mContext, null);
        mEffectFilter.setAnimListener(new AnimListener() {
            @Override
            public void onAnimEvent(int i, int i1, String s) {
                AvLog.d("EffectFlingerInfo", "-->" + i + "/" + i1 + s);
            }
        });
        mShowFilter = new LazyFilter();
        mBigEyeFilter = new AyBigEyeFilter();
        mThinFaceFilter = new AyThinFaceFilter();
        mTrackFilter = new AyTrackFilter(context);
        //MatrixUtils.flip(mFilter.getVertexMatrix(),false,true);
    }


    public void runOnRender(Runnable runnable) {
        mTask.add(runnable);
    }


    @Override
    public void onLookUpFilterChanged(int key, String path) {

    }

    @Override
    public void onEffectChanged(int key, String path) {
        if (key == 0) {
            mEffectFilter.setEffect(null);
        } else {
            mEffectFilter.setEffect(path);
        }
    }

    public void setEffect(String path) {
        mEffectFilter.setEffect(path);
    }

    private int mBeautyType = AiyaBeauty.TYPE1;

    @Override
    public void onBeautyChanged(int key) {
        runOnRender(() -> {
            mBeautyType = key;
            if (mAiyaBeautyFilter != null) {
                mAiyaBeautyFilter.destroy();
            }
            mAiyaBeautyFilter = new AyBeautyFilter(key);
            mAiyaBeautyFilter.create();
            mAiyaBeautyFilter.sizeChanged(mWidth, mHeight);

//            mAiyaBeautyFilter.setDegree(mBeautyDegree);
//            if (mSmoothDegree > 0) {
//                mAiyaBeautyFilter.setSmoothDegree(mSmoothDegree);
//            }
//            if (mSaturateDegree > 0) {
//                mAiyaBeautyFilter.setSaturateDegree(mSaturateDegree);
//            }
//            if (mBrightenDegree > 0) {
//                mAiyaBeautyFilter.setBrightenDegree(mBrightenDegree);
//            }

        });
    }


    @Override
    public void onBeautyDegreeChanged(float degree) {
        runOnRender(() -> {
            if (mAiyaBeautyFilter != null) {
                this.mBeautyDegree = degree;
                mAiyaBeautyFilter.setDegree(degree);
            }
        });
    }


    @Override
    public void onBeautySmoothDegreeChanged(float degree) {
        if (mBeautyType == AiyaBeauty.TYPE2) return;
        runOnRender(() -> {
            if (mAiyaBeautyFilter != null) {
                this.mSmoothDegree = degree;
                mAiyaBeautyFilter.setSmoothDegree(degree);
            }
        });
    }

    @Override
    public void onBeautySaturateDegreeChanged(float degree) {
        if (mBeautyType == AiyaBeauty.TYPE2) return;
        runOnRender(() -> {
            if (mAiyaBeautyFilter != null) {
                this.mSaturateDegree = degree;
                mAiyaBeautyFilter.setSaturateDegree(degree);
            }
        });
    }

    @Override
    public void onBeautyBrightenDegreeChanged(float degree) {
        if (mBeautyType == AiyaBeauty.TYPE2) return;
        runOnRender(() -> {
            if (mAiyaBeautyFilter != null) {
                this.mBrightenDegree = degree;
                mAiyaBeautyFilter.setBrightenDegree(degree);
            }
        });
    }


    @Override
    public void onShortVideoEffectChanged(int key, String name, Class<? extends BaseFilter> clazz) {
        runOnRender(() -> {
            if (key == 0) {
                mNowSvClazz = null;
            } else {
                mNowSvClazz = clazz;
            }
        });
    }


    @Override
    public void create() {
        mTrackFilter.create();
        mEffectFilter.create();
        mShowFilter.create();
        mBigEyeFilter.create();
        mThinFaceFilter.create();
    }

    @Override
    public void sizeChanged(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        mTrackFilter.sizeChanged(width, height);
        mEffectFilter.sizeChanged(width, height);
        mShowFilter.sizeChanged(width, height);
        mBigEyeFilter.sizeChanged(width, height);
        mThinFaceFilter.sizeChanged(width, height);
    }


    @Override
    public void draw(int texture) {
        while (!mTask.isEmpty()) {
            mTask.removeFirst().run();
        }
        mTrackFilter.drawToTexture(texture);
        //礼物特效处理
        mEffectFilter.setFaceDataID(mTrackFilter.getFaceDataID());
        texture = mEffectFilter.drawToTexture(texture);
        //美颜处理
        if (mAiyaBeautyFilter != null) {
            texture = mAiyaBeautyFilter.drawToTexture(texture);
        }
        //大眼处理
        if (mBigEyeDegree > 0) {
            if (mTrackFilter != null) {
                mBigEyeFilter.setFaceDataID(mTrackFilter.getFaceDataID());
            }
            texture = mBigEyeFilter.drawToTexture(texture);
        }
        //瘦脸处理
        if (mThinFaceDegree > 0) {
            if (mTrackFilter != null) {
                mThinFaceFilter.setFaceDataID(mTrackFilter.getFaceDataID());
            }
            texture = mThinFaceFilter.drawToTexture(texture);
        }
        //短视频特效处理
        if (mNowSvClazz != null) {
            texture = mSvTool.processTexture(texture, mWidth, mHeight, mNowSvClazz);
        }
        mShowFilter.draw(texture);
    }

    @Override
    public void destroy() {
        AvLog.d("aiyaapp", "-->flinger destroy");
        if(mThinFaceFilter != null)
            mThinFaceFilter.destroy();
        if(mBigEyeFilter != null)
            mBigEyeFilter.destroy();
        if(mAiyaBeautyFilter != null)
            mAiyaBeautyFilter.destroy();
        mEffectFilter.destroy();
        mShowFilter.destroy();
        mSvTool.onGlDestroy();
        mTrackFilter.destroy();
    }


    public void release() {
        if (mEffectFilter != null) {
            mEffectFilter.release();
        }
        if (mTrackFilter != null) {
            mTrackFilter.release();
        }
    }


    @Override
    public void onBigEyeDegreeChanged(float degree) {
        this.mBigEyeDegree = degree;
        mBigEyeFilter.setDegree(degree);
    }


    @Override
    public void onThinFaceDegreeChanged(float degree) {
        this.mThinFaceDegree = degree;
        mThinFaceFilter.setDegree(degree);
    }
}
