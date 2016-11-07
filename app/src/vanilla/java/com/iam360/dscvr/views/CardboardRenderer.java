package com.iam360.dscvr.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.view.View;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import javax.microedition.khronos.egl.EGLConfig;

import com.iam360.dscvr.R;
import com.iam360.dscvr.model.SendStoryChild;
import com.iam360.dscvr.model.StoryChild;
import com.iam360.dscvr.opengl.Cube;
import com.iam360.dscvr.opengl.PinMarker;
import com.iam360.dscvr.opengl.Sphere;
import com.iam360.dscvr.sensors.CombinedMotionManager;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private List<PinMarker> planes = new ArrayList<PinMarker>();
    private PinMarker plane;

    private static final float V_DISTANCE = 20f;

    private float[] unInverseRotationMatrix = new float[16];

    private static final float[] ROTATION_AHEAD_FIRST = {0, 1, 0, 0};
    private static final float[] ROTATION_AHEAD_SECOND = {0, 0, 1, 0};

    private Bitmap planeTexture;
    private float[] headView = new float[16];

    private Sphere sphere;
    private float sphereRadius = 30f;

    public CardboardRenderer(Context context) {

        this.plane = new PinMarker();
        this.plane.initializeProgram();
        this.plane.setInitRotation(Maths.buildRotationMatrix(ROTATION_AHEAD_SECOND, ROTATION_AHEAD_FIRST));
        planeTexture = BitmapFactory.decodeResource(context.getResources(), R.drawable.main_pin_icn);
        initializeCubes();

        sphere = new Sphere(5, sphereRadius);
        sphere.initializeProgram();
        setSpherePosition(2.0f, 1.0f, 2.0f);

    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Set the camera position
        Matrix.setLookAtM(camera, 0,
                0.0f, 0.0f, 0.0f, // eye
                0.0f, 0.0f, 0.01f, // center
                0.0f, 1.0f, 0.0f); // up

        headTransform.getHeadView(headView, 0);
    }

    @Override
    public void onDrawEye(Eye eye) {
        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

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

        /*************/

        Matrix.invertM(unInverseRotationMatrix, 0, eye.getEyeView(), 0);
        float[] vector = {0, 0, V_DISTANCE, 0};
        float[] newPosition = new float[4];
//        Matrix.multiplyMV(newPosition, 0, unInverseRotationMatrix, 0, vector, 0);

        float[] POS_MATRIX_MULTIPLY_VEC = {0, 0, 0, 1.0f};
        Matrix.multiplyMV(newPosition, 0, headView, 0, POS_MATRIX_MULTIPLY_VEC, 0);
        plane.setCenter(newPosition[0], newPosition[1], newPosition[2]);
        setSpherePosition(newPosition[0], newPosition[1], newPosition[2]);
        sphere.draw(modelViewProjection);

        /******************/
        float[] scaleRotationMatrix = new float[16];
        float[] newRotation = new float[16];
        float[] scales = Maths.buildScaleMatrix(2);
        float[] modelMatrix = new float[16];

        // draw pins
        for(int a=0; a< planes.size(); a++){
            if(planes.get(a).isInitiliazed()){
//                if(overlapSpheres(plane, planes.get(a))){
//                    Timber.d("OVERLAP YES");
//                    selectedPin = a;
//                    overlapChcker = true;
//                }
                float[] modelView3 = new float[16];
                float[] translationMatrix = planes.get(a).getTranslation();
                float phi2 = planes.get(a).getxRotation();
                float theta2 = planes.get(a).getyRotation();

                float[] rotationX2 = {(float) Math.toDegrees(theta2), 1, 0, 0};
                float[] rotationY2 = {(float) -Math.toDegrees(phi2), 0, 1, 0};
                float[] rotations = Maths.buildRotationMatrix(rotationY2, rotationX2);

                Matrix.multiplyMM(newRotation, 0, plane.getInitRotation(), 0, rotations, 0);
                Matrix.multiplyMM(scaleRotationMatrix, 0, translationMatrix, 0, scales, 0);
                Matrix.multiplyMM(modelMatrix, 0, scaleRotationMatrix, 0, newRotation, 0);

                Matrix.multiplyMM(modelView3, 0, modelViewProjection, 0, modelMatrix, 0);

                planes.get(a).draw(modelView3);

                boolean isLooking = isLookingAtObject(modelMatrix);
                Timber.d("LookingAt: " + isLooking);
            }
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

    private boolean isLookingAtObject(float[] rotations) {
        float[] POS_MATRIX_MULTIPLY_VEC = {0, 0, 0, 1.0f};
        final float YAW_LIMIT = 0.12f;
        final float PITCH_LIMIT = 0.12f;

        float[] headInverse  = new float[16];
        float[] rotationInverse  = new float[16];
        float[] modelView  = new float[16];
        float[] tempPosition  = new float[4];

        Matrix.invertM(rotationInverse, 0, rotations, 0);
        Matrix.invertM(headInverse, 0, headView, 0);
        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, headInverse, 0, rotations, 0);
        Matrix.multiplyMV(tempPosition, 0, modelView, 0, POS_MATRIX_MULTIPLY_VEC, 0);

        float pitch = (float) Math.atan2(tempPosition[1], -tempPosition[2]);
        float yaw = (float) Math.atan2(tempPosition[0], -tempPosition[2]);

        Timber.d("LOOKING : " + pitch + " " + yaw);
        return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
    }



    public static boolean overlapSpheres(PinMarker point, PinMarker marker) {

        float x, y, z;
        x = point.getCenter().x - marker.getCenter().x;
        y = point.getCenter().y - marker.getCenter().y;
        z = point.getCenter().z - marker.getCenter().z;

        double distance = Math.sqrt( x*x + y*y + z*z );

        Timber.d("Overlap : " + distance + " = " + point.getCenter().x + ":" + point.getCenter().y + ":" + point.getCenter().z + " " + marker.getCenter().x + ":" + marker.getCenter().y + ":" + marker.getCenter().z);
        return distance <= 1;
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        // projection is already handled by eyes
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        this.leftCube.initialize();
        this.rightCube.initialize();
        sphere.initializeProgram();

        for(int a=0; a< planes.size(); a++)
            planes.get(planes.size() - 1).initializeProgram();

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












    public void planeSetter(SendStoryChild chld) {

        Timber.d("PINMARKER planeSetter");
        planes.add(new PinMarker());
        planes.get(planes.size() - 1).setTranslation(Maths.buildTranslationMatrix(new float[]{Float.parseFloat(chld.getStory_object_position().get(0)), Float.parseFloat(chld.getStory_object_position().get(1)), Float.parseFloat(chld.getStory_object_position().get(2))}));
        planes.get(planes.size() - 1).setxRotation(Float.parseFloat(chld.getStory_object_rotation().get(0)));
        planes.get(planes.size() - 1).setyRotation(Float.parseFloat(chld.getStory_object_rotation().get(1)));
        planes.get(planes.size() - 1).setInitiliazed(true);
        planes.get(planes.size() - 1).setMediaType(chld.getStory_object_media_type());
        planes.get(planes.size() - 1).setMediaAdditionalData(chld.getStory_object_media_additional_data());
        planes.get(planes.size() - 1).setMediaDescription(chld.getStory_object_media_description());
        planes.get(planes.size() - 1).setMediaFace(chld.getStory_object_media_face());
        planes.get(planes.size() - 1).setObjectMediaFileurl(chld.getStory_object_media_fileurl());
        planes.get(planes.size() - 1).setObjectMediaFilename(chld.getStory_object_media_filename());
        planes.get(planes.size() - 1).setMarkerName(chld.getStory_object_media_type() + "-" + (planes.size() - 1));
        planes.get(planes.size() - 1).setCenter(Float.parseFloat(chld.getStory_object_position().get(0)), Float.parseFloat(chld.getStory_object_position().get(1)), Float.parseFloat(chld.getStory_object_position().get(2)));
        planes.get(planes.size() - 1).updateTexture(planeTexture);

        for(int a=0; a< planes.size(); a++) {
            if (!planes.get(a).isInitiliazed()) {
                planes.get(a).setInitiliazed(true);
                planes.get(a).setMediaType(chld.getStory_object_media_type());
                planes.get(a).setMediaAdditionalData(chld.getStory_object_media_additional_data());
                planes.get(a).setMediaDescription(chld.getStory_object_media_description());
                planes.get(a).setMediaFace(chld.getStory_object_media_face());
                planes.get(a).setObjectMediaFilename(chld.getStory_object_media_filename());
                planes.get(a).setObjectMediaFileurl(chld.getStory_object_media_fileurl());
                planes.get(a).setMarkerName(chld.getStory_object_media_type()+"-"+a);

                planes.get(a).setTranslation(Maths.buildTranslationMatrix(new float[]{Float.parseFloat(chld.getStory_object_position().get(0)),Float.parseFloat(chld.getStory_object_position().get(1)), Float.parseFloat(chld.getStory_object_position().get(2))}));
                planes.get(a).setCenter(Float.parseFloat(chld.getStory_object_position().get(0)),Float.parseFloat(chld.getStory_object_position().get(1)), Float.parseFloat(chld.getStory_object_position().get(2)));

                planes.get(a).setxRotation(Float.parseFloat(chld.getStory_object_rotation().get(0)));
                planes.get(a).setyRotation(Float.parseFloat(chld.getStory_object_rotation().get(1)));
                break;
            }
        }
    }

}
