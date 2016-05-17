package co.optonaut.optonaut.views.new_design;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import co.optonaut.optonaut.BR;
import co.optonaut.optonaut.OptographDetailsBinding;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.bus.BusProvider;
import co.optonaut.optonaut.model.LogInReturn;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.network.ApiConsumer;
import co.optonaut.optonaut.opengl.Optograph2DCubeView;
import co.optonaut.optonaut.record.GlobalState;
import co.optonaut.optonaut.util.Cache;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.util.DBHelper;
import co.optonaut.optonaut.views.GestureDetectors;
import co.optonaut.optonaut.views.MainActivityRedesign;
import co.optonaut.optonaut.views.VRModeActivity;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class OptographDetailsActivity extends AppCompatActivity implements SensorEventListener {
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
    private boolean gyroActive = false;
    private boolean littlePlanetActive = false;

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

        inVRMode = false;
        inVRPositionSince = null;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        registerAccelerationListener();

        binding.optograph2dview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isFullScreenMode) {
                    if (GestureDetectors.singleClickDetector.onTouchEvent(event)) {
                        if (GestureDetectors.TAP_TYPE == GestureDetectors.DOUBLE_TAP)
                            finish();
                        toggleFullScreen();
                    } else {
                    }
                } else {
                    if (GestureDetectors.singleClickDetector.onTouchEvent(event)) {
                        if (GestureDetectors.TAP_TYPE == GestureDetectors.DOUBLE_TAP)
                            finish();
                        toggleFullScreen();
                    } else {
                        // need to return true here to prevent touch-stealing of parent!
//                        return true;
                    }
                }
                return binding.optograph2dview.getOnTouchListener().onTouch(v, event);
            }
        });

        binding.personAvatarAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProfile();
            }
        });

        binding.arrowMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideShowAni();
            }
        });

        binding.littlePlanetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (littlePlanetActive) {
                    littlePlanetActive = false;
                    binding.littlePlanetButton.setBackgroundResource(R.drawable.little_planet_inactive_icn);
                } else {
                    littlePlanetActive = true;
                    binding.littlePlanetButton.setBackgroundResource(R.drawable.little_planet_active_icn);
                }
            }
        });

        binding.gyroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gyroActive) {
                    gyroActive = false;
                    binding.gyroButton.setBackgroundResource(R.drawable.gyro_inactive_icn);
                } else {
                    gyroActive = true;
                    binding.gyroButton.setBackgroundResource(R.drawable.gyro_active_icn);
                }
            }
        });

        binding.heartLabel.setTypeface(Constants.getInstance().getIconTypeface());
        binding.heartLabel.setOnClickListener(v -> {
//                Snackbar.make(v, holder.itemView.getResources().getString(R.string.feature_favorites_soon), Snackbar.LENGTH_SHORT).show();
            if (!cache.getString(Cache.USER_TOKEN).equals("") && !optograph.is_starred()) {
//                    userLikesOptograph = true;
                mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, 1);
                optograph.setIs_starred(true);
                optograph.setStars_count(optograph.getStars_count() + 1);
                binding.heartLabel.setText(getResources().getString(R.string.heart_count, optograph.getStars_count(), String.valueOf((char) 0xe90d)));

                apiConsumer.postStar(optograph.getId(), new Callback<LogInReturn.EmptyResponse>() {
                    @Override
                    public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                        if (!response.isSuccess()) {
                            mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, 0);
                            optograph.setIs_starred(response.isSuccess());
                            optograph.setStars_count(optograph.getStars_count() - 1);
                            binding.heartLabel.setText(getResources().getString(R.string.heart_count, optograph.getStars_count(), String.valueOf((char) 0xe90d)));
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, 0);
                        optograph.setIs_starred(false);
                        optograph.setStars_count(optograph.getStars_count() - 1);
                    }
                });
            } else if (!cache.getString(Cache.USER_TOKEN).equals("") && optograph.is_starred()) {
                mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, 0);
                optograph.setIs_starred(false);
                optograph.setStars_count(optograph.getStars_count() - 1);
                binding.heartLabel.setText(getResources().getString(R.string.heart_count, optograph.getStars_count(), String.valueOf((char) 0xe90d)));

                apiConsumer.deleteStar(optograph.getId(), new Callback<LogInReturn.EmptyResponse>() {
                    @Override
                    public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                        if (!response.isSuccess()) {
                            mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, 1);
                            optograph.setIs_starred(response.isSuccess());
                            optograph.setStars_count(optograph.getStars_count() + 1);
                            binding.heartLabel.setText(getResources().getString(R.string.heart_count, optograph.getStars_count(), String.valueOf((char) 0xe90d)));
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, 1);
                        optograph.setIs_starred(true);
                        optograph.setStars_count(optograph.getStars_count() + 1);
                    }
                });
            } else {
//                    Snackbar.make(v,"Login first.",Snackbar.LENGTH_SHORT).show();
//                    MainActivityRedesign activity = (MainActivityRedesign) context;
//                    activity.prepareProfile(false);
            }
        });

        binding.heartLabel.setText(getResources().getString(R.string.heart_count, optograph.getStars_count(), String.valueOf((char) 0xe90d)));

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
                    .translationY(-120)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            binding.littlePlanetButton.animate()
                    .translationYBy(0)
                    .translationY(-60)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.littlePlanetButton.setVisibility(View.GONE);
                    binding.gyroButton.setVisibility(View.GONE);
                    binding.arrowMenu.setBackgroundResource(R.drawable.arrow_down_icn);
                }
            }, getResources().getInteger(android.R.integer.config_mediumAnimTime));
        }else{
            arrowClicked = true;
            binding.littlePlanetButton.setVisibility(View.VISIBLE);
            binding.gyroButton.setVisibility(View.VISIBLE);
            binding.littlePlanetButton.animate()
                    .translationYBy(-30)
                    .translationY(0)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            binding.gyroButton.animate()
                    .translationYBy(-30)
                    .translationY(0)
                    .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.arrowMenu.setBackgroundResource(R.drawable.arrow_up_icn);
                }
            }, getResources().getInteger(android.R.integer.config_mediumAnimTime));
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
            binding.profileBar.setVisibility(View.INVISIBLE);
            binding.menuLayout.setVisibility(View.INVISIBLE);
            isFullScreenMode = true;
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
            binding.profileBar.setVisibility(View.VISIBLE);
            binding.menuLayout.setVisibility(View.VISIBLE);
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
}
