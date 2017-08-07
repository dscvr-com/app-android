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
        }

    }

    public static void savePanoramaToLocationWithExif(Bitmap bitmap, String filename) {
        FileOutputStream out = null;
        try {
            File file = new File(filename);
            File parent = file.getParentFile();
            parent.mkdirs();
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

            DscvrApp.getInstance().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(parent.getAbsolutePath())));

            ExifInterface exif = new ExifInterface(file.getCanonicalPath());
            exif.setAttribute(ExifInterface.TAG_MODEL, Constants.CAMERA_MODEL);
            exif.setAttribute(ExifInterface.TAG_MAKE, Constants.CAMERA_MAKE);
            exif.saveAttributes();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
