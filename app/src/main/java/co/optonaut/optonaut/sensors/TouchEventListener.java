package co.optonaut.optonaut.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import co.optonaut.optonaut.util.Maths;

/**
 * @author Nilan Marktanner
 * @date 2015-12-26
 */
public class TouchEventListener extends RotationMatrixProvider {
    private float[] rotationCurrent;

    public TouchEventListener() {
        rotationCurrent = Maths.getIdentity(3);
    }

    @Override
    public float[] getRotationMatrix() {
        return rotationCurrent;
    }
}
