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

/**
 * AiyaBeauty 美颜
 *
 * @author wuwang
 * @version v1.0 2017:11:14 15:18
 */
public class AiyaBeauty {

    public static final int TYPE1=0x1000;

    public static final int TYPE2=0x1002;

    public static final int TYPE3=0x1003;

    public static final int TYPE4=0x1004;

    public static final int TYPE5=0x1005;

    public static final int TYPE6=0x1006;

    public static final int BIG_EYE=0x2001;

    public static final int THIN_FACE=0x2002;

    /**
     * 创建native的美颜对象
     * @param type 美颜类型，{@link #TYPE1}、{@link #TYPE2}、{@link #TYPE3}
     * @return 对象ID
     */
    public native static long nCreateNativeObj(int type);

    /**
     * 销毁native的美颜对象
     * @param id 需要被销毁的对象的id
     */
    public native static void nDestroyNativeObj(long id);

    /**
     * 给native对象设置参数
     * @param id native对象ID
     * @param key 参数名
     * @param data 参数值
     * @return 设置结果，当前全部返回0
     */
    public native static int nSet(long id,String key,float data);

    public native static int nSet(long id,String key,long data);

    /**
     * 初始化美颜对象需要的gl环境
     * @param id native对象ID
     * @return 处理结果，当前全部返回0
     */
    public native static int nGlInit(long id);

    /**
     * 销毁GL美颜对象使用到的gl环境
     * @param id native对象ID
     * @return 处理结果，当前全部返回0
     */
    public native static int nGlDestroy(long id);

    /**
     * 重新启动对象
     * @param id native对象ID
     * @return 处理结果，当前全部返回0
     */
    public native static int nRestart(long id);

    /**
     * 调用美颜对象进行纹理的处理
     * @param id native对象ID
     * @param tex 输入纹理
     * @param x 渲染起始x坐标
     * @param y 渲染起始y坐标
     * @param w 渲染区域宽度
     * @param h 渲染区域高度
     * @return 处理结果，当前全部返回0
     */
    public native static int nDraw(long id,int tex,int x,int y,int w,int h);

    /**
     * 获取当前模块的版本代码
     * @return 版本代码
     */
    public int getVersionCode(){
        return nVersionCode();
    }

    /**
     * 获取当前模块的版本名
     * @return 版本名
     */
    public String getVersionName(){
        return nVersionName();
    }

    private native static int nVersionCode();

    private native static String nVersionName();

    static {
        System.loadLibrary("AyCoreSdk");
        System.loadLibrary("AyCoreJni");
        System.loadLibrary("BaseEffects");
        System.loadLibrary("Beauty");
        System.loadLibrary("Faceprocess");
        System.loadLibrary("AiyaEffectLib");
        System.loadLibrary("AyBeautyJni");
    }

}
