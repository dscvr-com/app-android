#include <jni.h>
#include <android/log.h>
#include "online-stitcher/src/stitcher/stitcher.hpp"
#include "online-stitcher/src/stitcher/globalAlignment.hpp"
#include "online-stitcher/src/io/checkpointStore.hpp"
#include "online-stitcher/src/math/projection.hpp"
#include "online-stitcher/src/imgproc/panoramaBlur.hpp"

using namespace optonaut;

#define DEBUG_TAG "Alignment.cpp"

std::shared_ptr<CheckpointStore> leftStore;
std::shared_ptr<CheckpointStore> rightStore;
std::shared_ptr<CheckpointStore> postStore2;


std::shared_ptr<GlobalAlignment> globalAligner;

extern "C" {
    void Java_co_optonaut_optonaut_record_Alignment_align(JNIEnv *env, jobject thiz, jstring path, jstring sharedPath, jstring storagePath);
    void Java_co_optonaut_optonaut_record_Alignment_clear(JNIEnv *env, jobject thiz, jstring path, jstring sharedPath);
};



void Java_co_optonaut_optonaut_record_Alignment_align(JNIEnv *env, jobject thiz, jstring path, jstring sharedPath, jstring storagePath)
{

    const char *cPath = env->GetStringUTFChars(path, NULL);
    const char *cSharedPath = env->GetStringUTFChars(sharedPath, NULL);


    //CheckpointStore postStore(cPath, cSharedPath);


    const char *cString = env->GetStringUTFChars(storagePath, NULL);
    std::string s_path(cString);

    //postStore = std::make_shared<CheckpointStore>(cPath, cSharedPath);
    leftStore = std::make_shared<CheckpointStore>(s_path + "left/", s_path + "shared/");
    rightStore = std::make_shared<CheckpointStore>(s_path + "right/", s_path + "shared/");
    postStore2 = std::make_shared<CheckpointStore>(s_path + "post/", s_path + "shared/");

    leftStore->Clear();
    rightStore->Clear();

    globalAligner = std::make_shared<GlobalAlignment>( *postStore2, *leftStore, *rightStore);
    globalAligner->Finish();



}

void Java_co_optonaut_optonaut_record_Alignment_clear(JNIEnv *env, jobject thiz, jstring path, jstring sharedPath)
{
    const char *cPath = env->GetStringUTFChars(path, NULL);
    const char *cSharedPath = env->GetStringUTFChars(sharedPath, NULL);
    CheckpointStore store(cPath, cSharedPath);

    store.Clear();
}