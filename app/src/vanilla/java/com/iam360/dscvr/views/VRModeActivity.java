package com.iam360.dscvr.views;


import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.iam360.dscvr.model.SendStoryChild;
import com.iam360.dscvr.model.StoryChild;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.iam360.dscvr.R;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.opengl.Cube;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.ImageUrlBuilder;
import com.iam360.dscvr.util.MixpanelHelper;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-12-30
 */
public class VRModeActivity extends CardboardActivity implements SensorEventListener {
    private static final int MILLISECONDS_THRESHOLD_FOR_SWITCH = 500;

    private CardboardRenderer cardboardRenderer;
//    private Optograph optograph;
    private ArrayList<Optograph> optographList;

    private DateTime creationTime;
    private boolean thresholdForSwitchReached;

    private boolean inVRMode;
    private SensorManager sensorManager;

    private int currentIndex = 0;
    private int optoListSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Constants.initializeConstants(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrmode);
        initializeOptograph();
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardRenderer = new CardboardRenderer(getApplicationContext());
        cardboardView.setRenderer(cardboardRenderer);

        // might use this for performance boost...
        // cardboardView.setRestoreGLStateEnabled(false);
        setCardboardView(cardboardView);

        initializeTextures();
        MixpanelHelper.trackViewViewer(this);

        creationTime = DateTime.now();
        thresholdForSwitchReached = false;
        inVRMode = true;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerAccelerationListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterAccelerationListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerAccelerationListener();
        creationTime = DateTime.now();
        inVRMode = true;
    }

    @Override
    public void onDestroy() {
//        MixpanelHelper.flush(this);
        super.onDestroy();
    }

    private void registerAccelerationListener() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterAccelerationListener() {
        sensorManager.unregisterListener(this);
    }

    private void initializeTextures() {
        if (optographList.get(currentIndex) == null) {
            return;
        }

        for (int i = 0; i < Cube.FACES.length; ++i) {
            String leftUri = ImageUrlBuilder.buildCubeUrl(optographList.get(currentIndex), true, Cube.FACES[i]);
            String rightUri  = ImageUrlBuilder.buildCubeUrl(optographList.get(currentIndex), false, Cube.FACES[i]);
            if (optographList.get(currentIndex).is_local()) {
                Picasso.with(this)
                        .load(new File(leftUri))
                        .into(cardboardRenderer.getLeftCube().getCubeTextureSet().getTextureTarget(Cube.FACES[i]));

                Picasso.with(this)
                        .load(new File(rightUri))
                        .into(cardboardRenderer.getRightCube().getCubeTextureSet().getTextureTarget(Cube.FACES[i]));
            } else {
                Picasso.with(this)
                        .load(leftUri)
                        .into(cardboardRenderer.getLeftCube().getCubeTextureSet().getTextureTarget(Cube.FACES[i]));

                Picasso.with(this)
                        .load(rightUri)
                        .into(cardboardRenderer.getRightCube().getCubeTextureSet().getTextureTarget(Cube.FACES[i]));
            }
        }

        initStoryChildrens();
    }

    private void initializeOptograph() {
        Intent intent = getIntent();
        if (intent != null) {

            optographList = this.getIntent().getParcelableArrayListExtra("opto_list");
            optoListSize = optographList.size();
//        optograph = getIntent().getExtras().getParcelable("opto");

//            if(optographArr != null) optograph = optographArr[0];

//            if (optograph == null) {
                //throw new RuntimeException("No optograph reveiced in VRActivity!");
//                Timber.e("No optograph reveiced in VRActivity!");
//            } else {
//                Timber.v("creating VRActivity for Optograph %s", optograph.getId());
//            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // only listen for Accelerometer if we did not switch to normal mode yet
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && inVRMode) {
            if (!thresholdForSwitchReached) {
                Interval timePassed = new Interval(creationTime, DateTime.now());
                Duration  duration = timePassed.toDuration();
                long millisecondsPassed = duration.getMillis();
                if (millisecondsPassed > MILLISECONDS_THRESHOLD_FOR_SWITCH) {
                    thresholdForSwitchReached = true;
                } else {
                    // don't switch
                    return;
                }
            }

            // switch to normal mode if phone was turned
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float length = (float) Math.sqrt(x*x + y*y);
            if (length < Constants.MINIMUM_AXIS_LENGTH) {
                return;
            }

            if (x + Constants.ACCELERATION_EPSILON < y) {
                switchToNormalMode();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onCardboardTrigger() {
        Timber.v("onCardboardTrigger " + currentIndex);
        currentIndex++;
        currentIndex = currentIndex % optoListSize;
        initializeTextures();
        super.onCardboardTrigger();
    }

    private void switchToNormalMode() {
        Timber.v("switching to feed mode");
        inVRMode = false;
//        this.finish();
    }

    @Override
    public void onBackPressed() {
        // TODO: stop endless switching loop with timer?
        // switchToNormalMode();
    }


    public void initStoryChildrens() {
        Timber.d("PINMARKER initStoryChildrens");

        if(optographList.get(currentIndex).getStory() != null && !optographList.get(currentIndex).getStory().getId().equals("") && optographList.get(currentIndex).getStory().getChildren().size() > 0){
            Timber.d("PINMARKER initStoryChildrens IF");
            List<StoryChild> chldrns = optographList.get(currentIndex).getStory().getChildren();
            for(int a=0; a < chldrns.size(); a++){
                Timber.d("PINMARKER initStoryChildrens FOR");
                if(chldrns.get(a).getStory_object_media_type().equals("MUS")){
//                    playBGM(chldrns.get(a).getStory_object_media_fileurl());
                    continue;
                }else if(chldrns.get(a).getStory_object_media_type().equals("FXTXT")){
//                    showFixTxt(chldrns.get(a).getStory_object_media_additional_data());
                }
                SendStoryChild stryChld = new SendStoryChild();
                stryChld.setStory_object_media_face(chldrns.get(a).getStory_object_media_face());
                stryChld.setStory_object_media_type(chldrns.get(a).getStory_object_media_type());
                stryChld.setStory_object_rotation(chldrns.get(a).getStory_object_rotation());
                stryChld.setStory_object_position(chldrns.get(a).getStory_object_position());
                stryChld.setStory_object_media_additional_data(chldrns.get(a).getStory_object_media_additional_data());

                cardboardRenderer.planeSetter(stryChld);
            }
        }
    }

//    private void playBGM(String mp3Url){
//        MediaPlayer mp = new MediaPlayer();
//
//        try {
//            mp.setDataSource("https://bucket.dscvr.com"+mp3Url);
//            mp.prepare();
//            mp.start();
//            mp.setLooping(true);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    private void showFixTxt(String txt){
//        binding.storyFixTxt.setText(txt);
//        binding.storyFixTxt.setVisibility(View.VISIBLE);
//    }


}