package co.optonaut.optonaut.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Environment;
import android.util.Log;
import android.util.SizeF;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-02-07
 */
public class CameraUtils {
    public static final String CACHE_PATH = Constants.getInstance().getCachePath().concat("/");
    public static final String PERSISTENT_STORAGE_PATH = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES).getPath().concat("/").concat("Optonaut").concat("/");

    public static Camera getCameraInstance(){
        Camera camera = null;
        try {
            camera = Camera.open(0);
        } catch (Exception e){
            Timber.e(e, "Camera not available at the moment.");
            Crashlytics.log(Log.ERROR, "Optonaut", "Camera was accessed but not available.");

            // TODO - handle cleanly
        }

        Camera.Size[] sizes = camera.getParameters().getSupportedPreviewSizes().toArray(new Camera.Size[0]);
        Camera.Size size = null;

        // Find smallest HD camera.
        for(int i = 1; i < sizes.length; i++) {
            if(sizes[i].width == 1280 && sizes[i].height == 720) {
                size = sizes[i];
            }
            Timber.v("size: [%s, %s]", sizes[i].width, sizes[i].height);
        }

        if (size == null) {
            throw new RuntimeException("No suitable camera size found!");
        }

        Camera.Parameters params = camera.getParameters();
        params.setPreviewSize(size.width, size.height);
        camera.setParameters(params);

        // returns null if camera is unavailable
        return camera;
    }

    public static void saveBitmapToLocation(Bitmap bitmap, String filename) {
        FileOutputStream out = null;
        try {
            File file = new File(filename);
            File parent = file.getParentFile();
            parent.mkdirs();
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static float[] getCameraResolution(Context context, int camNum) {
        // those are sizes obtained with the OnePlus One phone...
        float[] size = {4.6541333f, 3.4623966f};
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                String[] cameraIds = manager.getCameraIdList();
                if (cameraIds.length > camNum) {
                    CameraCharacteristics character = manager.getCameraCharacteristics(cameraIds[camNum]);
                    SizeF sizeF = character.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                    size[0] = sizeF.getWidth();
                    size[1] = sizeF.getHeight();
                }
            } catch (CameraAccessException e) {
                Timber.e("YourLogString", e.getMessage(), e);
            }
            Timber.v("size: [%s, %s]", size[0], size[1]);
        }
        return size;
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
        return 0xff000000 | (r<<16) | (g<<8) | b;
    }
}
