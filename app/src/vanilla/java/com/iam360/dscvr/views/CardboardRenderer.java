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
import com.iam360.dscvr.util.CircleCountDownView;
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

    private TextView bubbleText_L;
    private TextView bubbleText_R;
    private LinearLayout bubbleTextLayout;
    private LinearLayout bubbleTextLayoutL;
    private LinearLayout bubbleTextLayoutR;
    private Activity act;

    private List<PinMarker> planes = new ArrayList<PinMarker>();
    private PinMarker plane;
    private PinMarker backMarker;

    private boolean markerShown = false;
    private int storyType = 1; //0=creator; 1=viewer; 2=edit
    private boolean withStory = false;
    private SendStory myStory = new SendStory();
    private List<SendStoryChild> myStoryChld = new ArrayList<SendStoryChild>();
    public boolean overlapChcker = false;
    private int selectedPin = 0;

    private static final float V_DISTANCE = 15f;

    private static final float[] ROTATION_AHEAD_FIRST = {0, 1, 0, 0};
    private static final float[] ROTATION_AHEAD_SECOND = {0, 0, 1, 0};

    private Bitmap planeTexture;
    private float[] headView = new float[16];
    private float[] headTransFormEulersAngles = {0, 0, 0};
    private float[] headTransFormQuaternion = {0, 0, 0, 0};
    private Sphere sphere;
    private float sphereRadius = 30f;

    private List<Optograph> optographs = new ArrayList<Optograph>();
    private List<String> cacheStories = new ArrayList<String>();
    private Optograph originalOpto;
    private boolean storyPageOriginal = true;
    private LinearLayout loadingScreen_L;
    private CircleCountDownView countDownView_L;
    private LinearLayout loadingScreen_R;
    private CircleCountDownView countDownView_R;

    private CountDownTimer countDownTimer;
    private int loaderTimer = 4;

    private int halfWidthScrn;
    private Context context;

    public CardboardRenderer(Context context) {
        this.context = context;
        this.backMarker = new PinMarker();
        this.plane = new PinMarker();

        initializeCubes();
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        this.leftCube.initialize();
        this.rightCube.initialize();

        sphere = new Sphere(5, sphereRadius);
        setSpherePosition(2.0f, 1.0f, 2.0f);
        sphere.initializeProgram();

        this.backMarker.initializeProgram();
        this.backMarker.setInitiliazed(true);
        backMarker.setCenter(-0.5321962f, -3.774146f, 14.507673f);
        this.plane.initializeProgram();
        this.plane.setInitRotation(Maths.buildRotationMatrix(ROTATION_AHEAD_SECOND, ROTATION_AHEAD_FIRST));
        planeTexture = BitmapFactory.decodeResource(context.getResources(), R.drawable.main_pin_icn);
        Bitmap planeTexture3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrowright);
        this.plane.updateTexture(planeTexture);
        this.backMarker.updateTexture(planeTexture3);


        for(int a=0; a < 20; a++){
            planes.add(new PinMarker());
            planes.get(a).initializeProgram();
            planes.get(a).updateTexture(planeTexture);
        }
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

        /*************/
        float[] vector = {0, 0, V_DISTANCE, 0};
        float[] newPosition = new float[4];
        Matrix.multiplyMV(newPosition, 0, eye.getEyeView(), 0, vector, 0);

        setSpherePosition(newPosition[0], newPosition[1], newPosition[2]);
        sphere.draw(modelViewProjection);

        plane.setCenter(newPosition[0], newPosition[1], newPosition[2]);

        float[] modelView = new float[16];
        float[] tMatrix = Maths.buildTranslationMatrix(new float[]{-0.5321962f, -3.774146f, 14.507673f});

        float[] rot = new float[16];
        float[] newRot = new float[16];
        float[] scaleRotMatrix = new float[16];
        float[] scales = Maths.buildScaleMatrix(2);
        float[] modelMatrixs = new float[16];
        Matrix.setRotateEulerM(rot,0, headTransFormEulersAngles[0], headTransFormEulersAngles[1], headTransFormEulersAngles[2]);

        Matrix.multiplyMM(newRot, 0, plane.getInitRotation(), 0, rot, 0);
        Matrix.multiplyMM(scaleRotMatrix, 0, tMatrix, 0, scales, 0);
        Matrix.multiplyMM(modelMatrixs, 0, scaleRotMatrix, 0, newRot, 0);

        Matrix.multiplyMM(modelView, 0, modelViewProjection, 0, modelMatrixs, 0);
        if(!storyPageOriginal){
            backMarker.draw(modelView);
        }
        /******************/


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
                float[] translationMatrix = planes.get(a).getTranslation();
                float phi2 = planes.get(a).getxRotation();
                float theta2 = planes.get(a).getyRotation();

                float[] rotationX2 = {(float) Math.toDegrees(theta2), 1, 0, 0};
                float[] rotationY2 = {(float) -Math.toDegrees(phi2), 0, 1, 0};
                float[] newRotation = new float[16];
                float[] scaleRotationMatrix = new float[16];
                float[] modelMatrix = new float[16];
                float[] rotations = Maths.buildRotationMatrix(rotationY2, rotationX2);

                Matrix.multiplyMM(newRotation, 0, plane.getInitRotation(), 0, rotations, 0);
                Matrix.multiplyMM(scaleRotationMatrix, 0, translationMatrix, 0, scales, 0);
                Matrix.multiplyMM(modelMatrix, 0, scaleRotationMatrix, 0, newRotation, 0);

                Matrix.multiplyMM(modelView3, 0, modelViewProjection, 0, modelMatrix, 0);

                if(storyPageOriginal){
                    planes.get(a).draw(modelView3);
                }
            }
        }

        if(bubbleTextLayout != null && planes!=null && act != null &&  planes.get(selectedPin) != null){
            showHideBubbleText("hide");
        }

        if(markerShown){
            if(overlapChcker && bubbleTextLayout != null && planes.get(selectedPin).getMediaType().equals("TXT")) {
                showHideBubbleText("show");
                sphere.draw(modelViewProjection);
            }else if(overlapChcker && storyType == 1 && !storyPageOriginal){
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

                if(!cacheStories.contains(optoId)){
                    cacheStories.add(optoId);
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DBHelper mydb = new DBHelper(act);
                            Cursor res = mydb.getData(optoId, DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
//                            Log.d("MARKS","res.getCount() = "+res.getCount());
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
                sphere.draw(modelViewProjection);
            }
        }
        Timber.d("storyPageOriginal = "+storyPageOriginal);
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




    public static boolean overlapSpheres(PinMarker point, PinMarker marker) {
        float x, y, z;
        x = point.getCenter().x - marker.getCenter().x;
        y = point.getCenter().y - marker.getCenter().y;
        z = point.getCenter().z - marker.getCenter().z;

        double distance = Math.sqrt( x*x + y*y + z*z );

        //idk why?
        return distance <= 1;
    }

    public void setBubbleText(TextView bubbleText_L, TextView bubbleText_R) {
        this.bubbleText_L = bubbleText_L;
        this.bubbleText_R = bubbleText_R;
    }

    public void setBubbleTextLayout(LinearLayout bubbleTextLayout, LinearLayout bubbleTextLayoutL, LinearLayout bubbleTextLayoutR) {
        this.bubbleTextLayout = bubbleTextLayout;
        this.bubbleTextLayoutL = bubbleTextLayoutL;
        this.bubbleTextLayoutR = bubbleTextLayoutR;
    }

    public void setActvty(Activity act) {
        this.act = act;
    }

    public void setOriginalOpto(Optograph opto) {
        this.originalOpto = opto;
    }

    public void setLoadingScreen(LinearLayout ls_L, LinearLayout ls_R, CircleCountDownView cd_L, CircleCountDownView cd_R) {
        this.loadingScreen_L = ls_L;
        this.countDownView_L = cd_L;
        this.loadingScreen_R = ls_R;
        this.countDownView_R = cd_R;
    }

    private void animateLoader(boolean show){
        if(loadingScreen_L != null){
//            Log.d("MARKSSS","animateLoader show = "+show);
            if(show){
                if(loadingScreen_L.getVisibility() == View.VISIBLE){
                    return;
                }
                initLoader();
                loadingScreen_L.setVisibility(View.VISIBLE);
                loadingScreen_R.setVisibility(View.VISIBLE);
            }else{
                if(loadingScreen_L.getVisibility() == View.GONE){
                    return;
                }
                loadingScreen_L.setVisibility(View.GONE);
                loadingScreen_R.setVisibility(View.GONE);
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
                countDownView_L.setProgress(progress[0], endTime);
                countDownView_R.setProgress(progress[0], endTime);
                progress[0] = progress[0] + 1;
            }
            @Override
            public void onFinish() {
                countDownView_L.setProgress(progress[0], endTime);
                countDownView_R.setProgress(progress[0], endTime);
            }
        };
        countDownTimer.start();
    }

    private void showHideBubbleText(String type){
        if(type.equals("show")){
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bubbleText_L.setText(planes.get(selectedPin).getMediaAdditionalData());
                    bubbleText_R.setText(planes.get(selectedPin).getMediaAdditionalData());

                    bubbleText_L.measure(0, 0);

                    int leftEyeLPost = (halfWidthScrn - bubbleText_L.getMeasuredWidth())/2 - 20;

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(leftEyeLPost, 0, (int) Constants.convertDpToPixel(10, context), (int) Constants.convertDpToPixel(10, context));
                    bubbleTextLayoutL.setLayoutParams(params);
                    loadingScreen_L.setLayoutParams(params);

                    params2.setMargins((leftEyeLPost * 2) - 20, 0, (int) Constants.convertDpToPixel(10, context), (int) Constants.convertDpToPixel(10, context));
                    bubbleTextLayoutR.setLayoutParams(params2);
                    loadingScreen_R.setLayoutParams(params);

                    bubbleTextLayout.setVisibility(View.VISIBLE);
                }
            });
        }else{
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bubbleText_L.setText("");
                    bubbleText_R.setText("");
                    bubbleTextLayout.setVisibility(View.GONE);
                }
            });
        }
    }

    public void planeSetter(SendStoryChild chld) {
        markerShown = true;
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
        planes.get(planes.size() - 1).setMarkerName("child_"+myStoryChld.size());
        planes.get(planes.size() - 1).setCenter(Float.parseFloat(chld.getStory_object_position().get(0)),Float.parseFloat(chld.getStory_object_position().get(1)), Float.parseFloat(chld.getStory_object_position().get(2)));


        chld.setStory_object_name("child_"+myStoryChld.size());
        myStoryChld.add(chld);
    }

    public void reInitializedTexture(Optograph opto, String type){
        Timber.d("reInitializedTexture type = "+type);
        if(type.equals("BACK")){
            storyPageOriginal = true;
        }else{
            storyPageOriginal = false;
        }
//        Log.d("MARKS","reInitializedTexture opto.is_local() = "+opto.is_local());
        for (int i = 0; i < Cube.FACES.length; ++i) {
            String leftUri = ImageUrlBuilder.buildCubeUrl(opto, true, Cube.FACES[i]);
            String rightUri  = ImageUrlBuilder.buildCubeUrl(opto, false, Cube.FACES[i]);
//            Log.d("MARKS","reInitializedTexture uri = "+uri);
            if (opto.is_local()) {
                Picasso.with(act)
                        .load(new File(leftUri))
                        .into(getTextureTargetLeft(Cube.FACES[i]));

                Picasso.with(act)
                        .load(new File(rightUri))
                        .into(getTextureTargetRight(Cube.FACES[i]));
            } else {
                Picasso.with(act)
                        .load(leftUri)
                        .into(getTextureTargetLeft(Cube.FACES[i]));

                Picasso.with(act)
                        .load(rightUri)
                        .into(getTextureTargetRight(Cube.FACES[i]));
            }
        }
    }

    public TextureSet.TextureTarget getTextureTargetLeft(int face) {
        return this.leftCube.getCubeTextureSet().getTextureTarget(face);

    }
    public TextureSet.TextureTarget getTextureTargetRight(int face) {
        return this.rightCube.getCubeTextureSet().getTextureTarget(face);

    }

    public void setHalfWidthScrn(int halfWidthScrn) {
        this.halfWidthScrn = halfWidthScrn;
    }
}
