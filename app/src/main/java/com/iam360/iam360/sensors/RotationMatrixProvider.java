package com.iam360.iam360.sensors;

import android.opengl.Matrix;

/**
 * @author Nilan Marktanner
 * @date 2015-12-26
 */
public abstract class RotationMatrixProvider {
    public abstract void getRotationMatrix(float[] target);

    public void getRotationMatrixInverse(float[] target) {
        float[] r = new float[16];
        getRotationMatrix(r);
        Matrix.invertM(target, 0, r, 0);
    }

    public float[] getRotationMatrix() {
        float[] r = new float[16];
        getRotationMatrix(r);
        return r;
    }

    public float[] getRotationMatrixInverse() {
        float[] r = new float[16];
        getRotationMatrixInverse(r);
        return r;
    }
}