package co.optonaut.optonaut.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-12-30
 */
public class Constants {
    public static final String DEBUG_TAG = "Optonaut";
    public static final float ACCELERATION_EPSILON = 1.0f;
    public static final float MINIMUM_AXIS_LENGTH = 4.0f;
	public static final float VFOV = 95.0f;

    // Modes of the ring
    public static int MODE_ALL = 0; // Full sphere
    public static int MODE_CENTER = 1; // Only center ring
    public static int MODE_TRUNCATED = 2; // Omit top and bottom ring (usually leads three rings)
    public static int MODE_NOBOT = 3; // Omits bottom ring
    public static int MODE_TINYDEBUG = 1337; // Three ring slices. Good for debugging state transistions during recording.

    private static final String MAIN_ICON_PATH = "logo-text-white-temporary.png";
    private static Constants constants;
    private static final String BLACK_DEFAULT_TEXTURE_PATH = "default_black.bmp";

    private String cachePath;

    private DisplayMetrics displayMetrics;
    private Display display;
    private float HFOV;

    private int maxX;
    private int maxY;
    private Bitmap defaultTexture;
    private BitmapDrawable mainIcon;
    private Typeface icomoon_typeface;
    private Typeface SF_UI_Light;
    private Typeface SF_UI_Regular;
    private int expectedStatusBarHeight;
    private int toolbarHeight;

    private Constants(Activity activity) {
        initializeDisplay(activity);
        initializeFieldOfView();

        initializeDefaultTexture(activity);

        initializeTypefaces(activity);

        initializeMainIcon(activity);

        initializeExpectedStatusBarPixelHeight(activity);

        initializeToolbarHeight(activity);

        initializeCachePath(activity);
    }

    private void initializeFieldOfView() {
        HFOV = VFOV * displayMetrics.widthPixels / (float) displayMetrics.heightPixels;
    }

    private void initializeDisplay(Activity activity) {
        display = activity.getWindowManager().getDefaultDisplay();
        displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
    }

    private void initializeToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
    }

    private void initializeExpectedStatusBarPixelHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            expectedStatusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        } else {
            Timber.e("Could not load expected StatusBar height!");
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
            Timber.e("Could not load main icon!");
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

    private void initializeTypefaces(Context context) {
        AssetManager am = context.getAssets();
        icomoon_typeface = Typeface.createFromAsset(am, "icons.ttf");
        SF_UI_Light = Typeface.createFromAsset(am, "SF-UI-Text-Light.otf");
        SF_UI_Regular = Typeface.createFromAsset(am, "SF-UI-Text-Regular.otf");
    }

    private void initializeDefaultTexture(Context context) {
        AssetManager am = context.getAssets();

        InputStream is = null;
        try {
            is = am.open(BLACK_DEFAULT_TEXTURE_PATH);
            defaultTexture = BitmapFactory.decodeStream(is);
        } catch (final IOException e) {
            Timber.e("Could not load default texture!");
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

    private void initializeCachePath(Activity activity) {
        if (activity.getExternalCacheDir() == null) {
            // TODO: user dialog
            throw new RuntimeException("External Storage is currently not mounted!");
        }

        cachePath = activity.getExternalCacheDir().getPath();
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

    public Typeface getIconTypeface() {
        return icomoon_typeface;
    }

    public Typeface getDefaultLightTypeFace() {
        return SF_UI_Light;
    }

    public Typeface getDefaultRegularTypeFace() {
        return SF_UI_Regular;
    }

    public int getExpectedStatusBarHeight() {
        return expectedStatusBarHeight;
    }

    public int getToolbarHeight() {
        return toolbarHeight;
    }

    public float getHFOV() {
        return HFOV;
    }

    public String getCachePath() {
        return cachePath;
    }
}
