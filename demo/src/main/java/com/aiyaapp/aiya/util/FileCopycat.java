/*
 *
 * StickerCopycat.java
 * 
 * Created by Wuwang on 2016/12/6
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.aiya.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.res.AssetManager;

import com.aiyaapp.camera.sdk.base.Log;

/**
 * Description:
 */
public class FileCopycat {

    private String parentPath;
    private ExecutorService mExecutor;

    private static FileCopycat instance;

    public static FileCopycat getInstance(){
        if(instance==null){
            synchronized (FileCopycat.class){
                if(instance==null){
                    instance=new FileCopycat();
                }
            }
        }
        return instance;
    }

    private FileCopycat(){
        mExecutor=Executors.newFixedThreadPool(5);
    }

    public void setParent(String path){
        if(!path.endsWith(File.separator)){
            this.parentPath=path+File.separator;
        }else{
            this.parentPath=path;
        }
        File file=new File(parentPath);
        if(!file.exists()){
            boolean b=file.mkdirs();
            //TODO something
        }
    }

    public void copyAssets(final AssetManager manager, final String src, String dst,
                               final int code, final Call<String> call){
        final File file=new File(parentPath+dst);
        if(file.exists()){
            call.onCall(code,file.getAbsolutePath());
        }else{
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    copyAssetsFolder(manager,src,file.getAbsolutePath());
                    call.onCall(code,file.getAbsolutePath());
                }
            });
        }
    }

    public void copySd(String src,String dst,int code,Call<String> call){

    }

    //文件不存在，则复制assets中文件
    private boolean copyAssetsFile(AssetManager manager,String src, String dst) {
        InputStream in;
        OutputStream out;
        try {
            File dstFile=new File(dst);
            if (!dstFile.exists()) {
                in = manager.open(src);
                out = new FileOutputStream(dstFile);
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
                out.close();
                in.close();
            }else{
                Log.d("file exits : " +dst);
            }
        } catch (Exception e) {
            Log.e(e.getMessage());
            return false;
        }
        return true;
    }

    //递归复制assets文件到指定目录
    private boolean copyAssetsFolder(AssetManager manager,String src, String dst) {
        try {
            String[] files = manager.list(src);
            if (files.length > 0) {     //如果是文件夹
                File folder = new File(dst);
                if(!folder.exists()){
                    boolean b = folder.mkdirs();
                    Log.d("create folder : "+dst);
                    if (!b) {
                        Log.e("create folder failed:" + dst);
                        return false;
                    }
                }
                for (String fileName : files) {
                    if (!copyAssetsFolder(manager,src + File.separator + fileName, dst +
                        File.separator + fileName)) {
                        return false;
                    }
                }
            } else {  //如果是文件
                if(!copyAssetsFile(manager,src, dst)){
                    return false;
                }
            }
        } catch (IOException e) {
            Log.e(e.getMessage());
            return false;
        }
        return true;
    }

}
