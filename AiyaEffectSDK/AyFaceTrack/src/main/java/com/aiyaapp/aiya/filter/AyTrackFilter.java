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
package com.aiyaapp.aiya.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.aiyaapp.aavt.gl.LazyFilter;
import com.aiyaapp.aiya.AiyaTracker;

import java.nio.ByteBuffer;

/**
 * AyTrackFilter
 *
 * @author wuwang
 * @version v1.0 2017:11:29 15:50
 */
public class AyTrackFilter extends LazyFilter {

    private AiyaTracker mTracker;
    private int mTrackWidth = 180;
    private int mTrackHeight = 320;
    private int[] viewPort = new int[4];
    private ByteBuffer mTrackBuffer;

    public AyTrackFilter(Context context) {
        mTracker = new AiyaTracker(context);
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        super.onSizeChanged(width, height);
        if (width > height && width > 320) {
            if (width > 320) {
                mTrackWidth = 320;
                mTrackHeight = 320 * height / width;
            }
        } else if (height > width && height > 320) {
            if (height > 320) {
                mTrackHeight = 320;
                mTrackWidth = 320 * width / height;
            }
        } else {
            mTrackWidth = width;
            mTrackHeight = height;
        }
        mTrackBuffer = ByteBuffer.allocate(mTrackWidth * mTrackHeight * 4);
    }

    @Override
    public void draw(int texture) {
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewPort, 0);
        GLES20.glViewport(0, 0, mTrackWidth, mTrackHeight);
        super.draw(texture);
        GLES20.glReadPixels(0, 0, mTrackWidth, mTrackHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mTrackBuffer);
        GLES20.glViewport(viewPort[0], viewPort[1], viewPort[2], viewPort[3]);
        mTracker.track(AiyaTracker.IMAGE_TYPE_RGBA, mTrackBuffer.array(), mTrackWidth, mTrackHeight);
    }


    public long getFaceDataID() {
        return mTracker.getFaceDataID();
    }


    public void release() {
        if (mTracker != null) {
            mTracker.release();
        }
    }


    public AiyaTracker getAiyaTracker() {
        return mTracker;
    }

}
