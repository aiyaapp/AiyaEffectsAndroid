#include <jni.h>
#include <string>
#include "AiyaEffectInterface.h"

extern "C"
JNIEXPORT jlong JNICALL
Java_com_aiyaapp_aiya_AyBeauty_Create(JNIEnv *env, jobject instance, jint type) {

    AYSDK::AiyaEffect *render = AYSDK::AiyaEffect::Create(type);
    return reinterpret_cast<jlong>(render);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyBeauty_Destroy(JNIEnv *env, jobject instance, jlong render) {

    AYSDK::AiyaEffect *AyBeauty_render = reinterpret_cast<AYSDK::AiyaEffect *>(render);
    if (AyBeauty_render) {
        AYSDK::AiyaEffect::Destroy(AyBeauty_render);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyBeauty_InitGLResource(JNIEnv *env, jobject instance, jlong render) {

    AYSDK::AiyaEffect *AyBeauty_render = reinterpret_cast<AYSDK::AiyaEffect *>(render);
    if (AyBeauty_render) {
        AyBeauty_render->initGLResource();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyBeauty_DeinitGLResource(JNIEnv *env, jobject instance, jlong render) {

    AYSDK::AiyaEffect *AyBeauty_render = reinterpret_cast<AYSDK::AiyaEffect *>(render);
    if (AyBeauty_render) {
        AyBeauty_render->deinitGLResource();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyBeauty_Set(JNIEnv *env, jobject instance, jlong render, jstring name_, jfloat value) {
    const char *name = env->GetStringUTFChars(name_, 0);

    AYSDK::AiyaEffect *AyBeauty_render = reinterpret_cast<AYSDK::AiyaEffect *>(render);
    if (AyBeauty_render) {
        AyBeauty_render->set(name, value);
    }

    env->ReleaseStringUTFChars(name_, name);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyBeauty_Draw(JNIEnv *env, jobject instance, jlong render, jint texture, jint x, jint y, jint width, jint height) {

    AYSDK::AiyaEffect *AyBeauty_render = reinterpret_cast<AYSDK::AiyaEffect *>(render);
    if (AyBeauty_render) {
        AyBeauty_render->draw(static_cast<unsigned int>(texture), x, y, width, height);
    }
}

