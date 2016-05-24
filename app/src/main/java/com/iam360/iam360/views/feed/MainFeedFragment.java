package com.iam360.iam360.views.feed;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import com.iam360.iam360.bus.BusProvider;
import com.iam360.iam360.bus.RecordFinishedEvent;
import com.iam360.iam360.record.GlobalState;
import com.iam360.iam360.util.Constants;
import com.iam360.iam360.util.MixpanelHelper;
import com.iam360.iam360.viewmodels.LocalOptographManager;
import com.iam360.iam360.views.VRModeActivity;
import com.iam360.iam360.views.dialogs.NetworkProblemDialog;
import com.iam360.iam360.views.MainActivityRedesign;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;


/**
 * @author Nilan Marktanner
 * @date 2015-12-15
 */
public class MainFeedFragment extends OptographListFragment implements SensorEventListener {
    public static final String TAG = MainFeedFragment.class.getSimpleName();
    private static final int MILLISECONDS_THRESHOLD_FOR_SWITCH = 250;

    NetworkProblemDialog networkProblemDialog;

    private SensorManager sensorManager;
    private boolean inVRMode;

    private DateTime inVRPositionSince;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        networkProblemDialog = new NetworkProblemDialog();
        inVRMode = false;
        inVRPositionSince = null;
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        registerAccelerationListener();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        inVRMode = false;
        BusProvider.getInstance().register(this);
        if (GlobalState.shouldHardRefreshFeed) {
            initializeFeed();
        }
    }

    private void registerAccelerationListener() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterAccelerationListener() {
        sensorManager.unregisterListener(this);
    }


    @Override
    protected void initializeFeed() {
        LocalOptographManager.getOptographs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(optographFeedAdapter::addItem);

        apiConsumer.getOptographs(5)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(() -> MixpanelHelper.trackViewViewer2D(getActivity()))
                .onErrorReturn(throwable -> {
                    networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                    return null;
                })
                .subscribe(optographFeedAdapter::addItem);

        GlobalState.shouldHardRefreshFeed = false;
    }

    @Override
    protected void loadMore() {
        apiConsumer.getOptographs(50, optographFeedAdapter.getOldest().getCreated_at())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
                    networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                    return null;
                })
                .subscribe(optographFeedAdapter::addItem);

        // TODO: prefetch textures
    }

    @Override
    protected void refresh() {
        // TODO: actually refresh data
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // only listen for Accelerometer if we did not yet start VR-activity and if we got an optograph
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !inVRMode && !optographFeedAdapter.isEmpty()) {
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

    private void switchToVRMode() {
        Timber.v("switching to VR mode");
        inVRMode = true;
        MainActivityRedesign activity = (MainActivityRedesign) getActivity();
        activity.prepareVRMode();

        Intent intent = new Intent(activity, VRModeActivity.class);
        intent.putExtra("optograph", getCurrentOptograph());
        activity.startActivity(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Subscribe
    public void recordFinished(RecordFinishedEvent event) {
        initializeFeed();
    }
}
