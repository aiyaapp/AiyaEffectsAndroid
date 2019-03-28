#include <jni.h>
#include <string>
#include <android/log.h>
#include <sstream>
#include "RenderSticker.h"

static JavaVM *ay_effectJvm = NULL;
static jobject ay_effectCallback;

void funcAyEffectMessage(int type, int ret, const char *info) {
    JNIEnv *env;

    ay_effectJvm->AttachCurrentThread(&env, NULL);

    if (ay_effectCallback == NULL) {
        ay_effectJvm->DetachCurrentThread();
        return;
    }

    jclass clazz = env->GetObjectClass(ay_effectCallback);
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
    env->CallVoidMethod(ay_effectCallback, methodID, type, ret);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_aiyaapp_aiya_AyEffect_Create(JNIEnv *env, jobject instance, jint type) {

    AiyaRender::RenderSticker *render = new AiyaRender::RenderSticker();
    return reinterpret_cast<jlong>(render);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyEffect_Callback(JNIEnv *env, jobject instance, jlong render_, jobject callback_) {

    env->GetJavaVM(&ay_effectJvm);

    AiyaRender::RenderSticker *render = reinterpret_cast<AiyaRender::RenderSticker *>(render_);
    if (render) {
        render->message = funcAyEffectMessage;
        ay_effectCallback = env->NewGlobalRef(callback_);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyEffect_Destroy(JNIEnv *env, jobject instance, jlong render_) {
    AiyaRender::RenderSticker *render = reinterpret_cast<AiyaRender::RenderSticker *>(render_);
    if (render) {
        render->release();
        ay_effectCallback = NULL;
        delete(render);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyEffect_SetFaceData(JNIEnv *env, jobject instance, jlong render_, jlong value_) {
    AiyaRender::RenderSticker *render = reinterpret_cast<AiyaRender::RenderSticker *>(render_);
    if (render) {

        FaceData **faceData = reinterpret_cast<FaceData **>(value_);

        if (faceData && *faceData) {
            render->setParam("FaceData", *faceData);
        } else {
            render->setParam("FaceData", NULL);
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyEffect_SetStickerPath(JNIEnv *env, jobject instance, jlong render_, jstring path_) {
    const char * path = env->GetStringUTFChars(path_,JNI_FALSE);

    AiyaRender::RenderSticker *render = reinterpret_cast<AiyaRender::RenderSticker *>(render_);
    if (render) {
        render->setParam("StickerType", (void *) path);
    }
    env->ReleaseStringUTFChars(path_, path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyEffect_SetEnableVFlip(JNIEnv *env, jobject instance, jlong render_, jboolean enable) {
    AiyaRender::RenderSticker *render = reinterpret_cast<AiyaRender::RenderSticker *>(render_);
    if (render) {
        render->setParam("EnableVFlip",&enable);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyEffect_SetPause(JNIEnv *env, jobject instance, jlong render_) {
    AiyaRender::RenderSticker *render = reinterpret_cast<AiyaRender::RenderSticker *>(render_);
    if (render) {
        int defaultValue = 1;
        render->setParam("Pause",&defaultValue);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyEffect_SetResume(JNIEnv *env, jobject instance, jlong render_) {
    AiyaRender::RenderSticker *render = reinterpret_cast<AiyaRender::RenderSticker *>(render_);
    if (render) {
        int defaultValue = 1;
        render->setParam("Resume",&defaultValue);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyEffect_Draw(JNIEnv *env, jobject instance, jlong render_, jint texture, jint width, jint height) {

    AiyaRender::RenderSticker *render = reinterpret_cast<AiyaRender::RenderSticker *>(render_);
    if (render) {
        render->draw(texture, width, height, NULL);
    }
}