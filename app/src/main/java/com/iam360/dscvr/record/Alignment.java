package com.iam360.dscvr.record;

public class Alignment {
    static {
        System.loadLibrary("ndkmodule");
    }

    public static native void align(String path, String sharedPath, String storagePath);

    public static native void clear(String path, String sharedPath);
}