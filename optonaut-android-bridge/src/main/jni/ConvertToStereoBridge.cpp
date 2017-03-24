
#include <jni.h>
#include "online-stitcher/src/stitcher/convertToStereo.hpp"

using namespace optonaut;

#define DEBUG_TAG "ConvertToStereo.cpp"

std::shared_ptr<ConvertToStereo> convertToStereo;

extern "C" {
    void Java_com_iam360_dscvr_record_ConvertToStereo_convert(JNIEnv *env, jobject thiz, jstring path, jstring sharedPath, jstring storagePath);

    void Java_com_iam360_dscvr_record_ConvertToStereo_clear(JNIEnv *env, jobject thiz, jstring path, jstring sharedPath);

};

void Java_com_iam360_dscvr_record_ConvertToStereo_convert(JNIEnv *env, jobject, jstring path, jstring sharedPath, jstring storagePath)
{

    const char *cPath = env->GetStringUTFChars(path, NULL);
    const char *cSharedPath = env->GetStringUTFChars(sharedPath, NULL);

    Log << "cPath " << cPath;
    Log << "cSharedPath " << cSharedPath;

    const char *cString = env->GetStringUTFChars(storagePath, NULL);
    std::string s_path(cString);

    CheckpointStore leftStore(s_path + "left/", s_path + "shared/");
    CheckpointStore rightStore(s_path + "right/", s_path + "shared/");
    CheckpointStore postDataStore(s_path + "post/", s_path + "shared/");

    leftStore.Clear();
    rightStore.Clear();

    convertToStereo = std::make_shared<ConvertToStereo>( postDataStore, leftStore, rightStore);
    convertToStereo->Finish();

}

void Java_com_iam360_dscvr_record_ConvertToStereo_clear(JNIEnv *env, jobject, jstring path, jstring sharedPath)
{
    const char *cPath = env->GetStringUTFChars(path, NULL);
    const char *cSharedPath = env->GetStringUTFChars(sharedPath, NULL);
    CheckpointStore store(cPath, cSharedPath);

    store.Clear();
}