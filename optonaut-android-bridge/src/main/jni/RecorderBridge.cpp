#include <jni.h>
#include "online-stitcher/src/common/jniHelper.hpp"
#include "online-stitcher/src/recorder/recorder.hpp"
#include "online-stitcher/src/recorder/recorder2.hpp"
#include "online-stitcher/src/recorder/multiRingRecorder2.hpp"

using namespace optonaut;

#define DEBUG_TAG "Recorder.cpp"

int counter = 0;

Mat intrinsics;

#ifndef __ANDROID__
#error "android flag is not set"
#endif

std::unique_ptr<CheckpointStore> leftStore;
std::unique_ptr<CheckpointStore> rightStore;
std::unique_ptr<CheckpointStore> postStore;

std::unique_ptr<Recorder2> recorder;
std::unique_ptr<MultiRingRecorder> multiRingRecorder;
std::string debugPath;
std::string path;
std::unique_ptr<StorageImageSink> leftSink;
std::unique_ptr<StorageImageSink> rightSink;
int internalRecordingMode;

jobject selectionPointToJava(JNIEnv *env, const SelectionPoint &selectionPoint);

extern "C" {
    // storagePath should end on "/"!
    void Java_com_iam360_dscvr_record_Recorder_initRecorder(JNIEnv *env, jobject thiz, jstring storagePath, jfloat sensorWidth, jfloat sensorHeight, jfloat focalLength, jint mode, jobject paramInfo);

    void Java_com_iam360_dscvr_record_Recorder_push(JNIEnv *env, jobject thiz, jobject bitmap, jint width, jint height, jdoubleArray extrinsicsData);

    void Java_com_iam360_dscvr_record_Recorder_setIdle(JNIEnv *env, jobject thiz, jboolean idle);

    jobjectArray Java_com_iam360_dscvr_record_Recorder_getSelectionPoints(JNIEnv *env, jobject thiz);

    jobject Java_com_iam360_dscvr_record_Recorder_lastKeyframe(JNIEnv *env, jobject thiz);

    void Java_com_iam360_dscvr_record_Recorder_finish(JNIEnv *env, jobject thiz);

    void Java_com_iam360_dscvr_record_Recorder_cancel(JNIEnv *env, jobject thiz);

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

    jboolean Java_com_iam360_dscvr_record_Recorder_previewAvailable(JNIEnv *env, jobject thiz);


    jdouble Java_com_iam360_dscvr_record_Recorder_getTopThetaValue(JNIEnv *env, jobject thiz);
    jdouble Java_com_iam360_dscvr_record_Recorder_getCenterThetaValue(JNIEnv *env, jobject thiz);
    jdouble Java_com_iam360_dscvr_record_Recorder_getBotThetaValue(JNIEnv *env, jobject thiz);


}

// On method signature IDs: http://www.rgagnon.com/javadetails/java-0286.html
double invokeMethodDouble(JNIEnv *env, jobject obj, const char* name) {
    jmethodID methodRef = env->GetMethodID(env->GetObjectClass(obj), name,"()D");
    return env->CallDoubleMethod(obj, methodRef);
}
float invokeMethodInt(JNIEnv *env, jobject obj, const char* name) {
    jmethodID methodRef = env->GetMethodID(env->GetObjectClass(obj), name,"()I");
    return env->CallIntMethod(obj, methodRef);
}
bool invokeMethodBoolean(JNIEnv *env, jobject obj, const char* name) {
    jmethodID methodRef = env->GetMethodID(env->GetObjectClass(obj), name,"()Z");
    return env->CallBooleanMethod(obj, methodRef);
}

optonaut::RecorderParamInfo convertParamInfo(JNIEnv *env, jobject param) {
    return RecorderParamInfo(
        invokeMethodDouble(env, param, "getGraphHOverlap"),
        invokeMethodDouble(env, param, "getGraphVOverlap"),
        invokeMethodDouble(env, param, "getStereoHBuffer"),
        invokeMethodDouble(env, param, "getStereoVBuffer"),
        invokeMethodDouble(env, param, "getTolerance"),
        invokeMethodBoolean(env, param, "getHalfGraph")
    );
}

jfloatArray matToJFloatArray(JNIEnv *env, const Mat& mat, int width, int height)
{
    Assert(mat.cols == width && mat.rows == height && mat.type() == CV_64F);
    double* doubles = (double*)  mat.data;
    int size = width*height;
    jfloatArray javaFloats = (jfloatArray) env->NewFloatArray(size);

    jfloat *body = env->GetFloatArrayElements(javaFloats, NULL);

    for (int i = 0; i < size; ++i)
    {
        body[i] = doubles[i];
    }

    env->ReleaseFloatArrayElements(javaFloats, body, 0);

    return javaFloats;
}

void Java_com_iam360_dscvr_record_Recorder_initRecorder(JNIEnv *env, jobject, jstring storagePath, jfloat sensorWidth, jfloat sensorHeight, jfloat focalLength, jint mode, jobject paramInfo)
{
    const char *cString = env->GetStringUTFChars(storagePath, NULL);
    std::string pathLocal(cString);
    std::string debugPath = "";
    //std::string debugPath = pathLocal + "/dgb/"; // If debug is enabled, the recorder will crash on finish.
    path = pathLocal;

    optonaut::JniHelper::jni_context = env;

    leftStore = std::unique_ptr<CheckpointStore>(new CheckpointStore(path + "left/", path + "shared/"));
    rightStore = std::unique_ptr<CheckpointStore>(new CheckpointStore(path + "right/", path + "shared/"));

    leftStore->Clear();
    rightStore->Clear();

    leftSink = std::unique_ptr<StorageImageSink>(new StorageImageSink(*leftStore));
    rightSink = std::unique_ptr<StorageImageSink>(new StorageImageSink(*rightStore));


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

    internalRecordingMode = mode;

   // RecorderParamInfo(const double graphHOverlap, const double graphVOverlap, const double stereoHBuffer, const double stereoVBuffer, const double tolerance, const bool halfGraph)

    if(internalRecordingMode == optonaut::RecorderGraph::ModeCenter) {
        recorder = std::unique_ptr<Recorder2>(
                new Recorder2(androidBase.clone(), zero.clone(), intrinsics, mode, 10.0, debugPath,
                              convertParamInfo(env, paramInfo)));
    } else {
        multiRingRecorder = std::unique_ptr<MultiRingRecorder>(
                new MultiRingRecorder(androidBase.clone(), zero.clone(), intrinsics, *leftSink, *rightSink, mode, 10.0, debugPath,
                              convertParamInfo(env, paramInfo)));
    }
}

void Java_com_iam360_dscvr_record_Recorder_push(JNIEnv *env, jobject, jobject bitmap, jint width, jint height, jdoubleArray extrinsicsData) {

    void *pixels = (env)->GetDirectBufferAddress(bitmap);

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


    if(internalRecordingMode == optonaut::RecorderGraph::ModeCenter) {
        Assert(recorder != nullptr);
        recorder->Push(image);
    } else {
        Assert(multiRingRecorder != nullptr);
        multiRingRecorder->Push(image);
    }

    env->ReleaseDoubleArrayElements(extrinsicsData, (jdouble *) temp, 0);
}

void Java_com_iam360_dscvr_record_Recorder_setIdle(JNIEnv *, jobject, jboolean idle)
{
    if(internalRecordingMode == optonaut::RecorderGraph::ModeCenter) {
        Assert(recorder != nullptr);
        recorder->SetIdle(idle);
    } else {
        Assert(multiRingRecorder != nullptr);
        multiRingRecorder->SetIdle(idle);
    }
}

jobjectArray Java_com_iam360_dscvr_record_Recorder_getSelectionPoints(JNIEnv *env, jobject) {
    std::vector<SelectionPoint> selectionPoints;

    if(internalRecordingMode == optonaut::RecorderGraph::ModeCenter) {
        Assert(recorder != nullptr);
        selectionPoints = recorder->GetSelectionPoints();
    } else {
        Assert(multiRingRecorder != nullptr);
        selectionPoints = multiRingRecorder->GetSelectionPoints();
    }

    jclass java_selection_point_class = env->FindClass("com/iam360/dscvr/record/SelectionPoint");
    jobjectArray javaSelectionPoints = (jobjectArray) env->NewObjectArray(selectionPoints.size(),
                                                                          java_selection_point_class, 0);
    for(size_t i = 0; i < selectionPoints.size(); ++i)
    {
        jobject current_point = selectionPointToJava(env, selectionPoints[i]);

        env->SetObjectArrayElement(javaSelectionPoints, i, current_point);
        env->DeleteLocalRef(current_point);
    }

    return javaSelectionPoints;
}

jobject Java_com_iam360_dscvr_record_Recorder_lastKeyframe(JNIEnv *env, jobject) {

    SelectionPoint selectionPoint;

    if(internalRecordingMode == optonaut::RecorderGraph::ModeCenter) {
        Assert(recorder != nullptr);
        selectionPoint = recorder->GetCurrentKeyframe().closestPoint;
    } else {
        Assert(multiRingRecorder != nullptr);
        selectionPoint = multiRingRecorder->GetCurrentKeyframe().closestPoint;
    }

    jobject javaSelectionPoint = selectionPointToJava(env, selectionPoint);

    return javaSelectionPoint;

}

jobject selectionPointToJava(JNIEnv *env, const SelectionPoint &selectionPoint) {
    jclass java_selection_point_class = env->FindClass("com/iam360/dscvr/record/SelectionPoint");
    jmethodID java_selection_point_init = env->GetMethodID(java_selection_point_class, "<init>", "([FIIIFF)V");
    jobject javaSelectionPoint = env->NewObject(java_selection_point_class, java_selection_point_init,
                                                matToJFloatArray(env, selectionPoint.extrinsics, 4, 4),
                                                selectionPoint.globalId,
                                                selectionPoint.ringId,
                                                selectionPoint.localId,
                                                (float)selectionPoint.vPos,
                                                (float)selectionPoint.hPos);
    return javaSelectionPoint;
}

void Java_com_iam360_dscvr_record_Recorder_finish(JNIEnv *, jobject)
{
    if(internalRecordingMode == optonaut::RecorderGraph::ModeCenter) {
        Assert(recorder != nullptr);
        recorder->Finish();
    } else {
        Assert(multiRingRecorder != nullptr);
        multiRingRecorder->Finish();
    }

    CheckpointStore leftStore(path + "left/", path + "shared/");
    CheckpointStore rightStore(path + "right/", path + "shared/");

    leftStore.SaveOptograph(recorder->GetLeftResult());
    rightStore.SaveOptograph(recorder->GetRightResult());
}

void Java_com_iam360_dscvr_record_Recorder_cancel(JNIEnv *, jobject)
{
    if(internalRecordingMode == optonaut::RecorderGraph::ModeCenter) {
        Assert(recorder != nullptr);
        recorder->Cancel();
    } else {
        Assert(multiRingRecorder != nullptr);
        multiRingRecorder->Cancel();
    }
}

void Java_com_iam360_dscvr_record_Recorder_dispose(JNIEnv *, jobject )
{
    if(internalRecordingMode == optonaut::RecorderGraph::ModeCenter) {
        Assert(recorder != nullptr);
        recorder.reset();
    } else {
        Assert(multiRingRecorder != nullptr);
        multiRingRecorder.reset();
    }
}

jfloatArray Java_com_iam360_dscvr_record_Recorder_getBallPosition(JNIEnv *env, jobject)
{
    if(internalRecordingMode == optonaut::RecorderGraph::ModeCenter) {
        Assert(recorder != nullptr);
        return matToJFloatArray(env, recorder->GetBallPosition(), 4, 4);
    } else {
        Assert(multiRingRecorder != nullptr);
        return matToJFloatArray(env, multiRingRecorder->GetBallPosition(), 4, 4);
    }
}

jboolean Java_com_iam360_dscvr_record_Recorder_isFinished(JNIEnv *, jobject)
{
    if(internalRecordingMode == optonaut::RecorderGraph::ModeCenter) {
        Assert(recorder != nullptr);
        return (jboolean) recorder->IsFinished();
    } else {
        Assert(multiRingRecorder != nullptr);
        return (jboolean) multiRingRecorder->IsFinished();
    }
}

jdouble Java_com_iam360_dscvr_record_Recorder_getDistanceToBall(JNIEnv *, jobject)
{
    if(internalRecordingMode == optonaut::RecorderGraph::ModeCenter) {
        Assert(recorder != nullptr);
        return recorder->GetDistanceToBall();
    } else {
        Assert(multiRingRecorder != nullptr);
        return multiRingRecorder->GetDistanceToBall();
    }
}

jfloatArray Java_com_iam360_dscvr_record_Recorder_getAngularDistanceToBall(JNIEnv *env, jobject) {
    if(internalRecordingMode == optonaut::RecorderGraph::ModeCenter) {
        Assert(recorder != nullptr);
        return matToJFloatArray(env, recorder->GetAngularDistanceToBall(), 1, 3);
    } else {
        Assert(multiRingRecorder != nullptr);
        return matToJFloatArray(env, multiRingRecorder->GetAngularDistanceToBall(), 1, 3);
    }
}

jboolean Java_com_iam360_dscvr_record_Recorder_hasStarted(JNIEnv *, jobject)
{
    if(internalRecordingMode == optonaut::RecorderGraph::ModeCenter) {
        Assert(recorder != nullptr);
        return recorder->HasStarted();
    } else {
        Assert(multiRingRecorder != nullptr);
        return multiRingRecorder->HasStarted();
    }
}

jboolean Java_com_iam360_dscvr_record_Recorder_isIdle(JNIEnv *, jobject)
{
    if(internalRecordingMode == optonaut::RecorderGraph::ModeCenter) {
        Assert(recorder != nullptr);
        return recorder->IsIdle();
    } else {
        Assert(multiRingRecorder != nullptr);
        return multiRingRecorder->IsIdle();
    }
}

void Java_com_iam360_dscvr_record_Recorder_enableDebug(JNIEnv *env, jobject, jstring storagePath)
{
    const char *cString = env->GetStringUTFChars(storagePath, NULL);
    std::string path(cString);
    debugPath = path + "debug/";
}

void Java_com_iam360_dscvr_record_Recorder_disableDebug(JNIEnv *, jobject)
{
    debugPath = "";
}

jint Java_com_iam360_dscvr_record_Recorder_getRecordedImagesCount(JNIEnv *, jobject) {

    if(internalRecordingMode == optonaut::RecorderGraph::ModeCenter) {
        Assert(recorder != nullptr);
        return recorder->GetRecordedImagesCount();
    } else {
        Assert(multiRingRecorder != nullptr);
        return multiRingRecorder->GetRecordedImagesCount();
    }
}

jint Java_com_iam360_dscvr_record_Recorder_getImagesToRecordCount(JNIEnv *, jobject) {

    if(internalRecordingMode == optonaut::RecorderGraph::ModeCenter) {
        Assert(recorder != nullptr);
        return recorder->GetImagesToRecordCount();
    } else {
        Assert(multiRingRecorder != nullptr);
        return multiRingRecorder->GetImagesToRecordCount();
    }
}

jfloatArray Java_com_iam360_dscvr_record_Recorder_getCurrentRotation(JNIEnv *, jobject)
{
    Assert(false);
    return NULL;
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

jboolean Java_com_iam360_dscvr_record_Recorder_previewAvailable(JNIEnv *, jobject)
{
    Assert(recorder != nullptr || multiRingRecorder != nullptr);
    return true;
}
