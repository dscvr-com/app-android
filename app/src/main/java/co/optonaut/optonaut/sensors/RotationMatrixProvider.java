package co.optonaut.optonaut.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * @author Nilan Marktanner
 * @date 2015-12-26
 */
public abstract class RotationMatrixProvider {
    public abstract float[] getRotationMatrix();
}