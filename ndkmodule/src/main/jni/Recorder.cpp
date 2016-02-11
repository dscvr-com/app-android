#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include "online-stitcher/src/recorder/recorder.hpp"
#include "online-stitcher/src/io/checkpointStore.hpp"
#include "online-stitcher/src/recorder/storageSink.hpp"
#include "online-stitcher/src/recorder/recorderGraph.hpp"

using namespace optonaut;

#define DEBUG_TAG "Recorder.cpp"

int counter = 0;

Mat intrinsics;

std::shared_ptr<Recorder> recorder;


extern "C" {
    // storagePath should end on "/"!
    void Java_co_optonaut_optonaut_record_Recorder_initRecorder(JNIEnv *env, jobject thiz, jstring storagePath, jfloat sensorWidth, jfloat sensorHeight, jfloat focalLength);

    void Java_co_optonaut_optonaut_record_Recorder_push(JNIEnv *env, jobject thiz, jobject bitmap, jdoubleArray extrinsicsData);

    jobjectArray Java_co_optonaut_optonaut_record_Recorder_getSelectionPoints(JNIEnv *env, jobject thiz);

    void Java_co_optonaut_optonaut_record_Recorder_finish(JNIEnv *env, jobject thiz);

    void Java_co_optonaut_optonaut_record_Recorder_dispose(JNIEnv *env, jobject thiz);
}

jfloatArray matToJFloatArray(JNIEnv *env, const Mat& mat)
{
    assert(mat.cols == 4 && mat.rows == 4 && mat.type() == CV_64F);
    double* doubles = (double*)  mat.data;
    jfloatArray javaFloats = (jfloatArray) env->NewFloatArray(16);

    jfloat *body = env->GetFloatArrayElements(javaFloats, false);

    for (int i = 0; i < 16; ++i)
    {
        body[i] = doubles[i];
    }

    env->ReleaseFloatArrayElements(javaFloats, body, 0);

    return javaFloats;
}

void Java_co_optonaut_optonaut_record_Recorder_initRecorder(JNIEnv *env, jobject thiz, jstring storagePath, jfloat sensorWidth, jfloat sensorHeight, jfloat focalLength)
{
    const char *cString = env->GetStringUTFChars(storagePath, NULL);
    std::string path(cString);
    __android_log_print(ANDROID_LOG_VERBOSE, DEBUG_TAG, "%s %s", "Initializing Recorder with path", cString);

    CheckpointStore leftStore(path + "left", path + "shared");;
    CheckpointStore rightStore(path + "right", path + "shared");;

    StorageSink sink(leftStore, rightStore);

    double androidBaseData[16] = {
            -1, 0, 0, 0,
            0, -1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    };

    double intrinsicsData[9] = {
            focalLength, 0, sensorHeight / 2.0,
            0, focalLength, sensorWidth / 2.0,
            0, 0, 1
    };

    Mat androidBase(4, 4, CV_64F, androidBaseData);
    Mat zero = Mat::eye(4,4, CV_64F);
    intrinsics = Mat(3, 3, CV_64F, intrinsicsData).clone();

    // 1 -> RecorderGraph::ModeCenter
    recorder = std::make_shared<Recorder>(androidBase.clone(), zero.clone(), intrinsics.clone(), sink, "", 1, true);
    recorder->SetIdle(false);
}

void Java_co_optonaut_optonaut_record_Recorder_push(JNIEnv *env, jobject thiz, jobject bitmap, jdoubleArray extrinsicsData) {
    AndroidBitmapInfo  info;
    uint32_t          *pixels;
    int                ret;

    AndroidBitmap_getInfo(env, bitmap, &info);

    if(info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        __android_log_print(ANDROID_LOG_ERROR, DEBUG_TAG, "%s", "Bitmap format is not RGBA_8888!");
    }

    AndroidBitmap_lockPixels(env, bitmap, reinterpret_cast<void **>(&pixels));

    // Now you can use the pixel array 'pixels', which is in RGBA format
    double *temp = (double *) (env)->GetDoubleArrayElements(extrinsicsData, NULL);

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

jobjectArray Java_co_optonaut_optonaut_record_Recorder_getSelectionPoints(JNIEnv *env, jobject thiz) {
    std::vector<SelectionPoint> selectionPoints = recorder->GetSelectionPoints();

    jclass java_selection_point_class = env->FindClass("co/optonaut/optonaut/record/SelectionPoint");
    jobjectArray javaSelectionPoints = (jobjectArray) env->NewObjectArray(selectionPoints.size(),
                                                                          java_selection_point_class, 0);

    // [F for float array, III for three ints
    jmethodID java_selection_point_init = env->GetMethodID(java_selection_point_class, "<init>", "([FIII)V");

    for(int i = 0; i < selectionPoints.size(); ++i)
    {
        jobject current_point =  env->NewObject(java_selection_point_class, java_selection_point_init,
                                                matToJFloatArray(env, selectionPoints[i].extrinsics),
                                                selectionPoints[i].globalId,
                                                selectionPoints[i].ringId,
                                                selectionPoints[i].localId);

        env->SetObjectArrayElement(javaSelectionPoints, i, current_point);
    }

    return javaSelectionPoints;
}

void Java_co_optonaut_optonaut_record_Recorder_finish(JNIEnv *env, jobject thiz)
{
    recorder->Finish();
}

void Java_co_optonaut_optonaut_record_Recorder_dispose(JNIEnv *env, jobject thiz)
{
    recorder->Dispose();
    recorder = NULL;
}

