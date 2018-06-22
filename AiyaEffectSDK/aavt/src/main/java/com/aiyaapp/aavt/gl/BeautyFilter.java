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

import android.content.res.Resources;
import android.opengl.GLES20;

/**
 * BeautyFilter 美颜滤镜
 *
 * @author wuwang
 * @version v1.0 2017:10:31 11:46
 */
public class BeautyFilter extends BaseFilter {

    private int mGLaaCoef;
    private int mGLmixCoef;
    private int mGLiternum;


    private float aaCoef;
    private float mixCoef;
    private int iternum;

    public BeautyFilter(Resources resource) {
        super(resource,"shader/beauty/beauty.vert", "shader/beauty/beauty.frag");
        shaderNeedTextureSize(true);
        setBeautyLevel(0);
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        mGLaaCoef= GLES20.glGetUniformLocation(mGLProgram,"uACoef");
        mGLmixCoef=GLES20.glGetUniformLocation(mGLProgram,"uMixCoef");
        mGLiternum=GLES20.glGetUniformLocation(mGLProgram,"uIternum");
    }

    public BaseFilter setBeautyLevel(int level){
        switch (level){
            case 1:
                a(1,0.19f,0.54f);
                break;
            case 2:
                a(2,0.29f,0.54f);
                break;
            case 3:
                a(3,0.17f,0.39f);
                break;
            case 4:
                a(3,0.25f,0.54f);
                break;
            case 5:
                a(4,0.13f,0.54f);
                break;
            case 6:
                a(4,0.19f,0.69f);
                break;
            default:
                a(0,0f,0f);
                break;
        }
        return this;
    }

    private void a(final int a, final float b, final float c){
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1f(mGLaaCoef,b);
                GLES20.glUniform1f(mGLmixCoef,c);
                GLES20.glUniform1i(mGLiternum,a);
            }
        });
    }

}

