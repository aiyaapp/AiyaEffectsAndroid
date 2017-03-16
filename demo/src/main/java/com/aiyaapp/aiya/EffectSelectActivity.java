/*
 *
 * EffectSelectActivity.java
 * 
 * Created by Wuwang on 2017/2/25
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.aiya;

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
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.aiyaapp.aiya.camera.LogUtils;
import com.aiyaapp.aiya.camera.MenuAdapter;
import com.aiyaapp.aiya.camera.MenuBean;
import com.aiyaapp.aiya.util.ClickUtils;
import com.aiyaapp.camera.sdk.AiyaEffects;
import com.aiyaapp.camera.sdk.base.ISdkManager;
import com.aiyaapp.camera.sdk.base.Log;

/**
 * Description:
 */
public class EffectSelectActivity extends AppCompatActivity {

    private ArrayList<MenuBean> mStickerData;
    private RecyclerView mMenuView;
    private MenuAdapter mStickerAdapter;
    private TextView mBtnStick,mBtnBeauty;
    private int mBeautyFlag=0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected void initData(){
        mMenuView= (RecyclerView)findViewById(R.id.mMenuView);
        mBtnStick= (TextView)findViewById(R.id.mLeft);
        mBtnBeauty= (TextView)findViewById(R.id.mRight);
        mBtnStick.setSelected(true);
        refreshRightBtn();

        mStickerData=new ArrayList<>();
        mMenuView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
        mStickerAdapter=new MenuAdapter(this,mStickerData);
        mStickerAdapter.setOnClickListener(new ClickUtils.OnClickListener() {
            @Override
            public void onClick(View v, int type, int pos, int child) {
                MenuBean m=mStickerData.get(pos);
                String name=m.name;
                mStickerAdapter.checkPos=pos;
                v.setSelected(true);
                mStickerAdapter.notifyDataSetChanged();
                if (name.equals("原始")) {
                    AiyaEffects.getInstance().setEffect(null);
                }else{
                    AiyaEffects.getInstance().setEffect("assets/modelsticker/"+m.path);
                }
            }
        });
        mMenuView.setAdapter(mStickerAdapter);
        initEffectMenu("modelsticker/stickers.json");
    }

    //刷新美颜按钮
    public void refreshRightBtn(){
        if(mBeautyFlag==0){
            mBtnBeauty.setText("美颜关");
            mBtnBeauty.setSelected(false);
        }else{
            mBtnBeauty.setText("美颜"+mBeautyFlag);
            mBtnBeauty.setSelected(true);
        }
    }

    //初始化特效按钮菜单
    protected void initEffectMenu(String menuPath) {
        try {
            Log.e( "解析菜单->" +menuPath);
            JsonReader r = new JsonReader(new InputStreamReader(getAssets().open(menuPath)));
            r.beginArray();
            while (r.hasNext()) {
                MenuBean menu = new MenuBean();
                r.beginObject();
                String name;
                while (r.hasNext()) {
                    name = r.nextName();
                    if (name.equals("name")) {
                        menu.name = r.nextString();
                    } else if (name.equals("path")) {
                        menu.path = r.nextString();
                    }
                }
                mStickerData.add(menu);
                Log.e( "增加菜单->" + menu.name);
                r.endObject();
            }
            r.endArray();
            mStickerAdapter.notifyDataSetChanged();
        } catch (IOException e) {
            e.printStackTrace();
            mStickerAdapter.notifyDataSetChanged();
        }
    }

    //View的点击事件处理
    public void onClick(View view){
        switch (view.getId()){
            case R.id.mLeft:
                mMenuView.setVisibility(mMenuView.getVisibility()==View.VISIBLE?
                    View.GONE:View.VISIBLE);
                view.setSelected(mMenuView.getVisibility()==View.VISIBLE);
                break;
            case R.id.mRight:
                mBeautyFlag=++mBeautyFlag>=7?0:mBeautyFlag;
                AiyaEffects.getInstance().set(ISdkManager.SET_BEAUTY_LEVEL,mBeautyFlag);
                refreshRightBtn();
                break;
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
