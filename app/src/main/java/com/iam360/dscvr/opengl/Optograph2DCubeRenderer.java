package com.iam360.dscvr.opengl;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iam360.dscvr.R;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.SendStory;
import com.iam360.dscvr.model.SendStoryChild;
import com.iam360.dscvr.sensors.CombinedMotionManager;
import com.iam360.dscvr.sensors.TouchEventListener;
import com.iam360.dscvr.util.CircleCountDownView;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.DBHelper;
import com.iam360.dscvr.util.ImageUrlBuilder;
import com.iam360.dscvr.util.Maths;
import com.squareup.picasso.Picasso;

import java.io.File;
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
    private float sphereRadius = 20f;
    private CombinedMotionManager combinedMotionManager;

    private Cube cube;
    private String optoType;
    private TextView bubbleText;
    private LinearLayout bubbleTextLayout;
    private Activity act;

    //    private MarkerNode sphere;
    private List<PinMarker> planes = new ArrayList<PinMarker>();
    private Sphere sphere;
    private PinMarker plane;
    private PinMarker backMarker;


    private Context context;

    private static final float[] ROTATION_AHEAD_FIRST = {0, 1, 0, 0};
    private static final float[] ROTATION_AHEAD_SECOND = {0, 0, 1, 0};

    private Bitmap planeTexture;
    private Bitmap planeTexture2;
    private Bitmap planeTexture3;


    private boolean markerShown = false;
    private int storyType = 0; //0=creator; 1=viewer; 2=edit
    private boolean withStory = false;
    private SendStory myStory = new SendStory();
    private List<SendStoryChild> myStoryChld = new ArrayList<SendStoryChild>();
    public boolean overlapChcker = false;
    private int selectedPin = 0;
    private boolean surfaceCreated = false;

    private List<Optograph> optographs = new ArrayList<Optograph>();
    private List<String> cacheStories = new ArrayList<String>();
    private Optograph originalOpto;
    private boolean storyPageOriginal = true;
    private RelativeLayout loadingScreen;
    private CircleCountDownView countDownView;
    private CountDownTimer countDownTimer;
    private ImageButton deleteStoryMarkerImage;
    private int loaderTimer = 4;


    public Optograph2DCubeRenderer(Context context) {
//        Timber.v("cube renderer constructor");
        this.context = context;
        this.cube = new Cube();
        this.plane = new PinMarker();
        this.backMarker = new PinMarker();
        this.plane.setInitRotation(Maths.buildRotationMatrix(ROTATION_AHEAD_SECOND, ROTATION_AHEAD_FIRST));
        this.combinedMotionManager = new CombinedMotionManager(DAMPING_FACTOR, Constants.getInstance().getDisplayMetrics().widthPixels, Constants.getInstance().getDisplayMetrics().heightPixels, FIELD_OF_VIEW_Y);

        Matrix.setIdentityM(rotationMatrix, 0);

        myStory.setChildren(myStoryChld);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        Timber.v("onSurfaceCreated");
        surfaceCreated = true;

        this.cube.initialize();
        this.plane.initializeProgram();
        this.backMarker.initializeProgram();
        backMarker.setCenter(0.026925718f, -4.939698f, 19.380367f);

        planeTexture = BitmapFactory.decodeResource(context.getResources(), R.drawable.main_pin_icn);
        planeTexture2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.close_x_icn);
        planeTexture3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrowright);

        this.plane.updateTexture(planeTexture);
        this.backMarker.updateTexture(planeTexture3);

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
        Log.d("MARK","newPosition == "+Arrays.toString(newPosition));

        // Draw shape
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        cube.draw(mvpMatrix);

        plane.setCenter(newPosition[0], newPosition[1], newPosition[2]);

        float[] modelView = new float[16]; //center plane

        float[] translationMatrix = Maths.buildTranslationMatrix(new float[]{newPosition[0], newPosition[1], newPosition[2]});
        float[] backMarkerTranslationMatrix = Maths.buildTranslationMatrix(new float[]{0.026925718f, -4.939698f, 19.380367f});

//        float[] rotations = combinedMotionManager.getRotationMatrix();

        float phi = touchEventListener.getPhi();
        float theta = touchEventListener.getTheta();
//        Log.d("MARK","onDrawFrame phi = "+phi);
//        Log.d("MARK","onDrawFrame theta = "+theta);
        float[] rotationX = {(float) Math.toDegrees(theta), 1, 0, 0};
        float[] rotationY = {(float) -Math.toDegrees(phi), 0, 1, 0};
        float[] rotations = Maths.buildRotationMatrix(rotationY, rotationX);

        float[] scales = Maths.buildScaleMatrix(2);

        float[] modelMatrix = new float[16];
        float[] scaleRotationMatrix = new float[16];
        float[] newRotation = new float[16];

        Matrix.multiplyMM(newRotation, 0, plane.getInitRotation(), 0, rotations, 0);
        Matrix.multiplyMM(scaleRotationMatrix, 0, backMarkerTranslationMatrix, 0, scales, 0);
        Matrix.multiplyMM(modelMatrix, 0, scaleRotationMatrix, 0, newRotation, 0);
        Matrix.multiplyMM(modelView, 0, mvpMatrix, 0, modelMatrix, 0); //center plane

        plane.setxRotation(phi);
        plane.setyRotation(theta);
        plane.setzRotation(0.9f);

        plane.setTranslation(translationMatrix);

        if(!storyPageOriginal){
            backMarker.draw(modelView);
        }

        overlapChcker = false;
        for(int a=0; a< planes.size(); a++){
            if(planes.get(a).isInitiliazed()){
                if(storyPageOriginal) {
                    if(overlapSpheres(plane, planes.get(a))){
                        selectedPin = a;
                        overlapChcker = true;
                    }
                }else{
                    if(overlapSpheres(plane, backMarker)){
                        selectedPin = a;
                        overlapChcker = true;
                    }
                }

                float[] modelView3 = new float[16];
//                translationMatrix = planes.get(a).getTranslation();
                float phi2 = planes.get(a).getxRotation();
                float theta2 = planes.get(a).getyRotation();

                float[] rotationX2 = {(float) Math.toDegrees(theta2), 1, 0, 0};
                float[] rotationY2 = {(float) -Math.toDegrees(phi2), 0, 1, 0};
                rotations = Maths.buildRotationMatrix(rotationY2, rotationX2);

                Matrix.multiplyMV(newPosition, 0, rotations, 0, vector, 0);
                translationMatrix = Maths.buildTranslationMatrix(new float[]{newPosition[0], newPosition[1], newPosition[2]});

                planes.get(a).setCenter(newPosition[0], newPosition[1], newPosition[2]);

                Matrix.multiplyMM(newRotation, 0, plane.getInitRotation(), 0, rotations, 0);
                Matrix.multiplyMM(scaleRotationMatrix, 0, translationMatrix, 0, scales, 0);
                Matrix.multiplyMM(modelMatrix, 0, scaleRotationMatrix, 0, newRotation, 0);

                Matrix.multiplyMM(modelView3, 0, mvpMatrix, 0, modelMatrix, 0);

                if(storyPageOriginal){
                    planes.get(a).draw(modelView3);
                }
            }
        }
        if(bubbleTextLayout != null && planes!=null && act != null &&  planes.get(selectedPin) != null){
            showHideBubbleText("hide");
        }
        if(deleteStoryMarkerImage != null){
            showHideMarkerRemover("hide");
        }
        Log.d("MARKSS","overlapChcker = "+overlapChcker);
        Log.d("MARKSS","storyType = "+storyType);
        Log.d("MARKSS","storyPageOriginal = "+storyPageOriginal);

        if(markerShown){
            if(overlapChcker && (storyType == 0 || storyType == 2) && deleteStoryMarkerImage != null) {
                Log.d("MARKS","markerShown if");
                showHideMarkerRemover("show");
                sphere.draw(mvpMatrix);
            }else if(overlapChcker && bubbleTextLayout != null && planes.get(selectedPin).getMediaType().equals("TXT")) {
                Log.d("MARKS","markerShown else if");
                showHideBubbleText("show");
                sphere.draw(mvpMatrix);
            }else if(overlapChcker && storyType == 1 && !storyPageOriginal){
                Log.d("MARKS","originalOpto = "+originalOpto.getId());
                Log.d("MARKS","storyPageOriginal = "+storyPageOriginal);

                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        animateLoader(true);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(overlapChcker){
                                    reInitializedTexture(originalOpto, "BACK");
                                }
                                animateLoader(false);
                            }
                        }, (loaderTimer * 1000));
                    }
                });
            }else if(overlapChcker && storyType == 1 && planes.get(selectedPin).getMediaType().equals("NAV")){
                String optoId = planes.get(selectedPin).getMediaAdditionalData();
                Log.d("MARKS","getMediaAdditionalData = "+planes.get(selectedPin).getMediaAdditionalData());
                Log.d("MARKS","optoId = "+optoId);

                if(!cacheStories.contains(optoId)){
                    cacheStories.add(optoId);
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DBHelper mydb = new DBHelper(act);
                            Cursor res = mydb.getData(optoId, DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
                            Log.d("MARKS","res.getCount() = "+res.getCount());
                            if(res.getCount() > 0) {
                                Optograph opto = mydb.getOptoDataFromLocalDB(res);
                                animateLoader(true);
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(overlapChcker){
                                            reInitializedTexture(opto, "NAV");
                                        }
                                        animateLoader(false);
                                    }
                                }, (loaderTimer * 1000));
                                optographs.add(opto);
                            }
                        }
                    });
                }else{
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            animateLoader(true);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(overlapChcker) {
                                        for (int a = 0; a < optographs.size(); a++) {
                                            if (optographs.get(a).getId().equals(optoId) && overlapChcker) {
                                                //Log.d("MARKS","cacheStories.contains else");
                                                Optograph opto = optographs.get(a);
                                                reInitializedTexture(opto, "NAV");
                                            }
                                        }
                                    }
                                    animateLoader(false);
                                }
                            }, (loaderTimer * 1000));
                        }
                    });
                }
            }else {
                Log.d("MARKS","markerShown else");
                sphere.draw(mvpMatrix);
            }
        }
    }

    public TextureSet.TextureTarget getTextureTarget(int face) {
        return this.cube.getCubeTextureSet().getTextureTarget(face);
    }

    public void reInitializedTexture(Optograph opto, String type){
        storyPageOriginal = type.equals("BACK");
//        Log.d("MARKS","reInitializedTexture opto.is_local() = "+opto.is_local());
        for (int i = 0; i < Cube.FACES.length; ++i) {
            String uri = ImageUrlBuilder.buildCubeUrl(opto, true, Cube.FACES[i]);
//            Log.d("MARKS","reInitializedTexture uri = "+uri);
            if (opto.is_local()) {
                Picasso.with(act)
                        .load(new File(uri))
                        .into(getTextureTarget(Cube.FACES[i]));
            } else {
                Picasso.with(act)
                        .load(uri)
                        .into(getTextureTarget(Cube.FACES[i]));
            }
        }
    }

    public void setMode(int mode) { combinedMotionManager.setMode(mode);}

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
//        Log.d("addMarker","planes.size = "+planes.size());

//        if(planes.size() > 0){
//            if(overlapSpheres(plane, planes.get(planes.size() - 1))){
//                return;
//            }
//        }


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
//              chld.setStory_object_position( Arrays.asList(Float.toString(plane.getCenter().x), Float.toString(plane.getCenter().y), Float.toString(plane.getCenter().z)));
//              chld.setStory_object_rotation( Arrays.asList(Float.toString(plane.getxRotation()), Float.toString(plane.getyRotation()), Float.toString(plane.getzRotation())));
                chld.setStory_object_position(Arrays.asList("0","0","0"));
                chld.setStory_object_rotation(Arrays.asList("0","0","0"));

                chld.setStory_object_phi(String.valueOf(plane.getxRotation()));
                chld.setStory_object_theta(String.valueOf(plane.getyRotation()));

//              chld.setStory_object_position( Arrays.asList(Float.toString(convertedPosition[0]), Float.toString(convertedPosition[1]), Float.toString(convertedPosition[2])));
//              chld.setStory_object_rotation( Arrays.asList(Float.toString(convertedRotation[0]), Float.toString(convertedRotation[1]), Float.toString(convertedRotation[2])));

                chld.setStory_object_name("child_"+myStoryChld.size());
                myStoryChld.add(chld);

//                Log.d("MARK","addMarker xRot = "+plane.getxRotation());
//                Log.d("MARK","addMarker yRot = "+plane.getyRotation());

                float[] rotationX2 = {(float) Math.toDegrees(plane.getyRotation()), 1, 0, 0};
                float[] rotationY2 = {(float) -Math.toDegrees(plane.getxRotation()), 0, 1, 0};
                float[] rotations = Maths.buildRotationMatrix(rotationY2, rotationX2);
//                Log.d("MARK","addMarker rotations = "+ Arrays.toString(rotations));


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
                planes.get(a).setMarkerName("child_"+myStoryChld.size());
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
//            removePin(selectedPin);
            return;
        }
        if(chld.getStory_object_media_face().equals("pin")){
            addMarker(chld);
        }else{
            chld.setStory_object_phi(String.valueOf(plane.getxRotation()));
            chld.setStory_object_theta(String.valueOf(plane.getyRotation()));
//            chld.setStory_object_position( Arrays.asList("0", "0", "0"));
//            chld.setStory_object_rotation( Arrays.asList("0", "0", "0"));
            chld.setStory_object_name("child_"+myStoryChld.size());
            myStoryChld.add(chld);
        }
    }

    public void planeSetter(SendStoryChild chld) {
        markerShown = true;

        planes.add(new PinMarker());
        planes.get(planes.size() - 1).setTranslation(Maths.buildTranslationMatrix(new float[]{Float.parseFloat(chld.getStory_object_position().get(0)),Float.parseFloat(chld.getStory_object_position().get(1)), Float.parseFloat(chld.getStory_object_position().get(2))}));
        planes.get(planes.size() - 1).setxRotation(Float.parseFloat(chld.getStory_object_phi()));
        planes.get(planes.size() - 1).setyRotation(Float.parseFloat(chld.getStory_object_theta()));
        planes.get(planes.size() - 1).setInitiliazed(true);
        planes.get(planes.size() - 1).setMediaType(chld.getStory_object_media_type());
        planes.get(planes.size() - 1).setMediaAdditionalData(chld.getStory_object_media_additional_data());
        planes.get(planes.size() - 1).setMediaDescription(chld.getStory_object_media_description());
        planes.get(planes.size() - 1).setMediaFace(chld.getStory_object_media_face());
        planes.get(planes.size() - 1).setObjectMediaFileurl(chld.getStory_object_media_fileurl());
        planes.get(planes.size() - 1).setObjectMediaFilename(chld.getStory_object_media_filename());
        planes.get(planes.size() - 1).setMarkerName("child_"+myStoryChld.size());
        planes.get(planes.size() - 1).setCenter(Float.parseFloat(chld.getStory_object_position().get(0)),Float.parseFloat(chld.getStory_object_position().get(1)), Float.parseFloat(chld.getStory_object_position().get(2)));

//        Log.d("MARK","planeSetter size = "+planes.size());

//        chld.setStory_object_position( Arrays.asList(Float.toString(plane.getCenter().x), Float.toString(plane.getCenter().y), Float.toString(plane.getCenter().z)));
//        chld.setStory_object_rotation( Arrays.asList(Float.toString(plane.getxRotation()), Float.toString(plane.getyRotation()), "0"));
        chld.setStory_object_name("child_"+myStoryChld.size());
        myStoryChld.add(chld);

//        for(int a=0; a< planes.size(); a++) {
////            Log.d("MARK","planeSetter is init? = "+planes.get(a).isInitiliazed());
//            if (!planes.get(a).isInitiliazed()) {
//                chld.setStory_object_position( Arrays.asList(Float.toString(plane.getCenter().x), Float.toString(plane.getCenter().y), Float.toString(plane.getCenter().z)));
//                chld.setStory_object_rotation( Arrays.asList(Float.toString(plane.getxRotation()), Float.toString(plane.getyRotation()), "0"));
//                chld.setStory_object_name("child_"+myStoryChld.size());
//                myStoryChld.add(chld);
////                Log.d("MARK","planeSetter2 x = "+chld.getStory_object_position().get(0));
////                Log.d("MARK","planeSetter2 y = "+chld.getStory_object_position().get(1));
////                Log.d("MARK","planeSetter2 z = "+chld.getStory_object_position().get(2));
//
//                planes.get(a).setInitiliazed(true);
//                planes.get(a).setMediaType(chld.getStory_object_media_type());
//                planes.get(a).setMediaAdditionalData(chld.getStory_object_media_additional_data());
//                planes.get(a).setMediaDescription(chld.getStory_object_media_description());
//                planes.get(a).setMediaFace(chld.getStory_object_media_face());
//                planes.get(a).setObjectMediaFilename(chld.getStory_object_media_filename());
//                planes.get(a).setObjectMediaFileurl(chld.getStory_object_media_fileurl());
//                planes.get(a).setMarkerName("child_"+myStoryChld.size());
//
//                planes.get(a).setTranslation(Maths.buildTranslationMatrix(new float[]{Float.parseFloat(chld.getStory_object_position().get(0)),Float.parseFloat(chld.getStory_object_position().get(1)), Float.parseFloat(chld.getStory_object_position().get(2))}));
//                planes.get(a).setCenter(Float.parseFloat(chld.getStory_object_position().get(0)),Float.parseFloat(chld.getStory_object_position().get(1)), Float.parseFloat(chld.getStory_object_position().get(2)));
//
//                planes.get(a).setxRotation(Float.parseFloat(chld.getStory_object_rotation().get(0)));
//                planes.get(a).setyRotation(Float.parseFloat(chld.getStory_object_rotation().get(1)));
//                break;
//            }
//        }
//        Log.d("MARK","planeSetter myStoryChld.size = "+myStoryChld.size());
    }

    public static boolean overlapSpheres(PinMarker point, PinMarker marker) {
//        Timber.d("pointx = "+point.getCenter().x+"  poiny = "+point.getCenter().y+"   pointz = "+point.getCenter().z);
//        Timber.d("markerx = "+marker.getCenter().x+"  markery = "+marker.getCenter().y+"   markerz = "+marker.getCenter().z);

        float x, y, z;
        x = point.getCenter().x - marker.getCenter().x;
        y = point.getCenter().y - marker.getCenter().y;
        z = point.getCenter().z - marker.getCenter().z;

        double distance = Math.sqrt( x*x + y*y + z*z );
//        Timber.d("Overlap : " + distance + " = " + point.getCenter().x + ":" + point.getCenter().y + ":" + point.getCenter().z + " " + marker.getCenter().x + ":" + marker.getCenter().y + ":" + marker.getCenter().z);

        //idk why?
//        Timber.d("overlapSpheres = "+distance);
        return distance <= 1;
    }

    public void removePin(){
        if(overlapChcker){
//            Log.d("MARK","removePin size = "+myStoryChld.size());
            planes.get(selectedPin).setInitiliazed(false);
            int counter = myStoryChld.size();
            for(int a=0; a < counter; a++){
                if(myStoryChld.get(a).getStory_object_name().equals(planes.get(selectedPin).getMarkerName())){
                    myStoryChld.remove(a);
                    break;
                }
            }
//            Log.d("MARK","removePin size after = "+myStoryChld.size());
        }
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

    public void setOriginalOpto(Optograph opto) {
        this.originalOpto = opto;
    }

    public void setLoadingScreen(RelativeLayout ls, CircleCountDownView cd) {
        this.loadingScreen = ls;
        this.countDownView = cd;
    }

    private void animateLoader(boolean show){
        if(loadingScreen != null){
//            Log.d("MARKSSS","animateLoader show = "+show);
            if(show){
                if(loadingScreen.getVisibility() == View.VISIBLE){
                    return;
                }
                initLoader();
                loadingScreen.setVisibility(View.VISIBLE);
            }else{
                if(loadingScreen.getVisibility() == View.GONE){
                    return;
                }
                loadingScreen.setVisibility(View.GONE);
            }
        }
    }

    private void initLoader(){
//        Log.d("MARKSSS","initLoader");
        final int[] progress = {1};
        int endTime = loaderTimer - 1; // up to finish time
        countDownTimer = new CountDownTimer(endTime * 1000 /*finishTime**/, 1000 /*interval**/) {
            @Override
            public void onTick(long millisUntilFinished) {
                countDownView.setProgress(progress[0], endTime);
                progress[0] = progress[0] + 1;
            }
            @Override
            public void onFinish() {
                countDownView.setProgress(progress[0], endTime);
            }
        };
        countDownTimer.start();
    }

    public void setDeleteStoryMarkerImage(ImageButton deleteStoryMarkerImage) {
        this.deleteStoryMarkerImage = deleteStoryMarkerImage;
    }

    public void setStoryType(int storyType) {
        this.storyType = storyType;
    }

    public int getStoryType() {
        return storyType;
    }

    private void showHideBubbleText(String type){
        if(type.equals("show")){
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bubbleText.setText(planes.get(selectedPin).getMediaAdditionalData());
                    bubbleTextLayout.setVisibility(View.VISIBLE);
                }
            });
        }else{
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bubbleText.setText("");
                    bubbleTextLayout.setVisibility(View.GONE);
                }
            });
        }
    }
    private void showHideMarkerRemover(String type){
        if(type.equals("show")){
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(planes.get(selectedPin).getMediaType().equals("TXT")){
                        showHideBubbleText("show");
                    }
                    deleteStoryMarkerImage.setVisibility(View.VISIBLE);
                }
            });
        }else {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    deleteStoryMarkerImage.setVisibility(View.GONE);
                }
            });
        }
    }
}