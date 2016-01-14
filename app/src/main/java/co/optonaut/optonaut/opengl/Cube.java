package co.optonaut.optonaut.opengl;

import android.graphics.Bitmap;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.optonaut.optonaut.util.Constants;

/**
 * @author Nilan Marktanner
 * @date 2016-01-12
 */
public class Cube {
    private static final int PLANES_PER_CUBE = 6;

    private List<float[]> transforms = new ArrayList<>();
    private Plane[] planes = new Plane[PLANES_PER_CUBE];

    // SCALE
    private static final int SIDE_LENGTH = 10;

    private static final float HALF_SIDE_LENGTH = SIDE_LENGTH / 2.0f;
    // POSITIONS
    private static final float[] POSITION_AHEAD = {0, 0, HALF_SIDE_LENGTH};

    private static final float[] POSITION_BEHIND = {0, 0, -HALF_SIDE_LENGTH};
    private static final float[] POSITION_RIGHT = {HALF_SIDE_LENGTH, 0, 0};

    private static final float[] POSITION_LEFT = {-HALF_SIDE_LENGTH, 0, 0};
    private static final float[] POSITION_TOP = {0, HALF_SIDE_LENGTH, 0};

    private static final float[] POSITION_BOTTOM = {0, -HALF_SIDE_LENGTH, 0};
    // ROTATIONS
    private static final float[] ROTATION_AHEAD = {90, 1, 0, 0};

    private static final float[] ROTATION_BEHIND = {-90, 1, 0, 0};
    private static final float[] ROTATION_RIGHT = {-90, 0, 0, 1};

    private static final float[] ROTATION_LEFT = {90, 0, 0, 1};
    private static final float[] ROTATION_TOP = {90, 0, 1, 0};

    private static final float[] ROTATION_BOTTOM = {-90, 0, 1, 0};

    public Cube(Bitmap texture) {
        initializeTransforms();
        initializePlanes(texture);
    }

    private void initializeTransforms() {
        transforms.add(computeTransform(POSITION_AHEAD, ROTATION_AHEAD, SIDE_LENGTH));
        transforms.add(computeTransform(POSITION_BEHIND, ROTATION_BEHIND, SIDE_LENGTH));

        transforms.add(computeTransform(POSITION_LEFT, ROTATION_LEFT, SIDE_LENGTH));
        transforms.add(computeTransform(POSITION_RIGHT, ROTATION_RIGHT, SIDE_LENGTH));

        transforms.add(computeTransform(POSITION_TOP, ROTATION_TOP, SIDE_LENGTH));
        transforms.add(computeTransform(POSITION_BOTTOM, ROTATION_BOTTOM, SIDE_LENGTH));
    }

    private void initializePlanes(Bitmap texture) {
        for (int i = 0; i < PLANES_PER_CUBE; ++i) {
            planes[i] = new Plane(texture);
        }
    }

    public void draw(float[] mvpMatrix) {
        for (int i = 0; i < PLANES_PER_CUBE; ++i) {
            float[] modelView = new float[16];
            Matrix.multiplyMM(modelView, 0, mvpMatrix, 0, transforms.get(i), 0);
            planes[i].draw(modelView);
        }
    }

    private float[] computeTransform(float[] translation, float[] rotation, float scale) {
        // scale, rotate, translate
        float[] scaleMatrix = new float[16];
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, scale, scale, scale);
        Log.d(Constants.DEBUG_TAG, "scale: " + Arrays.toString(scaleMatrix));

        float[] rotationMatrix = new float[16];
        Matrix.setRotateM(rotationMatrix, 0, rotation[0], rotation[1], rotation[2], rotation[3]);
        Log.d(Constants.DEBUG_TAG, "rotation: " + Arrays.toString(rotationMatrix));

        float[] rsMatrix = new float[16];
        Matrix.multiplyMM(rsMatrix, 0, rotationMatrix, 0, scaleMatrix, 0);
        Log.d(Constants.DEBUG_TAG, "rs: " + Arrays.toString(rsMatrix));

        float[] translationMatrix = new float[16];
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.translateM(translationMatrix, 0, translation[0], translation[1], translation[2]);

        float[] trsMatrix = new float[16];
        Matrix.multiplyMM(trsMatrix, 0, translationMatrix, 0, rsMatrix, 0);

        return trsMatrix;
    }

}
