package co.optonaut.optonaut.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.util.Log;

import java.util.Arrays;

import co.optonaut.optonaut.util.Maths;

/**
 * @author Nilan Marktanner
 * @date 2015-12-26
 */
public class RotationVectorListener extends RotationMatrixProvider {
    private float[] rotationMatrix = new float[16];


    public RotationVectorListener() {
        Matrix.setIdentityM(rotationMatrix, 0);
    }

    public void handleSensorEvent(SensorEvent event) {
        // It is good practice to check that we received the proper sensor event
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
        {
            // TODO: replace this manual conversion with SensorManager.remapCoordinateSystem
            // for some reason, we have to use minus x and minus z. we have to negate w too for quaternion reasons
            float[] newValues = new float[event.values.length];
            newValues[0] = -event.values[0]; // -x
            newValues[1] = event.values[1];  // y
            newValues[2] = -event.values[2]; // -z
            newValues[3] = -event.values[3]; // -w
            newValues[4] = event.values[4];  // should not be needed (refer to source code of getRotationMatrixFromVector)

            // Convert the rotation-vector to a 4x4 matrix.
            SensorManager.getRotationMatrixFromVector(rotationMatrix, newValues);
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
