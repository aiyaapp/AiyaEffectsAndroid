package com.aiyaapp.aavt.core;

import java.util.ArrayList;

/*
 * Created by Wuwang on 2017/10/23
 */
public class Observable<Type> implements IObservable<Type> {

    private ArrayList<IObserver<Type>> temp;

    @Override
    public void addObserver(IObserver<Type> observer) {
        if(temp==null){
            temp=new ArrayList<>();
        }
        temp.add(observer);
    }

    public void clear(){
        if(temp!=null){
            temp.clear();
            temp=null;
        }
    }

    @Override
    public void notify(Type type) {
        for (IObserver<Type> t:temp){
            t.onCall(type);
        }
    }

}
