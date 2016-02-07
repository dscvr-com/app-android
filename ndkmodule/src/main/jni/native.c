#include <jni.h>
#include <android/log.h>

#define DEBUG_TAG "NDK_AndroidNDK1SampleActivity"

void Java_co_optonaut_optonaut_nativecode_TestUtil_test(JNIEnv * env, jobject this, jstring logThis)
{
    jboolean isCopy;
    const char * szLogThis = (*env)->GetStringUTFChars(env, logThis, &isCopy);

    __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", szLogThis);

    (*env)->ReleaseStringUTFChars(env, logThis, szLogThis);
}