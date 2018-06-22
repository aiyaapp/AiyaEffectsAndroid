//
// Created by aiya on 2017/8/12.
//
#include <jni.h>
#include <assert.h>
#include "Log.h"
#include "NdkTools.h"
#include "AiyaEffectInterface.h"


#define SHORT_VIDEO_JAVA "com/aiyaapp/aiya/AiyaShaderEffect"
#define SHORT_VIDEO_VERSION_CODE 4002
#define SHORT_VIDEO_VERSION_NAME "v4.0.02"

#ifdef __cplusplus
extern "C" {
#endif

using namespace AYSDK;


jint aySvVersionCode(JNIEnv * env, jclass clazz){
    return SHORT_VIDEO_VERSION_CODE;
}

jstring aySvVersionName(JNIEnv * env, jclass clazz){
    return env->NewStringUTF(SHORT_VIDEO_VERSION_NAME);
}


static inline AiyaEffect * sp(jlong id){
    return (AiyaEffect *) id;
}

jlong aySvCreateObj(JNIEnv * env, jclass clazz,jint type){
    Log::d("create effect : %d",type);
    return (jlong)AiyaEffect::Create(type);
}

void aySvDestroyObj(JNIEnv * env,jclass clazz,jlong id){
    AiyaEffect* pointer = (AiyaEffect*)id;
    AiyaEffect::Destroy(pointer);
}

jint aySvSetFloat(JNIEnv * env,jclass clazz,jlong id,jstring key,jfloat value){
    const char * cKey=env->GetStringUTFChars(key,JNI_FALSE);
    int ret=sp(id)->set(cKey,value);
    env->ReleaseStringUTFChars(key,cKey);
    return ret;
}

jint aySvSetContext(JNIEnv * env,jclass clazz,jlong id,jobject ctx){
    return sp(id)->set("AssetManager",1,NdkTools::getAssetsManager(env,ctx));
}

jint aySvSetTexture(JNIEnv * env,jclass clazz,jlong id,jstring key,jint texture,jint w,jint h){
    const char * cKey=env->GetStringUTFChars(key,JNI_FALSE);
    int ret=sp(id)->set(cKey, (unsigned int) texture, w, h);
    env->ReleaseStringUTFChars(key,cKey);
    return ret;
}

jint aySvSetFile(JNIEnv * env,jclass clazz,jlong id,jstring key,jstring value){
    const char * cKey=env->GetStringUTFChars(key,JNI_FALSE);
    const char * cValue=env->GetStringUTFChars(value,JNI_FALSE);
    int ret=sp(id)->set(cKey,cValue);
    env->ReleaseStringUTFChars(key,cKey);
    env->ReleaseStringUTFChars(value,cValue);
    return ret;
}

jint aySvInitGL(JNIEnv * env,jclass clazz,jlong id){
    return sp(id)->initGLResource();
}

jint aySvDestroyGL(JNIEnv * env,jclass clazz,jlong id){
    return sp(id)->deinitGLResource();
}

jint aySvRestart(JNIEnv * env,jclass clazz,jlong id){
    return sp(id)->restart();
}

jint aySvDraw(JNIEnv * env,jclass clazz,jlong id,jint tex,jint x,jint y,jint w,jint h){
    return sp(id)->draw((unsigned int) tex, x, y, w, h);
}


static JNINativeMethod g_short_video_methods[]={
        {"nCreateNativeObj",      "(I)J", (void *)aySvCreateObj},
        {"nDestroyNativeObj",     "(J)V",                            (void *)aySvDestroyObj},
        {"nSet",                  "(JLjava/lang/String;F)I",  (void *)aySvSetFloat},
        {"nSet",                   "(JLandroid/content/Context;)I",  (void *)aySvSetContext},
        {"nSet",                   "(JLjava/lang/String;III)I",      (void *)aySvSetTexture},
        {"nSet",                   "(JLjava/lang/String;Ljava/lang/String;)I",   (void *)aySvSetFile},
        {"nGlInit",                "(J)I",                           (void *)aySvInitGL},
        {"nGlDestroy",              "(J)I",                          (void *)aySvDestroyGL},
        {"nRestart",                "(J)I",                          (void *)aySvRestart},
        {"nDraw",                   "(JIIIII)I",                     (void *)aySvDraw},
        {"nVersionCode",            "()I",                           (void *)aySvVersionCode},
        {"nVersionName",            "()Ljava/lang/String;",          (void *)aySvVersionName},
};


JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv* env = nullptr;

    if (vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_ERR;
    }
    assert(env != nullptr);
    jclass clazz=env->FindClass(SHORT_VIDEO_JAVA);
    env->RegisterNatives(clazz, g_short_video_methods, (int) (sizeof(g_short_video_methods) / sizeof((g_short_video_methods)[0])));

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *jvm, void *reserved){
    //todo try ayDeInit here
}

#ifdef __cplusplus
}
#endif
