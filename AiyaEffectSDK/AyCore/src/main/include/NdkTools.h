//
// Created by aiya on 2017/7/20.
//

#ifndef ANDROIDNATIVE_NDKTOOLS_H
#define ANDROIDNATIVE_NDKTOOLS_H

#include "jni.h"
#include <android/asset_manager_jni.h>

class NdkTools{
public:
    static const char * getAppId(JNIEnv * env,jobject obj);
    static const char * getDeviceId(JNIEnv * env,jobject obj);
    static const char * getAndroidId(JNIEnv * env,jobject obj);
    static const char * getFileDirPath(JNIEnv * env,jobject obj);

    static AAssetManager * getAssetsManager(JNIEnv * env,jobject obj);
};


#endif //ANDROIDNATIVE_NDKTOOLS_H
