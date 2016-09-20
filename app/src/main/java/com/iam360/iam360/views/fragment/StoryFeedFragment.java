package com.iam360.iam360.views.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iam360.iam360.R;
import com.iam360.iam360.StoryFeedBinding;
import com.iam360.iam360.model.StoryFeed;
import com.iam360.iam360.network.Api2Consumer;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.viewmodels.InfiniteScrollListener;
import com.iam360.iam360.viewmodels.StoryFeedAdapter;
import com.iam360.iam360.views.activity.ImagePickerActivity;
import com.iam360.iam360.views.activity.MainActivity;
import com.iam360.iam360.views.activity.OptographDetailsActivity;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class StoryFeedFragment extends Fragment implements View.OnClickListener {
    private Api2Consumer api2Consumer;
    private Cache cache;
    private StoryFeedBinding binding;

    private LinearLayoutManager storyLayoutManager;
    private LinearLayoutManager myStoryLayoutManager;
    private StoryFeedAdapter storyFeedAdapter;
    private StoryFeedAdapter myStoryFeedAdapter;

    private int feedpage = 0;
    private int feedsize = 5;
    private int youpage = 0;
    private int yousize = 5;


    public StoryFeedFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        api2Consumer = new Api2Consumer(token.equals("") ? null : token, "story");
        storyFeedAdapter = new StoryFeedAdapter(getActivity(), true);
        myStoryFeedAdapter = new StoryFeedAdapter(getActivity(), false);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.story_feed_fragment, container, false);
        storyLayoutManager = new LinearLayoutManager(getContext());
        myStoryLayoutManager = new LinearLayoutManager(getContext());

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        storyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.storyFeeds.setLayoutManager(storyLayoutManager);
        binding.storyFeeds.setAdapter(storyFeedAdapter);
        binding.storyFeeds.setItemViewCacheSize(10);

        myStoryLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.myStoryFeeds.setLayoutManager(myStoryLayoutManager);
        binding.myStoryFeeds.setAdapter(myStoryFeedAdapter);
        binding.myStoryFeeds.setItemViewCacheSize(10);

        binding.homeBtn.setOnClickListener(this);
        binding.createStoryBtn.setOnClickListener(this);

        binding.storyFeeds.addOnScrollListener(new InfiniteScrollListener(storyLayoutManager) {
            @Override
            public void onLoadMore() {
                loadMore(true, false);
            }


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }

        });

        binding.myStoryFeeds.addOnScrollListener(new InfiniteScrollListener(myStoryLayoutManager) {
            @Override
            public void onLoadMore() {
                loadMore(false, true);
            }


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }

        });

    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshFeed(true, true);
    }

    public void refreshFeed(boolean loadFeed, boolean loadYou) {

        if(loadFeed) feedpage++;
        if (loadYou) youpage++;

        // TODO remove hardcoded person ID, this is for testing with data
        String userId = "c0d5cb2b-7f8a-4de9-a5de-6f7c6cf1cf1a"; // cache.getString(Cache.USER_ID)
        api2Consumer.getStories(userId, feedpage, feedsize, youpage, yousize, new Callback<StoryFeed>() {
            @Override
            public void onResponse(Response<StoryFeed> response, Retrofit retrofit) {

                if (!response.isSuccess()) {
                    return;
                }

                StoryFeed storyFeed = response.body();

                Timber.d("Feed Count : " + storyFeed.getFeed().size());
                Timber.d("You Count : " + storyFeed.getYou().size());

                if(loadFeed) {
                    Observable.from(storyFeed.getFeed())
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnCompleted(() -> {
                                Timber.d("Count : " + storyFeedAdapter.getItemCount());
                            })
                            .onErrorReturn(throwable -> {
                                throwable.printStackTrace();
                                return null;
                            })
                            .subscribe(storyFeedAdapter::addItem);
                }

                if(loadYou) {
                    Observable.from(storyFeed.getYou())
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnCompleted(() -> { Timber.d("Count : " + myStoryFeedAdapter.getItemCount()); })
                            .onErrorReturn(throwable -> {
                                throwable.printStackTrace();
                                return null;
                            })
                            .subscribe(myStoryFeedAdapter::addItem);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d(t.getMessage());
            }
        });

    }

    public void loadMore(boolean loadFeed, boolean loadYou) {
        Timber.d("loadMore");
        feedpage++;
        refreshFeed(loadFeed, loadYou);
    }
    public void refresh() {}

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_btn:
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).onBackPressed();
                break;
            case R.id.create_story_btn:
                Intent intent = new Intent(getActivity(), ImagePickerActivity.class);
                intent.putExtra(ImagePickerActivity.PICKER_MODE, ImagePickerActivity.CREATE_STORY_MODE);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
