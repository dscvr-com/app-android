package co.optonaut.optonaut.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Nilan Marktanner
 * @date 2015-12-30
 */
public class Constants {
    public static final String DEBUG_TAG = "Optonaut";
    public static final float ACCELERATION_EPSILON = 1.0f;

    private static final String MAIN_ICON_PATH = "logo-text-white-temporary.png";
    private static Constants constants;
    private static final String BLACK_DEFAULT_TEXTURE_PATH = "default_black.bmp";

    private DisplayMetrics displayMetrics;
    private Display display;
    private int maxX;
    private int maxY;
    private Bitmap defaultTexture;
    private BitmapDrawable mainIcon;
    private Typeface typeface;
    private int expectedStatusBarHeight;
    private int toolbarHeight;


    private Constants(Activity activity) {
        initializeDisplay(activity);

        initializeDefaultTexture(activity);

        initializeTypeface(activity);

        initializeMainIcon(activity);

        initializeExpectedStatusBarPixelHeight(activity);

        initializeToolbarHeight(activity);
    }

    private void initializeDisplay(Activity activity) {
        display = activity.getWindowManager().getDefaultDisplay();
        displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
    }

    private void initializeToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
    }

    private void initializeExpectedStatusBarPixelHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            expectedStatusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        } else {
            Log.d(DEBUG_TAG, "Could not load expected StatusBar height!");
            expectedStatusBarHeight = 0;
        }
    }

    private void initializeMainIcon(Context context) {
        AssetManager am = context.getAssets();

        InputStream is = null;
        try {
            is = am.open(MAIN_ICON_PATH);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            mainIcon = new BitmapDrawable(context.getResources(), bitmap);
        } catch (final IOException e) {
            Log.d(DEBUG_TAG, "Could not load main icon!");
            e.printStackTrace();
            mainIcon = null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    private void initializeTypeface(Context context) {
        typeface = Typeface.createFromAsset(context.getAssets(), "icons.ttf");
    }

    private void initializeDefaultTexture(Context context) {
        AssetManager am = context.getAssets();

        InputStream is = null;
        try {
            is = am.open(BLACK_DEFAULT_TEXTURE_PATH);
            defaultTexture = BitmapFactory.decodeStream(is);
        } catch (final IOException e) {
            Log.d(DEBUG_TAG, "Could not load default texture!");
            e.printStackTrace();
            defaultTexture = null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
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

    public BitmapDrawable getMainIcon() {
        return mainIcon;
    }

    public Typeface getDefaultTypeface() {
        return typeface;
    }

    public int getExpectedStatusBarHeight() {
        return expectedStatusBarHeight;
    }

    public int getToolbarHeight() {
        return toolbarHeight;
    }
}
