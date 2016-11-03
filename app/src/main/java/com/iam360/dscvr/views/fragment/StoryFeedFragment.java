package com.iam360.dscvr.views.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iam360.dscvr.R;
import com.iam360.dscvr.StoryFeedBinding;
import com.iam360.dscvr.model.Person;
import com.iam360.dscvr.network.Api2Consumer;
import com.iam360.dscvr.network.ApiConsumer;
import com.iam360.dscvr.network.PersonManager;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.DBHelper;
import com.iam360.dscvr.util.DBHelper2;
import com.iam360.dscvr.viewmodels.InfiniteScrollListener;
import com.iam360.dscvr.viewmodels.OptographLocalGridAdapter;
import com.iam360.dscvr.viewmodels.StoryFeedAdapter;
import com.iam360.dscvr.views.activity.ImagePickerActivity;
import com.iam360.dscvr.views.activity.MainActivity;
import com.iam360.dscvr.views.dialogs.NetworkProblemDialog;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class StoryFeedFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = ProfileFragmentExercise.class.getSimpleName();
    private Api2Consumer api2Consumer;
    private Cache cache;
    private StoryFeedBinding binding;
    private Person person;
    private StoryFeedAdapter storyFeedAdapter;
    private NetworkProblemDialog networkProblemDialog;
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
        storyFeedAdapter = new StoryFeedAdapter(getActivity(), false);
        networkProblemDialog = new NetworkProblemDialog();

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

        binding.myStoryFeeds.setAdapter(storyFeedAdapter);
        GridLayoutManager manager = new GridLayoutManager(getContext(), OptographLocalGridAdapter.COLUMNS);

        binding.myStoryFeeds.setLayoutManager(manager);
        binding.myStoryFeeds.setItemViewCacheSize(10);

        binding.myStoryFeeds.addOnScrollListener(new InfiniteScrollListener(manager) {
            int yPos = 0;
            float height01=0;

            @Override
            public void onLoadMore() {
                loadMore();
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
//        refreshFeed(true, true);
    }

    public void refreshFeed() {
        Log.d("MARK","refreshFeed person.getId() = "+person.getId());
        Cursor cursor = mydb.getUserStories(person.getId() , DBHelper.STORY_TABLE_NAME, ApiConsumer.PROFILE_GRID_LIMIT);
        Log.d("MARK","refreshFeed cursor = "+cursor);
        if(cursor != null) {
            cursor.moveToFirst();
            Log.d("MARK","refreshStoryFeed cursor.getCount() = "+cursor.getCount());
            if (cursor.getCount() != 0) {
                storyFeedAdapter.clearData();
                new DBHelper2(getContext()).getOptographs(cursor, "story")
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnCompleted(() -> {
                            Timber.d("refreshStoryFeed Count : " + storyFeedAdapter.getItemCount());
                            api2Consumer.getStories(100, "")
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnCompleted(() -> { Timber.d("Count : " + storyFeedAdapter.getItemCount()); })
                                    .onErrorReturn(throwable -> {
                                        throwable.printStackTrace();
                                        return null;
                                    })
                                    .subscribe(storyFeedAdapter::addItem);
                        })
                        .onErrorReturn(throwable -> {
                            Log.d("myTag", "refreshStoryFeed Error: message: " + throwable.getMessage());
                            if (!networkProblemDialog.isAdded())
                                networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                            return null;
                        })
                        .subscribe(storyFeedAdapter::addItem);
            }
        }//else{
            api2Consumer.getStories(100, "")
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() -> { Timber.d("refreshStoryFeed Count2 : " + storyFeedAdapter.getItemCount()); })
                    .onErrorReturn(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    })
                    .onErrorReturn(throwable -> {
                        Log.d("myTag", "refreshStoryFeed Error: message: " + throwable.getMessage());
                        if (!networkProblemDialog.isAdded())
                            networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                        return null;
                    })
                    .subscribe(storyFeedAdapter::addItem);
//        }
    }

    public void loadMore() {
        Timber.d("loadMore");
        refreshFeed();
    }
    public void refresh() {}

    @Override
    public void onResume() {
        super.onResume();
        refreshFeed();
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

//
//    public Observable<Optograph> cur2Json(Cursor cursor) {
//        List<Optograph> optographs = new LinkedList<>();
//        cursor.moveToFirst();
//        String locId = "";
//
//        for(int a=0; a < cursor.getCount(); a++){
//            Story story = new Story();
//            story.setId(cursor.getString(cursor.getColumnIndex(DBHelper.STORY_ID)));
//            story.setCreated_at(cursor.getString(cursor.getColumnIndex(DBHelper.STORY_CREATED_AT)));
//            story.setUpdated_at(cursor.getString(cursor.getColumnIndex(DBHelper.STORY_UPDATED_AT)));
//            story.setDeleted_at(cursor.getString(cursor.getColumnIndex(DBHelper.STORY_DELETED_AT)));
//            story.setOptograph_id(cursor.getString(cursor.getColumnIndex(DBHelper.STORY_OPTOGRAPH_ID)));
//            story.setPerson_id(cursor.getString(cursor.getColumnIndex(DBHelper.STORY_PERSON_ID)));
//            String personId = cursor.getString(cursor.getColumnIndex(DBHelper.STORY_PERSON_ID));
//            String optoId = cursor.getString(cursor.getColumnIndex(DBHelper.STORY_OPTOGRAPH_ID));
//
//            Cursor res = mydb.getData(optoId, DBHelper.OPTO_TABLE_NAME_FEEDS,DBHelper.OPTOGRAPH_ID);
//            Optograph opto = null;
//            res.moveToFirst();
//            if (res.getCount()!= 0) {
//                opto = new Optograph(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_ID)));
//                opto.setCreated_at(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_CREATED_AT)));
//                opto.setIs_starred(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_STARRED)) == 1 ? true : false);
//                opto.setDeleted_at(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_DELETED_AT)));
//                opto.setStitcher_version(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_STITCHER_VERSION)));
//                opto.setText(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_TEXT)));
//                opto.setViews_count(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_STARS_COUNT)));
//                opto.setIs_staff_picked(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_STAFF_PICK)) == 1 ? true : false);
//                opto.setShare_alias(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_SHARE_ALIAS)));
//                opto.setIs_private(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_PRIVATE)) == 1 ? true : false);
//                opto.setIs_published(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_PUBLISHED)) == 1 ? true : false);
//                opto.setOptograph_type(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_TYPE)));
//                opto.setStars_count(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_STARS_COUNT)));
//                opto.setShould_be_published(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) == 1 ? true : false);
//                opto.setIs_local(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_LOCAL)) == 1 ? true : false);
//                opto.setIs_data_uploaded(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_DATA_UPLOADED)) == 1 ? true : false);
//                locId = res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_LOCATION_ID));
//            }
//
//            Person person = new Person();
//            if(personId !=null && !personId.equals("")){
//                res = mydb.getData(personId, DBHelper.PERSON_TABLE_NAME,"id");
//                res.moveToFirst();
//                if (res.getCount()!= 0) {
//                    person.setId(res.getString(res.getColumnIndex("id")));
//                    person.setCreated_at(res.getString(res.getColumnIndex("created_at")));
//                    person.setDeleted_at(res.getString(res.getColumnIndex("deleted_at")));
//                    person.setDisplay_name(res.getString(res.getColumnIndex("display_name")));
//                    person.setUser_name(res.getString(res.getColumnIndex("user_name")));
//                    person.setText(res.getString(res.getColumnIndex("email")));
//                    person.setEmail(res.getString(res.getColumnIndex("text")));
//                    person.setElite_status(res.getInt(res.getColumnIndex("elite_status")) == 1 ? true : false);
//                    person.setAvatar_asset_id(res.getString(res.getColumnIndex("avatar_asset_id")));
//                    person.setOptographs_count(res.getInt(res.getColumnIndex("optographs_count")));
//                    person.setFollowers_count(res.getInt(res.getColumnIndex("followers_count")));
//                    person.setFollowed_count(res.getInt(res.getColumnIndex("followed_count")));
//                    person.setIs_followed(res.getInt(res.getColumnIndex("is_followed")) == 1 ? true : false);
//                    person.setFacebook_user_id(res.getString(res.getColumnIndex("facebook_user_id")));
//                    person.setFacebook_token(res.getString(res.getColumnIndex("facebook_token")));
//                    person.setTwitter_token(res.getString(res.getColumnIndex("twitter_token")));
//                    person.setTwitter_secret(res.getString(res.getColumnIndex("twitter_secret")));
//                }
//            }
//            opto.setPerson(person);
//
//            Location location = new Location();
//            if(opto != null && locId !=null && !locId.equals("")){
//                res = mydb.getData(locId, DBHelper.LOCATION_TABLE_NAME,"id");
//                res.moveToFirst();
//                if (res.getCount()!= 0) {
//                    location.setId(res.getString(res.getColumnIndex("id")));
//                    location.setCreated_at(res.getString(res.getColumnIndex("created_at")));
//                    location.setText(res.getString(res.getColumnIndex("text")));
//                    location.setCountry(res.getString(res.getColumnIndex("id")));
//                    location.setCountry_short(res.getString(res.getColumnIndex("country")));
//                    location.setPlace(res.getString(res.getColumnIndex("place")));
//                    location.setRegion(res.getString(res.getColumnIndex("region")));
//                    location.setPoi(Boolean.parseBoolean(res.getString(res.getColumnIndex("poi"))));
//                    location.setLatitude(res.getDouble(res.getColumnIndex("latitude")));
//                    location.setLongitude(res.getDouble(res.getColumnIndex("longitude")));
//                }
//            }
//            opto.setLocation(location);
//
//            optographs.add(opto);
//
//            cursor.moveToNext();
//        }
//
//        cursor.close();
//
//        return Observable.from(optographs);
//    }
}
