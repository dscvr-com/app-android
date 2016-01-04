package co.optonaut.optonaut.util;

import android.app.Activity;
import android.content.Context;
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
        constants = new Constants(activity);
    }

    public static Constants getInstance() {
        if (constants == null) {
            throw new RuntimeException();
        }
        return constants;
    }

    public DisplayMetrics getDisplayMetrics() {
        return displayMetrics;
    }
}
