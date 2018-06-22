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
package com.aiyaapp.aavt.gl;

import android.opengl.GLES20;

/**
 * FrameBuffer 工具类
 *
 * @author wuwang
 * @version v1.0 2017:10:31 10:31
 */
public class FrameBuffer {

    private int[] mFrameTemp;
    private int lastWidth = 0, lastHeight = 0;

    /**
     * 绑定到FrameBuffer，不使用RenderBuffer。{@link #bindFrameBuffer(int, int, boolean)}
     *
     * @param width  宽度
     * @param height 高度
     * @return 绑定结果，0表示成功，其他值为GL错误
     */
    public int bindFrameBuffer(int width, int height) {
        return bindFrameBuffer(width, height, false);
    }

    /**
     * 绑定到FrameBuffer
     *
     * @param width           宽度
     * @param height          高度
     * @param hasRenderBuffer 是否使用renderBuffer
     * @return 绑定结果，0表示成功，其他值为GL错误
     */
    public int bindFrameBuffer(int width, int height, boolean hasRenderBuffer) {
        if (lastWidth != width || lastHeight != height) {
            destroyFrameBuffer();
            this.lastWidth = width;
            this.lastHeight = height;
        }
        if (mFrameTemp == null) {
            return createFrameBuffer(hasRenderBuffer, width, height, GLES20.GL_TEXTURE_2D, GLES20.GL_RGBA,
                    GLES20.GL_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE);
        } else {
            return bindFrameBuffer();
        }
    }

    /**
     * 创建FrameBuffer
     *
     * @param hasRenderBuffer 是否启用RenderBuffer
     * @param width           宽度
     * @param height          高度
     * @param texType         类型，一般为{@link GLES20#GL_TEXTURE_2D}
     * @param texFormat       纹理格式，一般为{@link GLES20#GL_RGBA}、{@link GLES20#GL_RGB}等
     * @param minParams       纹理的缩小过滤参数
     * @param maxParams       纹理的放大过滤参数
     * @param wrapS           纹理的S环绕参数
     * @param wrapT           纹理的W环绕参数
     * @return 创建结果，0表示成功，其他值为GL错误
     */
    public int createFrameBuffer(boolean hasRenderBuffer, int width, int height, int texType, int texFormat,
                                 int minParams, int maxParams, int wrapS, int wrapT) {
        mFrameTemp = new int[4];
        GLES20.glGenFramebuffers(1, mFrameTemp, 0);
        GLES20.glGenTextures(1, mFrameTemp, 1);
        GLES20.glBindTexture(texType, mFrameTemp[1]);
        GLES20.glTexImage2D(texType, 0, texFormat, width, height,
                0, texFormat, GLES20.GL_UNSIGNED_BYTE, null);
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameteri(texType, GLES20.GL_TEXTURE_MIN_FILTER, minParams);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameteri(texType, GLES20.GL_TEXTURE_MAG_FILTER, maxParams);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(texType, GLES20.GL_TEXTURE_WRAP_S, wrapS);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(texType, GLES20.GL_TEXTURE_WRAP_T, wrapT);
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, mFrameTemp, 3);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameTemp[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, texType, mFrameTemp[1], 0);
        if (hasRenderBuffer) {
            GLES20.glGenRenderbuffers(1, mFrameTemp, 2);
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mFrameTemp[2]);
            GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, mFrameTemp[2]);
        }
        return GLES20.glGetError();


    }

    /**
     * 绑定FrameBuffer，只有之前创建过FrameBuffer，才能调用此方法进行绑定
     *
     * @return 绑定结果
     */
    public int bindFrameBuffer() {
        if (mFrameTemp == null) {
            return -1;
        }
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, mFrameTemp, 3);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameTemp[0]);
        return GLES20.glGetError();
    }

    /**
     * 取消FrameBuffer绑定
     */
    public void unBindFrameBuffer() {
        if (mFrameTemp != null) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameTemp[3]);
        }
    }

    /**
     * 获取绘制再FrameBuffer中的内容
     *
     * @return FrameBuffer绘制内容的纹理ID
     */
    public int getCacheTextureId() {
        return mFrameTemp != null ? mFrameTemp[1] : -1;
    }

    /**
     * 销毁FrameBuffer
     */
    public void destroyFrameBuffer() {
        if (mFrameTemp != null) {
            GLES20.glDeleteFramebuffers(1, mFrameTemp, 0);
            GLES20.glDeleteTextures(1, mFrameTemp, 1);
            if (mFrameTemp[2] > 0) {
                GLES20.glDeleteRenderbuffers(1, mFrameTemp, 2);
            }
            mFrameTemp = null;
        }
    }

}

