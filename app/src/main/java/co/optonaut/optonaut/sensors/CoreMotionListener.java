package co.optonaut.optonaut.sensors;

import android.app.Service;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.util.Log;

import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.util.Maths;

/**
 * @author Nilan Marktanner
 * @date 2016-01-29
 */
public class CoreMotionListener extends RotationMatrixProvider implements SensorEventListener {
    private static CoreMotionListener coreMotionListener;

    private static SensorManager sensorManager;

    private static final float[] CORRECTION = Maths.buildRotationMatrix(new float[]{90, 1, 0, 0});
    private static int observers = 0;

    private float[] rotationMatrix;


    private CoreMotionListener(Context context) {
        rotationMatrix = null;
        sensorManager = (SensorManager) context.getSystemService(Service.SENSOR_SERVICE);
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

    public static void register() {
        if (true) {
            Log.v(Constants.DEBUG_TAG, "Registering CoreMotionListener");
            sensorManager.registerListener(coreMotionListener, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Log.v(Constants.DEBUG_TAG, "Skip Registering CoreMotionListener");
        }

        observers++;
    }

    public static void unregister() {
        observers--;
        if (observers == 0) {
            Log.v(Constants.DEBUG_TAG, "Unregistering CoreMotionListener");
            sensorManager.unregisterListener(coreMotionListener);
        } else if (observers < 0) {
            Log.w(Constants.DEBUG_TAG, "Unregister call but no observer!");
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
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
            newValues[2] = event.values[2]; // z
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public float[] getRotationMatrix() {
        return rotationMatrix;
    }
}
