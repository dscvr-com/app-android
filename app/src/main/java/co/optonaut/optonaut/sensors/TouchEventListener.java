package co.optonaut.optonaut.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.opengl.Matrix;

import co.optonaut.optonaut.util.Maths;

/**
 * @author Nilan Marktanner
 * @date 2015-12-26
 */
public class TouchEventListener extends RotationMatrixProvider {
    private float[] rotationCurrent = new float[16];

    public TouchEventListener() {
        Matrix.setIdentityM(rotationCurrent, 0);
    }

    @Override
    public float[] getRotationMatrix() {
        return rotationCurrent;
    }
}
