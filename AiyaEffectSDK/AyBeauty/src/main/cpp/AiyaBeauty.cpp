//
// Created by aiya on 2017/11/14.
//
#include <jni.h>
#include <assert.h>
#include "Log.h"
#include "NdkTools.h"
#include "AiyaEffectInterface.h"


#define BEAUTY_JAVA "com/aiyaapp/aiya/AiyaBeauty"
#define BEAUTY_VERSION_CODE 4002
#define BEAUTY_VERSION_NAME "v4.0.02"


#ifdef __cplusplus
extern "C"{
#endif

using namespace AYSDK;

jint ayBtVersionCode(JNIEnv * env,jclass clazz){
    return BEAUTY_VERSION_CODE;
}

jstring ayBtVersionName(JNIEnv * env,jclass clazz){
    return env->NewStringUTF(BEAUTY_VERSION_NAME);
}

static inline AiyaEffect * sp(jlong id){
    return (AiyaEffect *) id;
}

jlong ayBtCreateObj(JNIEnv * env, jclass clazz,jint type){
    Log::d("create effect : %d",type);
    return (jlong)AiyaEffect::Create(type);
}

void ayBtDestroyObj(JNIEnv * env,jclass clazz,jlong id){
    AiyaEffect* pointer = (AiyaEffect*)id;
    AiyaEffect::Destroy(pointer);
}

jint ayBtSetFloat(JNIEnv * env,jclass clazz,jlong id,jstring key,jfloat value){
    const char * cKey=env->GetStringUTFChars(key,JNI_FALSE);
    int ret=sp(id)->set(cKey,value);
    env->ReleaseStringUTFChars(key,cKey);
    return ret;
}

jint ayBtSetPointer(JNIEnv * env,jclass clazz,jlong id,jstring key,jlong value){
    const char * cKey=env->GetStringUTFChars(key,JNI_FALSE);
    int ret=sp(id)->set(cKey, 1, (void *) value);
    env->ReleaseStringUTFChars(key,cKey);
    return ret;
}

jint ayBtInitGL(JNIEnv * env,jclass clazz,jlong id){
    return sp(id)->initGLResource();
}

jint ayBtDestroyGL(JNIEnv * env,jclass clazz,jlong id){
    return sp(id)->deinitGLResource();
}

jint ayBtRestart(JNIEnv * env,jclass clazz,jlong id){
    return sp(id)->restart();
}

jint ayBtDraw(JNIEnv * env,jclass clazz,jlong id,jint tex,jint x,jint y,jint w,jint h){
    return sp(id)->draw((unsigned int) tex, x, y, w, h);
}


static JNINativeMethod g_methods[]={
        {"nCreateNativeObj",        "(I)J", (void *)ayBtCreateObj},
        {"nDestroyNativeObj",       "(J)V",                            (void *)ayBtDestroyObj},
        {"nSet",                    "(JLjava/lang/String;F)I",  (void *)ayBtSetFloat},
        {"nSet",                    "(JLjava/lang/String;J)I",  (void *)ayBtSetPointer},
        {"nGlInit",                 "(J)I",                           (void *)ayBtInitGL},
        {"nGlDestroy",              "(J)I",                          (void *)ayBtDestroyGL},
        {"nRestart",                "(J)I",                          (void *)ayBtRestart},
        {"nDraw",                   "(JIIIII)I",                     (void *)ayBtDraw},
        {"nVersionCode",            "()I",                           (void *)ayBtVersionCode},
        {"nVersionName",            "()Ljava/lang/String;",          (void *)ayBtVersionName},
};


JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv* env = nullptr;

    if (vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_ERR;
    }
    assert(env != nullptr);
    jclass clazz=env->FindClass(BEAUTY_JAVA);
    env->RegisterNatives(clazz, g_methods, (int) (sizeof(g_methods) / sizeof((g_methods)[0])));

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *jvm, void *reserved){
    //todo try ayDeInit here
}


#ifdef __cplusplus
};
#endif