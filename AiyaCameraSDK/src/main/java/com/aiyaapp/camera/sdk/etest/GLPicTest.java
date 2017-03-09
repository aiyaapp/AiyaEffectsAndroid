/*
 *
 * GLPicTest.java
 * 
 * Created by Wuwang on 2017/2/27
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.etest;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.opengl.GLES20;
import android.widget.Toast;

/**
 * Description:
 */
public class GLPicTest {

    private static GLPicTest instance;
    private String path;
    private boolean flag=false;
    private boolean take=false;

    private GLPicTest(){

    }

    public static GLPicTest getInstance(){
        if(instance==null){
            synchronized (GLPicTest.class){
                if(instance==null){
                    instance=new GLPicTest();
                }
            }
        }
        return instance;
    }

    public void setPath(String path){
        this.path=path;
    }

    public void take(){
        take=true;
    }

    public void start(){
        flag=true;
    }

    public void end(){
        if(flag&&take){
            flag=false;
            take=false;
        }
    }

    public void takePicIfNeeded(int width,int height){
        if(flag&&path!=null&&take){
            ByteBuffer buffer=ByteBuffer.allocate(width*height*4);
            GLES20.glReadPixels(0,0,width,height,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,buffer);
            Bitmap bitmap=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            saveBitmap(bitmap,path);
        }
    }

    public void takePicIfNeeded(int width,int height,byte[] data){
        if(flag&&path!=null&&take){
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            newOpts.inJustDecodeBounds = true;
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            YuvImage yuvImage=new YuvImage(data, ImageFormat.NV21,width,height,null);
            yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, baos);// 80--JPG图片的质量[0-100],100最高
            //将rawImage转换成bitmap
            byte[] bs=baos.toByteArray();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeByteArray(bs, 0, bs.length, options);
            saveBitmap(bitmap,path);
        }
    }

    //图片保存
    public void saveBitmap(Bitmap b,String path){
        File folder=new File(path);
        if(!folder.exists()&&!folder.mkdirs()){
            return;
        }
        long dataTake = System.currentTimeMillis();
        final String jpegName=path+ dataTake +".jpg";
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            if(!b.isRecycled()){
                b.recycle();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
