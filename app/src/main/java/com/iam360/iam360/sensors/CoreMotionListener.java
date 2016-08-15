package com.iam360.iam360.sensors;

import android.app.Service;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.util.Log;

import com.iam360.iam360.util.Maths;

import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-01-29
 */
public class CoreMotionListener extends RotationMatrixProvider implements SensorEventListener {
    private static CoreMotionListener coreMotionListener;

    private SensorManager sensorManager;

    private final float[] CORRECTION;
    private int observers = 0;

    private float[] rotationMatrix;

    private CoreMotionListener(Context context) {
        rotationMatrix = null;
        sensorManager = (SensorManager) context.getSystemService(Service.SENSOR_SERVICE);

        float[] baseCorrection = Maths.buildRotationMatrix(new float[]{90, 1, 0, 0});
        //[1.0, 0.0, 0.0, 0.0, 0.0, -4.371139E-8, 1.0, 0.0, 0.0, -1.0, -4.371139E-8, 0.0, 0.0, 0.0, 0.0, 1.0]
        Log.d("MARK","baseCorrection  = "+ Arrays.toString(baseCorrection));

        String sensorVendor = "";
        String sensorName = "";

        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensors) {
            if(sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                sensorVendor = sensor.getVendor();
                sensorName = sensor.getName();
            }
        }

        String deviceModel = android.os.Build.MODEL;
        String deviceMan = android.os.Build.MANUFACTURER;

        Timber.w("Device/Man: " + deviceModel + "/" + deviceMan);
        Timber.w("Sensor/Man: " + sensorName + "/" + sensorVendor);

        if(deviceModel.equals("Nexus 6P") && deviceMan.equals("Huawei") && sensorVendor.equals("Bosch") && sensorName.equals("BMI160 accelerometer")) {
            CORRECTION = new float[16];
            float[] nexus6PSpecificCorrection = Maths.buildRotationMatrix(new float[]{(float)(0.045 / Math.PI * 180f), 1, 0, 0});
            Matrix.multiplyMM(CORRECTION, 0, nexus6PSpecificCorrection, 0, baseCorrection, 0);

            Timber.w("Applying Nexus 6P BMI160 rotation offset.");

        } else {
            CORRECTION = baseCorrection;
        }
    }

    public static void initialize(Context context) {
        if (coreMotionListener == null) {
            coreMotionListener = new CoreMotionListener(context);
        }
    }

    public static CoreMotionListener getInstance() {
        if (coreMotionListener == null) {
            throw new RuntimeException("CoreMotionListener not initialized!");
        }
        return coreMotionListener;
    }

    private void registerInternal() {
        if (observers == 0) {
            Timber.v("Registering CoreMotionListener");
            sensorManager.registerListener(coreMotionListener, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Timber.v("Skip Registering CoreMotionListener");
        }

        observers++;
    }

    private void unregisterInternal() {
        observers--;
        if (observers == 0) {
            Timber.v("Unregistering CoreMotionListener");
            sensorManager.unregisterListener(coreMotionListener);
        } else if (observers < 0) {
            Timber.w("Unregister call but no observer!");
        }
    }


    @Override
    public synchronized void onSensorChanged(SensorEvent event) {
        // It is good practice to check that we received the proper sensor event
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            /*
             * TODO:
             * replace this manual conversion with SensorManager.remapCoordinateSystem
             * SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Y, outR) doesn't seem to work
             */

            // flip y axis
            float[] newValues = new float[event.values.length];
            newValues[0] = event.values[0];  // x
            newValues[1] = -event.values[1]; // -y
            newValues[2] = event.values[2];  // z
            newValues[3] = event.values[3];  // w
            newValues[4] = event.values[4];  // should not be needed (refer to source code of getRotationMatrixFromVector)

            // Convert the rotation-vector to a 4x4 matrix.
            float[] temp = new float[16];
            SensorManager.getRotationMatrixFromVector(temp, newValues);

            // apply correction so we refer to the coordinate system of the phone when holding it "upright",
            // screen to the user and perpendicular to the ground plane
            rotationMatrix = new float[16];
            Matrix.multiplyMM(rotationMatrix, 0, CORRECTION, 0, temp, 0);
        }
    }

    public static void register() {
        getInstance().registerInternal();
    }

    public static void unregister() {
        getInstance().unregisterInternal();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if(accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
            Timber.w("Sensor accuracy is high");
        }
        else if(accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
            Timber.w("Sensor accuracy is low");
        }
        else if(accuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
            Timber.w("Sensor accuracy is medium");
        }
    }

    @Override
    public synchronized void getRotationMatrix(float[] t) {
        if(rotationMatrix != null)
            System.arraycopy(rotationMatrix, 0, t, 0, 16);
    }
}
