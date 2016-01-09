package co.optonaut.optonaut.views;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import javax.microedition.khronos.egl.EGLConfig;

import co.optonaut.optonaut.opengl.Sphere;
import co.optonaut.optonaut.util.Constants;

/**
 * @author Nilan Marktanner
 * @date 2016-01-08
 */
public class CardboardRendererTest implements CardboardView.StereoRenderer {
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private static final float CAMERA_Z = 0.01f;
    private static final float TIME_DELTA = 0.3f;

    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.12f;

    private static final int COORDS_PER_VERTEX = 3;

    // We keep the light always position just above the user.
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] { 0.0f, 2.0f, 0.0f, 1.0f };

    private final float[] lightPosInEyeSpace = new float[4];

    private int cubeProgram;
    private int floorProgram;

    private int cubePositionParam;
    private int cubeNormalParam;
    private int cubeColorParam;
    private int cubeModelParam;
    private int cubeModelViewParam;
    private int cubeModelViewProjectionParam;
    private int cubeLightPosParam;

    private int floorPositionParam;
    private int floorNormalParam;
    private int floorColorParam;
    private int floorModelParam;
    private int floorModelViewParam;
    private int floorModelViewProjectionParam;
    private int floorLightPosParam;

    private float[] modelCube;
    private float[] camera;
    private float[] view;
    private float[] headView;
    private float[] modelViewProjection;
    private float[] modelView;
    private float[] modelFloor;

    private int score = 0;
    private float objectDistance = 12f;
    private float floorDepth = 20f;

    public CardboardRendererTest() {
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        headTransform.getHeadView(headView, 0);
        checkGLError("onNewFrame");
    }

    @Override
    public void onDrawEye(Eye eye) {
        //GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glColorMask(true, true, true, true);
        GLES20.glDepthMask(true);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        checkGLError("onDrawEye");
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
        checkGLError("onFinishFrame");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        // projection is already handled by eyes
        checkGLError("onSurfaceChanged");
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 0.5f);
        checkGLError("clearColor");

        checkGLError("onSurfaceCreated");
    }

    @Override
    public void onRendererShutdown() {
        // do nothing
        checkGLError("onRendererShutdown");
    }

    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     *
     * @param label Label to report in case of error.
     */
    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(Constants.DEBUG_TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }
}
