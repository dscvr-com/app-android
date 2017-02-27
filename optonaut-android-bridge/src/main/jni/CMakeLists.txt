cmake_minimum_required(VERSION 2.8)
project(optonaut-stitcher)
include(GNUInstallDirs)


execute_process(COMMAND "${CMAKE_COMMAND}" "-E" "environment")

find_package( OpenCV REQUIRED )


set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -W -Wall -Wunreachable-code -std=c++1y -O3 -g -Werror -fverbose-asm")
add_subdirectory(online-stitcher/src)

add_library(optonaut-android-bridge
             SHARED
             AlignmentBridge.cpp StitcherBridge RecorderBridge ConvertToStereoBridge)


target_link_libraries(optonaut-android-bridge optonaut-lib)