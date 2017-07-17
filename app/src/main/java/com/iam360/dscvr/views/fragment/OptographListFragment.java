package com.iam360.dscvr.views.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iam360.dscvr.AAFeedBinding;
import com.iam360.dscvr.R;
import com.iam360.dscvr.viewmodels.InfiniteScrollListener;
import com.iam360.dscvr.viewmodels.OptographVideoFeedAdapter;
import com.iam360.dscvr.util.Cache;

public abstract class OptographListFragment extends Fragment {
    protected OptographVideoFeedAdapter optographFeedAdapter;
    protected Cache cache;
    protected AAFeedBinding binding;
    protected int firstVisible = 0;

    LinearLayoutManager mLayoutManager;

    public OptographListFragment() {
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cache = Cache.open();
        optographFeedAdapter = new OptographVideoFeedAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.aa_feed_fragment, container, false);
        mLayoutManager = new LinearLayoutManager(getContext());

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.optographFeed.setLayoutManager(mLayoutManager);
        binding.optographFeed.setAdapter(optographFeedAdapter);
        binding.optographFeed.setItemViewCacheSize(5);

        DefaultItemAnimator animator = new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
                return true;
            }
        };
        binding.optographFeed.setItemAnimator(animator);

        binding.optographFeed.addOnScrollListener(new InfiniteScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore() {
                loadMore();
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                firstVisible = mLayoutManager.findFirstCompletelyVisibleItemPosition();
            }
        });

        binding.optographFeed.setIsScrollable(false);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeFeed();
    }

    public abstract void initializeFeed();
    public abstract void loadMore();
    public abstract void refresh();

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}