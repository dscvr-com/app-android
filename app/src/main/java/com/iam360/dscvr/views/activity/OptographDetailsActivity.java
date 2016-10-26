package com.iam360.dscvr.views.activity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.iam360.dscvr.BR;
import com.iam360.dscvr.OptographDetailsBinding;
import com.iam360.dscvr.R;
import com.iam360.dscvr.model.LogInReturn;
import com.iam360.dscvr.model.MapiResponseObject;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.SendStoryChild;
import com.iam360.dscvr.model.StoryChild;
import com.iam360.dscvr.network.Api2Consumer;
import com.iam360.dscvr.network.ApiConsumer;
import com.iam360.dscvr.sensors.CombinedMotionManager;
import com.iam360.dscvr.sensors.GestureDetectors;
import com.iam360.dscvr.util.BubbleDrawable;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.CameraUtils;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.DBHelper;
import com.iam360.dscvr.util.GeneralUtils;
import com.iam360.dscvr.util.ImageUrlBuilder;
import com.iam360.dscvr.util.MixpanelHelper;
import com.iam360.dscvr.util.NotificationSender;
import com.iam360.dscvr.util.RFC3339DateFormatter;
import com.iam360.dscvr.views.VRModeActivity;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class OptographDetailsActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    private static final int MILLISECONDS_THRESHOLD_FOR_SWITCH = 250;
    private SensorManager sensorManager;
    private boolean inVRMode;

    private DateTime inVRPositionSince;
    private Optograph optograph;
    private ArrayList<Optograph> optographList;
    private boolean isFullScreenMode = false;
    private OptographDetailsBinding binding;
    private Cache cache;
    private DBHelper mydb;
    protected ApiConsumer apiConsumer;
    private AlertDialog alert = null;

    private boolean arrowClicked = false;
    private boolean isCurrentUser = false;

    private boolean hasSoftKey = false;
    private boolean isMultipleOpto = false;
    private int viewsWithSoftKey;

    private boolean withStory = false;
    private String storyType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MixpanelHelper.trackViewOptographDetails(this);

        optographList = this.getIntent().getParcelableArrayListExtra("opto_list");
        if(getIntent().getExtras().get("story") != null){
            withStory = (boolean) getIntent().getExtras().get("story");
            storyType = getIntent().getExtras().getString("type");
        }

        if(optographList != null) {
            isMultipleOpto = true;
            optograph = optographList.get(0);
        } else {
            isMultipleOpto = false;
            optograph = getIntent().getExtras().getParcelable("opto");
            optographList = new ArrayList<>();
        }

        cache = Cache.open();
        mydb = new DBHelper(this);
        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);

        if (getIntent().getExtras().getParcelable("notif")!=null) {
            new GeneralUtils().decrementBadgeCount(cache, this);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_optograph_details);
        binding.setVariable(BR.optograph, optograph);
        binding.setVariable(BR.person, optograph.getPerson());
        binding.setVariable(BR.location, optograph.getLocation());
        if(withStory){
            optograph.setWithStory(true);
            binding.setVariable(BR.story, optograph.getStory());

            BubbleDrawable myBubble = new BubbleDrawable(BubbleDrawable.CENTER);
            myBubble.setCornerRadius(20);
            myBubble.setPadding(25, 25, 25, 25);
            binding.bubbleTextLayout.setBackgroundDrawable(myBubble);

            binding.optograph2dview.setBubbleTextLayout(binding.bubbleTextLayout);
            binding.optograph2dview.setBubbleText(binding.bubbleText);
            binding.optograph2dview.setMyAct(this);
            binding.optograph2dview.setStoryType(storyType);
        }

        Log.d("mytTag", " delete: opto person's id: "+optograph.getPerson().getId()+" currentUserId: "+cache.getString(Cache.USER_ID)+" isLocal? "+optograph.is_local());
        if (optograph.is_local()) isCurrentUser = true;// TODO: if the Person table is created on the local DB remove this line and set the person's data on the optograph(OptographLocalGridAdapter->addItem(Optograph))
        if(optograph.getPerson().getId().equals(cache.getString(Cache.USER_ID))) isCurrentUser = true;
        if(isCurrentUser) {
            binding.followContainer.setVisibility(View.GONE);
            binding.follow.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);//change to VISIBLE if delete is needed here.
        } else binding.deleteButton.setVisibility(View.GONE);

        binding.badgeLayout.setVisibility(optograph.getPerson().isElite_status()?View.VISIBLE:View.GONE);

        if (optograph.is_local()) binding.shareContainer.setVisibility(View.GONE);

        instatiateFeedDisplayButton();

        inVRMode = false;
        inVRPositionSince = null;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        registerAccelerationListener();

//        binding.optograph2dview.setSensorMode(CombinedMotionManager.GYRO_MODE);
        binding.optograph2dview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isFullScreenMode) {
                    if (GestureDetectors.singleClickDetector.onTouchEvent(event)) {
                        if (GestureDetectors.TAP_TYPE == GestureDetectors.DOUBLE_TAP) {
                            //TODO uncomment for zooming
//                            binding.optograph2dview.toggleZoom();
                        }
                        //finish();
                        else toggleFullScreen();
                    } else {
                    }
                } else {
                    if (GestureDetectors.singleClickDetector.onTouchEvent(event)) {
                        if (GestureDetectors.TAP_TYPE == GestureDetectors.DOUBLE_TAP) {
                            //TODO uncomment for zooming
//                            binding.optograph2dview.toggleZoom();
                        }
                            //finish();
                        else toggleFullScreen();
                    } else {
                        // need to return true here to prevent touch-stealing of parent!
//                        return true;
                    }
                }
                return binding.optograph2dview.getOnTouchListener().onTouch(v, event);
            }
        });

        binding.menuLayout.setVisibility(View.INVISIBLE);// remove this line if settings is needed here

//        binding.profileBar.getBackground().setAlpha(204); // apha to 80%
        binding.closeBtn.setOnClickListener(this);
        binding.closeContainer.setOnClickListener(this);
        binding.profileBar.setOnClickListener(this);
        binding.personAvatarAsset.setOnClickListener(this);
        binding.personLocationInformation.setOnClickListener(this);
        binding.arrowMenu.setOnClickListener(this);
        binding.littlePlanetButton.setOnClickListener(this);
        binding.gyroButton.setOnClickListener(this);
        binding.exportButton.setOnClickListener(this);
        binding.vrButton.setOnClickListener(this);
        binding.heartContainer.setOnClickListener(this);
        binding.heartLabel.setOnClickListener(this);
        binding.shareContainer.setOnClickListener(this);
        binding.shareBtn.setOnClickListener(this);
        binding.followContainer.setOnClickListener(this);
        binding.follow.setOnClickListener(this);
        binding.deleteButton.setOnClickListener(this);

        setHeart(optograph.is_starred(), optograph.getStars_count());
        followPerson(optograph.getPerson().is_followed());

        adjustIfHasSoftKeys();
        getWindow().getDecorView().setSystemUiVisibility(viewsWithSoftKey);


        initStoryChildrens();

        Animation clockwiseRotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise);
        binding.circleBig.startAnimation(clockwiseRotateAnimation);
        Animation counterClockwiseRotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_counterclockwise);
        binding.circleSmall.startAnimation(counterClockwiseRotateAnimation);

    }

    private void hideShowAni() {
        if(arrowClicked){
            arrowClicked = false;
            binding.gyroButton.animate()
                    .translationYBy(0)
                    .translationY(-180)
                    .setDuration(300);
            binding.littlePlanetButton.animate()
                    .translationYBy(0)
                    .translationY(-90)
                    .setDuration(300);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.littlePlanetButton.setVisibility(View.GONE);
                    binding.gyroButton.setVisibility(View.GONE);
                    binding.arrowMenu.setBackgroundResource(R.drawable.arrow_down_icn);
                }
            }, 280);
//            getResources().getInteger(android.R.integer.config_mediumAnimTime)
        }else{
            arrowClicked = true;
            binding.littlePlanetButton.setVisibility(View.VISIBLE);
            binding.gyroButton.setVisibility(View.VISIBLE);
            binding.littlePlanetButton.animate()
                    .translationYBy(-60)
                    .translationY(0)
                    .setDuration(300);
            binding.gyroButton.animate()
                    .translationYBy(-60)
                    .translationY(0)
                    .setDuration(300);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.arrowMenu.setBackgroundResource(R.drawable.arrow_up_icn);
                }
            }, 280);
        }
    }

    private void startProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("person", optograph.getPerson());
        startActivity(intent);
    }

    private void switchToVRMode() {
        inVRMode = true;

        if(alert != null) alert.dismiss();

        if(isMultipleOpto) {
            Intent intent = new Intent(OptographDetailsActivity.this, VRModeActivity.class);
            intent.putParcelableArrayListExtra("opto_list", optographList);
            startActivity(intent);
        } else {
            optographList.add(optograph);
            Intent intent = new Intent(OptographDetailsActivity.this, VRModeActivity.class);
            intent.putParcelableArrayListExtra("opto_list", optographList);
            startActivity(intent);
//            Intent intent = new Intent(OptographDetailsActivity.this, VRModeActivity.class);
//            intent.putExtra("optograph", optograph);
//            startActivity(intent);
        }
    }

    private void setHeart(boolean liked, int count) {

        binding.heartLabel.setText(String.valueOf(count));
//        binding.heartLabel.setText("");
        if(liked) {
            mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, true);
            binding.heartLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.liked_icn, 0);
        } else {
            mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, false);
            binding.heartLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.like_icn, 0);
        }

        optograph.setIs_starred(liked);
        optograph.setStars_count(count);
    }

    private void followPerson(boolean isFollowed) {
        if(isFollowed) {
            optograph.getPerson().setIs_followed(true);
            optograph.getPerson().setFollowers_count(optograph.getPerson().getFollowers_count() + 1);
            binding.follow.setImageResource(R.drawable.feed_following_icn);
            NotificationSender.triggerSendNotification(optograph.getPerson(), "follow");
        } else {
            optograph.getPerson().setIs_followed(false);
            optograph.getPerson().setFollowers_count(optograph.getPerson().getFollowers_count() - 1);
            binding.follow.setImageResource(R.drawable.feed_follow_icn);
        }

        mydb.updateTableColumn(DBHelper.PERSON_TABLE_NAME, "id", optograph.getPerson().getId(), "is_followed", optograph.getPerson().is_followed());
        mydb.updateTableColumn(DBHelper.PERSON_TABLE_NAME, "id", optograph.getPerson().getId(), "followers_count", optograph.getPerson().getFollowers_count());
    }

//    http://stackoverflow.com/questions/20264268/how-to-get-height-and-width-of-navigation-bar-programmatically
//    adjustIfHasSoftKeys and isTablet
    private void adjustIfHasSoftKeys() {
            viewsWithSoftKey = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

        boolean hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);

        if(!hasMenuKey && !hasBackKey) {
            //The device has a navigation bar
            Resources resources = this.getResources();

            int orientation = getResources().getConfiguration().orientation;
            int resourceId;
            if (isTablet(this)) {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
            } else {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
            }

            if (resourceId > 0) {
                Log.d("myTag", " softkey: resourceId >0: " + resources.getDimensionPixelSize(resourceId) + " instance of Margin? " + (binding.profileBar.getLayoutParams() instanceof ViewGroup.MarginLayoutParams));
                if (binding.profileBar1.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) binding.profileBar1.getLayoutParams();
                    p.setMargins(0, 0, 0, resources.getDimensionPixelSize(resourceId));
                    binding.profileBar1.requestLayout();
                }
            }
        }
    }

    private boolean isTablet(Context c) {
        return (c.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private void toggleFullScreen() {
        adjustIfHasSoftKeys();
        if(!isFullScreenMode) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
            binding.closeContainer.setVisibility(View.INVISIBLE);
            binding.profileBar.setVisibility(View.INVISIBLE);
            binding.menuLayout.setVisibility(View.INVISIBLE);
            binding.menuLayout1.setVisibility(View.INVISIBLE);
            binding.deleteButton.setVisibility(View.INVISIBLE);
            binding.descriptionBar.setVisibility(View.INVISIBLE);
            isFullScreenMode = true;
        } else {
            getWindow().getDecorView().setSystemUiVisibility(viewsWithSoftKey);

            binding.closeContainer.setVisibility(View.VISIBLE);
            binding.profileBar.setVisibility(View.VISIBLE);
            if(isCurrentUser) binding.deleteButton.setVisibility(View.VISIBLE);
            binding.menuLayout.setVisibility(View.INVISIBLE);// change to VISIBLE if settings is needed here
            binding.menuLayout1.setVisibility(View.VISIBLE);// change to VISIBLE if settings is needed here
            binding.descriptionBar.setVisibility(View.VISIBLE);
            isFullScreenMode = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // only listen for Accelerometer if we did not yet start VR-activity and if we got an optograph
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !inVRMode) {
            // fire VRModeActivity if phone was turned
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float length = (float) Math.sqrt(x*x + y*y);
            if (length < Constants.MINIMUM_AXIS_LENGTH) {
                inVRPositionSince = null;

                return;
            }
            if (x > y + Constants.ACCELERATION_EPSILON) {
                if (inVRPositionSince == null) {
                    inVRPositionSince = DateTime.now();
                }
                Interval timePassed = new Interval(inVRPositionSince, DateTime.now());
                Duration duration = timePassed.toDuration();
                long milliseconds = duration.getMillis();
                if (milliseconds > MILLISECONDS_THRESHOLD_FOR_SWITCH) {
                    switchToVRMode();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    private void registerAccelerationListener() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterAccelerationListener() {
        sensorManager.unregisterListener(this);
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
        inVRMode = false;
    }


    private void gyroValidation() {
        boolean gyro = cache.getBoolean(Cache.GYRO_ENABLE,false);
        boolean lilPlanet = cache.getBoolean(Cache.LITTLE_PLANET_ENABLE,false);
        if (!gyro && lilPlanet) cache.save(Cache.LITTLE_PLANET_ENABLE,false);
        cache.save(Cache.GYRO_ENABLE, !gyro);
    }

    private void littlePlanetValidation() {
        boolean gyro = cache.getBoolean(Cache.GYRO_ENABLE,false);
        boolean lilPlanet = cache.getBoolean(Cache.LITTLE_PLANET_ENABLE,false);
        if (!lilPlanet && gyro) cache.save(Cache.GYRO_ENABLE,false);
        cache.save(Cache.LITTLE_PLANET_ENABLE, !lilPlanet);
    }

    private void instatiateFeedDisplayButton() {
        boolean gyro = cache.getBoolean(Cache.GYRO_ENABLE,false);
        boolean lilPlanet = cache.getBoolean(Cache.LITTLE_PLANET_ENABLE,false);

        if(gyro)
            binding.optograph2dview.setSensorMode(CombinedMotionManager.GYRO_MODE);
        else if(!gyro && !lilPlanet)
            binding.optograph2dview.setSensorMode(CombinedMotionManager.STILL_MODE); //PANNING MODE
        else
            binding.optograph2dview.setSensorMode(CombinedMotionManager.STILL_MODE);

        binding.gyroButton.setBackgroundResource(gyro?R.drawable.gyro_small_active_icn:R.drawable.gyro_small_inactive);
        binding.littlePlanetButton.setBackgroundResource(lilPlanet ? R.drawable.little_planet_active_icn_copy : R.drawable.little_planet_inactive_icn_copy);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_btn:
            case R.id.close_container:
                finish();
                break;
            case R.id.profile_bar:
                break;
            case R.id.person_location_information:
            case R.id.person_avatar_asset:
                startProfile();
                break;
            case R.id.arrow_menu:
                hideShowAni();
                break;
            case R.id.little_planet_button:
                littlePlanetValidation();
                instatiateFeedDisplayButton();
                break;
            case R.id.vr_button:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getResources().getString(R.string.dialog_vrmode_explanation));
                builder.setCancelable(true);

                builder.setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                alert = builder.create();
                alert.show();
                break;
            case R.id.gyro_button:
                gyroValidation();
                instatiateFeedDisplayButton();
                break;
            case R.id.share_container:
            case R.id.share_btn:
                Intent intent = new Intent(this, SharingActivity.class);
                intent.putExtra("opto", optograph);
                startActivity(intent);
                break;
            case R.id.heart_container:
            case R.id.heart_label:
                if(!cache.getString(Cache.USER_TOKEN).equals("")) {
                    if (!cache.getString(Cache.USER_TOKEN).equals("") && !optograph.is_starred()) {

                        setHeart(true, optograph.getStars_count() + 1);
                        apiConsumer.postStar(optograph.getId(), new Callback<LogInReturn.EmptyResponse>() {
                            @Override
                            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                                // revert star count on failure
                                if (!response.isSuccess()) {
                                    setHeart(false, optograph.getStars_count() - 1);
                                }else{
                                    if(!optograph.getId().equals(cache.getString(Cache.USER_ID)))
                                        NotificationSender.triggerSendNotification(optograph, "like", optograph.getId());
                                    Cursor res = mydb.getData(optograph.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
                                    res.moveToFirst();
                                    if (res.getCount() > 0) {
                                        mydb.updateTableColumn(DBHelper.OPTO_TABLE_NAME_FEEDS,DBHelper.OPTOGRAPH_ID, optograph.getId(), "optograph_is_starred", true);
                                        mydb.updateTableColumn(DBHelper.OPTO_TABLE_NAME_FEEDS,DBHelper.OPTOGRAPH_ID, optograph.getId(), "optograph_stars_count", optograph.getStars_count());
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                // revert star count on failure
                                setHeart(false, optograph.getStars_count() - 1);
                            }
                        });
                    } else if (optograph.is_starred()) {
                        setHeart(false, optograph.getStars_count() - 1);

                        apiConsumer.deleteStar(optograph.getId(), new Callback<LogInReturn.EmptyResponse>() {
                            @Override
                            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                                // revert star count on failure
                                if (!response.isSuccess()) {
                                    setHeart(true, optograph.getStars_count() + 1);
                                }else{
                                    Cursor res = mydb.getData(optograph.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
                                    res.moveToFirst();
                                    if (res.getCount() > 0) {
                                        mydb.updateTableColumn(DBHelper.OPTO_TABLE_NAME_FEEDS,DBHelper.OPTOGRAPH_ID, optograph.getId(), "optograph_is_starred", false);
                                        mydb.updateTableColumn(DBHelper.OPTO_TABLE_NAME_FEEDS,DBHelper.OPTOGRAPH_ID, optograph.getId(), "optograph_stars_count", optograph.getStars_count());
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                // revert star count on failure
                                setHeart(true, optograph.getStars_count() + 1);
                            }
                        });
                    }
                } else {
                    Snackbar.make(v, getString(R.string.profile_login_first), Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.follow_container:
            case R.id.follow:
                if (!cache.getString(Cache.USER_TOKEN).equals("")) {
                    if (optograph.getPerson().is_followed()) {
                        followPerson(false);
                        apiConsumer.unfollow(optograph.getPerson().getId(), new Callback<LogInReturn.EmptyResponse>() {
                            @Override
                            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                                // revert follow count on failure
                                if (!response.isSuccess()) {
                                    followPerson(true);
                                }
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                followPerson(true);
                                Timber.e("Error on unfollowing.");
                            }
                        });
                    } else if (!optograph.getPerson().is_followed()) {
                        followPerson(true);
                        apiConsumer.follow(optograph.getPerson().getId(), new Callback<LogInReturn.EmptyResponse>() {
                            @Override
                            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                                // revert follow count on failure
                                if (!response.isSuccess()) {
                                    followPerson(false);
                                }
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                followPerson(false);
                                Timber.e("Error on following.");
                            }
                        });
                    }
                } else {
                    Snackbar.make(v, getString(R.string.profile_login_first), Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.delete_button:
                deleteImageItemDialog(optograph);
                break;
            case R.id.export_button:
                exportImageDialog(optograph);
                break;
            default:
                break;
        }
    }

    private void deleteImageItemDialog(Optograph optograph) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(withStory){
            builder.setMessage(R.string.story_delete_message)
                    .setPositiveButton(getResources().getString(R.string.dialog_fire), (dialog, which) -> {
                        deleteStory(optograph.getStory().getId());
                    }).setNegativeButton(getResources().getString(R.string.cancel_label), (dialog, which) -> {
                dialog.dismiss();
            });
        }else{
            builder.setMessage(R.string.profile_delete_message)
                    .setPositiveButton(getResources().getString(R.string.dialog_fire), (dialog, which) -> {
                        deleteOptograph(optograph);
                    }).setNegativeButton(getResources().getString(R.string.cancel_label), (dialog, which) -> {
                dialog.dismiss();
            });
        }

        builder.create().show();
    }

    private void deleteStory(String storyId){
        binding.overlayDelete.setVisibility(View.VISIBLE);
        String token = cache.getString(Cache.USER_TOKEN);
        Api2Consumer api2Consumer = new Api2Consumer(token.equals("") ? null : token, "story");
        api2Consumer.deleteStory(storyId, new Callback<MapiResponseObject>() {
            @Override
            public void onResponse(Response<MapiResponseObject> response, Retrofit retrofit) {
                binding.overlayDelete.setVisibility(View.GONE);
                if (response.isSuccess()) {
                    Toast.makeText(OptographDetailsActivity.this, "Delete successful.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("id", optograph.getId());
                    intent.putExtra("local", false);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                binding.overlayDelete.setVisibility(View.GONE);
                Log.d("myTag", "ERROR: delete story : " + t.getMessage());
                Toast.makeText(OptographDetailsActivity.this, "Delete failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteOptograph(Optograph optograph) {
        binding.overlayDelete.setVisibility(View.VISIBLE);
        if (optograph.is_local()) {
            deleteOptographFromPhone(optograph.getId());
            mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_DELETED_AT, RFC3339DateFormatter.toRFC3339String(DateTime.now()));
            mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_TEXT, "deleted");
//            mydb.deleteEntry(DBHelper.FACES_TABLE_NAME,DBHelper.FACES_ID,optograph.getId());
//            mydb.deleteEntry(DBHelper.OPTO_TABLE_NAME,DBHelper.OPTOGRAPH_ID,optograph.getId());
            binding.overlayDelete.setVisibility(View.GONE);
            Toast.makeText(OptographDetailsActivity.this, "Delete successful.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra("id", optograph.getId());
            intent.putExtra("local",true);
            setResult(RESULT_OK, intent);
            finish();
            return;
        }
        apiConsumer.deleteOptonaut(optograph.getId(), new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                binding.overlayDelete.setVisibility(View.GONE);
                if (response.isSuccess()) {
                    mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_DELETED_AT, RFC3339DateFormatter.toRFC3339String(DateTime.now()));
                    mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_TEXT, "deleted");
                    Toast.makeText(OptographDetailsActivity.this, "Delete successful.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("id", optograph.getId());
                    intent.putExtra("local", false);
                    setResult(RESULT_OK, intent);
                    finish();
                } else
                    Toast.makeText(OptographDetailsActivity.this, "Delete failed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Throwable t) {
                binding.overlayDelete.setVisibility(View.GONE);
                Log.d("myTag", "ERROR: delete optograph: " + t.getMessage());
                Toast.makeText(OptographDetailsActivity.this, "Delete failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void deleteOptographFromPhone(String id) {
        Log.d("myTag", " delete: Path: " + CameraUtils.PERSISTENT_STORAGE_PATH + id);
        File dir = new File(CameraUtils.PERSISTENT_STORAGE_PATH + id);

        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    for (File file1: file.listFiles()) {
                        boolean result = file1.delete();
                        Log.d("myTag", " delete: getName: " + file1.getName() + " getPath: " + file1.getPath()+" delete: "+result);
                    }
                    boolean result = file.delete();
                    Log.d("myTag", "delete: getName: " + file.getName() + " getPath: " + file.getPath()+" delete: "+result);
                } else {
                    // ignore
                }
            }
            boolean result = dir.delete();
            Log.d("myTag", "delete: getName: " + dir.getName() + " getPath: " + dir.getPath() + " delete: " + result);
        }
    }

    private void exportImageDialog(Optograph optograph) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.profile_export_message)
                .setPositiveButton(getResources().getString(R.string.dialog_fire), (dialog, which) -> {
                    exportImage(optograph);
                }).setNegativeButton(getResources().getString(R.string.cancel_label), (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void exportImage(Optograph optograph) {
        String DIR_NAME = getResources().getString(R.string.app_name);
        String filename = optograph.getId() + ".jpg";
        String downloadUrlOfImage = ImageUrlBuilder.buildERUrl(optograph.getId());
        File direct = new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .getAbsolutePath() + "/" + DIR_NAME + "/");

        if (!direct.exists()) {
            direct.mkdir();
        }

        if(isOnline()) {
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(downloadUrlOfImage);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(filename)
                    .setMimeType("image/jpeg")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,
                            File.separator + DIR_NAME + File.separator + filename);

            dm.enqueue(request);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.dialog_network_retry));
            builder.setCancelable(true);

            builder.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            alert = builder.create();
            alert.show();
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public void initStoryChildrens() {
        if(optograph != null && optograph.getStory() != null && !optograph.getStory().getId().equals("") && optograph.getStory().getChildren().size() > 0){
            Log.d("MARK","initStoryChildrens  optograph.getStory().getId = "+optograph.getStory().getId());
            Log.d("MARK","initStoryChildrens  optograph.getStory().getChildren().size() = "+optograph.getStory().getChildren().size());
            Log.d("MARK","initStoryChildrens  optograph.getStory().getId = "+optograph.getStory().getId());
            List<StoryChild> chldrns = optograph.getStory().getChildren();
            for(int a=0; a < chldrns.size(); a++){
                Log.d("MARK","initStoryChildrens  chldrns.get(a).getStory_object_media_type() = "+chldrns.get(a).getStory_object_media_type());
                if(chldrns.get(a).getStory_object_media_type().equals("MUS")){
                    Log.d("MARK","initStoryChildrens  chldrns.get(a).getStory_object_media_fileurl() = "+chldrns.get(a).getStory_object_media_fileurl());
                    playBGM(chldrns.get(a).getStory_object_media_fileurl());
                }else if(chldrns.get(a).getStory_object_media_type().equals("FXTXT")){
                    showFixTxt(chldrns.get(a).getStory_object_media_additional_data());
                }else{
                    SendStoryChild stryChld = new SendStoryChild();
                    stryChld.setStory_object_media_face(chldrns.get(a).getStory_object_media_face());
                    stryChld.setStory_object_media_type(chldrns.get(a).getStory_object_media_type());
                    stryChld.setStory_object_rotation(chldrns.get(a).getStory_object_rotation());
                    stryChld.setStory_object_position(chldrns.get(a).getStory_object_position());
                    stryChld.setStory_object_media_additional_data(chldrns.get(a).getStory_object_media_additional_data());

                    binding.optograph2dview.planeSetter(stryChld);
                }
            }
            binding.optograph2dview.setLoadingScreen(binding.loadingScreen);
        }
    }


    private void playBGM(String mp3Url){
        MediaPlayer mp = new MediaPlayer();

        try {
            mp.setDataSource("https://bucket.dscvr.com"+mp3Url);
            mp.prepare();
            mp.start();
            mp.setLooping(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showFixTxt(String txt){
        binding.storyFixTxt.setText(txt);
        binding.storyFixTxt.setVisibility(View.VISIBLE);
    }


}
