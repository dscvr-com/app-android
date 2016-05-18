package co.optonaut.optonaut.views.new_design;

import android.databinding.DataBindingUtil;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.optonaut.optonaut.NewFeedBinding;
import co.optonaut.optonaut.OptographDetailsBinding;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.network.ApiConsumer;
import co.optonaut.optonaut.util.Cache;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.viewmodels.InfiniteScrollListener;
import co.optonaut.optonaut.views.new_design.OptographFeedAdapter;
import co.optonaut.optonaut.views.SnappyLinearLayoutManager;
import co.optonaut.optonaut.views.SnappyRecyclerView;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public abstract class OptographListFragment extends Fragment {
    protected OptographFeedAdapter optographFeedAdapter;
    protected ApiConsumer apiConsumer;
    protected Cache cache;
    protected NewFeedBinding binding;

    public OptographListFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        optographFeedAdapter = new OptographFeedAdapter(getActivity());

    }

    //    Settings start
    private void initializeButtons() {
        if (cache.getInt(Cache.CAMERA_MODE)== Constants.ONE_RING_MODE)
            activeOneRing();
        else activeThreeRing();

        if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE)==Constants.MANUAL_MODE)
            activeManualType();
        else activeMotorType();
    }

    private void activeOneRing() {
        binding.oneRingButton.setBackgroundResource(R.drawable.one_ring_active_icn);
        binding.settingsOneRing.setTextColor(getResources().getColor(R.color.text_active));
        binding.threeRingButton.setBackgroundResource(R.drawable.three_ring_inactive_icn);
        binding.settingsThreeRing.setTextColor(getResources().getColor(R.color.text_inactive));
    }

    private void activeThreeRing() {
        binding.threeRingButton.setBackgroundResource(R.drawable.three_ring_active_icn);
        binding.settingsThreeRing.setTextColor(getResources().getColor(R.color.text_active));
        binding.oneRingButton.setBackgroundResource(R.drawable.one_ring_inactive_icn);
        binding.settingsOneRing.setTextColor(getResources().getColor(R.color.text_inactive));
    }

    private void activeManualType() {
        binding.manualButton.setBackgroundResource(R.drawable.manual_active_icn);
        binding.settingsManual.setTextColor(getResources().getColor(R.color.text_active));
        binding.motorButton.setBackgroundResource(R.drawable.motor_inactive_icn);
        binding.settingsMotor.setTextColor(getResources().getColor(R.color.text_inactive));
    }

    private void activeMotorType() {
        binding.motorButton.setBackgroundResource(R.drawable.motor_active_icn);
        binding.settingsMotor.setTextColor(getResources().getColor(R.color.text_active));
        binding.manualButton.setBackgroundResource(R.drawable.manual_inactive_icn);
        binding.settingsManual.setTextColor(getResources().getColor(R.color.text_inactive));
    }
    // Settings end

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.new_feed_fragment, container, false);
//        ButterKnife.bind(this, view);
        binding = DataBindingUtil.inflate(inflater, R.layout.new_feed_fragment, container, false);

//        Settings start
        initializeButtons();

        binding.oneRingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cache.getInt(Cache.CAMERA_MODE) != Constants.ONE_RING_MODE) {
                    cache.save(Cache.CAMERA_MODE, Constants.ONE_RING_MODE);
                    activeOneRing();
                }
            }
        });
        binding.threeRingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cache.getInt(Cache.CAMERA_MODE)!=Constants.THREE_RING_MODE) {
                    cache.save(Cache.CAMERA_MODE, Constants.THREE_RING_MODE);
                    activeThreeRing();
                }
            }
        });
        binding.manualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE)!=Constants.MANUAL_MODE) {
                    cache.save(Cache.CAMERA_CAPTURE_TYPE, Constants.MANUAL_MODE);
                    activeManualType();
                }
            }
        });

        binding.motorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE) != Constants.MOTOR_MODE) {
                    cache.save(Cache.CAMERA_CAPTURE_TYPE, Constants.MOTOR_MODE);
                    activeMotorType();
                }
            }
        });
//        Settings end

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        binding.optographFeed.setLayoutManager(llm);
        binding.optographFeed.setAdapter(optographFeedAdapter);
        binding.optographFeed.setItemViewCacheSize(5);

        binding.optographFeed.addOnScrollListener(new InfiniteScrollListener(llm) {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        });
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // load first few optographs
        initializeFeed();
    }

    public Optograph getCurrentOptograph() {
        Optograph optograph = null;

        if (!optographFeedAdapter.isEmpty()) {
            SnappyLinearLayoutManager lm = ((SnappyLinearLayoutManager) binding.optographFeed.getLayoutManager());
            optograph = optographFeedAdapter.get(lm.findFirstVisibleItemPosition());
        }

        return optograph;
    }

    protected abstract void initializeFeed();
    protected abstract void loadMore();
    protected abstract void refresh();


}
