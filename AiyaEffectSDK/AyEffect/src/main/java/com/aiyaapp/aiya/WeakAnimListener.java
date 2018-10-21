package com.aiyaapp.aiya;

import com.aiyaapp.aiya.render.AnimListener;

import java.lang.ref.WeakReference;

/** Created by aiya on 2017/9/25. */
class WeakAnimListener implements AnimListener {

  // private WeakReference<AnimListener> mWeak;
  private AnimListener mListener;

  WeakAnimListener(AnimListener listener) {
    // mWeak = new WeakReference<>(listener);
    mListener = listener;
  }

  @Override
  public void onAnimEvent(int type, int ret, String message) {
    // AnimListener listener = mWeak.get();
    if (mListener != null) {
      mListener.onAnimEvent(type, ret, message);
    }
  }
}
