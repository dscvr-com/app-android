package co.optonaut.optonaut.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Nilan Marktanner
 * @date 2015-12-30
 */
public class Constants {
    public static final String DEBUG_TAG = "Optonaut";
    private static Constants constants;
    private static final String BLACK_DEFAULT_TEXTURE_PATH = "default_black.bmp";

    private DisplayMetrics displayMetrics;
    private Bitmap defaultTexture;


    private Constants(Activity activity) {
        displayMetrics = new DisplayMetrics();

        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        initializeDefaultTexture(activity);
    }

    private void initializeDefaultTexture(Context context) {
        AssetManager am = context.getAssets();

        InputStream is = null;
        try {
            is = am.open(BLACK_DEFAULT_TEXTURE_PATH);
            defaultTexture = BitmapFactory.decodeStream(is);
        } catch (final IOException e) {
            Log.d(DEBUG_TAG, "Could not load default texture!");
            defaultTexture = null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static void initializeConstants(Activity activity) {
        if (constants == null) {
            constants = new Constants(activity);
        }
    }

    public static Constants getInstance() {
        if (constants == null) {
            throw new RuntimeException("Constants singleton was not initialized!");
        }
        return constants;
    }

    public DisplayMetrics getDisplayMetrics() {
        return displayMetrics;
    }

    public Bitmap getDefaultTexture() {
        return defaultTexture;
    }
}
