package co.optonaut.optonaut.views.deprecated;

import android.os.Bundle;

import co.optonaut.optonaut.views.feed.OptographListFragment;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Nilan Marktanner
 * @date 2015-12-15
 */
public class SearchFeedFragment extends OptographListFragment {
    private String keyword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args.containsKey("keyword")) {
            keyword = args.getString("keyword");
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    protected void initializeFeed() {
        apiConsumer.searchOptographs(5, keyword)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(optographFeedAdapter::addItem);
    }

    @Override
    protected void loadMore() {
        apiConsumer.searchOptographs(5, optographFeedAdapter.getOldest().getCreated_at(), keyword)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(optographFeedAdapter::addItem);
    }

    @Override
    protected void refresh() {
        // TODO!
    }

    public static SearchFeedFragment newInstance(String keyword) {
        SearchFeedFragment searchFeedFragment = new SearchFeedFragment();
        Bundle args = new Bundle();
        args.putString("keyword", keyword);
        searchFeedFragment.setArguments(args);
        return searchFeedFragment;
    }
}
