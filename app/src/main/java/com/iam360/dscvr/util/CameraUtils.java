package com.iam360.dscvr.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.iam360.dscvr.DscvrApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Nilan Marktanner
 * @date 2016-02-07
 */
public class CameraUtils {
    public static final String CACHE_PATH = Constants.getInstance().getCachePath().concat("/");
    public static final String PERSISTENT_STORAGE_PATH = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES).getPath().concat("/").concat("DSCVR").concat("/");

    public static void saveBitmapToLocation(Bitmap bitmap, String filename) {
        FileOutputStream out = null;
        File parent = null;
        try {
            File file = new File(filename);
            parent = file.getParentFile();
            parent.mkdirs();
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                    if(parent != null){
                        DscvrApp.getInstance().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(parent.getAbsolutePath())));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void saveBitmapToLocationEQ(Bitmap bitmap, String filename) {
        FileOutputStream out = null;
        try {
            File file = new File(filename);
            File parent = file.getParentFile();
            parent.mkdirs();
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

            ExifInterface exif = null;
            try {
                exif = new ExifInterface(file.getCanonicalPath());
                exif.setAttribute(ExifInterface.TAG_MODEL, Constants.CAMERA_MODEL);
                exif.setAttribute(ExifInterface.TAG_MAKE, Constants.CAMERA_MAKE);
                exif.saveAttributes();
            } catch (IOException e) {
                Log.e("myTag"," ERROR adding of attributes message: "+e.getMessage());
            }
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
