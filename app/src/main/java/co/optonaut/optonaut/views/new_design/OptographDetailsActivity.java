package co.optonaut.optonaut.views.new_design;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.opengl.Optograph2DCubeView;
import co.optonaut.optonaut.record.GlobalState;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.views.GestureDetectors;
import co.optonaut.optonaut.views.MainActivityRedesign;
import co.optonaut.optonaut.views.VRModeActivity;
import timber.log.Timber;

public class OptographDetailsActivity extends AppCompatActivity implements SensorEventListener {
    private static final int MILLISECONDS_THRESHOLD_FOR_SWITCH = 250;
    private SensorManager sensorManager;
    private boolean inVRMode;

    private DateTime inVRPositionSince;
    private Optograph optograph;
    private boolean isFullScreenMode = false;
    private OptographDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        optograph = getIntent().getExtras().getParcelable("opto");

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
                        toggleFullScreen();
//                        return binding.optograph2dview.getOnTouchListener().onTouch(v, event);
                    } else {
//                        return binding.optograph2dview.getOnTouchListener().onTouch(v, event);
                    }
                } else {
                    if (GestureDetectors.singleClickDetector.onTouchEvent(event)) {
                        toggleFullScreen();
//                        return true;
                    } else {
                        // need to return true here to prevent touch-stealing of parent!
//                        return true;
                    }
                }
                binding.optograph2dview.toggleRegisteredOnSensors();
                return binding.optograph2dview.getOnTouchListener().onTouch(v, event);
            }
        });

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

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
            isFullScreenMode = true;
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
            binding.profileBar.setVisibility(View.VISIBLE);
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
