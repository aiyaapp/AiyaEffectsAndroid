/*
 *
 * AssetsManager.java
 * 
 * Created by Wuwang on 2016/11/8
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.camera.sdk.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Environment;

/**
 * Description:
 */
public class Assets {

    private AssetManager mManager;
    private String rootDir;
    private Context mContext;


    public Assets(Context context, String rootDir) {
        this.mContext=context;
        this.mManager = context.getAssets();
        this.rootDir = rootDir;
    }

    public Assets(Context context) {
        this(context,context.getFilesDir().getAbsolutePath());
    }

    //Todo 后面model_sticker的文件不应该放到assets文件夹下，利用外部加载的方式
    public boolean doCopy() {
        boolean a=copyFileFromAssets("config", rootDir);
        if(!a){
            Log.e("check config data error");
        }

//        if(!new File(getSD()+"/AiyaCamera/model_sticker").exists()||isCopyRes()){
//            deleteFile(getSD()+"/AiyaCamera/model_sticker");
//            boolean b=copyForSticker();
//            if(!b){
//                Log.e("copy sticker data error");
//            }else{
//                markCopyRes(true);
//            }
////            else{
////                File sdSourceFile=new File(getSD()+"/AiyaCamera/model_sticker");
////                if(!sdSourceFile.exists()){
////                    sdSourceFile.mkdirs();
////                    copyFolder(rootDir+"/model_sticker",sdSourceFile.getAbsolutePath());
////                }
////            }
//        }
        return a;
    }

    public void clearCache(){
        deleteFile(rootDir);
    }

    private boolean copyForSticker(){
        Log.d("check data for sticker");
        return copyFileFromAssets("modelsticker",getSD()+"/AiyaCamera/model_sticker");
    }

    private void deleteFile(String path){
        File file=new File(path);
        if(file.exists()){
            if(file.isFile()){
                boolean b=file.delete();
                if(!b){
                    Log.d("delete file error:"+path);
                }
            }else{
                String[] fileList=file.list();
                for (String fp:fileList){
                    deleteFile(path+File.separator+fp);
                }
                boolean b=file.delete();
                if(!b){
                    Log.d("delete file error:"+path);
                }
            }
        }
    }

    //递归复制assets文件到指定目录
    private boolean copyFileFromAssets(String src, String dst) {
        try {
            String[] files = mManager.list(src);
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
                    if (!copyFileFromAssets(src + File.separator + fileName, dst +
                        File.separator + fileName)) {
                        return false;
                    }
                }
            } else {  //如果是文件
                if(!copyAssetsFile(src, dst)){
                    return false;
                }
            }
        } catch (IOException e) {
            Log.e(e.getMessage());
            return false;
        }
        return true;
    }

    //文件不存在，则复制assets中文件
    private boolean copyAssetsFile(String src, String dst) {
        InputStream in;
        OutputStream out;
        try {
            File file = new File(dst);
            if (!file.exists()) {
                Log.d("copy File : "+dst);
                in = mManager.open(src);
                out = new FileOutputStream(dst);
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

    /**
     * 复制单个文件
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    private void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }

    }

    /**
     * 复制整个文件夹内容
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    private void copyFolder(String oldPath, String newPath) {

        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a=new File(oldPath);
            String[] file=a.list();
            File temp=null;
            for (int i = 0; i < file.length; i++) {
                if(oldPath.endsWith(File.separator)){
                    temp=new File(oldPath+file[i]);
                }
                else{
                    temp=new File(oldPath+File.separator+file[i]);
                }

                if(temp.isFile()){
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +temp.getName());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ( (len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    Log.d("copy to sd:"+newPath);
                    output.flush();
                    output.close();
                    input.close();
                }
                if(temp.isDirectory()){//如果是子文件夹
                    copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i]);
                }
            }
        }
        catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();
        }

    }

    private String getSD(){
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }
}
