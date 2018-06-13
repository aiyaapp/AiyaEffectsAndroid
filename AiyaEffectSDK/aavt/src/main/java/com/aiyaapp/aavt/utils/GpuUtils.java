package com.aiyaapp.aavt.utils;

import android.content.res.Resources;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import com.aiyaapp.aavt.log.AvLog;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

public enum GpuUtils {
    ;
    /**
     * 读取Assets中的文本文件
     * @param mRes res
     * @param path 文件路径
     * @return 文本内容
     */
    public static String readText(Resources mRes,String path){
        StringBuilder result=new StringBuilder();
        try{
            InputStream is=mRes.getAssets().open(path);
            int ch;
            byte[] buffer=new byte[1024];
            while (-1!=(ch=is.read(buffer))){
                result.append(new String(buffer,0,ch));
            }
        }catch (Exception e){
            return null;
        }
        return result.toString().replaceAll("\\r\\n","\n");
    }

    /**
     * 加载Shader
     * @param shaderType Shader类型
     * @param source Shader代码
     * @return shaderId
     */
    public static int loadShader(int shaderType,String source){
        if(source==null){
            glError(1,"Shader source ==null : shaderType ="+shaderType);
            return 0;
        }
        int shader= GLES20.glCreateShader(shaderType);
        if(0!=shader){
            GLES20.glShaderSource(shader,source);
            GLES20.glCompileShader(shader);
            int[] compiled=new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS,compiled,0);
            if(compiled[0]==0){
                glError(1,"Could not compile shader:"+shaderType);
                glError(1,"GLES20 Error:"+ GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader=0;
            }
        }
        return shader;
    }

    /**
     * 通过字符串创建GL程序
     * @param vertexSource 顶点着色器
     * @param fragmentSource 片元着色器
     * @return programId
     */
    public static int createGLProgram(String vertexSource, String fragmentSource){
        int vertex=loadShader(GLES20.GL_VERTEX_SHADER,vertexSource);
        if(vertex==0){
            return 0;
        }
        int fragment=loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentSource);
        if(fragment==0){
            return 0;
        }
        int program= GLES20.glCreateProgram();
        if(program!=0){
            GLES20.glAttachShader(program,vertex);
            GLES20.glAttachShader(program,fragment);
            GLES20.glLinkProgram(program);
            int[] linkStatus=new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS,linkStatus,0);
            if(linkStatus[0]!= GLES20.GL_TRUE){
                glError(1,"Could not link program:"+ GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program=0;
            }
        }
        return program;
    }

    /**
     * 通过assets中的文件创建GL程序
     * @param res res
     * @param vertex 顶点作色器路径
     * @param fragment 片元着色器路径
     * @return programId
     */
    public static int createGLProgramByAssetsFile(Resources res,String vertex,String fragment){
        return createGLProgram(readText(res,vertex),readText(res,fragment));
    }

    private static void glError(int code,Object index){
        AvLog.e("glError:"+code+"---"+index);
    }

    public static int createTextureID(boolean isOes) {
        int target= GLES20.GL_TEXTURE_2D;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            target = isOes? GLES11Ext.GL_TEXTURE_EXTERNAL_OES: GLES20.GL_TEXTURE_2D;
        }
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(target, texture[0]);
        GLES20.glTexParameterf(target,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(target,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(target,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(target,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

}
