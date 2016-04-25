package co.optonaut.optonaut.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.network.ApiConsumer;
import co.optonaut.optonaut.util.Cache;
import co.optonaut.optonaut.viewmodels.InfiniteScrollListener;
import co.optonaut.optonaut.viewmodels.OptographFeedAdapter;
import co.optonaut.optonaut.views.redesign.SnappyLinearLayoutManager;
import co.optonaut.optonaut.views.redesign.SnappyRecyclerView;
import timber.log.Timber;

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
