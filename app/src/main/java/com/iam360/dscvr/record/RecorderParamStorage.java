package com.iam360.dscvr.record;

import android.util.Log;

/**
 * Created by Emi on 17/07/2017.
 */

public class RecorderParamStorage {
    public static RecorderParamInfo getRecorderParams(float flen, float sx, float sy, boolean usingMotor) {
        Log.d("DEVICEINFO", "flen: " + flen + ", sx: " + sx + ", sy: " + sy + " motor-on: " + usingMotor);
        Log.d("DEVICEINFO", "Device: " + android.os.Build.DEVICE + ", Model: " + android.os.Build.MODEL + " ("+ android.os.Build.PRODUCT + ")");

        //  RecorderParamInfo(double graphHOverlap, double graphVOverlap, double stereoHBuffer, double stereoVBuffer, double tolerance, boolean halfGraph)

        double vBuffer = usingMotor ? 0.0 : -0.05;

        switch(android.os.Build.MODEL) {
            /*
            07-17 09:48:58.264 18589-18589/com.iam360.dscvr D/DEVICEINFO: flen: 4.367, sx: 4.6032, sy: 3.5168
            07-17 09:48:58.264 18589-18589/com.iam360.dscvr D/DEVICEINFO: Device: hammerhead, Model: Nexus 5 (hammerhead)
            */
            case "Nexus 5":
            /*
            07-17 11:05:14.055 27477-27477/com.iam360.dscvr D/DEVICEINFO: flen: 5.137, sx: 6.324, sy: 4.6934
            07-17 11:05:14.055 27477-27477/com.iam360.dscvr D/DEVICEINFO: Device: angler, Model: Nexus 6P (angler)
             */
            case "Nexus 6p":
            /*
            07-17 13:15:52.434 11738-11738/com.iam360.dscvr D/DEVICEINFO: flen: 4.5133004, sx: 5.2326403, sy: 3.93344
            07-17 13:15:52.434 11738-11738/com.iam360.dscvr D/DEVICEINFO: Device: OnePlus5, Model: ONEPLUS A5000 (OnePlus5)
             */
            case "ONEPLUS A5000":
            case "SM-N920I": return new RecorderParamInfo(0.8, 0.25, 0.50, vBuffer, 2.0, true);
            default: return new RecorderParamInfo(0.8, 0.25, 0.6, vBuffer, 2.0, true); // FALL TROUGH
        }
    }
}
