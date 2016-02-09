package co.optonaut.optonaut.nativecode;

import android.graphics.Bitmap;

/**
 * @author Nilan Marktanner
 * @date 2016-02-07
 */
public class TestUtil {
    static {
        System.loadLibrary("ndkmodule");
    }

    private native void initRecorder();
    private native void push(Bitmap bitmap, double[] extrinsicsData);


    public void initialize() {
        initRecorder();
    }

    public void pushImage(Bitmap bitmap, double[] extrinsicsData) {
        push(bitmap, extrinsicsData);
    }
}