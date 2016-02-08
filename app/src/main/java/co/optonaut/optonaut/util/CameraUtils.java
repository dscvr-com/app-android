package co.optonaut.optonaut.util;

import android.hardware.Camera;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-02-07
 */
public class CameraUtils {
    public static Camera getCameraInstance(){
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e){
            // Camera is not available (in use or does not exist)
            Timber.e(e, "Camera not available at the moment.");
            Crashlytics.log(Log.ERROR, "Optonaut", "Camera was accessed but not available.");

        }
        // returns null if camera is unavailable
        return camera;
    }
}
