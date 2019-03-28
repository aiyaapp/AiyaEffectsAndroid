#include <jni.h>
#include <string>
#include "AyCoreAuth.h"
#include "Observer.h"

static JavaVM *jvm = NULL;
static jobject ayCoreCallback;

void func_ay_auth_message(int type, int ret, const char *info) {
    if (type == ObserverMsg::MSG_TYPE_AUTH) {
        JNIEnv *env;

        jvm->AttachCurrentThread(&env, NULL);

        if (ayCoreCallback == NULL) {
            jvm->DetachCurrentThread();
            return;
        }

        jclass clazz = env->GetObjectClass(ayCoreCallback);
        if (clazz == NULL) {
            jvm->DetachCurrentThread();
            return;
        }

        jmethodID methodID = env->GetMethodID(clazz, "onResult", "(I)V");
        if (methodID == NULL) {
            jvm->DetachCurrentThread();
            return;
        }

        //调用该java方法
        env->CallVoidMethod(ayCoreCallback, methodID, ret);

        env->DeleteGlobalRef(ayCoreCallback);

        ayCoreCallback = NULL;

        jvm->DetachCurrentThread();
    }
}

Observer ay_auth_observer = {func_ay_auth_message};

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyCore_InitLicense(JNIEnv *env, jclass instance, jobject context,
                                         jstring appKey_, jobject callback) {

    const char *appKey = env->GetStringUTFChars(appKey_, 0);
    env->GetJavaVM(&jvm);

    ayCoreCallback = env->NewGlobalRef(callback);

    AyCore_Auth2(env, context, appKey, &ay_auth_observer);

    env->ReleaseStringUTFChars(appKey_, appKey);
}
