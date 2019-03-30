#include <jni.h>
#include <string>
#include "libyuv.h"

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AYYuvUtil_RGBA_1To_1I420(JNIEnv *env, jclass type, jobject bgra_,
                                               jobject yuv_, jint width, jint height) {

    const uint8 *bgra = reinterpret_cast<const uint8 *>(env->GetDirectBufferAddress(bgra_));
    int bgraStride = width * 4;

    void *yuv = env->GetDirectBufferAddress(yuv_);

    uint8 *y = reinterpret_cast<uint8 *>(yuv);
    int yStride = width;

    uint8 *u = y + (width * height);
    int uStride = width / 2;

    uint8 *v = u + (width * height) / 4;
    int vStride = width / 2;

    libyuv::ABGRToI420(bgra, bgraStride, y, yStride, u, uStride, v, vStride, width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AYYuvUtil_I420_1To_1RGBA(JNIEnv *env, jclass type, jobject yuv_,
                                               jobject bgra_, jint width, jint height) {

    uint8 *bgra = reinterpret_cast<uint8 *>(env->GetDirectBufferAddress(bgra_));
    int bgraStride = width * 4;

    void *yuv = env->GetDirectBufferAddress(yuv_);

    uint8 *y = reinterpret_cast<uint8 *>(yuv);
    int yStride = width;

    uint8 *u = y + (width * height);
    int uStride = width / 2;

    uint8 *v = u + (width * height) / 4;
    int vStride = width / 2;

    libyuv::I420ToARGB(y, yStride, v, vStride, u, uStride, bgra, bgraStride, width, height);
}
