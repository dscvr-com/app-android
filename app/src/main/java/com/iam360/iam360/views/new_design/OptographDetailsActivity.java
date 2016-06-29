package com.iam360.iam360.views.new_design;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import com.iam360.iam360.BR;
import com.iam360.iam360.OptographDetailsBinding;
import com.iam360.iam360.R;
import com.iam360.iam360.model.LogInReturn;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.sensors.CombinedMotionManager;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.Constants;
import com.iam360.iam360.util.DBHelper;
import com.iam360.iam360.util.RFC3339DateFormatter;
import com.iam360.iam360.views.GestureDetectors;
import com.iam360.iam360.views.VRModeActivity;
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
    private boolean isFullScreenMode = false;
    private OptographDetailsBinding binding;
    private Cache cache;
    private DBHelper mydb;
    protected ApiConsumer apiConsumer;

    private boolean arrowClicked = false;
    private boolean isCurrentUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        optograph = getIntent().getExtras().getParcelable("opto");
        cache = Cache.open();
        mydb = new DBHelper(this);
        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_optograph_details);
        binding.setVariable(BR.optograph, optograph);
        binding.setVariable(BR.person, optograph.getPerson());
        binding.setVariable(BR.location, optograph.getLocation());

        if(optograph.getPerson().getId().equals(cache.getString(Cache.USER_ID))) isCurrentUser = true;
        if(isCurrentUser) {
            binding.followContainer.setVisibility(View.GONE);
            binding.follow.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);//change to VISIBLE if delete is needed here.
        } else binding.deleteButton.setVisibility(View.GONE);

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
        binding.heartContainer.setOnClickListener(this);
        binding.heartLabel.setOnClickListener(this);
        binding.followContainer.setOnClickListener(this);
        binding.follow.setOnClickListener(this);
        binding.deleteButton.setOnClickListener(this);

        setHeart(optograph.is_starred(), optograph.getStars_count());
        followPerson(optograph.getPerson().is_followed());

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

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

        Intent intent = new Intent(OptographDetailsActivity.this, VRModeActivity.class);
        intent.putExtra("optograph", optograph);
        startActivity(intent);
    }

    private void setHeart(boolean liked, int count) {

        binding.heartLabel.setText(String.valueOf(count));
        if(liked) {
            mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, 1);
            binding.heartLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.liked_icn, 0);
        } else {
            mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, 0);
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
        } else {
            optograph.getPerson().setIs_followed(false);
            optograph.getPerson().setFollowers_count(optograph.getPerson().getFollowers_count() - 1);
            binding.follow.setImageResource(R.drawable.feed_follow_icn);
        }
    }

    private void toggleFullScreen() {
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
            isFullScreenMode = true;
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
            binding.closeContainer.setVisibility(View.VISIBLE);
            binding.profileBar.setVisibility(View.VISIBLE);
            binding.menuLayout.setVisibility(View.INVISIBLE);// change to VISIBLE if settings is needed here
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

        binding.gyroButton.setBackgroundResource(gyro?R.drawable.gyro_active_icn_copy:R.drawable.gyro_inactive_icn_copy);
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
            case R.id.gyro_button:
                gyroValidation();
                instatiateFeedDisplayButton();
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
            default:
                break;
        }

    }

    private void deleteImageItemDialog(Optograph optograph) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.profile_delete_message)
                .setPositiveButton(getResources().getString(R.string.dialog_fire), (dialog, which) -> {
                    deleteOptograph(optograph);
                }).setNegativeButton(getResources().getString(R.string.cancel_label), (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void deleteOptograph(Optograph optograph) {
        apiConsumer.deleteOptonaut(optograph.getId(), new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_DELETED_AT, RFC3339DateFormatter.toRFC3339String(DateTime.now()));
                    mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_TEXT, "deleted");
                    Log.d("myTag", " time: " + RFC3339DateFormatter.toRFC3339String(DateTime.now()) + " text: " + optograph.getText() + " delAt: " + optograph.getDeleted_at());
                    Toast.makeText(OptographDetailsActivity.this, "Delete successful.", Toast.LENGTH_SHORT).show();
                    finish();
                } else Toast.makeText(OptographDetailsActivity.this, "Delete failed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag", "ERROR: delete optograph: " + t.getMessage());
                Toast.makeText(OptographDetailsActivity.this, "Delete failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
