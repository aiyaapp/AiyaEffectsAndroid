package com.aiyaapp.aiya;

import android.content.Context;

import com.aiyaapp.aiya.gpuImage.AYGPUImageFilter;

import java.util.ArrayList;
import java.util.List;

public class AYAnimHandler extends AYEffectHandler {
    public AYAnimHandler(Context context) {
        super(context);
    }

    public AYAnimHandler(Context context, boolean useCurrentEGLContext) {
        super(context, useCurrentEGLContext);
    }

    @Override
    protected void commonProcess(boolean useDelay) {
        if (!initCommonProcess) {
            List<AYGPUImageFilter> filterChainArray = new ArrayList<AYGPUImageFilter>();

            if (effectFilter != null) {
                filterChainArray.add(effectFilter);
            }

            if (filterChainArray.size() > 0) {
                commonInputFilter.addTarget(filterChainArray.get(0));
                for (int x = 0; x < filterChainArray.size() - 1; x++) {
                    filterChainArray.get(x).addTarget(filterChainArray.get(x+1));
                }
                filterChainArray.get(filterChainArray.size()-1).addTarget(commonOutputFilter);

            }else {
                commonInputFilter.addTarget(commonOutputFilter);
            }

            initCommonProcess = true;
        }
    }
}
