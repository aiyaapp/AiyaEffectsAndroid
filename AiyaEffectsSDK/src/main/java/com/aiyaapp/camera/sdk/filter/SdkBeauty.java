package com.aiyaapp.camera.sdk.filter;

import android.content.res.Resources;
import android.util.Log;
import com.aiyaapp.camera.sdk.AiyaEffects;

/**
 * Created by aiya on 2017/7/21.
 */

public class SdkBeauty extends AFilter {

    private int type;
    private int width;
    private int height;
    private int level;

    public SdkBeauty(Resources mRes){
        super(mRes);
    }

    public void setType(int type){
        this.type=type;
    }

    public void setLevel(int level){
        this.level=level;
    }

    @Override
    protected void onCreate() {

    }

    @Override
    protected void onSizeChanged(int width, int height) {
        this.width=width;
        this.height=height;
    }

    @Override
    public void draw() {
        int ret=AiyaEffects.getInstance().beauty(type,getTextureId(),width,height,level);
        Log.e("SdkBeauty","beauty ret:"+ret);
    }
}
