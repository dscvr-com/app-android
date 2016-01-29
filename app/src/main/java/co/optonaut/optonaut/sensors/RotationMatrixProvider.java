package co.optonaut.optonaut.sensors;

import android.opengl.Matrix;

/**
 * @author Nilan Marktanner
 * @date 2015-12-26
 */
public abstract class RotationMatrixProvider {
    public abstract float[] getRotationMatrix();

    public float[] getRotationMatrixInverse() {
        float[] rotationMatrixInverse = new float[16];
        Matrix.invertM(rotationMatrixInverse, 0, getRotationMatrix(), 0);

        return rotationMatrixInverse;
    }
}