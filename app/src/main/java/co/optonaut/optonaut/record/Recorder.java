package co.optonaut.optonaut.record;

import android.graphics.Bitmap;

/**
 * @author Nilan Marktanner
 * @date 2016-02-07
 */
public class Recorder {
    static {
        System.loadLibrary("ndkmodule");
    }

    private static native void initRecorder(String storagePath);
    private static native void push(Bitmap bitmap, double[] extrinsicsData);


    public static void initialize(String storagePath) {
        initRecorder(storagePath);
    }

    public static void pushImage(Bitmap bitmap, double[] extrinsicsData) {
        push(bitmap, extrinsicsData);
    }
}