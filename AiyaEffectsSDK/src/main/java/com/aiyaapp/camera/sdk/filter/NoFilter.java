/*
 *
 * NoFilter.java
 * 
 * Created by Wuwang on 2016/11/19
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.filter;

import android.content.res.Resources;

/**
 * 无特殊处理的Filter，用于将一张纹理渲染到指定的位置
 */
public class NoFilter extends AFilter {

    public NoFilter(Resources res) {
        super(res);
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/base_vertex.sh",
            "shader/base_fragment.sh");
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }

}
