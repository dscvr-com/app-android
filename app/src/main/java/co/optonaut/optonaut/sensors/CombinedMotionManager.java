package co.optonaut.optonaut.sensors;

import android.graphics.Point;
import android.opengl.Matrix;

import java.util.Arrays;

import co.optonaut.optonaut.util.Maths;

/**
 * @author Nilan Marktanner
 * @date 2016-01-28
 */
public class CombinedMotionManager extends RotationMatrixProvider {
    private TouchEventListener touchEventListener;

    private float[] lastCoreMotionMatrix = null;
    private boolean registeredOnCoreMotionListener;

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
        float[] coreMotionMatrix = CoreMotionListener.getInstance().getRotationMatrix();

        if (!touchEventListener.isTouching()) {
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

        if (coreMotionMatrix != null) {
            lastCoreMotionMatrix = Arrays.copyOf(coreMotionMatrix, 16);
        }

        return touchEventListener.getRotationMatrix();
    }

    public void registerOnCoreMotionListener() {
        CoreMotionListener.register();
        registeredOnCoreMotionListener = true;
    }

    public void unregisterOnCoreMotionListener() {
        CoreMotionListener.unregister();
        registeredOnCoreMotionListener = false;
    }

    public boolean isRegisteredOnCoreMotionListener() {
        return registeredOnCoreMotionListener;
    }

    public boolean isTouching() {
        return touchEventListener.isTouching();
    }
}
