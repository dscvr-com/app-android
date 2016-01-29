package co.optonaut.optonaut.views;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * @author Nilan Marktanner
 * @date 2016-01-29
 */
public class GestureDetectors {
    private static boolean isInitialized = false;
    private static GestureDetectors gestureDetectors;

    public static GestureDetector singleClickDetector;

    private GestureDetectors(Context context) {
        singleClickDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    public static void initialize(Context context){
        if (!isInitialized) {
            gestureDetectors = new GestureDetectors(context);
            isInitialized = true;
        }
    }

    public static GestureDetectors getInstance() {
        return gestureDetectors;
    }

}
