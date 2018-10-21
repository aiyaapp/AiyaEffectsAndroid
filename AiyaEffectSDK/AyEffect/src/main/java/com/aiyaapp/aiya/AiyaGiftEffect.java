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
package com.aiyaapp.aiya;

import android.content.Context;
import android.util.Log;

import com.aiyaapp.aavt.log.AvLog;
import com.aiyaapp.aiya.base.IComponent;
import com.aiyaapp.aiya.render.AnimListener;

/**
 * AiyaGiftEffect 是脸萌效果、直播礼物特效等特效的入口类。它提供从资源文件中获取图像，
 * 并将图像在指定GL环境中渲染、或者呈现在界面上的功能。
 *
 * @author wuwang
 * @version v1.0 2017:10:30 15:06
 */
public class AiyaGiftEffect {
    private static final String TAG = "aiyaapp";
    /**
     * GL环境错误
     */
    public static final int MSG_ERROR_GL_ENVIRONMENT = 0xFE100001;

    /**
     * 特效初始化
     */
    public static final int MSG_STAT_EFFECTS_INIT = 0x00010000;

    /**
     * 特效播放中
     */
    public static final int MSG_STAT_EFFECTS_PLAY = 0x00020000;

    /**
     * 特效播放结束
     */
    public static final int MSG_STAT_EFFECTS_END = 0x00040000;

    /**
     * 特效播放开始
     */
    public static final int MSG_STAT_EFFECTS_START = 0x00080000;

    public static final WeakEventListener mListener = new WeakEventListener();

    private long nativeId;
    private IComponent mTracker;
    private final Object LOCK = new Object();
    private long count;
    private long total;

    public AiyaGiftEffect(Context context) {
        nativeId = _createGiftObject(context, 0);
        count = 0;
        total = 0;
        Log.d(TAG, "create nativeId:" + nativeId);
    }

    /**
     * 设置特效
     *
     * @param effect 特效路径
     */
    public void setEffect(String effect) {
        _setEffect(nativeId, effect);
    }

    /**
     * 渲染背景及特效
     *
     * @param textureId 背景纹理
     * @param width     渲染宽度
     * @param height    渲染高度
     * @return 渲染结果
     */
    public int draw(int textureId, int width, int height) {
        return _draw(nativeId, textureId, width, height);
    }

    /**
     * 渲染背景及特效
     *
     * @param textureId 背景纹理
     * @param width     渲染宽度
     * @param height    渲染高度
     * @param data      人脸检测用的图像数据
     * @return 渲染结果
     */
    public int draw(int textureId, int width, int height, byte[] data) {
        long start = System.currentTimeMillis();
        int ret = _draw(nativeId, textureId, width, height, data);
        count++;
        total += (System.currentTimeMillis() - start);
        if(count == 300) {
            Log.d(TAG, "AiyaGiftEffect average cost time:" + total/count);
            count = total = 0;
        }
        return ret;
    }

    /**
     * 暂停特效的播放
     *
     * @return
     */
    public int pause() {
        return _pause(nativeId, 1);
    }

    /**
     * 恢复特效的播放
     *
     * @return
     */
    public int resume() {
        return _pause(nativeId, 0);
    }

    public void onDestroyGL() {
        synchronized (LOCK) {
            _destroyGL(nativeId);
        }
    }

    public int setTracker(IComponent component, int imageType) {
        this.mTracker = component;
        return _setTracker(nativeId, mTracker.getId(), imageType);
    }

    public void setFaceDataID(long id) {
        _setOptions(nativeId, "FaceData", id);
    }

    public int setTrackSize(int width, int height) {
        return _setTrackSize(nativeId, width, height);
    }

    public String getVersionName() {
        return _getVersionName();
    }

    public int getVersionCode() {
        return _getVersionCode();
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }




    /**
     * 释放对象
     */
    public void release() {
        synchronized (LOCK) {
            Log.d(TAG, "nativeID:" + nativeId);
             _release(nativeId);
            nativeId = 0;
            if (mTracker != null) {
                mTracker.release();
            }
        }
    }

    /**
     * 设置事件监听器
     *
     * @param listener 监听器
     */
    public void setEventListener(AnimListener listener) {
        _setEventListener(nativeId, new WeakAnimListener(listener));
    }

    private static native long _createGiftObject(Context context, int type);

    private static native int _init(long id, String data);

    private static native int _setEffect(long id, String effect);

    private static native int _draw(long id, int texture, int width, int height);

    private static native int _draw(long id, int texture, int width, int height, byte[] data);

    private static native void _setEventListener(long id, AnimListener listener);

    private static native int _release(long id);

    private static native int _destroyGL(long id);

    private static native int _pause(long id, int pause);

    private static native int _setTracker(long id, long track, int type);

    private static native int _setTrackSize(long id, int width, int height);

    private static native int _setOptions(long id, String key, long value);

    private static native int _getVersionCode();

    private static native String _getVersionName();

    static {
        System.loadLibrary("AyCoreSdk");
        System.loadLibrary("AyCoreJni");
        System.loadLibrary("assimp");
        System.loadLibrary("gameplay");
        System.loadLibrary("ayeffects");
        System.loadLibrary("AyEffectJni");
    }

}

