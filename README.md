# app-android
Android App Repository

# Requirements
## Build
 * In `ndkmodule`, add file `gradle.properties` and set `ndk_build_path` to your `ndk-build`:
    `ndk_build_path=/path/to/android-ndk/ndk-build`
 * Set `OPENCV_ANDROID_PATH` to OpenCV.mk of the OpenCV-Android-SDK:
    `export OPENCV_ANDROID_PATH=/path/to/opencv-android-sdk/sdk/native/jni/OpenCV.mk`

## Release
 * Setup your keystore with the correct signing keys. (TODO: use environment variable `OPTONAUT_KEYSTORE_PATH`)
