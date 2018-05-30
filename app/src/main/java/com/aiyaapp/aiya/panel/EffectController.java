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

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.ViewAnimator;

import com.aiyaapp.aavt.gl.BaseFilter;
import com.aiyaapp.aiya.R;
import com.xw.repo.BubbleSeekBar;

/**
 * EffectControlWindow
 *
 * @author wuwang
 * @version v1.0 2017:11:09 09:42
 */
public class EffectController implements EffectListener.EffectFlinger, EffectListener.OnEffectCheckListener {

    private View container;
    private RecyclerView mEffectList;
    private RecyclerView mLookupList;
    private RecyclerView mBeautyList;
    private RecyclerView mShortVideoEffect;
    private EffectAdapter mEffAdapter;
    private LookupAdapter mLooAdapter;
    private ShortVideoEffectAdapter mShoAdapter;
    private ViewAnimator mViewAnim;
    private RadioGroup mSelectGroup;

    private BubbleSeekBar mSeekBarFilter;
    private BubbleSeekBar mSeekBarBeauty;
    private BubbleSeekBar mSeekBarDayan;
    private BubbleSeekBar mSeekBarShoulian;

    private BubbleSeekBar mSeekBarMeibai;
    private BubbleSeekBar mSeekBarMopi;
    private BubbleSeekBar mSeekBarHongrun;

    public EffectListener.EffectFlinger mFlinger;

    private SparseIntArray selectKey = new SparseIntArray();
    private Activity act;

    public EffectController(final Activity act, View container, EffectListener.EffectFlinger flinger) {
        this.act = act;
        this.container = container;
        this.mFlinger = flinger;
        selectKey.append(0, R.id.select_group_0);
        selectKey.append(1, R.id.select_group_1);
        selectKey.append(2, R.id.select_group_2);
        selectKey.append(3, R.id.select_group_3);
        selectKey.append(4, R.id.select_group_4);
        selectKey.append(5, R.id.select_group_5);

        mSelectGroup = $(R.id.select_group);
        mViewAnim = $(R.id.mSelectAnim);

        mEffectList = $(R.id.mEffectList);
        mEffectList.setLayoutManager(new GridLayoutManager(act.getApplicationContext(), 5));
        mEffectList.setAdapter(mEffAdapter = new EffectAdapter(act));
        mEffAdapter.setEffectChangedListener(this);
        mEffAdapter.setEffectCheckListener(this);

        mLookupList = $(R.id.mLookupList);
        mLookupList.setLayoutManager(new LinearLayoutManager(act.getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        mLookupList.setAdapter(mLooAdapter = new LookupAdapter(act));
        mLooAdapter.setSelectListener(this);

        mBeautyList = $(R.id.mBeautyList);
        mBeautyList.setLayoutManager(new LinearLayoutManager(act.getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        BeautyAdapter adapter = new BeautyAdapter(act);
        adapter.setOnBeautyChangedListener(this);
        mBeautyList.setAdapter(adapter);

        mShortVideoEffect = $(R.id.mShortVideoEffect);
        mShortVideoEffect.setLayoutManager(new GridLayoutManager(act.getApplicationContext(), 5));
        mShoAdapter = new ShortVideoEffectAdapter(act.getApplicationContext());
        mShoAdapter.setOnBeautyChangedListener(this);
        mShortVideoEffect.setAdapter(mShoAdapter);

        mSeekBarFilter = $(R.id.mSeekBarFilter);
        mSeekBarBeauty = $(R.id.mSeekBarBeauty);


        mSeekBarDayan = $(R.id.mSeekBarDayan);
        mSeekBarShoulian = $(R.id.mSeekBarShoulian);

        mSeekBarMeibai = $(R.id.mSeekBarMeibai);
        mSeekBarMopi = $(R.id.mSeekBarMopi);
        mSeekBarHongrun = $(R.id.mSeekBarHongrun);

        mSelectGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                Log.e("doggycoder", "choose checkid:" + checkedId);
                mViewAnim.setDisplayedChild(selectKey.indexOfValue(checkedId));
            }
        });
        //滤镜程度控制
        mSeekBarFilter.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
            }

            @Override
            public void getProgressOnActionUp(int progress, float progressFloat) {
            }

            @Override
            public void getProgressOnFinally(int progress, float progressFloat) {
            }
        });

        //美颜等级控制，小数0-1
        mSeekBarBeauty.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                onBeautyDegreeChanged(progressFloat);
            }
            @Override
            public void getProgressOnActionUp(int progress, float progressFloat) {
            }
            @Override
            public void getProgressOnFinally(int progress, float progressFloat) {
            }
        });

        //大眼程度控制，0-100
        mSeekBarDayan.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                onBigEyeDegreeChanged(progressFloat);
            }
            @Override
            public void getProgressOnActionUp(int progress, float progressFloat) {
            }
            @Override
            public void getProgressOnFinally(int progress, float progressFloat) {
            }
        });
        //瘦脸程度控制，0-100
        mSeekBarShoulian.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                onThinFaceDegreeChanged(progressFloat);
            }
            @Override
            public void getProgressOnActionUp(int progress, float progressFloat) {

            }
            @Override
            public void getProgressOnFinally(int progress, float progressFloat) {
            }
        });
        //美白程度控制，0-1
        mSeekBarMeibai.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                onBeautyBrightenDegreeChanged(progressFloat);
            }

            @Override
            public void getProgressOnActionUp(int progress, float progressFloat) {
            }

            @Override
            public void getProgressOnFinally(int progress, float progressFloat) {
            }
        });
        //磨皮程度控制，0-1
        mSeekBarMopi.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                onBeautySmoothDegreeChanged(progressFloat);
            }

            @Override
            public void getProgressOnActionUp(int progress, float progressFloat) {
            }

            @Override
            public void getProgressOnFinally(int progress, float progressFloat) {
            }
        });

        //红润程度控制，0-1
        mSeekBarHongrun.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                onBeautySaturateDegreeChanged(progressFloat);
            }

            @Override
            public void getProgressOnActionUp(int progress, float progressFloat) {
            }

            @Override
            public void getProgressOnFinally(int progress, float progressFloat) {
            }
        });
    }

    public View getContentView() {
        return container;
    }

    public void show() {
        container.setVisibility(View.VISIBLE);
    }

    public void hide() {
        container.setVisibility(View.GONE);
    }

    public <T> T $(int id) {
        return (T) getContentView().findViewById(id);
    }

    public void release() {
        mViewAnim = null;
    }

    @Override
    public void onEffectChanged(int key, String path) {
        mFlinger.onEffectChanged(key, path);
    }

    @Override
    public void onLookUpFilterChanged(int key, String path) {
        mFlinger.onLookUpFilterChanged(key, path);
    }


    @Override
    public void onBeautyChanged(int key) {
        if (key == 0) {//没有美颜
            mSeekBarBeauty.setProgress(0);
            mSeekBarMeibai.setProgress(0);
            mSeekBarHongrun.setProgress(0);
            mSeekBarMopi.setProgress(0);
        } else {
            mSeekBarBeauty.setProgress(0.4f);
            mSeekBarMeibai.setProgress(0.4f);
            mSeekBarHongrun.setProgress(0.4f);
            mSeekBarMopi.setProgress(0.4f);
        }
        mFlinger.onBeautyChanged(key);
    }

    @Override
    public void onBeautyDegreeChanged(float degree) {
        mFlinger.onBeautyDegreeChanged(degree);
    }

    @Override
    public void onBeautySmoothDegreeChanged(float degree) {
        mFlinger.onBeautySmoothDegreeChanged(degree);
    }

    @Override
    public void onBeautySaturateDegreeChanged(float degree) {
        mFlinger.onBeautySaturateDegreeChanged(degree);
    }

    @Override
    public void onBeautyBrightenDegreeChanged(float degree) {
        mFlinger.onBeautySaturateDegreeChanged(degree);
    }

    @Override
    public void onShortVideoEffectChanged(int key, String name, Class<? extends BaseFilter> clazz) {
        mFlinger.onShortVideoEffectChanged(key, name, clazz);
    }

    @Override
    public void onBigEyeDegreeChanged(float degree) {
        mFlinger.onBigEyeDegreeChanged(degree);
    }

    @Override
    public void onThinFaceDegreeChanged(float degree) {
        mFlinger.onThinFaceDegreeChanged(degree);
    }


    @Override
    public boolean onEffectChecked(int pos, String path) {
        if (pos == -1) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            act.startActivityForResult(Intent.createChooser(intent, "请选择一个json文件"), 101);
            return true;
        }
        return false;
    }
}
