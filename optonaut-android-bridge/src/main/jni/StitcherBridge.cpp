#include <jni.h>
#include <android/log.h>

#include "online-stitcher/src/stitcher/stitcher.hpp"
#include "online-stitcher/src/io/checkpointStore.hpp"
#include "online-stitcher/src/math/projection.hpp"
#include "online-stitcher/src/imgproc/panoramaBlur.hpp"

using namespace optonaut;

#define DEBUG_TAG "Stitcher.cpp"

extern "C" {
    jobjectArray Java_com_iam360_dscvr_record_Stitcher_getResult(JNIEnv *env, jobject thiz, jstring path, jstring sharedPath, jint mode);
    jobject Java_com_iam360_dscvr_record_Stitcher_getEQResult(JNIEnv *env, jobject thiz, jstring path, jstring sharedPath, jint mode);
    void Java_com_iam360_dscvr_record_Stitcher_clear(JNIEnv *env, jobject thiz, jstring path, jstring sharedPath);
    jboolean Java_com_iam360_dscvr_record_Stitcher_hasUnstitchedRecordings(JNIEnv *env, jobject thiz, jstring path, jstring sharedPath);
};



std::vector<Mat> getCubeFaces(const Mat& sphere)
{
    std::vector<Mat> cubeFaces(6);

    int width = sphere.cols / 4;
    for (int i = 0; i < 6; ++i)
    {
        CreateCubeMapFace(sphere, cubeFaces[i], i, width, width);
    }

    return cubeFaces;
}

std::vector<Mat> getResult(const std::string& path, const std::string& sharedPath)
{
    CheckpointStore store(path, sharedPath);
//    Stitcher stitcher(store);
//    Mat sphere = stitcher.Finish(ProgressCallback::Empty)->image.data;

    StitchingResultP result = store.LoadOptograph();
    result->image.Load();
    Mat sphere = result->image.data;

    Mat blurred;
    optonaut::PanoramaBlur panoBlur(sphere.size(), cv::Size(sphere.cols, std::max(sphere.cols / 2, sphere.rows)));
    panoBlur.Blur(sphere, blurred);
    sphere.release();

    return getCubeFaces(blurred);
}

std::vector<Mat> getResultThreeRing(const std::string& path, const std::string& sharedPath)
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

Mat getEQResult(const std::string& path, const std::string& sharedPath)
{
    CheckpointStore store(path, sharedPath);
//    Stitcher stitcher(store);
//    Mat sphere = stitcher.Finish(ProgressCallback::Empty)->image.data;
    StitchingResultP result = store.LoadOptograph();
    result->image.Load();
    Mat sphere = result->image.data;

    Mat blurred;
    optonaut::PanoramaBlur panoBlur(sphere.size(), cv::Size(sphere.cols, std::max(sphere.cols / 2, sphere.rows)));
    panoBlur.Black(sphere, blurred);
    sphere.release();

    return blurred;
}

Mat getEQResultThreeRing(const std::string& path, const std::string& sharedPath)
{
    CheckpointStore store(path, sharedPath);
    Stitcher stitcher(store);
    Mat sphere = stitcher.Finish(ProgressCallback::Empty)->image.data;
    Mat blurred;
    optonaut::PanoramaBlur panoBlur(sphere.size(), cv::Size(sphere.cols, std::max(sphere.cols / 2, sphere.rows)));
    panoBlur.Black(sphere, blurred);
    sphere.release();

    return blurred;
}

jobject matToBitmap(JNIEnv *env, const Mat& mat)
{
    jclass bitmapConfig = env->FindClass("android/graphics/Bitmap$Config");
    jfieldID rgba8888FieldID = env->GetStaticFieldID(bitmapConfig, "ARGB_8888", "Landroid/graphics/Bitmap$Config;");
    jobject rgba8888Obj = env->GetStaticObjectField(bitmapConfig, rgba8888FieldID);

    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapMethodID = env->GetStaticMethodID(bitmapClass,"createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jobject bitmapObj = env->CallStaticObjectMethod(bitmapClass, createBitmapMethodID, mat.cols, mat.rows, rgba8888Obj);

    jintArray pixels = env->NewIntArray(mat.cols * mat.rows);

    jint *body = env->GetIntArrayElements(pixels, NULL);

    cv::cvtColor(
            mat,
            cv::Mat(mat.rows, mat.cols, CV_8UC4, body),
            cv::COLOR_RGB2RGBA);

    env->ReleaseIntArrayElements(pixels, body, 0);

    jmethodID setPixelsMid = env->GetMethodID(bitmapClass, "setPixels", "([IIIIIII)V");
    env->CallVoidMethod(bitmapObj, setPixelsMid, pixels, 0, mat.cols, 0, 0, mat.cols, mat.rows);

    return bitmapObj;
}


jobjectArray Java_com_iam360_dscvr_record_Stitcher_getResult(JNIEnv *env, jobject, jstring path, jstring sharedPath, jint mode)
{
    const char *cPath = env->GetStringUTFChars(path, NULL);
    const char *cSharedPath = env->GetStringUTFChars(sharedPath, NULL);

    std::vector<Mat> result(6);
//    std::vector<Mat> result;// = NULL;// = getResult(cPath, cSharedPath);
    if(mode == RecorderGraph::ModeCenter) {
        result = getResult(cPath, cSharedPath);
    } else {
        result = getResultThreeRing(cPath, cSharedPath);
    }

    AssertEQ(result.size(), (size_t) 6);

    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    jobjectArray bitmaps = (jobjectArray) env->NewObjectArray(result.size(), bitmapClass, 0);

    for(size_t i = 0; i < result.size(); ++i)
    {
        env->SetObjectArrayElement(bitmaps, i, matToBitmap(env, result[i]));
    }

    return bitmaps;
}

jobject Java_com_iam360_dscvr_record_Stitcher_getEQResult(JNIEnv *env, jobject, jstring path, jstring sharedPath, jint mode)
{
    const char *cPath = env->GetStringUTFChars(path, NULL);
    const char *cSharedPath = env->GetStringUTFChars(sharedPath, NULL);

    Mat result;

    if(mode == RecorderGraph::ModeCenter ) {
        result = getEQResult(cPath, cSharedPath);
    } else {
        result = getEQResultThreeRing(cPath, cSharedPath);
    }

    return matToBitmap(env, result);
}

void Java_com_iam360_dscvr_record_Stitcher_clear(JNIEnv *env, jobject, jstring path, jstring sharedPath)
{
    const char *cPath = env->GetStringUTFChars(path, NULL);
    const char *cSharedPath = env->GetStringUTFChars(sharedPath, NULL);
    CheckpointStore store(cPath, cSharedPath);

    store.Clear();
}

jboolean Java_com_iam360_dscvr_record_Stitcher_hasUnstitchedRecordings(JNIEnv *env, jobject, jstring path, jstring sharedPath)
{
    const char *cPath = env->GetStringUTFChars(path, NULL);
    const char *cSharedPath = env->GetStringUTFChars(sharedPath, NULL);
    CheckpointStore store(cPath, cSharedPath);

    return store.HasUnstitchedRecording();

//    return Stores::left.HasUnstitchedRecording() || Stores::right.HasUnstitchedRecording();


}