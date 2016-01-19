package co.optonaut.optonaut.util;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * @author Nilan Marktanner
 * @date 2015-12-30
 */
public class Constants {
    public static final String DEBUG_TAG = "Optonaut";
    private static Constants constants;

    private DisplayMetrics displayMetrics;


    private Constants(Activity activity) {
        displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
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
}
