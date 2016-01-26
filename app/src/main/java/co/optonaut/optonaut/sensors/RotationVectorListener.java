package co.optonaut.optonaut.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.opengl.Matrix;

/**
 * @author Nilan Marktanner
 * @date 2015-12-26
 */
public class RotationVectorListener extends RotationMatrixProvider {
    private float[] correctionMatrix = new float[16];
    private float[] rotationMatrix = new float[16];


    public RotationVectorListener() {
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setRotateM(correctionMatrix, 0, 90, 1, 0, 0);
    }

    public void handleSensorEvent(SensorEvent event) {
        // It is good practice to check that we received the proper sensor event
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
        {
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
            Matrix.multiplyMM(rotationMatrix, 0, correctionMatrix, 0, temp, 0);
        }
    }

    @Override
    public float[] getRotationMatrix() {
        return rotationMatrix;
    }

    public float[] getRotationMatrixInverse() {
        float[] rotationMatrixInverse = new float[16];
        Matrix.invertM(rotationMatrixInverse, 0, rotationMatrix, 0);

        return rotationMatrixInverse;
    }
}
