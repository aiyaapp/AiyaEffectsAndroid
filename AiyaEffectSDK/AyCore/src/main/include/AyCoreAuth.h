////
//// Created by bobdr on 2017/11/10.
////
//
//#ifndef AIYASDK_AYCOREAUTH_H
//#define AIYASDK_AYCOREAUTH_H
//#include <string>
//#include "Observer.h"
//
//#ifdef __cplusplus
//extern "C"
//{
//#endif
//
//void AyCore_Release();
//
//void AyCore_Auth(std::string path, std::string appId, std::string appKey, std::string imei,Observer * observer);
//
//#ifdef __cplusplus
//};
//#endif
//#endif //AIYASDK_AYCOREAUTH_H

//
// Created by bobdr on 2017/11/10.
//

#ifndef AIYASDK_AYCOREAUTH_H
#define AIYASDK_AYCOREAUTH_H
#include <jni.h>
#include <string>
#include "Observer.h"

#ifdef __cplusplus
extern "C"
{
#endif

void AyCore_Release();

void AyCore_Auth2(JNIEnv *env, jobject obj, std::string appKey, Observer * observer);

#ifdef __cplusplus
};
#endif
#endif //AIYASDK_AYCOREAUTH_H

