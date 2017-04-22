package com.iam360.dscvr.record;

public class ConvertToStereo {
    static {
        System.loadLibrary("optonaut-android-bridge");
    }

    public static native void convert(String path, String sharedPath, String storagePath);

    public static native void clear(String path, String sharedPath);


}