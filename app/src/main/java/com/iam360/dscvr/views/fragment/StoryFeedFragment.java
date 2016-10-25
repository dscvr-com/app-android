package com.iam360.dscvr.views.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iam360.dscvr.R;
import com.iam360.dscvr.StoryFeedBinding;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.Person;
import com.iam360.dscvr.network.Api2Consumer;
import com.iam360.dscvr.network.PersonManager;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.DBHelper;
import com.iam360.dscvr.viewmodels.InfiniteScrollListener;
import com.iam360.dscvr.viewmodels.OptographLocalGridAdapter;
import com.iam360.dscvr.viewmodels.StoryFeedAdapter;
import com.iam360.dscvr.views.activity.ImagePickerActivity;
import com.iam360.dscvr.views.activity.MainActivity;

import java.util.List;

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
        myStoryFeedAdapter = new StoryFeedAdapter(getActivity(), false);

        setPerson();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.story_feed_fragment, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.myStoryFeeds.setAdapter(myStoryFeedAdapter);
        GridLayoutManager manager = new GridLayoutManager(getContext(), OptographLocalGridAdapter.COLUMNS);

        binding.myStoryFeeds.setLayoutManager(manager);
        binding.myStoryFeeds.setItemViewCacheSize(10);

        binding.myStoryFeeds.addOnScrollListener(new InfiniteScrollListener(manager) {
            int yPos = 0;
            float height01=0;

            @Override
            public void onLoadMore() {
                loadMore(false, true);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                View view = binding.myStoryFeeds.getChildAt(1);
                if (view==null)return;
                yPos += dy;
                float top = view.getY();
                if((top + view.getHeight())>height01) {
                    height01 = top + view.getHeight();
                }
                if (height01 <= yPos) {
                    binding.toolbar.setVisibility(View.GONE);
                }
            }
        });


        binding.homeBtn.setOnClickListener(this);
        binding.createStoryBtn.setOnClickListener(this);
        binding.createStoryBtn2.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshFeed(true, true);
    }

    public void refreshFeed(boolean loadFeed, boolean loadYou) {

        if (loadYou) youpage++;

        // TODO remove hardcoded person ID, this is for testing with data
        api2Consumer.getStories(100, "", new Callback<List<Optograph>>() {
            @Override
            public void onResponse(Response<List<Optograph>> response, Retrofit retrofit) {

                if (!response.isSuccess()) {
                    return;
                }

                List<Optograph> storyFeed = response.body();

                if(loadYou) {
                    Observable.from(storyFeed)
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
        youpage = 0;
        refreshFeed(true, true);
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
