package com.aiyaapp.aavt.gl;

import android.content.res.Resources;

/**
 * Created by 15581 on 2017/9/30.
 */

public class CandyFilter extends GroupFilter {

    public CandyFilter(Resources resource) {
        super(resource);
    }

    @Override
    protected void initBuffer() {
        super.initBuffer();
        addFilter(new GrayFilter(mRes));
        addFilter(new BaseFuncFilter(mRes,BaseFuncFilter.FILTER_GAUSS));
        addFilter(new BaseFuncFilter(mRes,BaseFuncFilter.FILTER_SOBEL));
    }
}
