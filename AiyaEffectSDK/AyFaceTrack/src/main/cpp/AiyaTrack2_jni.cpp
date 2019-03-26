//
// Created by aiya on 2017/9/16.
//
#include "AiyaTrack.h"
#include <jni.h>
#include <assert.h>
#include "Log.h"
#include "Observer.h"
#include <string>
#include <string.h>


#define AYEFFECTS_JAVA "com/aiyaapp/aiya/AiyaTracker"
#define TRACK_VERSION_CODE 4002
#define TRACK_VERSION_NAME "v4.0.02"

#ifdef __cplusplus
extern "C" {
#endif

using namespace AiyaTrack;

static FaceData mFaceData;
static FaceData *pmFaceData;

jint ayTrackVersionCode(JNIEnv * env, jclass clazz){
    return TRACK_VERSION_CODE;
}

jstring ayTrackVersionName(JNIEnv * env, jclass clazz){
    return env->NewStringUTF(TRACK_VERSION_NAME);
}

jlong ayCreateTracker(JNIEnv * env, jclass clazz,jint type){
    Log::d("create effect : %d",type);
    FaceTrack * tracker=new FaceTrack();
    return (jlong)tracker;
}

jint ayInit(JNIEnv * env,jclass clazz,jlong id,jstring data){
    const char * d=env->GetStringUTFChars(data,JNI_FALSE);
    int ret = ((FaceTrack *)id)->loadModel(std::string(d));
    env->ReleaseStringUTFChars(data,d);
    return ret;
}

jint ayTrack(JNIEnv * env,jclass clazz,jlong id,jint type,jbyteArray input,jint width,jint height,
             jfloatArray output){
    jbyte * in=env->GetByteArrayElements(input,JNI_FALSE);
    jfloat * out=env->GetFloatArrayElements(output,JNI_FALSE);
    int size=env->GetArrayLength(output);
    Log::d("track start");
    int ret=((FaceTrack *)id)->track((uint8_t *) in, width, height, AiyaTrack::ImageType::tImageTypeRGBA,&mFaceData);
    Log::d("track end: ret=%d",ret);

    int len=std::min(size,mFaceData.numfeaturePoints2D*2)* sizeof(float);
    if(len>0){
        memcpy(out, mFaceData.featurePoints2D, (size_t) len);
    }
    env->ReleaseByteArrayElements(input,in,JNI_COMMIT);
    env->ReleaseFloatArrayElements(output,out,JNI_COMMIT);
    return ret;
}

jint ayTrackFaceData(JNIEnv * env,jclass clazz,jlong id,jint type,jbyteArray input,jint width,jint height){
    jbyte * in=env->GetByteArrayElements(input,JNI_FALSE);
    int ret=((FaceTrack *)id)->track((uint8_t *) in, width, height, AiyaTrack::ImageType::tImageTypeRGBA,&mFaceData);
    if(ret != 0)
        Log::d("ayTrackFaceData end: ret=%d",ret);
    pmFaceData = ret == 0 ? &mFaceData : NULL;
    env->ReleaseByteArrayElements(input,in,JNI_ABORT);
    return ret;
}

jlong ayGetFaceData(){
    return (jlong)pmFaceData;
}

jint ayRelease(JNIEnv * env, jclass clazz,jlong id){
    if(id!=0){
        delete (FaceTrack *)id;
    }
    return 0;
}

static JNINativeMethod g_methods[]={
        {"_createNativeObj",       "(I)J",                      (void *)ayCreateTracker},
        {"_init",                  "(JLjava/lang/String;)I",    (void *)ayInit},
        {"_track",                 "(JI[BII[F)I",               (void *)ayTrack},
        {"_track",                 "(JI[BII)I",                 (void *)ayTrackFaceData},
        {"_getFaceDataID",         "()J",                       (void *)ayGetFaceData},
        {"_release",               "(J)I",                      (void *)ayRelease},
        {"_getVersionCode",        "()I",                       (void *)ayTrackVersionCode},
        {"_getVersionName",        "()Ljava/lang/String;",      (void *)ayTrackVersionName},
};


JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv* env = nullptr;

    if (vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_ERR;
    }
    assert(env != nullptr);
    jclass clazz=env->FindClass(AYEFFECTS_JAVA);
    env->RegisterNatives(clazz, g_methods, (int) (sizeof(g_methods) / sizeof((g_methods)[0])));

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *jvm, void *reserved){
    //todo try ayDeInit here
}

#ifdef __cplusplus
}
#endif
