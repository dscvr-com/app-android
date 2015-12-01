package co.optonaut.optonaut.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import java.util.List;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.bus.BusProvider;
import co.optonaut.optonaut.bus.OptographsReceivedEvent;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.network.FeedManager;
import co.optonaut.optonaut.util.FeedMerger;
import co.optonaut.optonaut.viewmodels.InfiniteScrollListener;
import co.optonaut.optonaut.viewmodels.OptographAdapter;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public class FeedFragment extends Fragment {
    private OptographAdapter adapter;
    private SwipeRefreshLayout swipeContainer;

    public FeedFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recList = (RecyclerView) view.findViewById(R.id.optographFeed);
        // our children have fixed size
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(view.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        adapter = new OptographAdapter();
        recList.setAdapter(adapter);

        recList.addOnScrollListener(new InfiniteScrollListener(llm) {
            @Override
            public void onLoadMore(int current_page) {
                FeedManager.loadOlderThan(adapter.last().getCreated_at());
            }
        });


        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FeedManager.reinitializeFeed();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        FeedManager.reinitializeFeed();

    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void reveiceOptographs(OptographsReceivedEvent optographsReceivedEvent) {
        swipeContainer.setRefreshing(false);
        List<Optograph> optographs = optographsReceivedEvent.getOptographs();
        adapter.setOptographs(FeedMerger.mergeOptographsIntoFeed(adapter.getOptographs(), optographs));

    }
}
