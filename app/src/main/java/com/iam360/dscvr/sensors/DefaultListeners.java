package com.iam360.dscvr.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.iam360.dscvr.sensors.fusion.thirdparty.OptimizedCoreMotionListener;

import timber.log.Timber;

/**
 * Created by emi on 14/11/2016.
 */

public class DefaultListeners {

    private static int observers = 0;
    private static SensorManager sensorManager;
    private static CoreMotionListener coreMotionListener;

    public static RotationMatrixProvider getInstance() {
        if (coreMotionListener == null) {
            throw new RuntimeException("MotionListener not initialized!");
        }
        return coreMotionListener;
    }

    public static void initialize(Context context) {
        if (coreMotionListener == null) {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

            // Change instance type here!
            coreMotionListener = new OptimizedCoreMotionListener(context);
        }
    }

    private static void registerInternal() {
        if (observers == 0) {
            Timber.v("Registering CoreMotionListener");
            for(int t : coreMotionListener.getRequiredSensorTypes()) {
                sensorManager.registerListener(coreMotionListener, sensorManager.getDefaultSensor(t), SensorManager.SENSOR_DELAY_FASTEST);
            }
        } else {
            Timber.v("Skip Registering CoreMotionListener");
        }

        observers++;
    }

    private static void unregisterInternal() {
        observers--;
        if (observers == 0) {
            Timber.v("Unregistering CoreMotionListener");
            sensorManager.unregisterListener(coreMotionListener);
        } else if (observers < 0) {
            Timber.w("Unregister call but no observer!");
        }
    }


    public static void register() {
        registerInternal();
    }

    public static void unregister() {
        unregisterInternal();
    }
}

interface SensorTypeList {
    int[] getRequiredSensorTypes();
}