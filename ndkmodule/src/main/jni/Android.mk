LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_CAMERA_MODULES:=off
OPENCV_LIB_TYPE:=STATIC

include $(OPENCV_ANDROID_PATH)
#include /Users/emi/Projects/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := ndkmodule
LOCAL_SRC_FILES := online-stitcher/src/common/static_timer.cpp \
                   online-stitcher/src/common/image.cpp \
                   online-stitcher/src/common/progressCallback.cpp \
                   online-stitcher/src/common/static_counter.cpp \
                   online-stitcher/src/io/checkpointStore.cpp \
                   online-stitcher/src/io/inputImage.cpp \
                   online-stitcher/src/io/io.cpp \
                   online-stitcher/src/math/quat.cpp \
                   online-stitcher/src/math/support.cpp \
                   online-stitcher/src/recorder/recorder.cpp \
                   online-stitcher/src/stereo/monoStitcher.cpp \
                   online-stitcher/src/stitcher/dynamicSeamer.cpp \
                   online-stitcher/src/stitcher/multiringStitcher.cpp \
                   online-stitcher/src/stitcher/ringStitcher.cpp \
                   online-stitcher/src/stitcher/simpleSphereStitcher.cpp \
                   online-stitcher/src/stitcher/simplePlaneStitcher.cpp \
                   online-stitcher/src/debug/debugHook.cpp \
                   RecorderBridge.cpp \
                   AlignmentBridge.cpp \
                   StitcherBridge.cpp
LOCAL_EXPORT_LDLIBS := -llog
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog -lz
LOCAL_LDFLAGS += -ljnigraphics
include $(BUILD_SHARED_LIBRARY)