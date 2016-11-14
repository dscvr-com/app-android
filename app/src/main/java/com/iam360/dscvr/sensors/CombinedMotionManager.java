package com.iam360.dscvr.sensors;

import android.graphics.Point;
import android.opengl.Matrix;

import java.util.Arrays;

import com.iam360.dscvr.util.Maths;

/**
 * @author Nilan Marktanner
 * @date 2016-01-28
 */
public class CombinedMotionManager extends RotationMatrixProvider {
    private TouchEventListener touchEventListener;

    public static final int STILL_MODE = 0;
    public static final int PANNING_MODE = 1;
    public static final int GYRO_MODE = 2;

    private float[] lastCoreMotionMatrix = null;
    private boolean registeredOnCoreMotionListener;
    private int MODE = STILL_MODE;

    public CombinedMotionManager(float dampFactor, int sceneWidth, int sceneHeight, float vfov) {
        touchEventListener = new TouchEventListener(dampFactor, sceneWidth, sceneHeight, vfov);
    }

    public void touchStart(Point point) {
        touchEventListener.touchStart(point);
    }

    public void touchMove(Point point) {
        touchEventListener.touchMove(point);
    }

    public void touchEnd(Point point) {
        touchEventListener.touchEnd(point);
    }

    public void reset() {
        touchEventListener.reset();
    }

    public void setDirection() {
        // TODO: implement
    }

    @Override
    public float[] getRotationMatrix() {
        float[] coreMotionMatrix = DefaultListeners.getInstance().getRotationMatrix();

        if (!touchEventListener.isTouching() && MODE == GYRO_MODE) {
            // Update from motion and damping
            if (lastCoreMotionMatrix != null) {
                float[] inverse = Maths.buildInverse(lastCoreMotionMatrix);
                float[] diffRotationMatrix = new float[16];
                Matrix.multiplyMM(diffRotationMatrix, 0, inverse, 0, coreMotionMatrix, 0);

                // m21 equals [9] and m22 equals [10] for zero-based matrix indices
                float diffRotationTheta = (float) Math.atan2(diffRotationMatrix[9], diffRotationMatrix[10]);
                float diffRotationPhi = (float) Math.atan2(-diffRotationMatrix[8],
                        Math.sqrt(diffRotationMatrix[9] * diffRotationMatrix[9] +
                                diffRotationMatrix[10] * diffRotationMatrix[10]));

                touchEventListener.setPhi(touchEventListener.getPhi() + diffRotationPhi);
                touchEventListener.setTheta(touchEventListener.getTheta() - diffRotationTheta);
            }
        }

        if (coreMotionMatrix != null && MODE == PANNING_MODE) {
            touchEventListener.setPhi(touchEventListener.getPhi() + 0.003f);
           // lastCoreMotionMatrix = Arrays.copyOf(coreMotionMatrix, 16);
        } else if (coreMotionMatrix != null && MODE == GYRO_MODE) {
             lastCoreMotionMatrix = Arrays.copyOf(coreMotionMatrix, 16);
        }

        return touchEventListener.getRotationMatrix();
    }

    @Override
    public void getRotationMatrix(float[] target) {
        System.arraycopy(getRotationMatrix(), 0, target, 0, 16);
    }

    public void registerOnCoreMotionListener() {
        DefaultListeners.register();
        registeredOnCoreMotionListener = true;
    }

    public void unregisterOnCoreMotionListener() {
        DefaultListeners.unregister();
        registeredOnCoreMotionListener = false;
    }

    public boolean isRegisteredOnCoreMotionListener() {
        return registeredOnCoreMotionListener;
    }

    public boolean isTouching() {
        return touchEventListener.isTouching();
    }

    public void setMode(int mode) {
        this.MODE = mode;
    }
}
