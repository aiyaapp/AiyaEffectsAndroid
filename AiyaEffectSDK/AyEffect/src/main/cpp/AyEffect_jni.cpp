#include <jni.h>
#include <string>
#include <android/log.h>
#include <sstream>
#include "RenderSticker.h"

static JavaVM *ay_effectJvm = NULL;

class AyEffect
{
public:
    AiyaRender::RenderSticker *render;
    jobject callback;

    void effectMessage(int type, int ret, const char *info){
        JNIEnv *env;

        ay_effectJvm->AttachCurrentThread(&env, NULL);

        if (callback == NULL) {
            ay_effectJvm->DetachCurrentThread();
            return;
        }

        jclass clazz = env->GetObjectClass(callback);
        if (clazz == NULL) {
            ay_effectJvm->DetachCurrentThread();
            return;
        }

        jmethodID methodID = env->GetMethodID(clazz, "aiyaEffectMessage", "(II)V");
        if (methodID == NULL) {
            ay_effectJvm->DetachCurrentThread();
            return;
        }

        //调用该java方法
        env->CallVoidMethod(callback, methodID, type, ret);
    }
};

extern "C"
JNIEXPORT jlong JNICALL
Java_com_aiyaapp_aiya_AyEffect_Create(JNIEnv *env, jobject instance) {

    AiyaRender::RenderSticker *render = new AiyaRender::RenderSticker();
    AyEffect *ayEffect = new AyEffect();
    ayEffect->render = render;

    return reinterpret_cast<jlong>(ayEffect);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyEffect_Callback(JNIEnv *env, jobject instance, jlong render_, jobject callback_) {

    env->GetJavaVM(&ay_effectJvm);

    AyEffect *ayEffect = reinterpret_cast<AyEffect *>(render_);
    if (ayEffect) {
        ayEffect->render->message = std::bind(&AyEffect::effectMessage, ayEffect, std::placeholders::_1, std::placeholders::_2, std::placeholders::_3);
        ayEffect->callback = env->NewGlobalRef(callback_);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyEffect_Destroy(JNIEnv *env, jobject instance, jlong render_) {
    AyEffect *ayEffect = reinterpret_cast<AyEffect *>(render_);
    if (ayEffect) {
        ayEffect->render->release();
        delete(ayEffect->render);
        env->DeleteGlobalRef(ayEffect->callback);
        delete(ayEffect);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyEffect_SetFaceData(JNIEnv *env, jobject instance, jlong render_, jlong value_) {
    AyEffect *ayEffect = reinterpret_cast<AyEffect *>(render_);
    if (ayEffect) {

        FaceData **faceData = reinterpret_cast<FaceData **>(value_);

        if (faceData && *faceData) {
            ayEffect->render->setParam("FaceData", *faceData);
        } else {
            ayEffect->render->setParam("FaceData", NULL);
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyEffect_SetStickerPath(JNIEnv *env, jobject instance, jlong render_, jstring path_) {
    const char * path = env->GetStringUTFChars(path_,JNI_FALSE);

    AyEffect *ayEffect = reinterpret_cast<AyEffect *>(render_);
    if (ayEffect) {
        ayEffect->render->setParam("StickerType", (void *) path);
    }
    env->ReleaseStringUTFChars(path_, path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyEffect_SetEnableVFlip(JNIEnv *env, jobject instance, jlong render_, jboolean enable) {
    AyEffect *ayEffect = reinterpret_cast<AyEffect *>(render_);
    if (ayEffect) {
        ayEffect->render->setParam("EnableVFlip",&enable);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyEffect_SetPause(JNIEnv *env, jobject instance, jlong render_) {
    AyEffect *ayEffect = reinterpret_cast<AyEffect *>(render_);
    if (ayEffect) {
        int defaultValue = 1;
        ayEffect->render->setParam("Pause",&defaultValue);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyEffect_SetResume(JNIEnv *env, jobject instance, jlong render_) {
    AyEffect *ayEffect = reinterpret_cast<AyEffect *>(render_);
    if (ayEffect) {
        int defaultValue = 1;
        ayEffect->render->setParam("Resume",&defaultValue);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyEffect_Draw(JNIEnv *env, jobject instance, jlong render_, jint texture, jint width, jint height) {

    AyEffect *ayEffect = reinterpret_cast<AyEffect *>(render_);
    if (ayEffect) {
        ayEffect->render->draw(texture, width, height, NULL);
    }
}