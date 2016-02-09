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
            camera = Camera.open(0);
        } catch (Exception e){
            // Camera is not available (in use or does not exist)
            Timber.e(e, "Camera not available at the moment.");
            Crashlytics.log(Log.ERROR, "Optonaut", "Camera was accessed but not available.");

            // TODO - handle cleanly
        }

        Camera.Size[] sizes = camera.getParameters().getSupportedPreviewSizes().toArray(new Camera.Size[0]);

        Camera.Size size = sizes[0];

        // Find smallest HD camera.
        for(int i = 1; i < sizes.length; i++) {
            if(sizes[i].width < size.width && sizes[i].width >= 1280) {
                size = sizes[i];
            }
        }

        Camera.Parameters params = camera.getParameters();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(0, cameraInfo);
        Timber.v("Orientation: %s", cameraInfo.orientation);
        int rotate = 90;
        params.set("orientation", "landscape");
        params.setRotation(rotate);
        // height, width are switched
        params.setPreviewSize(size.width, size.height);
        camera.setParameters(params);

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
                    "IMG_" + timeStamp + ".jpg");
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


    /**
     * Converts YUV420 NV21 to ARGB8888
     *
     * @param data byte array on YUV420 NV21 format.
     * @param width pixels width
     * @param height pixels height
     * @return a RGB8888 pixels int array. Where each int is a pixels ARGB.
     */
    public static int[] convertYUV420_NV21toARGB8888(byte [] data, int width, int height) {
        int size = width * height;
        int offset = size;
        int[] pixels = new int[size];
        int u, v, y1, y2, y3, y4;

        // i percorre os Y and the final pixels
        // k percorre os pixles U e V
        for (int i = 0, k = 0; i < size; i += 2, k += 2) {
            y1 = data[i] & 0xff;
            y2 = data[i + 1] & 0xff;
            y3 = data[width + i] & 0xff;
            y4 = data[width + i + 1] & 0xff;

            u = data[offset + k] & 0xff;
            v = data[offset + k + 1] & 0xff;
            u = u - 128;
            v = v - 128;

            pixels[i] = convertYUVtoARGB(y1, u, v);
            pixels[i + 1] = convertYUVtoARGB(y2, u, v);
            pixels[width + i] = convertYUVtoARGB(y3, u, v);
            pixels[width + i + 1] = convertYUVtoARGB(y4, u, v);

            if (i != 0 && (i + 2) % width == 0)
                i += width;
        }
        return pixels;
    }

    private static int convertYUVtoARGB(int y, int u, int v) {
        int r,g,b;

        r = y + (int)1.402f*v;
        g = y - (int)(0.344f*u +0.714f*v);
        b = y + (int)1.772f*u;
        r = r>255? 255 : r<0 ? 0 : r;
        g = g>255? 255 : g<0 ? 0 : g;
        b = b>255? 255 : b<0 ? 0 : b;
        return 0xff000000 | (b<<16) | (g<<8) | r;
    }
}
