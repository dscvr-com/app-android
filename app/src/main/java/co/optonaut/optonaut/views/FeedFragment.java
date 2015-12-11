package co.optonaut.optonaut.views;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.bus.BusProvider;
import co.optonaut.optonaut.bus.OptographsReceivedEvent;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.network.ApiConsumer;
import co.optonaut.optonaut.network.FeedManager;
import co.optonaut.optonaut.util.FeedMerger;
import co.optonaut.optonaut.viewmodels.InfiniteScrollListener;
import co.optonaut.optonaut.viewmodels.OptographFeedAdapter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public class FeedFragment extends Fragment {
    private OptographFeedAdapter optographFeedAdapter;
    private SwipeRefreshLayout swipeContainer;
    private ApiConsumer apiConsumer;


    public FeedFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiConsumer = new ApiConsumer();
        optographFeedAdapter = new OptographFeedAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.feed_fragment, container, false);
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
        recList.setAdapter(optographFeedAdapter);

        recList.addOnScrollListener(new InfiniteScrollListener(llm) {
            @Override
            public void onLoadMore() {
                FeedManager.loadOlderThan(optographFeedAdapter.last().getCreated_at());
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

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(view1 -> {
            apiConsumer.getOptographsAsObservable(5)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<List<Optograph>>() {
                        @Override
                        public final void onCompleted() {
                            // dp nothing
                        }

                        @Override
                        public final void onError(Throwable e) {
                            Snackbar.make(view1, "There was a network error. Try again!", Snackbar.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(List<Optograph> optographs) {
                            optographFeedAdapter.clear();
                            optographFeedAdapter.addItems(optographs);
                        }
                    });

            Snackbar.make(view1, "Create new optograph: not implemented yet.", Snackbar.LENGTH_SHORT).show();
        });
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
        optographFeedAdapter.setOptographs(FeedMerger.mergeOptographsIntoFeed(optographFeedAdapter.getOptographs(), optographs));
    }
}
