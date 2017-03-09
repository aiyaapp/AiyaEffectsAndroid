/*
 *
 * MenuAdapter.java
 * 
 * Created by Wuwang on 2016/11/14
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.aiya.camera;

import java.util.ArrayList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aiyaapp.aiya.R;
import com.aiyaapp.aiya.util.ClickUtils;

/**
 * Description:
 */
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuHolder> {

    private Context mContext;
    public ArrayList<MenuBean> data;
    public int checkPos=0;

    public MenuAdapter(Context context, ArrayList<MenuBean> data){
        this.mContext=context;
        this.data=data;
    }

    @Override
    public MenuHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MenuHolder(LayoutInflater.from(mContext).inflate(R.layout.item_small_menu,parent,false));
    }

    @Override
    public void onBindViewHolder(MenuHolder holder, int position) {
        holder.setData(data.get(position),position);

    }

    @Override
    public int getItemCount() {
        return data!=null?data.size():0;
    }

    private View.OnClickListener mListener;
    public void setOnClickListener(View.OnClickListener listener){
        this.mListener=listener;
    }

    public class MenuHolder extends RecyclerView.ViewHolder{

        private TextView tv;

        public MenuHolder(View itemView) {
            super(itemView);
            tv= (TextView)itemView.findViewById(R.id.mMenu);
            ClickUtils.addClickTo(tv,mListener,R.id.mMenu);
        }

        public void setData(MenuBean bean,int pos){
            tv.setText(bean.name);
            tv.setSelected(pos==checkPos);
            ClickUtils.setPos(tv,pos);
        }

        public void select(boolean isSelect){
            tv.setSelected(isSelect);
        }
    }

}
