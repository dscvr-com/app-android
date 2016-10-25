package com.iam360.dscvr.opengl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iam360.dscvr.R;
import com.iam360.dscvr.model.SendStory;
import com.iam360.dscvr.model.SendStoryChild;
import com.iam360.dscvr.sensors.CombinedMotionManager;
import com.iam360.dscvr.sensors.TouchEventListener;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-01-17
 */
public class Optograph2DCubeRenderer implements GLSurfaceView.Renderer {
    private static final float FIELD_OF_VIEW_Y = 95.0f;
    private static final float FIELD_OF_VIEW_Y_ZOOM = 70.0f;
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 120.0f;

    private static final float V_DISTANCE = 20f;
    private float scaleFactor = 1.f;
    private float ratio = 1.f;

    private static final float DAMPING_FACTOR = 0.9f;

    private final float[] mvpMatrix = new float[16];

    private final float[] projection = new float[16];
    private final float[] camera = new float[16];
    private float[] rotationMatrix = new float[16];

    private float[] unInverseRotationMatrix = new float[16];
    private float sphereRadius = 30f;
    private CombinedMotionManager combinedMotionManager;

    private Cube cube;
    private String optoType;
    private TextView bubbleText;
    private LinearLayout bubbleTextLayout;
    private Activity act;

    //    private MarkerNode sphere;
    private List<PinMarker> planes = new ArrayList<PinMarker>();
    private boolean planesInit = false;
    private Sphere sphere;
    private PinMarker plane;

    private Context context;

    private static final float[] ROTATION_AHEAD_FIRST = {0, 1, 0, 0};
    private static final float[] ROTATION_AHEAD_SECOND = {0, 0, 1, 0};

    private Bitmap planeTexture;
    private Bitmap planeTexture2;

    private boolean markerShown = false;
    private int storyType = 0; //0=creator; 1=viewer; 2=edit
    private boolean withStory = false;
    private SendStory myStory = new SendStory();
    private List<SendStoryChild> myStoryChld = new ArrayList<SendStoryChild>();
    public boolean overlapChcker = false;
    private int selectedPin = 0;
    private boolean surfaceCreated = false;


    public Optograph2DCubeRenderer(Context context) {
        Timber.v("cube renderer constructor");
        this.context = context;
        this.cube = new Cube();
        this.plane = new PinMarker();
        this.plane.setInitRotation(Maths.buildRotationMatrix(ROTATION_AHEAD_SECOND, ROTATION_AHEAD_FIRST));
        this.combinedMotionManager = new CombinedMotionManager(DAMPING_FACTOR, Constants.getInstance().getDisplayMetrics().widthPixels, Constants.getInstance().getDisplayMetrics().heightPixels, FIELD_OF_VIEW_Y);

        Matrix.setIdentityM(rotationMatrix, 0);

        myStory.setChildren(myStoryChld);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Timber.v("onSurfaceCreated");
        // Create the GLText

        surfaceCreated = true;

        this.cube.initialize();
        this.plane.initializeProgram();

        planeTexture = BitmapFactory.decodeResource(context.getResources(), R.drawable.main_pin_icn);
        planeTexture2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.close_x_icn);

        this.plane.updateTexture(planeTexture);

        sphere = new Sphere(5, sphereRadius);
        sphere.initializeProgram();
        setSpherePosition(2.0f, 1.0f, 2.0f);

        planeInitializer();

        // Set the camera position
        Matrix.setLookAtM(camera, 0,
                0.0f, 0.0f, 0.0f, // eye; location
                0.0f, 0.0f, 0.01f, // center; looking toward center
                0.0f, 1.0f, 0.0f); // up; pointing along Y-axis

        // Set the background frame color
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClearDepthf(1.0f);

    }

    private void planeInitializer(){
        planesInit = true;
        for(int a=0; a < 20; a++){
//            Log.d("MARK","planeInitializer = "+planes.get(a));
//            if (planes.get(a) == null) {
                planes.add(new PinMarker());
//            }
            planes.get(a).initializeProgram();
            planes.get(a).updateTexture(planeTexture);
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

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Timber.v("onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
        ratio = (float) width / height;

        Matrix.perspectiveM(projection, 0, FIELD_OF_VIEW_Y, ratio, Z_NEAR, Z_FAR);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        TouchEventListener touchEventListener = combinedMotionManager.getTouchEventListener();

        unInverseRotationMatrix = combinedMotionManager.getRotationMatrix();
        float[] vector = {0, 0, V_DISTANCE, 0};
        float[] newPosition = new float[4];
        Matrix.multiplyMV(newPosition, 0, unInverseRotationMatrix, 0, vector, 0);

//        newPosition = new float[]{-8.253604f, -0.82867014f, 18.198664f, 0f};

        // rotate viewMatrix to allow for user-interaction
        float[] view = new float[16];

        rotationMatrix = combinedMotionManager.getRotationMatrixInverse();
        Matrix.multiplyMM(view, 0, camera, 0, rotationMatrix, 0);

        // zoom single ring optographs to remove blur
        if (optoType!=null && optoType.equals("optograph_1")) {
            Matrix.perspectiveM(projection, 0, FIELD_OF_VIEW_Y_ZOOM / scaleFactor, ratio, Z_NEAR, Z_FAR);
        } else {
            Matrix.perspectiveM(projection, 0, FIELD_OF_VIEW_Y / scaleFactor, ratio, Z_NEAR, Z_FAR);
        }

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, projection, 0, view, 0);

        setSpherePosition(newPosition[0], newPosition[1], newPosition[2]);

        // Draw shape
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        cube.draw(mvpMatrix);

        plane.setCenter(newPosition[0], newPosition[1], newPosition[2]);

        float[] modelView = new float[16];
        float[] modelView2 = new float[16];

        float[] translationMatrix = Maths.buildTranslationMatrix(new float[]{newPosition[0], newPosition[1], newPosition[2]});
//        float[] rotations = combinedMotionManager.getRotationMatrix();

//        float phi = 0.42578393f;
        float phi = combinedMotionManager.getTouchEventListener().getPhi();
//        float theta = 0.041445374f;
        float theta = combinedMotionManager.getTouchEventListener().getTheta();
        float[] rotationX = {(float) Math.toDegrees(theta), 1, 0, 0};
        float[] rotationY = {(float) -Math.toDegrees(phi), 0, 1, 0};
        float[] rotations = Maths.buildRotationMatrix(rotationY, rotationX);

        float[] scales = Maths.buildScaleMatrix(2);
        float[] scales2 = Maths.buildScaleMatrix(1);

        float[] modelMatrix = new float[16];
        float[] modelMatrix2 = new float[16];

        float[] scaleRotationMatrix = new float[16];
        float[] scaleRotationMatrix2 = new float[16];

        float[] newRotation = new float[16];


        Matrix.multiplyMM(newRotation, 0, plane.getInitRotation(), 0, rotations, 0);
        Matrix.multiplyMM(scaleRotationMatrix, 0, translationMatrix, 0, scales, 0);
        Matrix.multiplyMM(scaleRotationMatrix2, 0, translationMatrix, 0, scales2, 0);

        Matrix.multiplyMM(modelMatrix, 0, scaleRotationMatrix, 0, newRotation, 0);
        Matrix.multiplyMM(modelMatrix2, 0, scaleRotationMatrix2, 0, newRotation, 0);


        Matrix.multiplyMM(modelView, 0, mvpMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(modelView2, 0, mvpMatrix, 0, modelMatrix2, 0);

        plane.setxRotation(combinedMotionManager.getTouchEventListener().getPhi());
        plane.setyRotation(combinedMotionManager.getTouchEventListener().getTheta());

        plane.setTranslation(translationMatrix);

        overlapChcker = false;
        for(int a=0; a< planes.size(); a++){
            if(planes.get(a).isInitiliazed()){
                Log.d("MARK","renderer inits");
                if(overlapSpheres(plane, planes.get(a))){
                    selectedPin = a;
                    overlapChcker = true;
                    Log.d("MARK","plane intersection");
                }
                float[] modelView3 = new float[16];
                translationMatrix = planes.get(a).getTranslation();
                float phi2 = planes.get(a).getxRotation();
                float theta2 = planes.get(a).getyRotation();
                Log.d("MARK","renderer xRot = "+phi2);
                Log.d("MARK","renderer yRot = "+theta2);

                float[] rotationX2 = {(float) Math.toDegrees(theta2), 1, 0, 0};
                float[] rotationY2 = {(float) -Math.toDegrees(phi2), 0, 1, 0};
                rotations = Maths.buildRotationMatrix(rotationY2, rotationX2);
                Log.d("MARK","renderer rotations = "+ Arrays.toString(rotations));

                Matrix.multiplyMM(newRotation, 0, plane.getInitRotation(), 0, rotations, 0);
                Matrix.multiplyMM(scaleRotationMatrix, 0, translationMatrix, 0, scales, 0);
                Matrix.multiplyMM(modelMatrix, 0, scaleRotationMatrix, 0, newRotation, 0);

                Matrix.multiplyMM(modelView3, 0, mvpMatrix, 0, modelMatrix, 0);

                planes.get(a).draw(modelView3);
            }
        }
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bubbleTextLayout.setVisibility(View.GONE);
            }
        });

        if(markerShown){
            if(overlapChcker && storyType == 0) {
                plane.updateTexture(planeTexture2);
                plane.draw(modelView2);
            }else if(overlapChcker && storyType == 1 && planes.get(selectedPin).getMediaType().equals("TXT")){
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bubbleText.setText(planes.get(selectedPin).getMediaAdditionalData());
                        bubbleTextLayout.setVisibility(View.VISIBLE);
                    }
                });
                sphere.draw(mvpMatrix);
            }else {
                sphere.draw(mvpMatrix);
            }
        }
//        plane.draw(modelView);
    }

    public TextureSet.TextureTarget getTextureTarget(int face) {
        return this.cube.getCubeTextureSet().getTextureTarget(face);
    }

    public void setMode(int mode) { Log.v("mcandres", "cube renderer"); combinedMotionManager.setMode(mode);}

    public void reset() {
        this.cube.resetTextures();
    }

    public void reset(String type) {
        this.optoType = type;
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

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public void setType(String type) {
        this.optoType = type;
    }

    public void addMarker(SendStoryChild chld)  {
        Log.d("addMarker","planes.size = "+planes.size());

        if(planes.size() > 0){
            if(overlapSpheres(plane, planes.get(planes.size() - 1))){
                return;
            }
        }


//        planes.add(new PinMarker());
////        planes.get(planes.size() - 1).updateTexture(planeTexture);
//        planes.get(planes.size() - 1).setTranslation(plane.getTranslation());
//        planes.get(planes.size() - 1).setxRotation(plane.getxRotation());
//        planes.get(planes.size() - 1).setyRotation(plane.getyRotation());
//        planes.get(planes.size() - 1).setInitiliazed(true);
//        planes.get(planes.size() - 1).setMediaType(chld.getStory_object_media_type());
//        planes.get(planes.size() - 1).setMarkerName(chld.getStory_object_media_type()+"-"+(planes.size() - 1));
//        planes.get(planes.size() - 1).setCenter(plane.getCenter().x, plane.getCenter().y, plane.getCenter().z);
//        chld.setStory_object_position( Arrays.asList(Float.toString(plane.getCenter().x), Float.toString(plane.getCenter().y), Float.toString(plane.getCenter().z)));
//        chld.setStory_object_rotation( Arrays.asList(Float.toString(plane.getxRotation()), Float.toString(plane.getyRotation()), "0"));
//
//        myStoryChld.add(chld);


        for(int a=0; a< planes.size(); a++) {
            if (!planes.get(a).isInitiliazed()) {
                chld.setStory_object_position( Arrays.asList(Float.toString(plane.getCenter().x), Float.toString(plane.getCenter().y), Float.toString(plane.getCenter().z)));
                chld.setStory_object_rotation( Arrays.asList(Float.toString(plane.getxRotation()), Float.toString(plane.getyRotation()), "0"));
                myStoryChld.add(chld);

                Log.d("MARK","addMarker xRot = "+plane.getxRotation());
                Log.d("MARK","addMarker yRot = "+plane.getyRotation());

                float[] rotationX2 = {(float) Math.toDegrees(plane.getyRotation()), 1, 0, 0};
                float[] rotationY2 = {(float) -Math.toDegrees(plane.getxRotation()), 0, 1, 0};
                float[] rotations = Maths.buildRotationMatrix(rotationY2, rotationX2);
                Log.d("MARK","addMarker rotations = "+ Arrays.toString(rotations));


                planes.get(a).setTranslation(plane.getTranslation());
                planes.get(a).setxRotation(plane.getxRotation());
                planes.get(a).setyRotation(plane.getyRotation());
                planes.get(a).setInitiliazed(true);
                planes.get(a).setMediaType(chld.getStory_object_media_type());
                planes.get(a).setMediaAdditionalData(chld.getStory_object_media_additional_data());
                planes.get(a).setMediaDescription(chld.getStory_object_media_description());
                planes.get(a).setMediaFace(chld.getStory_object_media_face());
                planes.get(a).setObjectMediaFilename(chld.getStory_object_media_filename());
                planes.get(a).setObjectMediaFileurl(chld.getStory_object_media_fileurl());
                planes.get(a).setMarkerName(chld.getStory_object_media_type()+"-"+a);
                planes.get(a).setCenter(plane.getCenter().x, plane.getCenter().y, plane.getCenter().z);
                break;
            }else{
                if(overlapSpheres(plane, planes.get(a))){
                    return;
                }
            }
        }
    }

    public void addStoryChildren(SendStoryChild chld)  {
        if(overlapChcker){
            removePin(selectedPin);
            return;
        }
        if(chld.getStory_object_media_face().equals("pin")){
            addMarker(chld);
        }else{
            chld.setStory_object_position( Arrays.asList("0", "0", "0"));
            chld.setStory_object_rotation( Arrays.asList("0", "0", "0"));
        }
    }

    public void planeSetter(SendStoryChild chld) {
        markerShown = true;
        storyType = 1;
        Log.d("MARK","planeSetter size = "+planes.size());

        planes.add(new PinMarker());
        planes.get(planes.size() - 1).setTranslation(Maths.buildTranslationMatrix(new float[]{Float.parseFloat(chld.getStory_object_position().get(0)),Float.parseFloat(chld.getStory_object_position().get(1)), Float.parseFloat(chld.getStory_object_position().get(2))}));
        planes.get(planes.size() - 1).setxRotation(Float.parseFloat(chld.getStory_object_rotation().get(0)));
        planes.get(planes.size() - 1).setyRotation(Float.parseFloat(chld.getStory_object_rotation().get(1)));
        planes.get(planes.size() - 1).setInitiliazed(true);
        planes.get(planes.size() - 1).setMediaType(chld.getStory_object_media_type());
        planes.get(planes.size() - 1).setMediaAdditionalData(chld.getStory_object_media_additional_data());
        planes.get(planes.size() - 1).setMediaDescription(chld.getStory_object_media_description());
        planes.get(planes.size() - 1).setMediaFace(chld.getStory_object_media_face());
        planes.get(planes.size() - 1).setObjectMediaFileurl(chld.getStory_object_media_fileurl());
        planes.get(planes.size() - 1).setObjectMediaFilename(chld.getStory_object_media_filename());
        planes.get(planes.size() - 1).setMarkerName(chld.getStory_object_media_type()+"-"+(planes.size() - 1));
        planes.get(planes.size() - 1).setCenter(Float.parseFloat(chld.getStory_object_position().get(0)),Float.parseFloat(chld.getStory_object_position().get(1)), Float.parseFloat(chld.getStory_object_position().get(2)));

//
        for(int a=0; a< planes.size(); a++) {
            Log.d("MARK","planeSetter is init? = "+planes.get(a).isInitiliazed());
            if (!planes.get(a).isInitiliazed()) {
                Log.d("MARK","planeSetter2 x = "+chld.getStory_object_position().get(0));
                Log.d("MARK","planeSetter2 y = "+chld.getStory_object_position().get(1));
                Log.d("MARK","planeSetter2 z = "+chld.getStory_object_position().get(2));

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

    public static boolean overlapSpheres(PinMarker point, PinMarker marker) {
        Log.d("overlapSpheres","pointx = "+point.getCenter().x+"  poiny = "+point.getCenter().y+"   pointz = "+point.getCenter().z);
        Log.d("overlapSpheres","markerx = "+marker.getCenter().x+"  markery = "+marker.getCenter().y+"   markerz = "+marker.getCenter().z);

        float x, y, z;
        x = point.getCenter().x - marker.getCenter().x;
        y = point.getCenter().y - marker.getCenter().y;
        z = point.getCenter().z - marker.getCenter().z;

        double distance = Math.sqrt( x*x + y*y + z*z );

        //idk why?
        return distance <= 1;
    }

    private void removePin(int markerPos){
        planes.get(markerPos).setInitiliazed(false);
    }

    public void setMarkerShown(boolean markerShown) {
        this.markerShown = markerShown;
    }

    public boolean isWithStory() {
        return withStory;
    }

    public void setWithStory(boolean withStory) {
        this.withStory = withStory;
    }

    public SendStory getMyStory() {
        return myStory;
    }

    public boolean isSurfaceCreated() {
        return surfaceCreated;
    }


    public void setBubbleText(TextView bubbleText) {
        this.bubbleText = bubbleText;
    }

    public void setBubbleTextLayout(LinearLayout bubbleTextLayout) {
        this.bubbleTextLayout = bubbleTextLayout;
    }

    public void setActvty(Activity act) {
        this.act = act;
    }
}