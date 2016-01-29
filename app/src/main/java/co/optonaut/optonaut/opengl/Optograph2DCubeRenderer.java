package co.optonaut.optonaut.opengl;

import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import co.optonaut.optonaut.sensors.CombinedMotionManager;
import co.optonaut.optonaut.sensors.RotationVectorListener;
import co.optonaut.optonaut.sensors.TouchEventListener;
import co.optonaut.optonaut.util.Constants;

/**
 * @author Nilan Marktanner
 * @date 2016-01-17
 */
public class Optograph2DCubeRenderer implements GLSurfaceView.Renderer {
    private static final float FIELD_OF_VIEW_Y = 95.0f;
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 120.0f;

    private static final float DAMPING_FACTOR = 0.9f;

    private final float[] mvpMatrix = new float[16];
    private final float[] projection = new float[16];
    private final float[] camera = new float[16];
    private float[] rotationMatrix = new float[16];

    private CombinedMotionManager combinedMotionManager;

    private Cube cube;

    public Optograph2DCubeRenderer() {
        Log.v(Constants.DEBUG_TAG, "renderer constructor");
        this.cube = new Cube();
        this.combinedMotionManager = new CombinedMotionManager(DAMPING_FACTOR, Constants.getInstance().getDisplayMetrics().widthPixels, Constants.getInstance().getDisplayMetrics().heightPixels, FIELD_OF_VIEW_Y);
        Matrix.setIdentityM(rotationMatrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.v(Constants.DEBUG_TAG, "onSurfaceCreated");
        this.cube.initialize();

        // Set the camera position
        Matrix.setLookAtM(camera, 0,
                0.0f, 0.0f, 0.0f, // eye
                0.0f, 0.0f, 0.01f, // center
                0.0f, 1.0f, 0.0f); // up

        // Set the background frame color
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClearDepthf(1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.v(Constants.DEBUG_TAG, "onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        Matrix.perspectiveM(projection, 0, FIELD_OF_VIEW_Y, ratio, Z_NEAR, Z_FAR);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // rotate viewMatrix to allow for user-interaction
        float[] view = new float[16];
        rotationMatrix = combinedMotionManager.getRotationMatrixInverse();
        //Log.v(Constants.DEBUG_TAG, Arrays.toString(rotationMatrix));
        Matrix.multiplyMM(view, 0, camera, 0, rotationMatrix, 0);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, projection, 0, view, 0);

        // Draw shape
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        cube.draw(mvpMatrix);
    }

    public TextureSet.TextureTarget getTextureTarget(int face) {
        return this.cube.getCubeTextureSet().getTextureTarget(face);
    }

    public void reset() {
        this.cube.resetTextures();
    }

    public void touchStart(Point point) {
        combinedMotionManager.touchStart(point);
    }

    public void touchMove(Point point) {
        combinedMotionManager.touchMove(point);
    }

    public void touchEnd(Point point) {
        combinedMotionManager.touchEnd(point);
    }

    public void registerOnSensors() {
        combinedMotionManager.registerOnCoreMotionListener();
    }

    public void unregisterOnSensors() {
        combinedMotionManager.unregisterOnCoreMotionListener();
    }

    public boolean isRegisteredOnSensors() {
        return combinedMotionManager.isRegisteredOnCoreMotionListener();
    }

    public CombinedMotionManager getCombinedMotionManager() {
        return combinedMotionManager;
    }
}
