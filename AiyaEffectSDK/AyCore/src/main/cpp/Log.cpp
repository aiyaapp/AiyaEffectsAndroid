//
// Created by aiya on 2017/7/15.
//

#include "Log.h"
#include "android/log.h"

#define log(a,b,c,d)                                        \
        if(a){                                              \
            va_list e;                                      \
            va_start(e ,d);                                 \
            __android_log_vprint(b,c,d,e);                  \
            va_end(e);                                      \
        }

static bool isDebug= true;
static char * mTag= (char *) "aiyaapp";

void Log::debug(bool debug) {
    isDebug=debug;
}

void Log::tag(char *tag) {
    mTag=tag;
}

void Log::e(const char *info, ...) {
    log(isDebug,ANDROID_LOG_ERROR,mTag,info);
}

void Log::i(const char *info, ...) {
    log(isDebug,ANDROID_LOG_INFO,mTag,info);
}

void Log::d(const char *info, ...) {
    log(isDebug,ANDROID_LOG_DEBUG,mTag,info);
}

void Log::te(char *tag, const char *info, ...) {
    log(isDebug,ANDROID_LOG_ERROR,tag,info);
}

void Log::ti(char *tag, const char *info, ...) {
    log(isDebug,ANDROID_LOG_INFO,tag,info);
}

void Log::td(char *tag, const char *info, ...) {
    log(isDebug,ANDROID_LOG_DEBUG,tag,info);
}