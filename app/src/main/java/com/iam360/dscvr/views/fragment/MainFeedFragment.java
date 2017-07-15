package com.iam360.dscvr.views.fragment;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.iam360.dscvr.R;
import com.iam360.dscvr.bus.BusProvider;
import com.iam360.dscvr.bus.RecordFinishedEvent;
import com.iam360.dscvr.record.GlobalState;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.viewmodels.LocalOptographManager;
import com.iam360.dscvr.views.VRModeActivity;
import com.iam360.dscvr.views.activity.RecorderActivity;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class MainFeedFragment extends OptographListFragment implements View.OnClickListener, SensorEventListener {

    private static final int MILLISECONDS_THRESHOLD_FOR_SWITCH = 250;
    private DateTime inVRPositionSince = null;
    private SensorManager sensorManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.loadingScreen.setOnClickListener(this);

        Animation clockwiseRotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_clockwise);
        binding.circleBig.startAnimation(clockwiseRotateAnimation);
        Animation counterClockwiseRotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_counterclockwise);
        binding.circleSmall.startAnimation(counterClockwiseRotateAnimation);
        binding.cameraBtn.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterAccelerationListener();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerAccelerationListener();

        if(GlobalState.isAnyJobRunning) {
            binding.cameraBtn.setEnabled(false);
            binding.recordProgress.setVisibility(View.VISIBLE);
        } else {
            binding.cameraBtn.setEnabled(true);
            binding.recordProgress.setVisibility(View.GONE);
        }
        initializeFeed();

        BusProvider.getInstance().register(this);
    }

    @Override
    public void initializeFeed() {
        loadLocalOptographs();
        GlobalState.shouldHardRefreshFeed = false;
    }

    @Override
    public void loadMore() {
        loadLocalOptographs();
    }

    @Override
    public void refresh() {
        loadLocalOptographs();
        mLayoutManager.scrollToPosition(0);
    }

    private void loadLocalOptographs() {
        LocalOptographManager.getOptographs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(optographFeedAdapter::addItem);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loading_screen:
            case R.id.tap_to_hide:
                binding.loadingScreen.setVisibility(View.GONE);
                break;
            case R.id.camera_btn:
                if (GlobalState.isAnyJobRunning) {
                    Snackbar.make(binding.cameraBtn, R.string.dialog_wait_on_record_finish, Snackbar.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(getActivity(), RecorderActivity.class);
                startActivity(intent);

                break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Subscribe
    public void receiveFinishedImage(RecordFinishedEvent recordFinishedEvent) {
        Timber.d("receiveFinishedImage");
        binding.recordProgress.setVisibility(View.GONE);
        binding.cameraBtn.setEnabled(true);
        binding.optographFeed.scrollToPosition(0);//FIXME does this work?
    }

    @Subscribe
    public void recordFinished(RecordFinishedEvent event) {
        initializeFeed();
    }

    public boolean toggleFullScreen(boolean isFullScreenMode) {
        if(isFullScreenMode) {
            binding.overlayLayout.setVisibility(View.VISIBLE);
            return false;
        } else {
            binding.overlayLayout.setVisibility(View.GONE);
            return true;
        }
    }

    public void switchToVRMode() {

        if(optographFeedAdapter.getItemCount() > 0) {
            Intent intent = new Intent(getActivity(), VRModeActivity.class);
            intent.putParcelableArrayListExtra("opto_list", optographFeedAdapter.getNextOptographList(firstVisible, 5));
            startActivity(intent);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // only listen for Accelerometer if we did not yet start VR-activity and if we got an optograph
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
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
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void registerAccelerationListener() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterAccelerationListener() {
        sensorManager.unregisterListener(this);
    }
}