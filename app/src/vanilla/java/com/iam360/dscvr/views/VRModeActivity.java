package com.iam360.dscvr.views;


import android.content.Intent;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.iam360.dscvr.R;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.SendStoryChild;
import com.iam360.dscvr.model.StoryChild;
import com.iam360.dscvr.opengl.Cube;
import com.iam360.dscvr.util.AudioStreamWorkerTask;
import com.iam360.dscvr.util.BubbleDrawable;
import com.iam360.dscvr.util.CircleCountDownView;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.ImageUrlBuilder;
import com.iam360.dscvr.util.MixpanelHelper;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private ExpandableTextView storyFixTxt;
    private LinearLayout loadingScreen_L;
    private CircleCountDownView circleCountDownView_L;
    private LinearLayout loadingScreen_R;
    private CircleCountDownView circleCountDownView_R;

    private LinearLayout bubbleTextLayout;
    private LinearLayout bubbleTextLayoutL;
    private LinearLayout bubbleTextLayoutR;

    private int currentIndex = 0;
    private int optoListSize = 0;

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Constants.initializeConstants(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrmode);
        initializeOptograph();
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);

//        TextOverlay textOverlayLayout = (TextOverlay) findViewById(R.id.overlay);
        BubbleDrawable myBubble = new BubbleDrawable(BubbleDrawable.CENTER);
        myBubble.setCornerRadius(20);
        myBubble.setPadding(25, 25, 25, 25);
        bubbleTextLayout = (LinearLayout) findViewById(R.id.bubble_text_layout);
        bubbleTextLayoutL = (LinearLayout) findViewById(R.id.bubble_text_layout_L);
        bubbleTextLayoutL.setBackgroundDrawable(myBubble);
        bubbleTextLayoutR = (LinearLayout) findViewById(R.id.bubble_text_layout_R);
        bubbleTextLayoutR.setBackgroundDrawable(myBubble);


        TextView textOverlay_L = (TextView) findViewById(R.id.bubble_text_L);
        TextView textOverlay_R = (TextView) findViewById(R.id.bubble_text_R);

        storyFixTxt= (ExpandableTextView) findViewById(R.id.story_fix_txt);
        loadingScreen_L = (LinearLayout) findViewById(R.id.loading_screen_L);
        circleCountDownView_L = (CircleCountDownView) findViewById(R.id.circle_count_down_view_L);
        loadingScreen_R = (LinearLayout) findViewById(R.id.loading_screen_R);
        circleCountDownView_R = (CircleCountDownView) findViewById(R.id.circle_count_down_view_R);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

//        Timber.d("VRMODE h="+height+"   w="+width);
//
//        textOverlay_L.measure(0, 0);
//        textOverlay_R.measure(0, 0);
//        Timber.d("VRMODE textOverlay_LW="+textOverlay_L.getMeasuredWidth());
//        Timber.d("VRMODE textOverlay_RW="+textOverlay_R.getMeasuredWidth());

        int halfWidthScrn = width/2;
//        int leftEyeLPost = (halfWidthScrn - textOverlay_L.getMeasuredWidth())/2;

//        Timber.d("VRMODE leftEyeLPost="+leftEyeLPost);
//        Timber.d("VRMODE leftEyeLPost convertDpToPixel ="+ Constants.convertDpToPixel(leftEyeLPost, this));
//
//        Timber.d("VRMODE leftEyeLPost="+rightEyeLPost);
//        Timber.d("VRMODE leftEyeLPost convertDpToPixel ="+ Constants.convertDpToPixel(rightEyeLPost, this));


//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//        );
//        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//        );
//        params.setMargins(leftEyeLPost, 0, (int) Constants.convertDpToPixel(10, this), (int) Constants.convertDpToPixel(10, this));
//        bubbleTextLayoutL.setLayoutParams(params);
//
//        params2.setMargins((leftEyeLPost * 2), 0, (int) Constants.convertDpToPixel(10, this), (int) Constants.convertDpToPixel(10, this));
//        bubbleTextLayoutR.setLayoutParams(params2);

        cardboardRenderer = new CardboardRenderer(getApplicationContext());
        cardboardView.setRenderer(cardboardRenderer);

        cardboardRenderer.setHalfWidthScrn(halfWidthScrn);

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

        cardboardRenderer.setBubbleTextLayout(bubbleTextLayout, bubbleTextLayoutL, bubbleTextLayoutR);
        cardboardRenderer.setBubbleText(textOverlay_L, textOverlay_R);

        cardboardRenderer.setActvty(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBGM();
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
        stopBGM();
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

        cardboardRenderer.setOriginalOpto(optographList.get(currentIndex));
    }

    private void initializeOptograph() {
        Intent intent = getIntent();
        if (intent != null) {
            optographList = this.getIntent().getParcelableArrayListExtra("opto_list");
            optoListSize = optographList.size();
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
        stopBGM();
        this.finish();
    }

    @Override
    public void onBackPressed() {
        stopBGM();
        // TODO: stop endless switching loop with timer?
        // switchToNormalMode();
    }


    public void initStoryChildrens() {
        Timber.d(getClass().getSimpleName(),"optographList.get(currentIndex).getStory() = "+optographList.get(currentIndex).getStory());
        if(optographList.get(currentIndex).getStory() != null && !optographList.get(currentIndex).getStory().getId().equals("") && optographList.get(currentIndex).getStory().getChildren().size() > 0){
            List<StoryChild> chldrns = optographList.get(currentIndex).getStory().getChildren();
            for(int a=0; a < chldrns.size(); a++){
                Timber.d(getClass().getSimpleName(),"chldrns.get(a).getStory_object_media_type() = "+chldrns.get(a).getStory_object_media_type());
                if(chldrns.get(a).getStory_object_media_type().equals("MUS")){
                    playBGM(chldrns.get(a).getStory_object_media_fileurl(), chldrns.get(a).getStory_object_media_filename());
                    continue;
                }else if(chldrns.get(a).getStory_object_media_type().equals("FXTXT")){
                    showFixTxt(chldrns.get(a).getStory_object_media_additional_data());
                }else{
                    SendStoryChild stryChld = new SendStoryChild();
                    stryChld.setStory_object_media_face(chldrns.get(a).getStory_object_media_face());
                    stryChld.setStory_object_media_type(chldrns.get(a).getStory_object_media_type());
//                    stryChld.setStory_object_rotation(chldrns.get(a).getStory_object_rotation());
//                    stryChld.setStory_object_position(chldrns.get(a).getStory_object_position());
                    stryChld.setStory_object_rotation(Arrays.asList("0","0","0"));
                    stryChld.setStory_object_position(Arrays.asList("0","0","0"));
                    stryChld.setStory_object_phi(String.valueOf(chldrns.get(a).getStory_object_phi()));
                    stryChld.setStory_object_theta(String.valueOf(chldrns.get(a).getStory_object_theta()));

                    stryChld.setStory_object_media_additional_data(chldrns.get(a).getStory_object_media_additional_data());

                    cardboardRenderer.planeSetter(stryChld);
                }
            }
            cardboardRenderer.setLoadingScreen(loadingScreen_L, loadingScreen_R, circleCountDownView_L, circleCountDownView_R);
        }
    }

    private void playBGM(String mp3Url, String fName){
        new AudioStreamWorkerTask(this, new AudioStreamWorkerTask.OnCacheCallback() {

            @Override
            public void onSuccess(FileInputStream fileInputStream) {
                Log.i(getClass().getSimpleName() + ".MediaPlayer", "now playing...");
                if (fileInputStream != null) {
                    // reset media player here if necessary
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(fileInputStream.getFD());
                        mediaPlayer.prepare();
                        mediaPlayer.setVolume(1f, 1f);
                        mediaPlayer.setLooping(false);
                        mediaPlayer.start();
                        mediaPlayer.setLooping(true);
                        fileInputStream.close();
                    } catch (IOException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(getClass().getSimpleName() + ".MediaPlayer", "fileDescriptor is not valid");
                }
            }

            @Override
            public void onError() {
                Log.e(getClass().getSimpleName() + ".MediaPlayer", "Can't play audio file");
            }
        }).execute("https://bucket.dscvr.com"+mp3Url);
    }
    private void stopBGM(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
        }
    }
    private void showFixTxt(String txt){
        Timber.d(getClass().getSimpleName(),"Showfixtext");
        storyFixTxt.setText(txt);
        storyFixTxt.setVisibility(View.VISIBLE);
    }
}