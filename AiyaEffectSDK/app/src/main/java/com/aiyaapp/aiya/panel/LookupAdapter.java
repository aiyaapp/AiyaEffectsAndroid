package com.aiyaapp.aiya.panel;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aiyaapp.aiya.R;

/**
 * Created by aiya on 2017/7/27.
 */

public class LookupAdapter extends RecyclerView.Adapter<LookupAdapter.MenuHolder> implements View.OnClickListener {

    private Context context;

    private int checkPos = 0;
    private EffectListener.OnLookupFilterChangeListener mListener;

    private String[] filterName=new String[]{
         "无滤镜","滤镜一","滤镜二","滤镜三","滤镜四","滤镜五","滤镜六"
    };

    private String[] filters=new String[]{
        "","amatorka.png","clearLookup.jpg","highkey.png","peachLookup.jpg","purityLopokup.png","ruddyLookup.jpg"
    };

    public LookupAdapter(Context context) {
        this.context = context;
    }

    @Override
    public MenuHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new MenuHolder((LayoutInflater.from(context).inflate(R.layout.item_menu, viewGroup, false)));
    }

    @Override
    public void onBindViewHolder(MenuHolder imageHolder, int i) {
        imageHolder.setData(filterName[i],i);
    }

    @Override
    public int getItemCount() {
        return filters.length;
    }

    @Override
    public void onClick(View v) {
        checkPos = ClickUtils.getPos(v);
        if(mListener!=null){
            mListener.onLookUpFilterChanged(checkPos,filters[checkPos]);
        }
        notifyDataSetChanged();
    }

    public void setSelectListener(EffectListener.OnLookupFilterChangeListener listener){
        mListener=listener;
    }

    public class MenuHolder extends RecyclerView.ViewHolder{

        TextView tv;

        public MenuHolder(View itemView) {
            super(itemView);
            tv= (TextView)itemView.findViewById(R.id.mMenu);
            ClickUtils.addClickTo(tv, LookupAdapter.this, R.id.mMenu);
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
