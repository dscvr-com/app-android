#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include "online-stitcher/src/recorder/recorder.hpp"
#include "online-stitcher/src/recorder/recorder2.hpp"
#include "online-stitcher/src/io/checkpointStore.hpp"
//#include "online-stitcher/src/recorder/imageSink.hpp"
#include "online-stitcher/src/recorder/recorderGraph.hpp"

using namespace optonaut;

#define DEBUG_TAG "Recorder.cpp"

int counter = 0;

Mat intrinsics;

#ifndef __OPTIMIZE__
#error Optimization is disabled.
#endif


//std::shared_ptr<CheckpointStore> leftStore;
//std::shared_ptr<CheckpointStore> rightStore;
//std::shared_ptr<CheckpointStore> postStore;
std::shared_ptr<ImageSink> sink;

std::shared_ptr<Recorder2> recorder;
std::string debugPath;
std::string path;

extern "C" {
    // storagePath should end on "/"!
    void Java_com_iam360_dscvr_record_Recorder_initRecorder(JNIEnv *env, jobject thiz, jstring storagePath, jfloat sensorWidth, jfloat sensorHeight, jfloat focalLength, jint mode);

    void Java_com_iam360_dscvr_record_Recorder_push(JNIEnv *env, jobject thiz, jbyteArray bitmap, jint width, jint height, jdoubleArray extrinsicsData);
//    void Java_com_iam360_dscvr_record_Recorder_push(JNIEnv *env, jobject thiz, jobject bitmap, jdoubleArray extrinsicsData);

    void Java_com_iam360_dscvr_record_Recorder_setIdle(JNIEnv *env, jobject thiz, jboolean idle);

    jobjectArray Java_com_iam360_dscvr_record_Recorder_getSelectionPoints(JNIEnv *env, jobject thiz);

    jobject Java_com_iam360_dscvr_record_Recorder_lastKeyframe(JNIEnv *env, jobject thiz);

    void Java_com_iam360_dscvr_record_Recorder_finish(JNIEnv *env, jobject thiz);

    void Java_com_iam360_dscvr_record_Recorder_dispose(JNIEnv *env, jobject thiz);

    jfloatArray Java_com_iam360_dscvr_record_Recorder_getBallPosition(JNIEnv *env, jobject thiz);

    jboolean Java_com_iam360_dscvr_record_Recorder_isFinished(JNIEnv *env, jobject thiz);

    jdouble Java_com_iam360_dscvr_record_Recorder_getDistanceToBall(JNIEnv *env, jobject thiz);

    jfloatArray Java_com_iam360_dscvr_record_Recorder_getAngularDistanceToBall(JNIEnv *env, jobject thiz);

    jboolean Java_com_iam360_dscvr_record_Recorder_hasStarted(JNIEnv *env, jobject thiz);

    jboolean Java_com_iam360_dscvr_record_Recorder_isIdle(JNIEnv *env, jobject thiz);

    void Java_com_iam360_dscvr_record_Recorder_enableDebug(JNIEnv *env, jobject thiz, jstring storagePath);

    void Java_com_iam360_dscvr_record_Recorder_disableDebug(JNIEnv *env, jobject thiz);

    jfloatArray matToJFloatArray(JNIEnv *env, const Mat& mat, int width, int height);

    jint Java_com_iam360_dscvr_record_Recorder_getRecordedImagesCount(JNIEnv *env, jobject thiz);

    jint Java_com_iam360_dscvr_record_Recorder_getImagesToRecordCount(JNIEnv *env, jobject thiz);

    jfloatArray Java_com_iam360_dscvr_record_Recorder_getCurrentRotation(JNIEnv *env, jobject thiz);

    jobject Java_com_iam360_dscvr_record_Recorder_getPreviewImage(JNIEnv *env, jobject thiz);

    jboolean Java_com_iam360_dscvr_record_Recorder_previewAvailable(JNIEnv *env, jobject thiz);

}

jfloatArray matToJFloatArray(JNIEnv *env, const Mat& mat, int width, int height)
{
    assert(mat.cols == width && mat.rows == height && mat.type() == CV_64F);
    double* doubles = (double*)  mat.data;
    int size = width*height;
    jfloatArray javaFloats = (jfloatArray) env->NewFloatArray(size);

    jfloat *body = env->GetFloatArrayElements(javaFloats, false);

    for (int i = 0; i < size; ++i)
    {
        body[i] = doubles[i];
    }

    env->ReleaseFloatArrayElements(javaFloats, body, 0);

    return javaFloats;
}

void Java_com_iam360_dscvr_record_Recorder_initRecorder(JNIEnv *env, jobject thiz, jstring storagePath, jfloat sensorWidth, jfloat sensorHeight, jfloat focalLength, jint mode)
{
    const char *cString = env->GetStringUTFChars(storagePath, NULL);
    std::string pathLocal(cString);
    pathLocal = pathLocal + "/dgb/";
    path = pathLocal;

    Log << "Init'ing recorder";
    Log << "Sensor height " << sensorHeight;
    Log << "Sensor width " << sensorWidth;
    Log << "Focal len " << focalLength;
    Log << "Debug path " << path;

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
    recorder = std::make_shared<Recorder2>(androidBase.clone(), zero.clone(), intrinsics, mode, 1.0, path);
}

void Java_com_iam360_dscvr_record_Recorder_push(JNIEnv *env, jobject thiz, jbyteArray bitmap, jint width, jint height, jdoubleArray extrinsicsData) {

    char *pixels = (char *)env->GetByteArrayElements(bitmap, NULL);

    // Now you can use the pixel array 'pixels', which is in RGBA format
    double *temp = (double *) (env)->GetDoubleArrayElements(extrinsicsData, NULL);

    Mat extrinsics(4, 4, CV_64F, temp);

    InputImageP image(new InputImage());
    InputImageRef inputImageRef;
    inputImageRef.data = pixels;
    inputImageRef.width = width;
    inputImageRef.height = height;
    inputImageRef.colorSpace = colorspace::BGRA;
    image->dataRef = inputImageRef;
    image->id = counter++;
    image->originalExtrinsics = extrinsics.clone();
    image->intrinsics = intrinsics.clone();

    recorder->Push(image);
    env->ReleaseDoubleArrayElements(extrinsicsData, (jdouble *) temp, 0);
    env->ReleaseByteArrayElements(bitmap, (jbyte *) pixels, 0);
}

void Java_com_iam360_dscvr_record_Recorder_setIdle(JNIEnv *env, jobject thiz, jboolean idle)
{
    recorder->SetIdle(idle);
}

jobjectArray Java_com_iam360_dscvr_record_Recorder_getSelectionPoints(JNIEnv *env, jobject thiz) {
    std::vector<SelectionPoint> selectionPoints = recorder->GetSelectionPoints();

    jclass java_selection_point_class = env->FindClass("com/iam360/dscvr/record/SelectionPoint");
    jobjectArray javaSelectionPoints = (jobjectArray) env->NewObjectArray(selectionPoints.size(),
                                                                          java_selection_point_class, 0);

    // [F for float array, III for three ints
    jmethodID java_selection_point_init = env->GetMethodID(java_selection_point_class, "<init>", "([FIII)V");

    for(int i = 0; i < selectionPoints.size(); ++i)
    {
        jobject current_point =  env->NewObject(java_selection_point_class, java_selection_point_init,
                                                matToJFloatArray(env, selectionPoints[i].extrinsics, 4, 4),
                                                selectionPoints[i].globalId,
                                                selectionPoints[i].ringId,
                                                selectionPoints[i].localId);

        env->SetObjectArrayElement(javaSelectionPoints, i, current_point);
        env->DeleteLocalRef(current_point);
    }

    return javaSelectionPoints;
}

jobject Java_com_iam360_dscvr_record_Recorder_lastKeyframe(JNIEnv *env, jobject thiz) {

//    assert(recorder != NULL);
//    SelectionPoint* selectionPoint = ConvertSelectionPoint(env, recorder->GetCurrentKeyframe().closestPoint);
    SelectionPoint selectionPoint = recorder->GetCurrentKeyframe().closestPoint;

    jclass java_selection_point_class = env->FindClass("com/iam360/dscvr/record/SelectionPoint");
    jmethodID java_selection_point_init = env->GetMethodID(java_selection_point_class, "<init>", "([FIII)V");
    jobject javaSelectionPoint = env->NewObject(java_selection_point_class, java_selection_point_init,
                                                 matToJFloatArray(env, selectionPoint.extrinsics, 4, 4),
                                                 selectionPoint.globalId,
                                                 selectionPoint.ringId,
                                                 selectionPoint.localId);

    return javaSelectionPoint;

}

void Java_com_iam360_dscvr_record_Recorder_finish(JNIEnv *env, jobject thiz)
{
    recorder->Finish();

    CheckpointStore leftStore(path + "left/", path + "shared/");
    CheckpointStore rightStore(path + "right/", path + "shared/");

    leftStore.SaveOptograph(recorder->GetLeftResult());
    rightStore.SaveOptograph(recorder->GetRightResult());
}

void Java_com_iam360_dscvr_record_Recorder_dispose(JNIEnv *env, jobject thiz)
{
    assert(recorder != NULL);
    // Do nothing, except deleting
//    [[NSFileManager defaultManager] removeItemAtPath:self->tempPath error:nil]; //TODO
//    recorder->Dispose();
    recorder = NULL;
}

jfloatArray Java_com_iam360_dscvr_record_Recorder_getBallPosition(JNIEnv *env, jobject thiz)
{
    return matToJFloatArray(env ,recorder->GetBallPosition(), 4, 4);
}

jboolean Java_com_iam360_dscvr_record_Recorder_isFinished(JNIEnv *env, jobject thiz)
{
    return recorder->IsFinished();
}

jdouble Java_com_iam360_dscvr_record_Recorder_getDistanceToBall(JNIEnv *env, jobject thiz)
{
    return recorder->GetDistanceToBall();
}

jfloatArray Java_com_iam360_dscvr_record_Recorder_getAngularDistanceToBall(JNIEnv *env, jobject thiz)
{
    matToJFloatArray(env, recorder->GetAngularDistanceToBall(), 1, 3);
}

jboolean Java_com_iam360_dscvr_record_Recorder_hasStarted(JNIEnv *env, jobject thiz)
{
    return recorder->HasStarted();
}

jboolean Java_com_iam360_dscvr_record_Recorder_isIdle(JNIEnv *env, jobject thiz)
{
    return recorder->IsIdle();
}

void Java_com_iam360_dscvr_record_Recorder_enableDebug(JNIEnv *env, jobject thiz, jstring storagePath)
{
    const char *cString = env->GetStringUTFChars(storagePath, NULL);
    std::string path(cString);
    debugPath = path + "debug/";
}

void Java_com_iam360_dscvr_record_Recorder_disableDebug(JNIEnv *env, jobject thiz)
{
    debugPath = "";
}

jint Java_com_iam360_dscvr_record_Recorder_getRecordedImagesCount(JNIEnv *env, jobject thiz) {
    return recorder->GetRecordedImagesCount();
}

jint Java_com_iam360_dscvr_record_Recorder_getImagesToRecordCount(JNIEnv *env, jobject thiz) {
    return recorder->GetImagesToRecordCount();
}

jfloatArray Java_com_iam360_dscvr_record_Recorder_getCurrentRotation(JNIEnv *env, jobject thiz)
{
    assert(false);
//    return matToJFloatArray(env ,recorder->GetCurrentRotation(), 4, 4);
}

jobject matrixToBitmap(JNIEnv *env, const Mat& mat)
{
    jclass bitmapConfig = env->FindClass("android/graphics/Bitmap$Config");
    jfieldID rgba8888FieldID = env->GetStaticFieldID(bitmapConfig, "ARGB_8888", "Landroid/graphics/Bitmap$Config;");
    jobject rgba8888Obj = env->GetStaticObjectField(bitmapConfig, rgba8888FieldID);

    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapMethodID = env->GetStaticMethodID(bitmapClass,"createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jobject bitmapObj = env->CallStaticObjectMethod(bitmapClass, createBitmapMethodID, mat.cols, mat.rows, rgba8888Obj);

    jintArray pixels = env->NewIntArray(mat.cols * mat.rows);

    jint *body = env->GetIntArrayElements(pixels, false);

    cv::cvtColor(
            mat,
            cv::Mat(mat.rows, mat.cols, CV_8UC4, body),
            cv::COLOR_RGB2RGBA);

    env->ReleaseIntArrayElements(pixels, body, 0);

    jmethodID setPixelsMid = env->GetMethodID(bitmapClass, "setPixels", "([IIIIIII)V");
    env->CallVoidMethod(bitmapObj, setPixelsMid, pixels, 0, mat.cols, 0, 0, mat.cols, mat.rows);

    return bitmapObj;
}

jobject Java_com_iam360_dscvr_record_Recorder_getPreviewImage(JNIEnv *env, jobject thiz)
{
    assert(recorder != NULL);
//    Mat result = recorder->FinishPreview()->image.data;
    Mat result = recorder->GetPreviewImage()->image.data;
    return matrixToBitmap(env, result);
}

jboolean Java_com_iam360_dscvr_record_Recorder_previewAvailable(JNIEnv *env, jobject thiz)
{
    assert(recorder != NULL);
    return true;
//    return recorder->PreviewAvailable();
}
