package com.aiyaapp.camera.sdk.filter;

import android.content.res.Resources;

/**
 * Created by aiya on 2017/4/12.
 */

public class BaseFilter extends AFilter {

    private String vert;
    private String frag;

    public BaseFilter(String vert,String frag){
        super(null);
        this.vert=vert;
        this.frag=frag;
    }

    @Override
    protected void onCreate() {
        createProgram(vert,frag);
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }
}
