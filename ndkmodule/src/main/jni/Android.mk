LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := ndkmodule
LOCAL_SRC_FILES := native.c
LOCAL_EXPORT_LDLIBS := -llog
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
include $(BUILD_SHARED_LIBRARY)