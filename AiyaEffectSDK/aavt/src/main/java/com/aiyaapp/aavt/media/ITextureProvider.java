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
package com.aiyaapp.aavt.media;

import android.graphics.Point;
import android.graphics.SurfaceTexture;

/**
 * ITextureProvider
 *
 * @author wuwang
 * @version v1.0 2017:10:26 18:18
 */
public interface ITextureProvider {

    /**
     * 打开视频流数据源
     *
     * @param surface 数据流输出到此
     * @return 视频流的宽高
     */
    Point open(SurfaceTexture surface);

    /**
     * 关闭视频流数据源
     */
    void close();

    /**
     * 切换摄像头
     */
    void swithCamera();

    /**
     * 获取一帧数据
     *
     * @return 是否最后一帧
     */
    boolean frame();

    /**
     * 获取当前帧时间戳
     *
     * @return 时间戳
     */
    long getTimeStamp();

    /**
     * 视频流是否是横向的
     *
     * @return true or false
     */
    boolean isLandscape();

}