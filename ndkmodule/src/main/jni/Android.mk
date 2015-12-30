LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
include $(NDK_MODULE_PATH)/cflags.mk

LOCAL_MODULE := ndkmodule



## You can (unadvisedly) mix in cpp with your c code…  I’m doing it here because
## my client’s pre-built headers and structs were in cpp but I want to keep the JNI
## pieces in c...
# SRC_FILES := $(wildcard $(LOCAL_PATH)/*.cpp)
# LOCAL_SRC_FILES := $(SRC_FILES:$(LOCAL_PATH)/%=%) main.c
LOCAL_SRC_FILES := VrCubeWorld_Framework.cpp
$(warning $(LOCAL_SRC_FILES))

$(warning Optonaut $(LOCAL_STATIC_LIBRARIES))
$(warning shared $(LOCAL_SHARED_LIBRARIES))

LOCAL_STATIC_LIBRARIES  := systemutils vrsound vrlocale vrgui vrappframework libovrkernel
LOCAL_SHARED_LIBRARIES  := vrapi

$(warning Optonaut $(LOCAL_STATIC_LIBRARIES))
$(warning shared $(LOCAL_SHARED_LIBRARIES))

include $(BUILD_SHARED_LIBRARY)

$(call import-module,LibOVRKernel/Projects/AndroidPrebuilt/jni)
$(call import-module,VrApi/Projects/AndroidPrebuilt/jni)
$(call import-module,VrAppFramework/Projects/AndroidPrebuilt/jni)
$(warning $(LOCAL_EXPORT_C_INCLUDES))
# $(call import-module,VrAppSupport/SystemUtils/Projects/AndroidPrebuilt/jni)
# $(call import-module,VrAppSupport/VrGUI/Projects/AndroidPrebuilt/jni)
# $(call import-module,VrAppSupport/VrLocale/Projects/AndroidPrebuilt/jni)
# $(call import-module,VrAppSupport/VrSound/Projects/AndroidPrebuilt/jni)

$(warning Import of modules finished.)
$(warning $(LOCAL_EXPORT_C_INCLUDES))
