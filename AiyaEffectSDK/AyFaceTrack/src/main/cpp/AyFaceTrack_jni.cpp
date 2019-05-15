#include <jni.h>
#include <string>
#include <memory>
#include <android/log.h>
#include <sstream>
#include <cstdlib>
#include <cstring>
#include "AiyaTrack.h"

std::shared_ptr<AiyaTrack::FaceTrack> faceTrack;

FaceData AY_faceData;
FaceData *AY_faceData_p = &AY_faceData;

FaceData AY_cacheFaceData;
FaceData *AY_cacheFaceData_p = &AY_cacheFaceData;

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyFaceTrack_Init(JNIEnv *env, jclass type, jstring dstPath_) {
    const char * dstPath = env->GetStringUTFChars(dstPath_,JNI_FALSE);

    faceTrack = std::make_shared<AiyaTrack::FaceTrack>(dstPath);

    env->ReleaseStringUTFChars(dstPath_, dstPath);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyFaceTrack_Deinit(JNIEnv *env, jclass type) {
    faceTrack = nullptr;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_aiyaapp_aiya_AyFaceTrack_FaceData(JNIEnv *env, jclass type) {
    return reinterpret_cast<jlong>(&AY_faceData_p);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_aiyaapp_aiya_AyFaceTrack_CacheFaceData(JNIEnv *env, jclass type) {
    return reinterpret_cast<jlong>(&AY_cacheFaceData_p);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aiyaapp_aiya_AyFaceTrack_UpdateCacheFaceData(JNIEnv *env, jclass type) {
    if (AY_faceData_p != NULL) {
        AY_cacheFaceData = AY_faceData;
        AY_cacheFaceData_p = &AY_cacheFaceData;
    } else {
        AY_cacheFaceData_p = NULL;
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_aiyaapp_aiya_AyFaceTrack_TrackWithBGRABuffer(JNIEnv *env, jclass type, jobject pixelBuffer_, jint width, jint height) {

    uint8_t *pixelBuffer = static_cast<uint8_t *>(env->GetDirectBufferAddress(pixelBuffer_));

    uint8_t *clonePixelBuffer = (uint8_t *)(malloc(width * height * 4));
    memcpy(clonePixelBuffer, pixelBuffer, width * height * 4);

    int result = faceTrack->track(clonePixelBuffer, width, height, AiyaTrack::ImageType::tImageTypeRGBA, &AY_faceData);

    free(clonePixelBuffer);

    if (result == 0) {
        AY_faceData_p = &AY_faceData;

    } else {
        AY_faceData_p = NULL;
    }

    return result;
}