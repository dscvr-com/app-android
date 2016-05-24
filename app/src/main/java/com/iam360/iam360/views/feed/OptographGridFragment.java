package com.iam360.iam360.views.feed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bartoszlipinski.recyclerviewheader2.RecyclerViewHeader;

import com.iam360.iam360.R;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.viewmodels.InfiniteScrollListener;
import com.iam360.iam360.viewmodels.OptographGridAdapter;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public abstract class OptographGridFragment extends Fragment {
    protected OptographGridAdapter optographFeedAdapter;
    protected ApiConsumer apiConsumer;
    protected RecyclerView recList;
    protected RecyclerViewHeader header;
    public static final int NUM_COLUMNS = 3;

    protected Cache cache;


    public OptographGridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cache = Cache.open();

        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        optographFeedAdapter = new OptographGridAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.grid_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recList = (RecyclerView) view.findViewById(R.id.optographFeed);

//        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
//        GridLayoutManager llm = new GridLayoutManager(view.getContext(), NUM_COLUMNS);
        recList.setLayoutManager(llm);
        recList.setAdapter(optographFeedAdapter);
        recList.setItemViewCacheSize(10);

        recList.addOnScrollListener(new InfiniteScrollListener(llm) {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        });

        header = (RecyclerViewHeader) view.findViewById(R.id.header);
        header.attachTo(recList);
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
            GridLayoutManager lm = ((GridLayoutManager) recList.getLayoutManager());
            optograph = optographFeedAdapter.get(lm.findFirstVisibleItemPosition());
        }

        return optograph;
    }

    protected abstract void initializeFeed();
    protected abstract void loadMore();
    protected abstract void refresh();

}
