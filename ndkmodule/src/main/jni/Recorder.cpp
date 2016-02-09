#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include "online-stitcher/src/recorder/recorder.hpp"
#include "online-stitcher/src/io/checkpointStore.hpp"
#include "online-stitcher/src/recorder/storageSink.hpp"
#include "online-stitcher/src/recorder/recorderGraph.hpp"

using namespace optonaut;



#define DEBUG_TAG "NDK_AndroidNDK1SampleActivity"

const string path = "/storage/emulated/0/Pictures/Optonaut/";

int counter = 0;

Mat intrinsics;

CheckpointStore leftStore(path + "left", path + "shared");
CheckpointStore rightStore(path + "right", path + "shared");

StorageSink sink(leftStore, rightStore);

std::shared_ptr<Recorder> recorder;


extern "C" {
    void Java_co_optonaut_optonaut_nativecode_TestUtil_initRecorder(JNIEnv *env, jobject thiz);
    void Java_co_optonaut_optonaut_nativecode_TestUtil_push(JNIEnv *env, jobject thiz, jobject bitmap, jdoubleArray extrinsicsData);
}
void Java_co_optonaut_optonaut_nativecode_TestUtil_initRecorder(JNIEnv *env, jobject thiz)
{
    __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", "Initializing Recorder");

    double androidBaseData[16] = {
            -1, 0, 0, 0,
            0, -1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    };

    double intrinsicsData[9] = {
            1, 0, 1,
            0, 1, 1,
            0, 0, 1
    };

    Mat androidBase(4, 4, CV_64F, androidBaseData);
    Mat zero = Mat::eye(4,4, CV_64F);
    intrinsics = Mat(3, 3, CV_64F, intrinsicsData).clone();

    // 1 -> RecorderGraph::ModeCenter
    recorder = std::make_shared<Recorder>(androidBase.clone(), zero.clone(), intrinsics.clone(), sink, "", 1, true);
}

void Java_co_optonaut_optonaut_nativecode_TestUtil_push(JNIEnv *env, jobject thiz, jobject bitmap, jdoubleArray extrinsicsData) {
    AndroidBitmapInfo  info;
    uint32_t          *pixels;
    int                ret;

    AndroidBitmap_getInfo(env, bitmap, &info);

    if(info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", "Bitmap format is not RGBA_8888!");
    }

    AndroidBitmap_lockPixels(env, bitmap, reinterpret_cast<void **>(&pixels));
    // Now you can use the pixel array 'pixels', which is in RGBA format

    jboolean isCopy;
    double *temp = (double *) (env)->GetDoubleArrayElements(extrinsicsData, &isCopy);

    Mat extrinsics(4, 4, CV_64F, temp);

    InputImageP image(new InputImage());
    InputImageRef inputImageRef;
    inputImageRef.data = pixels;
    inputImageRef.width = info.width;
    inputImageRef.height = info.height;
    inputImageRef.colorSpace = colorspace::RGBA;
    image->dataRef = inputImageRef;
    image->id = counter++;
    image->originalExtrinsics = extrinsics.clone();
    image->intrinsics = intrinsics.clone();

    recorder->Push(image);
    env->ReleaseDoubleArrayElements(extrinsicsData, (jdouble *) temp, 0);

    AndroidBitmap_unlockPixels(env, bitmap);
}