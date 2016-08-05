package com.iam360.iam360.sensors;

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
    public static int SINGLE_TAP = 0;
    public static int DOUBLE_TAP = 1;
    public static int TAP_TYPE = -1;


    private GestureDetectors(Context context) {
        singleClickDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onSingleTapUp(MotionEvent e) {
                TAP_TYPE = SINGLE_TAP;
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                TAP_TYPE = DOUBLE_TAP;
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
