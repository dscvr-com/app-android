package co.optonaut.optonaut.record;

import android.graphics.Bitmap;

import java.util.List;

/**
 * @author Nilan Marktanner
 * @date 2016-02-07
 */
public class Recorder {
    static {
        System.loadLibrary("ndkmodule");
    }

    public static native void initRecorder(String storagePath);
    public static native void push(Bitmap bitmap, double[] extrinsicsData);
    public static native SelectionPoint[] getSelectionPoints();


    public static void initialize(String storagePath) {
        initRecorder(storagePath);
    }

    public static void pushImage(Bitmap bitmap, double[] extrinsicsData) {
        push(bitmap, extrinsicsData);
    }
}