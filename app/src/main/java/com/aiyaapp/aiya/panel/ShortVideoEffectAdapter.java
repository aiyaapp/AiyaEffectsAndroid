/*
 *
 * MenuAdapter.java
 * 
 * Created by Wuwang on 2016/11/14
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.aiyaapp.aiya.panel;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aiyaapp.aiya.R;
import com.aiyaapp.aiya.filter.SvBlackMagicFilter;
import com.aiyaapp.aiya.filter.SvBlackWhiteTwinkleFilter;
import com.aiyaapp.aiya.filter.SvCutSceneFilter;
import com.aiyaapp.aiya.filter.SvDysphoriaFilter;
import com.aiyaapp.aiya.filter.SvFinalZeligFilter;
import com.aiyaapp.aiya.filter.SvFluorescenceFilter;
import com.aiyaapp.aiya.filter.SvFourScreenFilter;
import com.aiyaapp.aiya.filter.SvHallucinationFilter;
import com.aiyaapp.aiya.filter.SvRollUpFilter;
import com.aiyaapp.aiya.filter.SvSeventysFilter;
import com.aiyaapp.aiya.filter.SvShakeFilter;
import com.aiyaapp.aiya.filter.SvSpiritFreedFilter;
import com.aiyaapp.aiya.filter.SvSplitScreenFilter;
import com.aiyaapp.aiya.filter.SvThreeScreenFilter;
import com.aiyaapp.aiya.filter.SvTimeTunnelFilter;
import com.aiyaapp.aiya.filter.SvVirtualMirrorFilter;
import com.wuwang.aavt.gl.LazyFilter;

/**
 * Description:
 */
public class ShortVideoEffectAdapter extends RecyclerView.Adapter<ShortVideoEffectAdapter.MenuHolder> implements View.OnClickListener {

    private Context mContext;
    private String[] filter_names=new String[]{
            "无特效","灵魂出窍","抖动","黑魔法","虚拟镜像","荧光","时光隧道","躁动",
            "终极变色","动感分屏","幻觉","70S","炫酷转动","四分屏","三分屏","黑白闪烁",
            "转场动画"
    };

    private Class[] filter_clazzs=new Class[]{
            LazyFilter.class, SvSpiritFreedFilter.class, SvShakeFilter.class,
            SvBlackMagicFilter.class, SvVirtualMirrorFilter.class, SvFluorescenceFilter.class,
            SvTimeTunnelFilter.class, SvDysphoriaFilter.class, SvFinalZeligFilter.class,
            SvSplitScreenFilter.class, SvHallucinationFilter.class, SvSeventysFilter.class,
            SvRollUpFilter.class, SvFourScreenFilter.class, SvThreeScreenFilter.class,
            SvBlackWhiteTwinkleFilter.class, SvCutSceneFilter.class
    };

    public int checkPos=0;

    private EffectListener.OnShortVideoEffectChangedListener mListener;

    public ShortVideoEffectAdapter(Context context){
        this.mContext=context;
    }

    public void setOnBeautyChangedListener(EffectListener.OnShortVideoEffectChangedListener listener){
        this.mListener=listener;
    }

    @Override
    public MenuHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MenuHolder(LayoutInflater.from(mContext).inflate(R.layout.item_menu,parent,false));
    }

    @Override
    public void onBindViewHolder(MenuHolder holder, int position) {
        holder.setData(filter_names[position],position);
    }

    @Override
    public int getItemCount() {
        return filter_names.length;
    }

    @Override
    public void onClick(View v) {
        checkPos=ClickUtils.getPos(v);
        notifyDataSetChanged();
        if(mListener!=null){
            mListener.onShortVideoEffectChanged(checkPos,filter_names[checkPos],filter_clazzs[checkPos]);
        }
    }

    public class MenuHolder extends RecyclerView.ViewHolder{

        private TextView tv;

        public MenuHolder(View itemView) {
            super(itemView);
            tv= (TextView)itemView.findViewById(R.id.mMenu);
            ClickUtils.addClickTo(tv, ShortVideoEffectAdapter.this, R.id.mMenu);
        }

        public void setData(String name,int pos){
            tv.setText(name);
            tv.setSelected(pos==checkPos);
            ClickUtils.setPos(tv,pos);
        }

        public void select(boolean isSelect){
            tv.setSelected(isSelect);
        }
    }

}
