package co.optonaut.optonaut.opengl;

import android.graphics.Bitmap;
import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.List;

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

    // POSITIONS
    private static final float[] POSITION_AHEAD = {0, 0, HALF_SIDE_LENGTH};
    private static final float[] POSITION_LEFT = {-HALF_SIDE_LENGTH, 0, 0};
    private static final float[] POSITION_BEHIND = {0, 0, -HALF_SIDE_LENGTH};
    private static final float[] POSITION_RIGHT = {HALF_SIDE_LENGTH, 0, 0};
    private static final float[] POSITION_TOP = {0, HALF_SIDE_LENGTH, 0};
    private static final float[] POSITION_BOTTOM = {0, -HALF_SIDE_LENGTH, 0};

    // ROTATIONS
    private static final float[] ROTATION_AHEAD = {90, 1, 0, 0};
    private static final float[] ROTATION_LEFT = {90, 0, 0, 1};
    private static final float[] ROTATION_BEHIND = {-90, 1, 0, 0};
    private static final float[] ROTATION_RIGHT = {-90, 0, 0, 1};
    private static final float[] ROTATION_TOP = {90, 0, 1, 0};
    private static final float[] ROTATION_BOTTOM = {-90, 0, 1, 0};

    // FACE CONSTANTS
    public static final int FACE_AHEAD  = 0;
    public static final int FACE_LEFT   = 1;
    public static final int FACE_BEHIND = 2;
    public static final int FACE_RIGHT  = 3;
    public static final int FACE_TOP    = 4;
    public static final int FACE_BOTTOM = 5;

    public static final int[] FACES = {
            FACE_AHEAD,
            FACE_LEFT,
            FACE_BEHIND,
            FACE_RIGHT,
            FACE_TOP,
            FACE_BOTTOM
    };

    public Cube() {
        this.cubeTextureSet = new CubeTextureSet();
        initializeTransforms();
        initializePlanes();
        isInitialized = false;
    }

    private void initializeTransforms() {
        transforms.add(computeTransform(POSITION_AHEAD, ROTATION_AHEAD, SIDE_LENGTH));
        transforms.add(computeTransform(POSITION_LEFT, ROTATION_LEFT, SIDE_LENGTH));
        transforms.add(computeTransform(POSITION_BEHIND, ROTATION_BEHIND, SIDE_LENGTH));
        transforms.add(computeTransform(POSITION_RIGHT, ROTATION_RIGHT, SIDE_LENGTH));
        transforms.add(computeTransform(POSITION_TOP, ROTATION_TOP, SIDE_LENGTH));
        transforms.add(computeTransform(POSITION_BOTTOM, ROTATION_BOTTOM, SIDE_LENGTH));
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
        for (int i = 0; i < FACES_PER_CUBE; ++i) {
            // transform mvpMatrix with the transform specific to this plane
            float[] modelView = new float[16];
            Matrix.multiplyMM(modelView, 0, mvpMatrix, 0, transforms.get(i), 0);
            planes[i].draw(modelView);
        }
    }

    private float[] computeTransform(float[] translation, float[] rotation, float scale) {
        // build scale matrix S
        float[] scaleMatrix = new float[16];
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, scale, scale, scale);

        // build rotation matrix
        float[] rotationMatrix = new float[16];
        Matrix.setRotateM(rotationMatrix, 0, rotation[0], rotation[1], rotation[2], rotation[3]);

        // R*S
        float[] rsMatrix = new float[16];
        Matrix.multiplyMM(rsMatrix, 0, rotationMatrix, 0, scaleMatrix, 0);

        // build translation matrix
        float[] translationMatrix = new float[16];
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.translateM(translationMatrix, 0, translation[0], translation[1], translation[2]);

        // T*R*S
        float[] trsMatrix = new float[16];
        Matrix.multiplyMM(trsMatrix, 0, translationMatrix, 0, rsMatrix, 0);

        return trsMatrix;
    }

    public TextureSet getCubeTextureSet() {
        return cubeTextureSet;
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

            planes[index].loadGLTexture(getTexture(index));
        }

        @Override
        public int getTextureSetSize() {
            return FACES_PER_CUBE;
        }

        private void checkIndex(int face) {
            if (face >= FACES_PER_CUBE)
                throw new IllegalArgumentException("Illegal face index in texture set!");
        }
    }
}
