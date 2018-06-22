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

import com.aiyaapp.aavt.utils.MatrixUtils;

import java.nio.ByteBuffer;

/**
 * YuvOutputFilter
 *
 * @author wuwang
 * @version v1.0 2017:11:10 12:18
 */
public class YuvOutputFilter extends BaseFilter {


    private ByteBuffer mTempBuffer;

    private int[] lastViewPort=new int[4];

    public static final int EXPORT_TYPE_I420=1;
    public static final int EXPORT_TYPE_YV12=2;
    public static final int EXPORT_TYPE_NV12=3;
    public static final int EXPORT_TYPE_NV21=4;

    private BaseFilter mExportFilter;
    private LazyFilter mScaleFilter;

    public YuvOutputFilter(int type) {
        super(null,"None","None");
        mExportFilter=new YuvOutputFilter.ExportFilter(type);
    }

    public YuvOutputFilter(Resources res, String yuvShader){
        super(null,"None","None");
        mExportFilter=new YuvOutputFilter.ExportFilter(res,yuvShader);
    }

    @Override
    public float[] getTextureMatrix() {
        return mExportFilter.getTextureMatrix();
    }

    @Override
    protected void onCreate() {
        mExportFilter.create();
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        mExportFilter.sizeChanged(width, height);
        mTempBuffer=ByteBuffer.allocate(mWidth*mHeight*3/2);
    }

    public void setInputTextureSize(final int width, final int height){
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                mScaleFilter=new LazyFilter();
                mScaleFilter.create();
                mScaleFilter.sizeChanged(mWidth,mHeight);
                MatrixUtils.getMatrix(mScaleFilter.getVertexMatrix(),MatrixUtils.TYPE_CENTERCROP,width,height,
                        mWidth,mHeight);
            }
        });
    }

    @Override
    public void draw(int texture) {
        onTaskExec();
        boolean isBlend= GLES20.glIsEnabled(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT,lastViewPort,0);
        GLES20.glViewport(0,0,mWidth,mHeight);
        if(mScaleFilter!=null){
            mExportFilter.draw(mScaleFilter.drawToTexture(texture));
        }else{
            mExportFilter.draw(texture);
        }
        GLES20.glReadPixels(0,0,mWidth,mHeight*3/8,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,mTempBuffer);
        GLES20.glViewport(lastViewPort[0],lastViewPort[1],lastViewPort[2],lastViewPort[3]);
        if(isBlend){
            GLES20.glEnable(GLES20.GL_BLEND);
        }
    }

    public void getOutput(byte[] data,int offset,int length){
        if(mTempBuffer!=null){
            mTempBuffer.get(data,offset,length);
            mTempBuffer.clear();
        }
    }

    @Override
    public void destroy() {
        mExportFilter.destroy();
    }

    private static class ExportShader{
        private final String HEAD="precision highp float;\n" +
                "precision highp int;\n" +
                "\n" +
                "varying vec2 vTextureCo;\n" +
                "uniform sampler2D uTexture;\n" +
                "\n" +
                "uniform float uWidth;\n" +
                "uniform float uHeight;\n" +
                "\n" +
                "float cY(float x,float y){\n" +
                "    vec4 c=texture2D(uTexture,vec2(x,y));\n" +
                "    return c.r*0.257+c.g*0.504+c.b*0.098+0.0625;\n" +
                "}\n" +
                "\n" +
                "vec4 cC(float x,float y,float dx,float dy){\n" +
                "    vec4 c0=texture2D(uTexture,vec2(x,y));\n" +
                "    vec4 c1=texture2D(uTexture,vec2(x+dx,y));\n" +
                "    vec4 c2=texture2D(uTexture,vec2(x,y+dy));\n" +
                "    vec4 c3=texture2D(uTexture,vec2(x+dx,y+dy));\n" +
                "    return (c0+c1+c2+c3)/4.;\n" +
                "}\n" +
                "\n" +
                "float cU(float x,float y,float dx,float dy){\n" +
                "    vec4 c=cC(x,y,dx,dy);\n" +
                "    return -0.148*c.r - 0.291*c.g + 0.439*c.b+0.5000;\n" +
                "}\n" +
                "\n" +
                "float cV(float x,float y,float dx,float dy){\n" +
                "    vec4 c=cC(x,y,dx,dy);\n" +
                "    return 0.439*c.r - 0.368*c.g - 0.071*c.b+0.5000;\n" +
                "}\n" +
                "\n" +
                "vec2 cPos(float t,float shiftx,float gy){\n" +
                "    vec2 pos=vec2(floor(uWidth*vTextureCo.x),floor(uHeight*gy));\n" +
                "    return vec2(mod(pos.x*shiftx,uWidth),(pos.y*shiftx+floor(pos.x*shiftx/uWidth))*t);\n" +
                "}\n" +
                "\n" +
                "vec4 calculateY(){\n" +
                "    vec2 pos=cPos(1.,4.,vTextureCo.y);\n" +
                "    vec4 oColor=vec4(0);\n" +
                "    float textureYPos=pos.y/uHeight;\n" +
                "    oColor[0]=cY(pos.x/uWidth,textureYPos);\n" +
                "    oColor[1]=cY((pos.x+1.)/uWidth,textureYPos);\n" +
                "    oColor[2]=cY((pos.x+2.)/uWidth,textureYPos);\n" +
                "    oColor[3]=cY((pos.x+3.)/uWidth,textureYPos);\n" +
                "    return oColor;\n" +
                "}\n" +
                "vec4 calculateU(float gy,float dx,float dy){\n" +
                "    vec2 pos=cPos(2.,8.,vTextureCo.y-gy);\n" +
                "    vec4 oColor=vec4(0);\n" +
                "    float textureYPos=pos.y/uHeight;\n" +
                "    oColor[0]= cU(pos.x/uWidth,textureYPos,dx,dy);\n" +
                "    oColor[1]= cU((pos.x+2.)/uWidth,textureYPos,dx,dy);\n" +
                "    oColor[2]= cU((pos.x+4.)/uWidth,textureYPos,dx,dy);\n" +
                "    oColor[3]= cU((pos.x+6.)/uWidth,textureYPos,dx,dy);\n" +
                "    return oColor;\n" +
                "}\n" +
                "vec4 calculateV(float gy,float dx,float dy){\n" +
                "    vec2 pos=cPos(2.,8.,vTextureCo.y-gy);\n" +
                "    vec4 oColor=vec4(0);\n" +
                "    float textureYPos=pos.y/uHeight;\n" +
                "    oColor[0]=cV(pos.x/uWidth,textureYPos,dx,dy);\n" +
                "    oColor[1]=cV((pos.x+2.)/uWidth,textureYPos,dx,dy);\n" +
                "    oColor[2]=cV((pos.x+4.)/uWidth,textureYPos,dx,dy);\n" +
                "    oColor[3]=cV((pos.x+6.)/uWidth,textureYPos,dx,dy);\n" +
                "    return oColor;\n" +
                "}\n" +
                "vec4 calculateUV(float dx,float dy){\n" +
                "    vec2 pos=cPos(2.,4.,vTextureCo.y-0.2500);\n" +
                "    vec4 oColor=vec4(0);\n" +
                "    float textureYPos=pos.y/uHeight;\n" +
                "    oColor[0]= cU(pos.x/uWidth,textureYPos,dx,dy);\n" +
                "    oColor[1]= cV(pos.x/uWidth,textureYPos,dx,dy);\n" +
                "    oColor[2]= cU((pos.x+2.)/uWidth,textureYPos,dx,dy);\n" +
                "    oColor[3]= cV((pos.x+2.)/uWidth,textureYPos,dx,dy);\n" +
                "    return oColor;\n" +
                "}\n" +
                "vec4 calculateVU(float dx,float dy){\n" +
                "    vec2 pos=cPos(2.,4.,vTextureCo.y-0.2500);\n" +
                "    vec4 oColor=vec4(0);\n" +
                "    float textureYPos=pos.y/uHeight;\n" +
                "    oColor[0]= cV(pos.x/uWidth,textureYPos,dx,dy);\n" +
                "    oColor[1]= cU(pos.x/uWidth,textureYPos,dx,dy);\n" +
                "    oColor[2]= cV((pos.x+2.)/uWidth,textureYPos,dx,dy);\n" +
                "    oColor[3]= cU((pos.x+2.)/uWidth,textureYPos,dx,dy);\n" +
                "    return oColor;\n" +
                "}\n";

        public String getFrag(int type){
            StringBuilder sb=new StringBuilder();
            sb.append(HEAD);
            switch (type){
                case YuvOutputFilter.EXPORT_TYPE_I420:
                    sb.append("void main() {\n" +
                            "    if(vTextureCo.y<0.2500){\n" +
                            "        gl_FragColor=calculateY();\n" +
                            "    }else if(vTextureCo.y<0.3125){\n" +
                            "        gl_FragColor=calculateU(0.2500,1./uWidth,1./uHeight);\n" +
                            "    }else if(vTextureCo.y<0.3750){\n" +
                            "        gl_FragColor=calculateV(0.3125,1./uWidth,1./uHeight);\n" +
                            "    }else{\n" +
                            "        gl_FragColor=vec4(0,0,0,0);\n" +
                            "    }\n" +
                            "}");
                    break;
                case YuvOutputFilter.EXPORT_TYPE_YV12:
                    sb.append("void main() {\n" +
                            "    if(vTextureCo.y<0.2500){\n" +
                            "        gl_FragColor=calculateY();\n" +
                            "    }else if(vTextureCo.y<0.3125){\n" +
                            "        gl_FragColor=calculateV(0.2500,1./uWidth,1./uHeight);\n" +
                            "    }else if(vTextureCo.y<0.3750){\n" +
                            "        gl_FragColor=calculateU(0.3125,1./uWidth,1./uHeight);\n" +
                            "    }else{\n" +
                            "        gl_FragColor=vec4(0,0,0,0);\n" +
                            "    }\n" +
                            "}");
                    break;
                case YuvOutputFilter.EXPORT_TYPE_NV12:
                    sb.append("void main() {\n" +
                            "    if(vTextureCo.y<0.2500){\n" +
                            "        gl_FragColor=calculateY();\n" +
                            "    }else if(vTextureCo.y<0.3750){\n" +
                            "        gl_FragColor=calculateUV(1./uWidth,1./uHeight);\n" +
                            "    }else{\n" +
                            "        gl_FragColor=vec4(0,0,0,0);\n" +
                            "    }\n" +
                            "}");
                    break;
                case YuvOutputFilter.EXPORT_TYPE_NV21:
                default:
                    sb.append("void main() {\n" +
                            "    if(vTextureCo.y<0.2500){\n" +
                            "        gl_FragColor=calculateY();\n" +
                            "    }else if(vTextureCo.y<0.3750){\n" +
                            "        gl_FragColor=calculateVU(1./uWidth,1./uHeight);\n" +
                            "    }else{\n" +
                            "        gl_FragColor=vec4(0,0,0,0);\n" +
                            "    }\n" +
                            "}");
                    break;
            }
            return sb.toString();
        }
    }

    private static class ExportFilter extends BaseFilter {

        private int mGLWidth;
        private int mGLHeight;

        public ExportFilter(Resources resource, String frag) {
            super(resource, "shader/base.vert", "shader/convert/"+frag);
        }

        public ExportFilter(int type){
            super(null,BASE_VERT,new ExportShader().getFrag(type));
        }

        @Override
        protected void onCreate() {
            super.onCreate();
            mGLWidth=GLES20.glGetUniformLocation(mGLProgram,"uWidth");
            mGLHeight=GLES20.glGetUniformLocation(mGLProgram,"uHeight");
        }

        @Override
        protected void onSetExpandData() {
            super.onSetExpandData();
            GLES20.glUniform1f(mGLWidth,this.mWidth);
            GLES20.glUniform1f(mGLHeight,this.mHeight);
        }
    }


}
