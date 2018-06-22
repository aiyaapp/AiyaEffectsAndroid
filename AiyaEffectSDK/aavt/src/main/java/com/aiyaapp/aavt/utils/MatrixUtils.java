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
package com.aiyaapp.aavt.utils;

import android.opengl.Matrix;
import android.widget.ImageView;

/**
 * MatrixUtils
 *
 * @author wuwang
 * @version v1.0 2017:11:01 10:41
 */
public enum MatrixUtils {
    ;
    public static final int TYPE_FITXY=0;
    public static final int TYPE_CENTERCROP=1;
    public static final int TYPE_CENTERINSIDE=2;
    public static final int TYPE_FITSTART=3;
    public static final int TYPE_FITEND=4;

    /**
     * 获取一个新的原始纹理坐标，每次调用，都会重新创建
     * @return 坐标数组
     */
    public static float[] getOriginalTextureCo(){
        return new float[]{
                0.0f,0.0f,
                0.0f,1.0f,
                1.0f,0.0f,
                1.0f,1.0f
        };
    }

    /**
     * 获取一个新的原始顶点坐标，每次调用，都会重新创建
     * @return 坐标数组
     */
    public static float[] getOriginalVertexCo(){
        return new float[]{
                -1.0f,-1.0f,
                -1.0f,1.0f,
                1.0f,-1.0f,
                1.0f,1.0f
        };
    }

    /**
     * 获取一个新的4*4单位矩阵
     * @return 矩阵数组
     */
    public static float[] getOriginalMatrix(){
        return new float[]{
                1,0,0,0,
                0,1,0,0,
                0,0,1,0,
                0,0,0,1
        };
    }

    /**
     * 根据预览的大小和图像的大小，计算合适的变换矩阵
     * @param matrix  接收变换矩阵的数组
     * @param type 变换的类型，参考{@link #TYPE_CENTERCROP}、{@link #TYPE_FITEND}、{@link #TYPE_CENTERINSIDE}、{@link #TYPE_FITSTART}、{@link #TYPE_FITXY}，对应{@link android.widget.ImageView}的{@link android.widget.ImageView#setScaleType(ImageView.ScaleType)}
     * @param imgWidth 图像的宽度
     * @param imgHeight 图像的高度
     * @param viewWidth 视图的宽度
     * @param viewHeight 视图的高度
     */
    public static void getMatrix(float[] matrix,int type,int imgWidth,int imgHeight,int viewWidth,
                                 int viewHeight){
        if(imgHeight>0&&imgWidth>0&&viewWidth>0&&viewHeight>0){
            float[] projection=new float[16];
            float[] camera=new float[16];
            if(type==TYPE_FITXY){
                Matrix.orthoM(projection,0,-1,1,-1,1,1,3);
                Matrix.setLookAtM(camera,0,0,0,1,0,0,0,0,1,0);
                Matrix.multiplyMM(matrix,0,projection,0,camera,0);
                return;
            }
            float sWhView=(float)viewWidth/viewHeight;
            float sWhImg=(float)imgWidth/imgHeight;
            if(sWhImg>sWhView){
                switch (type){
                    case TYPE_CENTERCROP:
                        Matrix.orthoM(projection,0,-sWhView/sWhImg,sWhView/sWhImg,-1,1,1,3);
                        break;
                    case TYPE_CENTERINSIDE:
                        Matrix.orthoM(projection,0,-1,1,-sWhImg/sWhView,sWhImg/sWhView,1,3);
                        break;
                    case TYPE_FITSTART:
                        Matrix.orthoM(projection,0,-1,1,1-2*sWhImg/sWhView,1,1,3);
                        break;
                    case TYPE_FITEND:
                        Matrix.orthoM(projection,0,-1,1,-1,2*sWhImg/sWhView-1,1,3);
                        break;
                    default:
                        break;
                }
            }else{
                switch (type){
                    case TYPE_CENTERCROP:
                        Matrix.orthoM(projection,0,-1,1,-sWhImg/sWhView,sWhImg/sWhView,1,3);
                        break;
                    case TYPE_CENTERINSIDE:
                        Matrix.orthoM(projection,0,-sWhView/sWhImg,sWhView/sWhImg,-1,1,1,3);
                        break;
                    case TYPE_FITSTART:
                        Matrix.orthoM(projection,0,-1,2*sWhView/sWhImg-1,-1,1,1,3);
                        break;
                    case TYPE_FITEND:
                        Matrix.orthoM(projection,0,1-2*sWhView/sWhImg,1,-1,1,1,3);
                        break;
                    default:
                        break;
                }
            }
            Matrix.setLookAtM(camera,0,0,0,1,0,0,0,0,1,0);
            Matrix.multiplyMM(matrix,0,projection,0,camera,0);
        }
    }

    /**
     * 翻转矩阵
     * @param m 需要被翻转的矩阵
     * @param x 是否x轴左右翻转
     * @param y 是否y轴左右翻转
     * @return 传入的矩阵
     */
    public static float[] flip(float[] m,boolean x,boolean y){
        if(x||y){
            Matrix.scaleM(m,0,x?-1:1,y?-1:1,1);
        }
        return m;
    }

}

