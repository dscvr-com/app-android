package co.optonaut.optonaut.views.new_design;

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

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.bus.BusProvider;
import co.optonaut.optonaut.bus.RecordFinishedEvent;
import co.optonaut.optonaut.record.GlobalState;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.util.MixpanelHelper;
import co.optonaut.optonaut.viewmodels.LocalOptographManager;
import co.optonaut.optonaut.views.MainActivityRedesign;
import co.optonaut.optonaut.views.VRModeActivity;
import co.optonaut.optonaut.views.dialogs.NetworkProblemDialog;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;


/**
 * @author Nilan Marktanner
 * @date 2015-12-15
 */
public class MainFeedFragment extends OptographListFragment implements View.OnClickListener {
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
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileButton.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        inVRMode = false;
        BusProvider.getInstance().register(this);
        if (GlobalState.shouldHardRefreshFeed) {
            initializeFeed();
        }
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

    @Subscribe
    public void recordFinished(RecordFinishedEvent event) {
        initializeFeed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_btn:
                ((MainActivity) getActivity()).setPage(MainActivity.PROFILE_MODE);
                break;
        }

    }
}
