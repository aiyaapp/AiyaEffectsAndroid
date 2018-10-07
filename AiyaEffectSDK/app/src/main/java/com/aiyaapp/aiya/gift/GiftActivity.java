package com.aiyaapp.aiya.gift;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.aiyaapp.aiya.AiyaGiftEffect;
import com.aiyaapp.aiya.Const;
import com.aiyaapp.aiya.R;
import com.aiyaapp.aiya.render.AiyaMutilEffectView;

/**
 * Created by aiya on 2017/9/21.
 */

public class GiftActivity extends AppCompatActivity {
    private static final String TAG = "aiyaapp";
    private AiyaMutilEffectView mGift;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift);
        mGift= (AiyaMutilEffectView) findViewById(R.id.mGift);
        mGift.forbidChangeSizeWhenSurfaceRecreate(true);
        mGift.pauseIfSurfaceDestroyed(true);
        mGift.setEffect(AiyaMutilEffectView.Layer.TOP,"assets/modelsticker/2017/meta.json");
        mGift.setEffect(AiyaMutilEffectView.Layer.BOTTOM,"assets/modelsticker/shiwaitaoyuan/meta.json");

        mGift.setMultiAnimListener(new AiyaMutilEffectView.MultiAnimListener() {

            @Override
            public void onAnimEvent(AiyaMutilEffectView.Layer layer, int i, int i1, String s) {
                if(i== Const.MSG_TYPE_INFO){
                    if(i1== AiyaGiftEffect.MSG_STAT_EFFECTS_END){
                        Log.d(TAG,"播放完成:"+layer.toString());
                        Log.d(TAG,"isAllAnimEnd:"+mGift.isAllAnimEnd());
                    }else if(i1==AiyaGiftEffect.MSG_STAT_EFFECTS_START){
                        Log.d(TAG,"播放开始:"+layer.toString());
                    }
                }else if(i==Const.MSG_TYPE_ERROR){
                    Log.e(TAG,"错误："+layer.toString()+"/"+i+"/"+i1+"/"+s);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGift.release();
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.mBtnReplay:
                mGift.setEffect(AiyaMutilEffectView.Layer.TOP,"assets/modelsticker/shiwaitaoyuan/meta.json");
                break;
        }
    }
}
