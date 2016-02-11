package co.optonaut.optonaut.opengl;

import android.graphics.Bitmap;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.util.Maths;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-01-12
 */
public class Cube {
    private CubeTextureSet cubeTextureSet;

    private boolean isInitialized;

    // PLANES
    public static final int FACES_PER_CUBE = 6;
    private Plane[] planes = new Plane[FACES_PER_CUBE];

    // TRANSFORMS
    private List<float[]> transforms = new ArrayList<>();

    // SCALE
    private static final int SIDE_LENGTH = 10;
    private static final float HALF_SIDE_LENGTH = SIDE_LENGTH / 2.0f;

    List<float[]> scales = new ArrayList<>();

    // TRANSLATIONS
    private static final float[] POSITION_AHEAD = {0, 0, HALF_SIDE_LENGTH};
    private static final float[] POSITION_RIGHT = {-HALF_SIDE_LENGTH, 0, 0};
    private static final float[] POSITION_BEHIND = {0, 0, -HALF_SIDE_LENGTH};
    private static final float[] POSITION_LEFT = {HALF_SIDE_LENGTH, 0, 0};
    private static final float[] POSITION_TOP = {0, HALF_SIDE_LENGTH, 0};
    private static final float[] POSITION_BOTTOM = {0, -HALF_SIDE_LENGTH, 0};

    List<float[]> translations = new ArrayList<>();

    // ROTATIONS
    private static final float[] ROTATION_AHEAD_FIRST = {90, 1, 0, 0};
    private static final float[] ROTATION_AHEAD_SECOND = {180, 0, 1, 0};

    private static final float[] ROTATION_RIGHT_FIRST = {-90, 0, 0, 1};
    private static final float[] ROTATION_RIGHT_SECOND = {90, 1, 0, 0};

    private static final float[] ROTATION_BEHIND_FIRST = {90, 1, 0, 0};
    private static final float[] ROTATION_BEHIND_SECOND = {0, 1, 0, 0};

    private static final float[] ROTATION_LEFT_FIRST = {90, 0, 0, 1};
    private static final float[] ROTATION_LEFT_SECOND = {90, 1, 0, 0};

    private static final float[] ROTATION_TOP_FIRST = {-90, 0, 1, 0};
    private static final float[] ROTATION_TOP_SECOND = {180, 0, 0, 1};

    private static final float[] ROTATION_BOTTOM_FIRST = {-90, 0, 1, 0};
    private static final float[] ROTATION_BOTTOM_SECOND = {0, 0, 0, 1};

    List<float[]> rotations = new ArrayList<>();

    // FACE CONSTANTS
    public static final int FACE_AHEAD  = 0;
    public static final int FACE_RIGHT  = 1;
    public static final int FACE_BEHIND = 2;
    public static final int FACE_LEFT   = 3;
    public static final int FACE_TOP    = 4;
    public static final int FACE_BOTTOM = 5;

    public static final int[] FACES = {
            FACE_AHEAD,
            FACE_RIGHT,
            FACE_BEHIND,
            FACE_LEFT,
            FACE_TOP,
            FACE_BOTTOM
    };

    private float[] cubeTransform = new float[16];

    public Cube() {
        this.cubeTextureSet = new CubeTextureSet();
        initializeTransforms();
        initializePlanes();
        Matrix.setIdentityM(cubeTransform, 0);
        isInitialized = false;
    }

    private void initializeTranslations() {
        translations.add(Maths.buildTranslationMatrix(POSITION_AHEAD));
        translations.add(Maths.buildTranslationMatrix(POSITION_RIGHT));
        translations.add(Maths.buildTranslationMatrix(POSITION_BEHIND));
        translations.add(Maths.buildTranslationMatrix(POSITION_LEFT));
        translations.add(Maths.buildTranslationMatrix(POSITION_TOP));
        translations.add(Maths.buildTranslationMatrix(POSITION_BOTTOM));
    }

    private void initializeRotations() {
        rotations.add(Maths.buildRotationMatrix(ROTATION_AHEAD_SECOND, ROTATION_AHEAD_FIRST));
        //rotations.add(buildRotationMatrix(new float[]{90, 0, 1, 0}));
        rotations.add(Maths.buildRotationMatrix(ROTATION_RIGHT_SECOND, ROTATION_RIGHT_FIRST));
        rotations.add(Maths.buildRotationMatrix(ROTATION_BEHIND_SECOND, ROTATION_BEHIND_FIRST));
        rotations.add(Maths.buildRotationMatrix(ROTATION_LEFT_SECOND, ROTATION_LEFT_FIRST));
        rotations.add(Maths.buildRotationMatrix(ROTATION_TOP_SECOND, ROTATION_TOP_FIRST));
        rotations.add(Maths.buildRotationMatrix(ROTATION_BOTTOM_SECOND, ROTATION_BOTTOM_FIRST));
    }

    private void initializeScales() {
        scales.add(Maths.buildScaleMatrix(SIDE_LENGTH));
        scales.add(Maths.buildScaleMatrix(SIDE_LENGTH));
        scales.add(Maths.buildScaleMatrix(SIDE_LENGTH));
        scales.add(Maths.buildScaleMatrix(SIDE_LENGTH));
        scales.add(Maths.buildScaleMatrix(SIDE_LENGTH));
        scales.add(Maths.buildScaleMatrix(SIDE_LENGTH));
    }

    private void initializeTransforms() {
        initializeTranslations();
        initializeRotations();
        initializeScales();

        for (int i = 0; i < FACES_PER_CUBE; ++i) {
            transforms.add(Maths.computeTransform(translations.get(i), rotations.get(i), scales.get(i)));
        }
    }

    private void initializePlanes() {
        for (int i = 0; i < FACES_PER_CUBE; ++i) {
            planes[i] = new Plane();
        }
    }

    public void initialize() {
        for (Plane plane : planes) {
            plane.initializeProgram();
        }
        isInitialized = true;
    };

    public void draw(float[] mvpMatrix) {
        if (!isInitialized)
            throw new RuntimeException("Cube not initialized!");
        float[] modelView = new float[16];
        float[] finalTransform = new float[16];
        for (int i = 0; i < FACES_PER_CUBE; ++i) {
            // transform cubeTransform with the transform specific to this plane
            Matrix.multiplyMM(finalTransform, 0, cubeTransform, 0, transforms.get(i), 0);
            Matrix.multiplyMM(modelView, 0, mvpMatrix, 0, finalTransform, 0);

            planes[i].draw(modelView);
        }
    }

    public TextureSet getCubeTextureSet() {
        return cubeTextureSet;
    }

    public void resetTextures() {
        this.cubeTextureSet.reset();
    }

    private class CubeTextureSet extends TextureSet {
        public CubeTextureSet() {
            super();
        }

        @Override
        public Bitmap getTexture(int index) {
            checkIndex(index);

            return textureTargets[index].getTexture();
        }

        @Override
        public void updateTexture(int index) {
            checkIndex(index);

            planes[index].updateTexture(getTexture(index));
        }

        @Override
        public int getTextureSetSize() {
            return FACES_PER_CUBE;
        }

        @Override
        public void reset() {
            Timber.i("Resetting planes");
            for (int i = 0; i < getTextureSetSize(); ++i) {
                planes[i].resetTexture();
            }
        }

        private void checkIndex(int face) {
            if (face >= FACES_PER_CUBE)
                throw new IllegalArgumentException("Illegal face index in texture set!");
        }
    }

    public float[] getCubeTransform() {
        return cubeTransform;
    }

    public void setCubeTransform(float[] cubeTransform) {
        this.cubeTransform = cubeTransform;
    }
}
