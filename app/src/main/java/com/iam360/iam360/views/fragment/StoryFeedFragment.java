package com.iam360.iam360.views.fragment;

import android.content.Intent;
import android.database.Cursor;
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
import com.iam360.iam360.model.Person;
import com.iam360.iam360.model.StoryFeed;
import com.iam360.iam360.network.Api2Consumer;
import com.iam360.iam360.network.PersonManager;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.DBHelper;
import com.iam360.iam360.viewmodels.InfiniteScrollListener;
import com.iam360.iam360.viewmodels.StoryFeedAdapter;
import com.iam360.iam360.views.activity.ImagePickerActivity;
import com.iam360.iam360.views.activity.MainActivity;

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

    private Person person;

//    private LinearLayoutManager storyLayoutManager;
    private LinearLayoutManager myStoryLayoutManager;
//    private StoryFeedAdapter storyFeedAdapter;
    private StoryFeedAdapter myStoryFeedAdapter;

    private int feedpage = 0;
    private int feedsize = 5;
    private int youpage = 0;
    private int yousize = 15;
    private DBHelper mydb;


    public StoryFeedFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        api2Consumer = new Api2Consumer(token.equals("") ? null : token, "story");
        mydb = new DBHelper(getContext());
//        storyFeedAdapter = new StoryFeedAdapter(getActivity(), true);
        myStoryFeedAdapter = new StoryFeedAdapter(getActivity(), false);

        setPerson();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.story_feed_fragment, container, false);
//        storyLayoutManager = new LinearLayoutManager(getContext());
        myStoryLayoutManager = new LinearLayoutManager(getContext());

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        storyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        binding.storyFeeds.setLayoutManager(storyLayoutManager);
//        binding.storyFeeds.setAdapter(storyFeedAdapter);
//        binding.storyFeeds.setItemViewCacheSize(10);

        myStoryLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.myStoryFeeds.setLayoutManager(myStoryLayoutManager);
        binding.myStoryFeeds.setAdapter(myStoryFeedAdapter);
        binding.myStoryFeeds.setItemViewCacheSize(10);

        binding.homeBtn.setOnClickListener(this);
        binding.createStoryBtn.setOnClickListener(this);
        binding.createStoryBtn2.setOnClickListener(this);

//        binding.storyFeeds.addOnScrollListener(new InfiniteScrollListener(storyLayoutManager) {
//            @Override
//            public void onLoadMore() {
//                loadMore(true, false);
//            }
//
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//            }
//
//        });

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
        String userId = cache.getString(Cache.USER_ID);
        api2Consumer.getStories(userId, feedpage, feedsize, youpage, yousize, new Callback<StoryFeed>() {
            @Override
            public void onResponse(Response<StoryFeed> response, Retrofit retrofit) {

                if (!response.isSuccess()) {
                    return;
                }

                StoryFeed storyFeed = response.body();

                Timber.d("Feed Count : " + storyFeed.getFeed().size());
                Timber.d("You Count : " + storyFeed.getYou().size());

//                if(loadFeed) {
//                    Observable.from(storyFeed.getFeed())
//                            .subscribeOn(Schedulers.newThread())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .doOnCompleted(() -> {
//                                Timber.d("Count : " + storyFeedAdapter.getItemCount());
//                            })
//                            .onErrorReturn(throwable -> {
//                                throwable.printStackTrace();
//                                return null;
//                            })
//                            .subscribe(storyFeedAdapter::addItem);
//                }

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
            case R.id.create_story_btn2:
                Intent intent = new Intent(getActivity(), ImagePickerActivity.class);
                intent.putExtra("person",person);
                intent.putExtra(ImagePickerActivity.PICKER_MODE, ImagePickerActivity.CREATE_STORY_MODE);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void setPerson() {
        PersonManager.loadPerson(cache.getString(Cache.USER_ID));

        myGetData(cache.getString(Cache.USER_ID));
    }

    private boolean myGetData(String id) {
        Person person1 = new Person();
        Cursor res = mydb.getData(id, DBHelper.PERSON_TABLE_NAME,"id");

        if (res==null || res.getCount()==0) return false;
        res.moveToFirst();
        person1.setId(res.getString(res.getColumnIndex("id")));
        person1.setCreated_at(res.getString(res.getColumnIndex("created_at")));
        person1.setDeleted_at(res.getString(res.getColumnIndex("deleted_at")));
        person1.setDisplay_name(res.getString(res.getColumnIndex("display_name")));
        person1.setUser_name(res.getString(res.getColumnIndex("user_name")));
        person1.setEmail(res.getString(res.getColumnIndex("email")));
        person1.setText(res.getString(res.getColumnIndex("text")));
        person1.setElite_status(res.getString(res.getColumnIndex("elite_status")).equalsIgnoreCase("true"));
        person1.setAvatar_asset_id(res.getString(res.getColumnIndex("avatar_asset_id")));
        person1.setOptographs_count(res.getInt(res.getColumnIndex("optographs_count")));
        person1.setFollowers_count(res.getInt(res.getColumnIndex("followers_count")));
        person1.setFollowed_count(res.getInt(res.getColumnIndex("followed_count")));
        person1.setIs_followed(res.getString(res.getColumnIndex("is_followed")).equalsIgnoreCase("true"));
        person1.setFacebook_user_id(res.getString(res.getColumnIndex("facebook_user_id")));
        person1.setFacebook_token(res.getString(res.getColumnIndex("facebook_token")));
        person1.setTwitter_token(res.getString(res.getColumnIndex("twitter_token")));
        person1.setTwitter_secret(res.getString(res.getColumnIndex("twitter_secret")));

        person = person1;
        return true;
    }
}
