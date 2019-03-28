#include <jni.h>
#include <string.h>
#include "AiyaEffectInterface.h"

extern "C"
JNIEXPORT jlong JNICALL
Java_com_aiyaapp_aiya_AyShortVideoEffect_Create(JNIEnv *env, jobject instance, jint type) {
    return reinterpret_cast<jlong>(AYSDK::AiyaEffect::Create(type));
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyShortVideoEffect_Destroy(JNIEnv *env, jobject instance, jlong render_) {

    AYSDK::AiyaEffect *render = reinterpret_cast<AYSDK::AiyaEffect *>(render_);
    if (render != NULL) {
        AYSDK::AiyaEffect::Destroy(render);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyShortVideoEffect_InitGLResource(JNIEnv *env, jobject instance, jlong render_) {

    AYSDK::AiyaEffect *render = reinterpret_cast<AYSDK::AiyaEffect *>(render_);
    if (render != NULL) {
        render->initGLResource();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyShortVideoEffect_DeinitGLResource(JNIEnv *env, jobject instance, jlong render_) {

    AYSDK::AiyaEffect *render = reinterpret_cast<AYSDK::AiyaEffect *>(render_);
    if (render != NULL) {
        render->deinitGLResource();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyShortVideoEffect_Restart(JNIEnv *env, jobject instance, jlong render_) {

    AYSDK::AiyaEffect *render = reinterpret_cast<AYSDK::AiyaEffect *>(render_);
    if (render != NULL) {
        render->restart();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyShortVideoEffect_Draw(JNIEnv *env, jobject instance, jlong render_, jint texture, jint x, jint y, jint width, jint height) {

    AYSDK::AiyaEffect *render = reinterpret_cast<AYSDK::AiyaEffect *>(render_);
    if (render != NULL) {
        render->draw(static_cast<unsigned int>(texture), x, y, width, height);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyShortVideoEffect_Set(JNIEnv *env, jobject instance, jlong render_, jstring key_, jfloat value) {
    const char *key = env->GetStringUTFChars(key_, 0);

    AYSDK::AiyaEffect *render = reinterpret_cast<AYSDK::AiyaEffect *>(render_);
    if (render != NULL) {
        render->set(key, value);
    }

    env->ReleaseStringUTFChars(key_, key);
}
