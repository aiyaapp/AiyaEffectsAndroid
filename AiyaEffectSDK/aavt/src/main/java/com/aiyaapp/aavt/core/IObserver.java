package com.aiyaapp.aavt.core;

/**
 * Created by wuwang on 2017/10/20.
 */

public interface IObserver<Type> {

    void onCall(Type type);

}
