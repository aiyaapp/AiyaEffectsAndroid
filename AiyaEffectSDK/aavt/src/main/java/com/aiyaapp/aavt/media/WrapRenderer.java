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

import com.aiyaapp.aavt.core.Renderer;
import com.aiyaapp.aavt.gl.OesFilter;


/**
 * WrapRenderer 用于包装其他Filter渲染OES纹理
 *
 * @author wuwang
 * @version v1.0 2017:10:27 08:53
 */
public class WrapRenderer implements Renderer {

    private Renderer mRenderer;
    private OesFilter mFilter;

    public static final int TYPE_MOVE = 0;
    public static final int TYPE_CAMERA = 1;

    public WrapRenderer(Renderer renderer) {
        this.mRenderer = renderer;
        mFilter = new OesFilter();
        setFlag(TYPE_MOVE);
    }

    public void setFlag(int flag) {
        if (flag == TYPE_MOVE) {
            mFilter.setVertexCo(new float[]{
                    -1.0f, 1.0f,
                    -1.0f, -1.0f,
                    1.0f, 1.0f,
                    1.0f, -1.0f,
            });
        } else if (flag == TYPE_CAMERA) {
            mFilter.setVertexCo(new float[]{
                    -1.0f, -1.0f,
                    1.0f, -1.0f,
                    -1.0f, 1.0f,
                    1.0f, 1.0f,
            });
        }
    }

    public float[] getTextureMatrix() {
        return mFilter.getTextureMatrix();
    }

    @Override
    public void create() {
        mFilter.create();
        if (mRenderer != null) {
            mRenderer.create();
        }
    }

    @Override
    public void sizeChanged(int width, int height) {
        mFilter.sizeChanged(width, height);
        if (mRenderer != null) {
            mRenderer.sizeChanged(width, height);
        }
    }

    @Override
    public void draw(int texture) {
        if (mRenderer != null) {
            mRenderer.draw(mFilter.drawToTexture(texture));
        } else {
            mFilter.draw(texture);
        }
    }

    @Override
    public void destroy() {
        if (mRenderer != null) {
            mRenderer.destroy();
        }
        mFilter.destroy();
    }
}

