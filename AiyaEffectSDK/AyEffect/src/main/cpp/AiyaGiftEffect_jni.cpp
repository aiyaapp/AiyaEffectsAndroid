//
// Created by aiya on 2017/9/15.
//
#include <assert.h>
#include <algorithm>
#include <functional>
#include "jni.h"
#include "DrawEffects.h"
#include "Log.h"
#include "Observer.h"
#include "NdkTools.h"
#include "RenderSticker.h"

#define AYEFFECTS_GIFT_JAVA "com/aiyaapp/aiya/AiyaGiftEffect"
#define SDK_GIFT_VERSION_CODE 4007
#define SDK_GIFT_VERSION_NAME "v4.0.7"

#ifdef __cplusplus
extern "C" {
#endif

using namespace AiyaRender;

static JavaVM * jvm;

jlong ayCreate(JNIEnv * env, jclass clazz,jobject obj,jint type){
    Log::d("create effect : %d",type);
    //return (jlong)new AyGiftEffect(NdkTools::getAssetsManager(env,obj));
    RenderSticker *render=new RenderSticker();
    render->setParam("AssetManager",NdkTools::getAssetsManager(env,obj));
    return (jlong) render;
}

jint ayGiftVersionCode(JNIEnv * env,jclass clazz){
    return SDK_GIFT_VERSION_CODE;
}

jstring ayGiftVersionName(JNIEnv * env,jclass clazz){
    return env->NewStringUTF(SDK_GIFT_VERSION_NAME);
}

jint ayInit(JNIEnv * env,jclass clazz,jlong id,jstring data){

    return 0;
}

jint aySetEffect(JNIEnv * env,jclass clazz,jlong id,jstring effect){
    if(effect!=NULL){
        const char * e=env->GetStringUTFChars(effect,JNI_FALSE);
        int ret=((RenderSticker *)id)->setParam("StickerType",(void *)e);
        env->ReleaseStringUTFChars(effect,e);
        return  ret;
    }else{
        ((RenderSticker *)id)->setParam("StickerType", nullptr);
    }
    return 0;
}

jint aySetTracker(JNIEnv * env,jclass clazz,jlong id,jlong track,jint imageType){
    ((RenderSticker *)id)->setParam("Tracker",(AiyaTrack::BaseTrack *)track);
    ((RenderSticker *)id)->setParam("TrackImageType", (AiyaTrack::ImageType *)&imageType);
    return 0;
}

jint aySetPause(JNIEnv * env,jclass clazz,jlong id,jint type){
    return ((RenderSticker *)id)->setParam("Pause", &type);
}

void ayGiftSetEventListener(JNIEnv * env,jclass clazz,jlong id,jobject listener){
    jobject javaListener=env->NewGlobalRef(listener);
    void (*message)(int,int,const char *,jobject)=[](int type,int ret,const char * info,
                                                     jobject jal){
        static jmethodID eventMethodId;
        static bool isChanged= true;
        if(jal != nullptr&&ret!=ObserverMsg::MSG_STAT_EFFECTS_PLAY&&ret!=ObserverMsg::MSG_STAT_EFFECTS_PAUSE){
            JNIEnv * envTemp;
            jvm->AttachCurrentThread(&envTemp,NULL);
            if(isChanged&&envTemp){
                jclass clazzTemp=envTemp->GetObjectClass(jal);
                eventMethodId=envTemp->GetMethodID(clazzTemp,"onAnimEvent","(IILjava/lang/String;)V");
                isChanged= false;
            }
            if(eventMethodId != nullptr){
                envTemp->CallVoidMethod(jal, eventMethodId, type, ret, envTemp->NewStringUTF(info));
            }
        }
        if(type==ObserverMsg::MSG_STAT_LOOP_EXIT){
            if(jal!= nullptr){
                jvm->DetachCurrentThread();
            }
        }
    };
    auto callback=std::bind(message,std::placeholders::_1,std::placeholders::_2,
                            std::placeholders::_3,javaListener);
    ((RenderSticker *)id)->message=callback;

}

jint ayDraw(JNIEnv * env,jclass clazz,jlong id,jint textureId,jint width,jint height){
    return ((RenderSticker *)id)->draw(textureId,width,height, nullptr);
}

jint ayDrawWithTrackData(JNIEnv * env,jclass clazz,jlong id,jint textureId,jint width,jint height,jbyteArray data){
    jbyte * d=env->GetByteArrayElements(data,JNI_FALSE);
    int ret=((RenderSticker *)id)->draw(textureId, width, height, (uint8_t *) d);
    env->ReleaseByteArrayElements(data,d,JNI_ABORT);
    return ret;
}

jint ayEffectSetOptions(JNIEnv * env,jclass clazz,jlong id,jstring key,jlong face){
    const char * cKey=env->GetStringUTFChars(key,JNI_FALSE);
    int ret=((RenderSticker *)id)->setParam(cKey, (void *) face);
    env->ReleaseStringUTFChars(key,cKey);
    return ret;
}

jint aySetTrackSize(JNIEnv * env,jclass clazz,jlong id,jint width,jint height){
    ((RenderSticker *)id)->setParam("TrackImageWidth",&width);
    ((RenderSticker *)id)->setParam("TrackImageHeight",&height);
    return 0;
}

jint ayDestroyGL(JNIEnv * env, jclass clazz,jlong id){
    if(id>0){
        ((RenderSticker *)id)->release();
    }
    return 0;
}

jint ayRelease(JNIEnv * env, jclass clazz,jlong id){
    if(id>0){
        ((RenderSticker *)id)->release();
        delete (RenderSticker *)id;
    }
    return 0;
}

static JNINativeMethod g_methods[]={
        {"_createGiftObject",     "(Landroid/content/Context;I)J",              (void *)ayCreate},
        {"_init",                 "(JLjava/lang/String;)I",                     (void *)ayInit},
        {"_setEffect",            "(JLjava/lang/String;)I",                     (void *)aySetEffect},
        {"_setEventListener",     "(JLcom/aiyaapp/aiya/render/AnimListener;)V", (void *)ayGiftSetEventListener},
        {"_draw",                 "(JIII)I",                                    (void *)ayDraw},
        {"_draw",                 "(JIII[B)I",                                  (void *)ayDrawWithTrackData},
        {"_release",              "(J)I",                                       (void *)ayRelease},
        {"_destroyGL",            "(J)I",                                       (void *)ayDestroyGL},
        {"_pause",                "(JI)I",                                      (void *)aySetPause},
        {"_setOptions",           "(JLjava/lang/String;J)I",                    (void *)ayEffectSetOptions},
        {"_setTracker",           "(JJI)I",                                     (void *)aySetTracker},
        {"_setTrackSize",         "(JII)I",                                     (void *)aySetTrackSize},
        {"_getVersionCode",       "()I",                                        (void *)ayGiftVersionCode},
        {"_getVersionName",       "()Ljava/lang/String;",                       (void *)ayGiftVersionName},
};


JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    jvm=vm;
    JNIEnv* env = nullptr;

    if (vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
    return JNI_ERR;
    }
    assert(env != nullptr);
    jclass clazz=env->FindClass(AYEFFECTS_GIFT_JAVA);
    env->RegisterNatives(clazz, g_methods, (int) (sizeof(g_methods) / sizeof((g_methods)[0])));
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *jvm, void *reserved){
    //todo try ayDeInit here
}

#ifdef __cplusplus
}
#endif
