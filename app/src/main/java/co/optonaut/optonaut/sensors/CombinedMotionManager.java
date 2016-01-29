package co.optonaut.optonaut.sensors;

import android.graphics.Point;
import android.opengl.Matrix;

import co.optonaut.optonaut.util.Maths;

/**
 * @author Nilan Marktanner
 * @date 2016-01-28
 */
public class CombinedMotionManager extends RotationMatrixProvider {
    private RotationVectorListener rotationVectorListener;
    private TouchEventListener touchEventListener;

    private float[] lastRotationVectorMatrix = new float[16];

    public CombinedMotionManager(float dampFactor, int sceneWidth, int sceneHeight, float vfov) {
        rotationVectorListener = new RotationVectorListener();
        touchEventListener = new TouchEventListener(dampFactor, sceneWidth, sceneHeight, vfov);

        Matrix.setIdentityM(lastRotationVectorMatrix, 0);
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
        float[] rotationVectorMatrix = rotationVectorListener.getRotationMatrix();

        if (!touchEventListener.isTouching()) {
            // Update from motion and damping
            float[] inverse = Maths.buildInverse(lastRotationVectorMatrix);
            float[] diffRotationMatrix = new float[16];
            Matrix.multiplyMM(diffRotationMatrix, 0, inverse, 0, rotationVectorMatrix, 0);

            // diffRotationMatrix.m21 == [10] and diffRotationMatrix.m22 [11] for zero-based matrix indices
            float diffRotationTheta = (float) Math.atan2(diffRotationMatrix[10], diffRotationMatrix[11]);
            float diffRotationPhi = (float) Math.atan2(-diffRotationMatrix[9],
                                                       Math.sqrt(diffRotationMatrix[10] * diffRotationMatrix[10] +
                                                               diffRotationMatrix[11] * diffRotationMatrix[11]));

            touchEventListener.setPhi(touchEventListener.getPhi() + diffRotationPhi);
            touchEventListener.setTheta(touchEventListener.getTheta() + diffRotationTheta);
        }

        lastRotationVectorMatrix = rotationVectorMatrix;

        return touchEventListener.getRotationMatrix();
    }
}
