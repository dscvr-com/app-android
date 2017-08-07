#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#include "online-stitcher/src/stitcher/stitcher.hpp"
#include "online-stitcher/src/io/checkpointStore.hpp"
#include "online-stitcher/src/math/projection.hpp"
#include "online-stitcher/src/imgproc/panoramaBlur.hpp"

using namespace optonaut;

#define DEBUG_TAG "Stitcher.cpp"

extern "C" {
    jobject Java_com_iam360_dscvr_record_Stitcher_getResult(JNIEnv *env, jobject, jstring path, jstring sharedPath);
    jobjectArray  Java_com_iam360_dscvr_record_Stitcher_getCubeMap(JNIEnv *env, jobject, jobject sphereBitmap);
    void Java_com_iam360_dscvr_record_Stitcher_clear(JNIEnv *env, jobject, jstring path, jstring sharedPath);
    jboolean Java_com_iam360_dscvr_record_Stitcher_hasUnstitchedRecordings(JNIEnv *env, jobject, jstring path, jstring sharedPath);
};

void jniThrow(JNIEnv *env, const string error) {
    jclass je = env->FindClass("java/lang/RuntimeException");
    env->ThrowNew(je, error.c_str());
}

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

jobject createBitmap(JNIEnv *env, int width, int height) {

    jclass bitmapConfig = env->FindClass("android/graphics/Bitmap$Config");
    jfieldID rgba8888FieldID = env->GetStaticFieldID(bitmapConfig, "ARGB_8888", "Landroid/graphics/Bitmap$Config;");
    jobject rgba8888Obj = env->GetStaticObjectField(bitmapConfig, rgba8888FieldID);

    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapMethodID = env->GetStaticMethodID(bitmapClass,"createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    Log << "create bitmap width: " << width << " create bitmap height: " << height;

    jobject bitmapObj = env->CallStaticObjectMethod(bitmapClass, createBitmapMethodID, width, height, rgba8888Obj);

    if(bitmapObj == NULL) {
        jniThrow(env, "Created bitmap was null");
    }

    return bitmapObj;
}

void transferToBitmap(JNIEnv *env, cv::Mat &mat, jobject bitmap) {

    AndroidBitmapInfo  info;

    void* pixels = 0;
    int res = AndroidBitmap_getInfo(env, bitmap, &info);
    if(res != 0) {
        jniThrow(env, (("Android Bitmap getInfo error: " + optonaut::ToString(res))).c_str());
    }

    if(mat.type() != CV_8UC3) {
        jniThrow(env, "Mat pixel format error");
    }
    if(info.width != mat.cols || info.height != mat.rows) {
        jniThrow(env, "Mat/bitmap width/height mismatch");
    }
    if(AndroidBitmap_lockPixels(env, bitmap, &pixels) != 0) {
        jniThrow(env, "Lock pixels error");
    }

    cv::cvtColor(
            mat,
            cv::Mat(mat.rows, mat.cols, CV_8UC4, pixels),
            cv::COLOR_RGB2RGBA);

    AndroidBitmap_unlockPixels(env, bitmap);
}


void transferFromBitmap(JNIEnv *env, cv::Mat &mat, jobject bitmap) {

    AndroidBitmapInfo  info;
    void* pixels = 0;
    int res = AndroidBitmap_getInfo(env, bitmap, &info);
    if(res != 0) {
        jniThrow(env, (("Android Bitmap getInfo error: " + optonaut::ToString(res))).c_str());
    }

    if(mat.type() != CV_8UC3 || info.width != mat.cols || info.height != mat.rows) {
        mat = Mat(info.height, info.width, CV_8UC3);
    }
    if(AndroidBitmap_lockPixels(env, bitmap, &pixels) != 0) {
        jniThrow(env, "Lock pixels error");
    }

    cv::cvtColor(
            cv::Mat(mat.rows, mat.cols, CV_8UC4, pixels),
            mat,
            cv::COLOR_RGBA2RGB);

    AndroidBitmap_unlockPixels(env, bitmap);
}

jobject getResult(JNIEnv *env, string storePath, string sharedPath) {
    CheckpointStore store(storePath, sharedPath);
    optonaut::Stitcher stitcher(store);
    Mat sphere = stitcher.Finish(ProgressCallback::Empty)->image.data;

    Log << sphere.size();

    jobject bitmap = createBitmap(env, sphere.cols, sphere.rows);

    transferToBitmap(env, sphere, bitmap);

    return bitmap;
}


jobject Java_com_iam360_dscvr_record_Stitcher_getResult(JNIEnv *env, jobject, jstring path, jstring sharedPath)
{
    const char *cPath = env->GetStringUTFChars(path, NULL);
    const char *cSharedPath = env->GetStringUTFChars(sharedPath, NULL);

    return getResult(env, cPath, cSharedPath);
}

jobjectArray  Java_com_iam360_dscvr_record_Stitcher_getCubeMap(JNIEnv *env, jobject, jobject sphereBitmap) {
    Mat sphere;
    Mat blurred;

    transferFromBitmap(env, sphere, sphereBitmap);
    optonaut::PanoramaBlur panoBlur(sphere.size(), cv::Size(sphere.cols, std::max(sphere.cols / 2, sphere.rows)));

    Log << sphere.size();

    panoBlur.Blur(sphere, blurred);
    sphere.release();

    auto faces = getCubeFaces(blurred);

    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    jobjectArray bitmaps = (jobjectArray) env->NewObjectArray(faces.size(), bitmapClass, 0);

    for(size_t i = 0; i < faces.size(); ++i)
    {
        jobject bitmap = createBitmap(env, faces[i].cols, faces[i].rows);
        transferToBitmap(env, faces[i], bitmap);
        env->SetObjectArrayElement(bitmaps, i, bitmap);
    }

    return bitmaps;
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
}
