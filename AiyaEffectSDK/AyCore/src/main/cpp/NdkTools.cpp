//
// Created by aiya on 2017/7/20.
//

#include "NdkTools.h"

//C++ try catch can not catch the exception from java

const char * NdkTools::getAppId(JNIEnv *env, jobject obj) {

    // 调用Java函数 context.getPackageName()
    jmethodID  getPackageNameMethod = env->GetMethodID(env->GetObjectClass(obj),"getPackageName","()Ljava/lang/String;");
    jstring packageName = (jstring) env->CallObjectMethod(obj, getPackageNameMethod);

    jboolean isCopy;
    return env->GetStringUTFChars(packageName, &isCopy);
}

const char * NdkTools::getDeviceId(JNIEnv *env, jobject obj) {

    // 调用Java函数 android.os.Build.SERIAL
    jclass buildClazz = env->FindClass("android/os/Build");
    jfieldID mSerialField = env->GetStaticFieldID(buildClazz,"SERIAL","Ljava/lang/String;");
    jstring serial = (jstring)env->GetStaticObjectField(buildClazz, mSerialField);

    jboolean isCopy;
    return env->GetStringUTFChars(serial, &isCopy);
}

const char * NdkTools::getAndroidId(JNIEnv *env, jobject obj) {

    // 调用Java函数 android.content.Context.getContentResolver()
    jmethodID getContentResolverMethod = env->GetMethodID(env->FindClass("android/content/Context"), "getContentResolver", "()Landroid/content/ContentResolver;");
    jobject resolver = env->CallObjectMethod(obj, getContentResolverMethod);

    // 调用Java函数 Secure.ANDROID_ID
    jclass secureClazz = env->FindClass("android/provider/Settings$Secure");
    jfieldID ANDROID_ID_Field = env->GetStaticFieldID(secureClazz, "ANDROID_ID", "Ljava/lang/String;");
    jstring androidIDType = (jstring)(env->GetStaticObjectField(secureClazz, ANDROID_ID_Field));

    // 调用Java函数 Secure.getString(resolver, Secure.ANDROID_ID);
    jmethodID getStringMethod = env->GetStaticMethodID(secureClazz, "getString", "(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;");
    jstring androidId = (jstring)env->CallStaticObjectMethod(secureClazz, getStringMethod, resolver, androidIDType);

    jboolean isCopy;
    return env->GetStringUTFChars(androidId, &isCopy);
}

const char * NdkTools::getFileDirPath(JNIEnv *env, jobject obj) {

    // 调用Java函数 context.getFilesDir()
    jmethodID getFilesDir = env->GetMethodID(env->GetObjectClass(obj), "getFilesDir", "()Ljava/io/File;");
    jobject file = env->CallObjectMethod(obj, getFilesDir);

    // 调用Java函数 file.getAbsolutePath()
    jclass fileClazz = env->FindClass("java/io/File");
    jmethodID getAbsolutePathMethod = env->GetMethodID(fileClazz, "getAbsolutePath", "()Ljava/lang/String;");
    jstring path = (jstring)env->CallObjectMethod(file, getAbsolutePathMethod);

    jboolean isCopy;
    return env->GetStringUTFChars(path, &isCopy);
}

AAssetManager * NdkTools::getAssetsManager(JNIEnv *env, jobject obj) {

    //调用Java函数 context.getAssets()
    jmethodID getAssetsMethod = env->GetMethodID(env->GetObjectClass(obj),"getAssets",
                                                 "()Landroid/content/res/AssetManager;");
    jobject assets=env->CallObjectMethod(obj,getAssetsMethod);

    return AAssetManager_fromJava(env, assets);
}