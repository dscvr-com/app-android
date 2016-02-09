package co.optonaut.optonaut.util;

import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.joda.time.DateTime;

import java.io.File;
import java.util.List;

import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-02-07
 */
public class CameraUtils {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static Camera getCameraInstance(){
        Camera camera = null;
        try {
            //camera = open();
        } catch (Exception e){
            // Camera is not available (in use or does not exist)
            Timber.e(e, "Camera not available at the moment.");
            Crashlytics.log(Log.ERROR, "Optonaut", "Camera was accessed but not available.");

        }
        // returns null if camera is unavailable
        return camera;
    }

    public static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    public static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if (!isExternalStorageWritable()) {
            Timber.d("SD Card not mounted!");
            return null;
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Optonaut");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Timber.d("Failed to create picture directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = RFC3339DateFormatter.toRFC3339String(DateTime.now());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static Camera.Size getBiggestSupportedPictureSize(Camera camera) {
        List<Camera.Size> sizes = camera.getParameters().getSupportedPictureSizes();

        int max = 0;
        int index = 0;

        for (int i = 0; i < sizes.size(); i++){
            Camera.Size s = sizes.get(i);
            int size = s.height * s.width;
            if (size > max) {
                index = i;
                max = size;
            }
        }

        // TODO: return correct size
        //return camera.Size(sizes.get(index).width, sizes.get(index).height);
        return null;
    }
}
