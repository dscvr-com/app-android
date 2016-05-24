package com.iam360.iam360.views.feed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iam360.iam360.R;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.viewmodels.InfiniteScrollListener;
import com.iam360.iam360.viewmodels.OptographFeedAdapter;
import com.iam360.iam360.views.SnappyLinearLayoutManager;
import com.iam360.iam360.views.SnappyRecyclerView;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public abstract class OptographListFragment extends Fragment {
    protected OptographFeedAdapter optographFeedAdapter;
    protected ApiConsumer apiConsumer;
    protected Cache cache;
    protected SnappyRecyclerView recList;


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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.feed_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recList = (SnappyRecyclerView) view.findViewById(R.id.optographFeed);
        // our children have fixed size
        recList.setHasFixedSize(true);
        SnappyLinearLayoutManager llm = new SnappyLinearLayoutManager(view.getContext());
        llm.setOrientation(SnappyLinearLayoutManager.VERTICAL);
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
