#include "main.h"

#include <errno.h>
#include <string.h>
#include <fcntl.h>
#include <malloc.h>
#include <unistd.h>
#include <android/log.h>
#include <jni.h>

#define LOG_TAG "Optonaut"
#define LOG_D(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

jint Java_co_optonaut_ndkmodule_MainNative_callWithArguments(JNIEnv* env, jobject thiz, jstring deviceName, jint width, jint height) {
    const char* dev_name = (*env)->GetStringUTFChars(env, deviceName, 0);
    (*env)->ReleaseStringUTFChars(env, deviceName, dev_name);

    //TODO something awesome

    return 0;
}

// Required for the default JNI implementation
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOG_D ("JNI_OnLoad");
    return JNI_VERSION_1_6;
}
