package co.optonaut.optonaut.views;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;

import co.optonaut.optonaut.network.ApiConsumer;
import co.optonaut.optonaut.viewmodels.OptographFeedAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Nilan Marktanner
 * @date 2015-12-15
 */
public class MainFeedFragment extends OptographListFragment {
    private SensorManager sensorManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
    }


    @Override
    protected void initializeFeed() {
        apiConsumer.getOptographs(2)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(optographFeedAdapter::addItem);
    }

    @Override
    protected void loadMore() {
        apiConsumer.getOptographs(5, optographFeedAdapter.getOldest().getCreated_at())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(optographFeedAdapter::addItem);

        // TODO: prefetch textures
    }

    @Override
    protected void refresh() {
        // TODO: actually refresh data
        swipeContainer.setRefreshing(false);
    }
}
