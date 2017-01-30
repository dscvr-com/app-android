package com.iam360.dscvr.removed_social.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.iam360.dscvr.R;
import com.iam360.dscvr.bus.BusProvider;
import com.iam360.dscvr.bus.RecordFinishedEvent;
import com.iam360.dscvr.record.GlobalState;
import com.iam360.dscvr.removed_social.viewmodels.LocalOptographManager;
import com.iam360.dscvr.util.DBHelper;
import com.squareup.otto.Subscribe;

import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class MainFeedFragment extends OptographListFragment implements View.OnClickListener {

    private boolean isFullScreenMode = false;
    private DBHelper mydb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mydb = new DBHelper(getContext());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.loadingScreen.setOnClickListener(this);

        Animation clockwiseRotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_clockwise);
        binding.circleBig.startAnimation(clockwiseRotateAnimation);
        Animation counterClockwiseRotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_counterclockwise);
        binding.circleSmall.startAnimation(counterClockwiseRotateAnimation);

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        binding.cameraBtn.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(GlobalState.isAnyJobRunning) {
            binding.cameraBtn.setEnabled(false);
            binding.recordProgress.setVisibility(View.VISIBLE);
        } else {
            binding.cameraBtn.setEnabled(true);
            binding.recordProgress.setVisibility(View.GONE);
        }

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
        binding.swipeRefreshLayout.setRefreshing(false);

    }

    private void loadLocalOptographs() {
        LocalOptographManager.getOptographs()
                .observeOn(AndroidSchedulers.mainThread())
                .filter(e -> mydb.inInLocalDB(e.getId()))
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

                Intent intent = new Intent(getActivity(), RingOptionActivity.class);
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
    }

    @Subscribe
    public void recordFinished(RecordFinishedEvent event) {
        initializeFeed();
    }

    public void toggleFullScreen() {
        if(isFullScreenMode) {
            binding.overlayLayout.setVisibility(View.VISIBLE);
            isFullScreenMode = false;
        } else {
            binding.overlayLayout.setVisibility(View.GONE);
            isFullScreenMode = true;
        }
    }

}