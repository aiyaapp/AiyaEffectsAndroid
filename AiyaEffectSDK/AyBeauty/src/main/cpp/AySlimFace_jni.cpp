#include <jni.h>
#include <string>
#include <FaceData.h>
#include <android/log.h>
#include <sstream>
#include "AiyaEffectInterface.h"

extern "C"
JNIEXPORT jlong JNICALL
Java_com_aiyaapp_aiya_AySlimFace_Create(JNIEnv *env, jobject instance) {

    AYSDK::AiyaEffect *render = AYSDK::AiyaEffect::Create(0x2002);
    return reinterpret_cast<jlong>(render);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AySlimFace_Destroy(JNIEnv *env, jobject instance, jlong render) {

    AYSDK::AiyaEffect *AyBeauty_render = reinterpret_cast<AYSDK::AiyaEffect *>(render);
    if (AyBeauty_render) {
        AYSDK::AiyaEffect::Destroy(AyBeauty_render);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AySlimFace_InitGLResource(JNIEnv *env, jobject instance, jlong render) {

    AYSDK::AiyaEffect *AyBeauty_render = reinterpret_cast<AYSDK::AiyaEffect *>(render);
    if (AyBeauty_render) {
        AyBeauty_render->initGLResource();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AySlimFace_DeinitGLResource(JNIEnv *env, jobject instance, jlong render) {

    AYSDK::AiyaEffect *AyBeauty_render = reinterpret_cast<AYSDK::AiyaEffect *>(render);
    if (AyBeauty_render) {
        AyBeauty_render->deinitGLResource();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AySlimFace_Set(JNIEnv *env, jobject instance, jlong render, jstring name_, jfloat value) {
    const char *name = env->GetStringUTFChars(name_, 0);

    AYSDK::AiyaEffect *AyBeauty_render = reinterpret_cast<AYSDK::AiyaEffect *>(render);
    if (AyBeauty_render) {
        AyBeauty_render->set(name, value);
    }

    env->ReleaseStringUTFChars(name_, name);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AySlimFace_SetFaceData(JNIEnv *env, jobject instance, jlong render, jlong value) {

    AYSDK::AiyaEffect *AyBeauty_render = reinterpret_cast<AYSDK::AiyaEffect *>(render);

    if (AyBeauty_render ) {

        FaceData **faceData = reinterpret_cast<FaceData **>(value);

        if (faceData && *faceData) {
            AyBeauty_render->set("FaceData", 1, *faceData);
        } else {
            AyBeauty_render->set("FaceData", 0, NULL);
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AySlimFace_Draw(JNIEnv *env, jobject instance, jlong render, jint texture, jint x, jint y, jint width, jint height) {

    AYSDK::AiyaEffect *AyBeauty_render = reinterpret_cast<AYSDK::AiyaEffect *>(render);
    if (AyBeauty_render) {
        AyBeauty_render->draw(static_cast<unsigned int>(texture), x, y, width, height);
    }
}