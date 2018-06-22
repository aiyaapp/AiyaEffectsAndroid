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

/**
 * LazyFilter 绘制原始纹理的Filter,通过矩阵提供旋转缩放等功能
 *
 * @author wuwang
 * @version v1.0 2017:10:31 11:42
 */
public class LazyFilter extends BaseFilter {

    public LazyFilter(Resources resource) {
        super(resource,"shader/base.vert","shader/base.frag");
    }

    public LazyFilter(String vert,String frag){
        super(null,vert,frag);
    }

    public LazyFilter(){
        super(null,"attribute vec4 aVertexCo;\n" +
                        "attribute vec2 aTextureCo;\n" +
                        "\n" +
                        "uniform mat4 uVertexMatrix;\n" +
                        "uniform mat4 uTextureMatrix;\n" +
                        "\n" +
                        "varying vec2 vTextureCo;\n" +
                        "\n" +
                        "void main(){\n" +
                        "    gl_Position = uVertexMatrix*aVertexCo;\n" +
                        "    vTextureCo = (uTextureMatrix*vec4(aTextureCo,0,1)).xy;\n" +
                        "}",
                "precision mediump float;\n" +
                        "varying vec2 vTextureCo;\n" +
                        "uniform sampler2D uTexture;\n" +
                        "void main() {\n" +
                        "    gl_FragColor = texture2D( uTexture, vTextureCo);\n" +
                        "}");
    }

    @Override
    protected void onCreate() {
        super.onCreate();
    }

}
