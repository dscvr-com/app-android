# DSCVR Android App

This repository contains the [DSCVR (former Optonaut) Android application](https://play.google.com/store/apps/details?id=com.iam360.dscvr). 

The applications wraps the image processing component (`online-stitcher`, found [here](https://github.com/dscvr-com/online-stitcher)), and adds the infrastucture necassary for recording and viewing on Android. 

## Project Structure

The project consists of java classes in `app/src/main/java/com/iam360/dscvr/` and an ndk module in `optonaut-android-bridge`. The ndk module simply bridges from java to the image stitcher, which is written in c++, using jni. 

The structure of the java code is the following: 
* *bus* Handles global events, such as the finishing of recording. 
* *bluetooth* Bluetooth implementation for the Orbit360 bluetooth base. 
* *model* Data Models for planned community support. 
* *network* Network protocol for planned community support. 
* *opengl* OpenGL code for gpu-accellerated viewing of panoramas and stereo panoramas.
* *record* Classes for recorder settings and recording itself, as well as OpenGL renderers for recording hints. 
* *sensors* Code to estimate image locations and head position during viewing. 
* *util* Several auxillary classes. 
* *viewmodels* View models for UI data binding. 
* *views* The application's UI components. 

## Developing

1) Please install NDK Tools, CMake and LLDB via the SDK Manager (Tools > Android > SDK-Manager)
2) Set the OPENCV_ANDROID_PATH environment variable properly to the opencv android sdk *directory* containing the `mk` files.
3) Launch android studio and compile, make sure the variable is set in OSX (for example by starting android studio using the terminal, calling`/Applications/Android\ Studio.app/Contents/MacOS/studio`. 
