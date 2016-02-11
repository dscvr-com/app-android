#include <jni.h>

#include "online-stitcher/src/stitcher/stitcher.hpp"
#include "online-stitcher/src/io/checkpointStore.hpp"
#include "online-stitcher/src/math/projection.hpp"
#include "online-stitcher/src/imgproc/panoramaBlur.hpp"

using namespace optonaut;

#define DEBUG_TAG "Stitcher.cpp"

extern "C" {
    void Java_co_optonaut_optonaut_record_Stitcher_getResult(JNIEnv *env, jobject thiz, jstring path, jstring sharedPath);
    void Java_co_optonaut_optonaut_record_Stitcher_clear(JNIEnv *env, jobject thiz);
};


std::vector<Mat> getCubeFaces(const Mat& sphere)
{
    std::vector<Mat> cubeFaces(6);

    int width = sphere.cols / 4;
    for (int i = 0; i < 6; ++i)
    {
        CreateCubeMapFace(sphere, cubeFaces[i], i, width, width);
    }
    cv::imwrite("/storage/emulated/0//Pictures/Optonaut/imwrite/sphere.jpg", sphere);

    return cubeFaces;
}

std::vector<Mat> getResult(const std::string& path, const std::string& sharedPath)
{
    CheckpointStore store(path, sharedPath);
    Stitcher stitcher(store);
    Mat sphere = stitcher.Finish(ProgressCallback::Empty)->image.data;
    Mat blurred;
    optonaut::PanoramaBlur panoBlur(sphere.size(), cv::Size(sphere.cols, std::max(sphere.cols / 2, sphere.rows)));
    panoBlur.Blur(sphere, blurred);
    sphere.release();

    return getCubeFaces(blurred);
}

void Java_co_optonaut_optonaut_record_Stitcher_getResult(JNIEnv *env, jobject thiz, jstring path, jstring sharedPath)
{
    const char *cPath = env->GetStringUTFChars(path, NULL);
    const char *cSharedPath = env->GetStringUTFChars(sharedPath, NULL);

    auto result = getResult(cPath, cSharedPath);
}

void Java_co_optonaut_optonaut_record_Stitcher_clear(JNIEnv *env, jobject thiz)
{
    // TODO: clear stores left, right
}