package co.optonaut.optonaut.record;

import android.graphics.Bitmap;

import java.util.List;

import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-02-07
 */
public class Recorder {
    static {
        System.loadLibrary("ndkmodule");
    }

    private static boolean isInitialized = false;

    private static native void initRecorder(String storagePath, float sensorWidth, float sensorHeight, float focalLength);
    public static native void push(Bitmap bitmap, double[] extrinsicsData);
    public static native SelectionPoint[] getSelectionPoints();
    public static native void finish();
    public static native void dispose();

    public static void pushImage(Bitmap bitmap, double[] extrinsicsData) {
        push(bitmap, extrinsicsData);
    }
    public static void initializeRecorder(String storagePath, float sensorWidth, float sensorHeight, float focalLength) {
        if (!isInitialized) {
            Timber.v("Initialized recorder");
            initRecorder(storagePath, sensorWidth, sensorHeight, focalLength);
            isInitialized = true;
        }
    }
}