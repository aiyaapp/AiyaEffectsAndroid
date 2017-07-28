/*
 *
 * EffectSelectActivity.java
 * 
 * Created by Wuwang on 2017/2/25
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.aiya;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.aiyaapp.aiya.camera.LogUtils;
import com.aiyaapp.aiya.camera.MenuAdapter;
import com.aiyaapp.aiya.camera.MenuBean;
import com.aiyaapp.aiya.ui.BeautyPopupWindow;
import com.aiyaapp.aiya.ui.EffectController;
import com.aiyaapp.aiya.ui.EffectPopupWindow;
import com.aiyaapp.aiya.ui.SelectListener;
import com.aiyaapp.aiya.util.ClickUtils;
import com.aiyaapp.camera.sdk.AiyaEffects;
import com.aiyaapp.camera.sdk.base.ISdkManager;
import com.aiyaapp.camera.sdk.base.Log;

/**
 * Description:
 */
public class EffectSelectActivity extends AppCompatActivity {

    private ImageView mBtnRight;
    private int mBeautyFlag=0;

    protected EffectController mEffectPopup;
    private View mBtnContainer;
    private View mEffectContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected void initData(){
        mBtnRight= (ImageView) findViewById(R.id.mRight);
        mBtnContainer=findViewById(R.id.mOtherMenu);
        mEffectContainer=findViewById(R.id.mEffectContainer);
        mEffectContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEffectContainer.setVisibility(View.GONE);
                mBtnContainer.setVisibility(View.VISIBLE);
            }
        });

        mEffectPopup=new EffectController(this,mEffectContainer);
    }

    public View getContentView(){
        return findViewById(android.R.id.content);
    }

    //View的点击事件处理
    public void onClick(View view){
        switch (view.getId()){
            case R.id.mLeft:
//                mMenuView.setVisibility(mMenuView.getVisibility()==View.VISIBLE?
//                    View.GONE:View.VISIBLE);
//                view.setSelected(mMenuView.getVisibility()==View.VISIBLE);
                mEffectContainer.setVisibility(View.VISIBLE);
                mBtnContainer.setVisibility(View.GONE);
                break;
            case R.id.mRight:
                mEffectContainer.setVisibility(View.VISIBLE);
                mBtnContainer.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(mEffectContainer.getVisibility()==View.VISIBLE){
            mEffectContainer.setVisibility(View.GONE);
            mBtnContainer.setVisibility(View.VISIBLE);
        }else{
            super.onBackPressed();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mEffectPopup!=null){
            mEffectPopup.release();
        }
    }

    protected String getSD(){
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }


    public void saveBitmapAsync(final byte[] bytes,final int width,final int height){
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtils.e("has take pic");
                Bitmap bitmap=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
                ByteBuffer b=ByteBuffer.wrap(bytes);
                bitmap.copyPixelsFromBuffer(b);
                saveBitmap(bitmap);
                bitmap.recycle();
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("wuwang","onActivityResult:rq:"+requestCode+"/"+resultCode);
        if(requestCode==101){
            if(resultCode==RESULT_OK){
                android.util.Log.e("wuwang","data:"+getRealFilePath(data.getData()));
                String dataPath=getRealFilePath(data.getData());
                if(dataPath!=null&&dataPath.endsWith(".json")){
                    AiyaEffects.getInstance().setEffect(dataPath);
                }
            }
        }
    }

    public String getRealFilePath(final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    //图片保存
    public void saveBitmap(Bitmap b){
        String path =  getSD()+ "/AiyaCamera/photo/";
        File folder=new File(path);
        if(!folder.exists()&&!folder.mkdirs()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(EffectSelectActivity.this, "无法保存照片", Toast.LENGTH_SHORT).show();
                }
            });
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
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EffectSelectActivity.this, "保存成功->"+jpegName, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
