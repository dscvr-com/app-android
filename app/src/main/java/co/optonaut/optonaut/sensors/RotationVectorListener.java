package co.optonaut.optonaut.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import co.optonaut.optonaut.util.Maths;

/**
 * @author Nilan Marktanner
 * @date 2015-12-26
 */
public class RotationVectorListener extends RotationMatrixProvider {
    private float[] rotationMatrix;

    public RotationVectorListener() {
        rotationMatrix = Maths.getIdentity(4);
    }

    public void handleSensorEvent(SensorEvent event) {
        // It is good practice to check that we received the proper sensor event
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
        {
            // Convert the rotation-vector to a 4x4 matrix.
            float[] rotationMatrixFromVector = new float[16];
            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, event.values);
            SensorManager.remapCoordinateSystem(rotationMatrixFromVector,
                    SensorManager.AXIS_X, SensorManager.AXIS_Z,
                    rotationMatrix);
        }
    }

    @Override
    public float[] getRotationMatrix() {
        return rotationMatrix;
    }
}
