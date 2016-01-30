package co.optonaut.optonaut.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import co.optonaut.optonaut.util.Constants;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Nilan Marktanner
 * @date 2015-12-15
 */
public class MainFeedFragment extends OptographListFragment implements SensorEventListener {
    private SensorManager sensorManager;
    private boolean inVRMode;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        inVRMode = false;
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
    }

    @Override
    public void onResume() {
        super.onResume();
        registerAccelerationListener();
        inVRMode = false;
    }

    private void registerAccelerationListener() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterAccelerationListener() {
        sensorManager.unregisterListener(this);
    }


    @Override
    protected void initializeFeed() {
        apiConsumer.getOptographs(5)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(optographFeedAdapter::addItem);
    }

    @Override
    protected void loadMore() {
        apiConsumer.getOptographs(50, optographFeedAdapter.getOldest().getCreated_at())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
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

            if (x > y + Constants.ACCELERATION_EPSILON) {
                switchToVRMode();
            }
        }
    }

    private void switchToVRMode() {
        Log.d(Constants.DEBUG_TAG, "Switched to VRMode");
        inVRMode = true;
        Activity activity = getActivity();
        Intent intent = new Intent(activity, VRModeActivity.class);
        intent.putExtra("optograph", getCurrentOptograph());

        //activity.finish();

        activity.startActivity(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
