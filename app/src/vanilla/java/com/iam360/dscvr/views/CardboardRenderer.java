package com.iam360.dscvr.views;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.iam360.dscvr.R;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.SendStory;
import com.iam360.dscvr.model.SendStoryChild;
import com.iam360.dscvr.opengl.Cube;
import com.iam360.dscvr.opengl.PinMarker;
import com.iam360.dscvr.opengl.Sphere;
import com.iam360.dscvr.opengl.TextureSet;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.DBHelper;
import com.iam360.dscvr.util.ImageUrlBuilder;
import com.iam360.dscvr.util.Maths;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;

import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-01-08
 */
public class CardboardRenderer implements CardboardView.StereoRenderer {
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 120.0f;

    private final float[] modelViewProjection = new float[16];
    private final float[] view = new float[16];
    private float[] camera = new float[16];

    private Cube leftCube;
    private Cube rightCube;

    private static final float V_DISTANCE = 20f;

    private static final float[] ROTATION_AHEAD_FIRST = {0, 1, 0, 0};
    private static final float[] ROTATION_AHEAD_SECOND = {0, 0, 1, 0};

    private Bitmap planeTexture;
    private float[] headView = new float[16];
    private float[] headTransFormEulersAngles = {0, 0, 0};
    private float[] headTransFormQuaternion = {0, 0, 0, 0};
    private Sphere sphere;
    private float sphereRadius = 20f;

    private List<Optograph> optographs = new ArrayList<Optograph>();
    private List<String> cacheStories = new ArrayList<String>();
    private Optograph originalOpto;
    private boolean storyPageOriginal = true;

    private CountDownTimer countDownTimer;
    private int loaderTimer = 4;

    private int halfWidthScrn;
    private Context context;

    public CardboardRenderer(Context context) {
        this.context = context;

        initializeCubes();
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        this.leftCube.initialize();
        this.rightCube.initialize();

        sphere = new Sphere(5, sphereRadius);
        setSpherePosition(2.0f, 1.0f, 2.0f);
        sphere.initializeProgram();
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}

    @Override
    public void onSurfaceChanged(int width, int height) {
        // projection is already handled by eyes
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Set the camera position
        Matrix.setLookAtM(camera, 0,
                0.0f, 0.0f, 0.0f, // eye
                0.0f, 0.0f, 0.01f, // center
                0.0f, 1.0f, 0.0f); // up

        headTransform.getHeadView(headView, 0);
        headTransform.getQuaternion(headTransFormQuaternion, 0);
        headTransform.getEulerAngles(headTransFormEulersAngles, 0);
    }

    @Override
    public void onDrawEye(Eye eye) {
        // Apply the eye transformation to the camera.
        float[] inverseEyeMatrix = new float[16];
        Matrix.invertM(inverseEyeMatrix, 0, eye.getEyeView(), 0);

        Matrix.multiplyMM(view, 0, camera, 0, inverseEyeMatrix, 0);
//        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, view, 0);
        /****************/

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        if (eye.getType() == Eye.Type.LEFT) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            this.leftCube.draw(modelViewProjection);
        } else if (eye.getType() == Eye.Type.RIGHT) {
            // Set the background frame color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            this.rightCube.draw(modelViewProjection);
        }

    }

    public void setSpherePosition(float x, float y, float z) {
        sphere.setTransform(new float[]{
                0.01f, 0, 0, 0,
                0, 0.01f, 0, 0,
                0, 0, 0.01f, 0,
                x, y, z, 1
        });
    }



    private void initializeCubes() {
        this.leftCube = new Cube();
        this.rightCube = new Cube();
    }

    @Override
    public void onRendererShutdown() {
        // do nothing
    }

    public Cube getRightCube() {
        return rightCube;
    }

    public Cube getLeftCube() {
        return leftCube;
    }

    public void setOriginalOpto(Optograph opto) {
        this.originalOpto = opto;
    }

    public void setHalfWidthScrn(int halfWidthScrn) {
        this.halfWidthScrn = halfWidthScrn;
    }
}
