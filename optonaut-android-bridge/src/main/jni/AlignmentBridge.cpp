#include <jni.h>
#include <android/log.h>
#include "online-stitcher/src/stitcher/stitcher.hpp"
//#include "online-stitcher/src/stitcher/globalAlignment.hpp"
#include "online-stitcher/src/io/checkpointStore.hpp"
#include "online-stitcher/src/math/projection.hpp"
#include "online-stitcher/src/imgproc/panoramaBlur.hpp"

using namespace optonaut;

#define DEBUG_TAG "Alignment.cpp"

//std::shared_ptr<CheckpointStore> leftStore;
//std::shared_ptr<CheckpointStore> rightStore;
//std::shared_ptr<CheckpointStore> postDataStore;


//std::shared_ptr<GlobalAlignment> globalAligner;

extern "C" {
void Java_com_iam360_iam360_record_Alignment_align(JNIEnv *env, jobject thiz, jstring path, jstring sharedPath, jstring storagePath);
void Java_com_iam360_iam360_record_Alignment_clear(JNIEnv *env, jobject thiz, jstring path, jstring sharedPath);
};



void Java_com_iam360_iam360_record_Alignment_align(JNIEnv *, jobject, jstring, jstring, jstring)
{

//    const char *cPath = env->GetStringUTFChars(path, NULL);
//    const char *cSharedPath = env->GetStringUTFChars(sharedPath, NULL);
//
//    const char *cString = env->GetStringUTFChars(storagePath, NULL);
//    std::string s_path(cString);
//
//    CheckpointStore leftStore(s_path + "left/", s_path + "shared/");
//    CheckpointStore rightStore(s_path + "right/", s_path + "shared/");
//    CheckpointStore postDataStore(s_path + "post/", s_path + "shared/");
//
//    leftStore.Clear();
//    rightStore.Clear();
//
//    globalAligner = std::make_shared<GlobalAlignment>( postDataStore, leftStore, rightStore);
//    globalAligner->Finish();

}

void Java_com_iam360_iam360_record_Alignment_clear(JNIEnv *, jobject, jstring, jstring)
{
//    const char *cPath = env->GetStringUTFChars(path, NULL);
//    const char *cSharedPath = env->GetStringUTFChars(sharedPath, NULL);
//    CheckpointStore store(cPath, cSharedPath);
//
//    store.Clear();
}