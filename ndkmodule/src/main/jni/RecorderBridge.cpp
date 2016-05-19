#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include "online-stitcher/src/recorder/recorder.hpp"
#include "online-stitcher/src/io/checkpointStore.hpp"
#include "online-stitcher/src/recorder/imageSink.hpp"
#include "online-stitcher/src/recorder/recorderGraph.hpp"

using namespace optonaut;

#define DEBUG_TAG "Recorder.cpp"

int counter = 0;

Mat intrinsics;

//std::shared_ptr<CheckpointStore> leftStore;
//std::shared_ptr<CheckpointStore> rightStore;
std::shared_ptr<CheckpointStore> postStore;
std::shared_ptr<ImageSink> sink;

std::shared_ptr<Recorder> recorder;
std::string debugPath;

extern "C" {
    // storagePath should end on "/"!
    void Java_co_optonaut_optonaut_record_Recorder_initRecorder(JNIEnv *env, jobject thiz, jstring storagePath, jfloat sensorWidth, jfloat sensorHeight, jfloat focalLength, jint mode);

    void Java_co_optonaut_optonaut_record_Recorder_push(JNIEnv *env, jobject thiz, jobject bitmap, jdoubleArray extrinsicsData);

    void Java_co_optonaut_optonaut_record_Recorder_setIdle(JNIEnv *env, jobject thiz, jboolean idle);

    jobjectArray Java_co_optonaut_optonaut_record_Recorder_getSelectionPoints(JNIEnv *env, jobject thiz);

    jobject Java_co_optonaut_optonaut_record_Recorder_lastKeyframe(JNIEnv *env, jobject thiz);

    void Java_co_optonaut_optonaut_record_Recorder_finish(JNIEnv *env, jobject thiz);

    void Java_co_optonaut_optonaut_record_Recorder_dispose(JNIEnv *env, jobject thiz);

    jfloatArray Java_co_optonaut_optonaut_record_Recorder_getBallPosition(JNIEnv *env, jobject thiz);

    jboolean Java_co_optonaut_optonaut_record_Recorder_isFinished(JNIEnv *env, jobject thiz);

    jdouble Java_co_optonaut_optonaut_record_Recorder_getDistanceToBall(JNIEnv *env, jobject thiz);

    jfloatArray Java_co_optonaut_optonaut_record_Recorder_getAngularDistanceToBall(JNIEnv *env, jobject thiz);

    jboolean Java_co_optonaut_optonaut_record_Recorder_hasStarted(JNIEnv *env, jobject thiz);

    jboolean Java_co_optonaut_optonaut_record_Recorder_isIdle(JNIEnv *env, jobject thiz);

    void Java_co_optonaut_optonaut_record_Recorder_enableDebug(JNIEnv *env, jobject thiz, jstring storagePath);

    void Java_co_optonaut_optonaut_record_Recorder_disableDebug(JNIEnv *env, jobject thiz);

    jfloatArray matToJFloatArray(JNIEnv *env, const Mat& mat, int width, int height);

    jint Java_co_optonaut_optonaut_record_Recorder_getRecordedImagesCount(JNIEnv *env, jobject thiz);

    jint Java_co_optonaut_optonaut_record_Recorder_getImagesToRecordCount(JNIEnv *env, jobject thiz);

    jfloatArray Java_co_optonaut_optonaut_record_Recorder_getCurrentRotation(JNIEnv *env, jobject thiz);

    jobject Java_co_optonaut_optonaut_record_Recorder_getPreviewImage(JNIEnv *env, jobject thiz);

    jboolean Java_co_optonaut_optonaut_record_Recorder_previewAvailable(JNIEnv *env, jobject thiz);

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

void Java_co_optonaut_optonaut_record_Recorder_initRecorder(JNIEnv *env, jobject thiz, jstring storagePath, jfloat sensorWidth, jfloat sensorHeight, jfloat focalLength, jint mode)
{
    const char *cString = env->GetStringUTFChars(storagePath, NULL);
    std::string path(cString);
    __android_log_print(ANDROID_LOG_VERBOSE, DEBUG_TAG, "%s %s", "Initializing Recorder with path", cString);

    //leftStore = std::make_shared<CheckpointStore>(path + "left/", path + "shared/");
    //rightStore = std::make_shared<CheckpointStore>(path + "right/", path + "shared/");
    postStore = std::make_shared<CheckpointStore>(path + "post/", path + "shared/");



    //leftStore->Clear();
    //rightStore->Clear();
    postStore->Clear();

    sink =std::make_shared<ImageSink>(*postStore);

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
    recorder = std::make_shared<Recorder>(androidBase.clone(), zero.clone(), intrinsics, *sink, debugPath, mode);
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

void Java_co_optonaut_optonaut_record_Recorder_setIdle(JNIEnv *env, jobject thiz, jboolean idle)
{
    recorder->SetIdle(idle);
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
                                                matToJFloatArray(env, selectionPoints[i].extrinsics, 4, 4),
                                                selectionPoints[i].globalId,
                                                selectionPoints[i].ringId,
                                                selectionPoints[i].localId);

        env->SetObjectArrayElement(javaSelectionPoints, i, current_point);
        env->DeleteLocalRef(current_point);
    }

    return javaSelectionPoints;
}

jobject Java_co_optonaut_optonaut_record_Recorder_lastKeyframe(JNIEnv *env, jobject thiz) {

//    assert(recorder != NULL);
//    SelectionPoint* selectionPoint = ConvertSelectionPoint(env, recorder->GetCurrentKeyframe().closestPoint);
    SelectionPoint selectionPoint = recorder->GetCurrentKeyframe().closestPoint;

    jclass java_selection_point_class = env->FindClass("co/optonaut/optonaut/record/SelectionPoint");
    jmethodID java_selection_point_init = env->GetMethodID(java_selection_point_class, "<init>", "([FIII)V");
    jobject javaSelectionPoint = env->NewObject(java_selection_point_class, java_selection_point_init,
                                                 matToJFloatArray(env, selectionPoint.extrinsics, 4, 4),
                                                 selectionPoint.globalId,
                                                 selectionPoint.ringId,
                                                 selectionPoint.localId);

    return javaSelectionPoint;

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

jfloatArray Java_co_optonaut_optonaut_record_Recorder_getBallPosition(JNIEnv *env, jobject thiz)
{
    return matToJFloatArray(env ,recorder->GetBallPosition(), 4, 4);
}

jboolean Java_co_optonaut_optonaut_record_Recorder_isFinished(JNIEnv *env, jobject thiz)
{
    return recorder->IsFinished();
}

jdouble Java_co_optonaut_optonaut_record_Recorder_getDistanceToBall(JNIEnv *env, jobject thiz)
{
    return recorder->GetDistanceToBall();
}

jfloatArray Java_co_optonaut_optonaut_record_Recorder_getAngularDistanceToBall(JNIEnv *env, jobject thiz)
{
    matToJFloatArray(env, recorder->GetAngularDistanceToBall(), 1, 3);
}

jboolean Java_co_optonaut_optonaut_record_Recorder_hasStarted(JNIEnv *env, jobject thiz)
{
    return recorder->HasStarted();
}

jboolean Java_co_optonaut_optonaut_record_Recorder_isIdle(JNIEnv *env, jobject thiz)
{
    return recorder->IsIdle();
}

void Java_co_optonaut_optonaut_record_Recorder_enableDebug(JNIEnv *env, jobject thiz, jstring storagePath)
{
    const char *cString = env->GetStringUTFChars(storagePath, NULL);
    std::string path(cString);
    debugPath = path + "debug/";
}

void Java_co_optonaut_optonaut_record_Recorder_disableDebug(JNIEnv *env, jobject thiz)
{
    debugPath = "";
}

jint Java_co_optonaut_optonaut_record_Recorder_getRecordedImagesCount(JNIEnv *env, jobject thiz) {
    return recorder->GetRecordedImagesCount();
}

jint Java_co_optonaut_optonaut_record_Recorder_getImagesToRecordCount(JNIEnv *env, jobject thiz) {
    return recorder->GetImagesToRecordCount();
}

jfloatArray Java_co_optonaut_optonaut_record_Recorder_getCurrentRotation(JNIEnv *env, jobject thiz)
{
    return matToJFloatArray(env ,recorder->GetCurrentRotation(), 4, 4);
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

jobject Java_co_optonaut_optonaut_record_Recorder_getPreviewImage(JNIEnv *env, jobject thiz)
{

    Mat result = recorder->FinishPreview()->image.data;
    return matrixToBitmap(env, result);
}

jboolean Java_co_optonaut_optonaut_record_Recorder_previewAvailable(JNIEnv *env, jobject thiz)
{
    return recorder->PreviewAvailable();
}
