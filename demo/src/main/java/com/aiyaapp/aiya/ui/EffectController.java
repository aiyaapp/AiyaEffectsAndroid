package com.aiyaapp.aiya.ui;

import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.ViewAnimator;

import com.aiyaapp.aiya.R;
import com.aiyaapp.camera.sdk.AiyaEffects;
import com.aiyaapp.camera.sdk.base.ISdkManager;
import com.aiyaapp.camera.sdk.filter.AFilter;
import com.aiyaapp.camera.sdk.filter.LookupFilter;
import com.aiyaapp.camera.sdk.filter.SdkBeauty;
import com.aiyaapp.camera.sdk.widget.AiyaController;
import com.aiyaapp.camera.sdk.widget.CameraView;
import com.xw.repo.BubbleSeekBar;

/**
 * Created by aiya on 2017/7/27.
 */

public class EffectController implements SelectListener {

    private View container;
    private RecyclerView mEffectList;
    private RecyclerView mLookupList;
    private RecyclerView mBeautyList;
    private EffectAdapter mEffAdapter;
    private LookupAdapter mLooAdapter;
    private ViewAnimator mViewAnim;
    private RadioGroup mSelectGroup;
    private BubbleSeekBar mSeekBarFilter;
    private BubbleSeekBar mSeekBarBeauty;
    private BubbleSeekBar mSeekBarDayan;
    private BubbleSeekBar mSeekBarShoulian;

    private BubbleSeekBar mSeekBarMeibai;
    private BubbleSeekBar mSeekBarMopi;
    private BubbleSeekBar mSeekBarHongrun;

    private AiyaController mController;
    private CameraView mCameraView;

    private LookupFilter mLookupFilter;
    private SdkBeauty mMeibaiFilter;
    private SdkBeauty mMopiFilter;
    private SdkBeauty mHongrunFilter;

    private SparseIntArray selectKey= new SparseIntArray();

    public EffectController(final Activity act, View container, EffectAdapter.OnEffectCheckListener listener){
        this.container=container;

        selectKey.append(0,R.id.select_group_0);
        selectKey.append(1,R.id.select_group_1);
        selectKey.append(2,R.id.select_group_2);
        selectKey.append(3,R.id.select_group_3);
        selectKey.append(4,R.id.select_group_4);

        mSelectGroup= $(R.id.select_group);
        mViewAnim= $(R.id.mSelectAnim);
        mEffectList= $(R.id.mEffectList);
        mEffectList.setLayoutManager(new GridLayoutManager(act.getApplicationContext(),5));
        mEffectList.setAdapter(mEffAdapter=new EffectAdapter(act));
        mEffAdapter.setEffectCheckListener(listener);
        mLookupList= $(R.id.mLookupList);
        mLookupList.setLayoutManager(new LinearLayoutManager(act.getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
        mLookupList.setAdapter(mLooAdapter=new LookupAdapter(act));
        mLooAdapter.setSelectListener(this);
        mBeautyList= $(R.id.mBeautyList);
        mBeautyList.setLayoutManager(new LinearLayoutManager(act.getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
        mBeautyList.setAdapter(new BeautyAdapter(act));

        mSeekBarFilter= $(R.id.mSeekBarFilter);
        mSeekBarBeauty= $(R.id.mSeekBarBeauty);
        mSeekBarDayan= $(R.id.mSeekBarDayan);
        mSeekBarShoulian=$(R.id.mSeekBarShoulian);
        mSeekBarMeibai=$(R.id.mSeekBarMeibai);
        mSeekBarMopi=$(R.id.mSeekBarMopi);
        mSeekBarHongrun=$(R.id.mSeekBarHongrun);

        mSelectGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                Log.e("doggycoder","choose checkid:"+checkedId);
                mViewAnim.setDisplayedChild(selectKey.indexOfValue(checkedId));
            }
        });
        //滤镜程度控制
        mSeekBarFilter.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                mLookupFilter.setIntensity(progress/100f);
            }

            @Override
            public void getProgressOnActionUp(int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(int progress, float progressFloat) {

            }
        });
        //美颜等级控制，整数0-6,
        mSeekBarBeauty.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener(){

            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                AiyaEffects.getInstance().set(ISdkManager.SET_BEAUTY_LEVEL,progress);
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
                AiyaEffects.getInstance().set(ISdkManager.SET_OXEYE,progress);
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
                AiyaEffects.getInstance().set(ISdkManager.SET_THIN_FACE,progress);
            }

            @Override
            public void getProgressOnActionUp(int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(int progress, float progressFloat) {

            }
        });
        //美白程度控制，0-100
        mSeekBarMeibai.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                mMeibaiFilter.setLevel(progress);
            }

            @Override
            public void getProgressOnActionUp(int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(int progress, float progressFloat) {

            }
        });
        //磨皮程度控制，0-6
        mSeekBarMopi.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                mMopiFilter.setLevel(progress);
            }

            @Override
            public void getProgressOnActionUp(int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(int progress, float progressFloat) {

            }
        });
        //红润程度控制，0-100
        mSeekBarHongrun.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
                mHongrunFilter.setLevel(progress);
            }

            @Override
            public void getProgressOnActionUp(int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(int progress, float progressFloat) {

            }
        });
        filterInit();
    }

    public View getContentView(){
        return container;
    }

    public <T> T $(int id){
        return (T) getContentView().findViewById(id);
    }

    private void filterInit(){
        //滤镜
        mLookupFilter=new LookupFilter(getContentView().getResources());
        mLookupFilter.setIntensity(0.5f);
        //美白磨皮红润的滤镜
        mMeibaiFilter=new SdkBeauty(getContentView().getResources());
        mMeibaiFilter.setType(ISdkManager.BEAUTY_WHITEN);
        mMopiFilter=new SdkBeauty(getContentView().getResources());
        mMopiFilter.setType(ISdkManager.BEAUTY_SMOOTH);
        mHongrunFilter=new SdkBeauty(getContentView().getResources());
        mHongrunFilter.setType(ISdkManager.BEAUTY_SATURATE);

    }

    @Override
    public void onSelect(int pos, String data) {
        if(pos==0){
            removeFilter(mLookupFilter);
        }else{
            removeFilter(mLookupFilter);
            mLookupFilter.setMaskImage("shader/lookup/"+data);
            addFilter(mLookupFilter);
        }
    }

    public void release(){
        mViewAnim=null;
    }

    public void attachTo(Object obj){
        if(obj instanceof CameraView){
            mCameraView= (CameraView) obj;
            mController=null;
        }else if(obj instanceof AiyaController){
            mController= (AiyaController) obj;
            mCameraView=null;
        }
        addFilter(mMopiFilter);
        addFilter(mMeibaiFilter);
        addFilter(mHongrunFilter);
    }

    private void addFilter(AFilter filter){
        if(mCameraView!=null){
            mCameraView.addFilter(filter,true);
        }
        if(mController!=null){
            mController.addFilter(filter,true);
        }
    }

    private void removeFilter(AFilter filter){
        if(mCameraView!=null){
            mCameraView.removeFilter(filter);
        }
        if(mController!=null){
            mController.removeFilter(filter);
        }
    }


}
