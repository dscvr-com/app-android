package co.optonaut.optonaut.views.new_design;

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

    @Bind(R.id.optographFeed) protected SnappyRecyclerView recList;
    @Bind(R.id.profile_btn) protected ImageButton profileButton;
    @Bind(R.id.camera_btn) protected ImageButton cameraButton;
    @Bind(R.id.settings_btn) protected ImageButton settingsButton;
    @Bind(R.id.theta_btn) protected ImageButton thetaButton;
    @Bind(R.id.header_logo) protected ImageButton headerLogoButton;
    @Bind(R.id.sliding_layout) protected SlidingUpPanelLayout slidingUpPanelLayout;
    @Bind(R.id.bar_transparent) protected View barTransparent;

    //    Settings
    @Bind(R.id.one_ring_button) protected ImageButton oneRingButton;
    @Bind(R.id.three_ring_button) protected ImageButton threeRingButton;
    @Bind(R.id.manual_button) protected ImageButton manualButton;
    @Bind(R.id.motor_button) protected ImageButton motorButton;
    @Bind(R.id.one_ring_text) protected TextView oneRingText;
    @Bind(R.id.three_ring_text) protected TextView threeRingText;
    @Bind(R.id.manual_text) protected TextView manualText;
    @Bind(R.id.motor_text) protected TextView motorText;


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
        oneRingButton.setBackgroundResource(R.drawable.one_ring_active_icn);
        oneRingText.setTextColor(getResources().getColor(R.color.text_active));
        threeRingButton.setBackgroundResource(R.drawable.three_ring_inactive_icn);
        threeRingText.setTextColor(getResources().getColor(R.color.text_inactive));
    }

    private void activeThreeRing() {
        threeRingButton.setBackgroundResource(R.drawable.three_ring_active_icn);
        threeRingText.setTextColor(getResources().getColor(R.color.text_active));
        oneRingButton.setBackgroundResource(R.drawable.one_ring_inactive_icn);
        oneRingText.setTextColor(getResources().getColor(R.color.text_inactive));
    }

    private void activeManualType() {
        manualButton.setBackgroundResource(R.drawable.manual_active_icn);
        manualText.setTextColor(getResources().getColor(R.color.text_active));
        motorButton.setBackgroundResource(R.drawable.motor_inactive_icn);
        motorText.setTextColor(getResources().getColor(R.color.text_inactive));
    }

    private void activeMotorType() {
        motorButton.setBackgroundResource(R.drawable.motor_active_icn);
        motorText.setTextColor(getResources().getColor(R.color.text_active));
        manualButton.setBackgroundResource(R.drawable.manual_inactive_icn);
        manualText.setTextColor(getResources().getColor(R.color.text_inactive));
    }
    // Settings end

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_feed_fragment, container, false);
        ButterKnife.bind(this, view);

//        Settings start
        initializeButtons();

        oneRingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cache.getInt(Cache.CAMERA_MODE) != Constants.ONE_RING_MODE) {
                    cache.save(Cache.CAMERA_MODE, Constants.ONE_RING_MODE);
                    activeOneRing();
                }
            }
        });
        threeRingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cache.getInt(Cache.CAMERA_MODE)!=Constants.THREE_RING_MODE) {
                    cache.save(Cache.CAMERA_MODE, Constants.THREE_RING_MODE);
                    activeThreeRing();
                }
            }
        });
        manualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE)!=Constants.MANUAL_MODE) {
                    cache.save(Cache.CAMERA_CAPTURE_TYPE, Constants.MANUAL_MODE);
                    activeManualType();
                }
            }
        });
        motorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE) != Constants.MOTOR_MODE) {
                    cache.save(Cache.CAMERA_CAPTURE_TYPE, Constants.MOTOR_MODE);
                    activeMotorType();
                }
            }
        });
//        Settings end

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setAdapter(optographFeedAdapter);
        recList.setItemViewCacheSize(5);

        recList.addOnScrollListener(new InfiniteScrollListener(llm) {
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
            SnappyLinearLayoutManager lm = ((SnappyLinearLayoutManager) recList.getLayoutManager());
            optograph = optographFeedAdapter.get(lm.findFirstVisibleItemPosition());
        }

        return optograph;
    }

    protected abstract void initializeFeed();
    protected abstract void loadMore();
    protected abstract void refresh();


}
