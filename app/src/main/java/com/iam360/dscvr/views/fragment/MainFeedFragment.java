package com.iam360.dscvr.views.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.iam360.dscvr.R;
import com.iam360.dscvr.bus.BusProvider;
import com.iam360.dscvr.bus.RecordFinishedEvent;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.network.PersonManager;
import com.iam360.dscvr.record.GlobalState;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.DBHelper;
import com.iam360.dscvr.util.DBHelper2;
import com.iam360.dscvr.util.GeneralUtils;
import com.iam360.dscvr.viewmodels.LocalOptographManager;
import com.iam360.dscvr.views.activity.BLEListActivity;
import com.iam360.dscvr.views.activity.ImagePickerActivity;
import com.iam360.dscvr.views.activity.MainActivity;
import com.iam360.dscvr.views.activity.RecorderActivity;
import com.iam360.dscvr.views.activity.RingOptionActivity;
import com.iam360.dscvr.views.activity.SearchActivity;
import com.iam360.dscvr.views.activity.WebViewActivity;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import me.leolin.shortcutbadger.ShortcutBadger;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author Nilan Marktanner
 * @date 2015-12-15
 */
public class MainFeedFragment extends OptographListFragment implements View.OnClickListener {
    public static final String TAG = MainFeedFragment.class.getSimpleName();
    private static final int MILLISECONDS_THRESHOLD_FOR_SWITCH = 250;

//    NetworkProblemDialog networkProblemDialog;
    private AlertDialog networkProblemAlert = null;


    private SensorManager sensorManager;
    private boolean inVRMode;

    private DateTime inVRPositionSince;

    private List<Optograph> optographs = new ArrayList<>();
    private DBHelper mydb;

    private int PICK_IMAGE_REQUEST = 1;
    public final static int WEBVIEW_REQUEST_CODE = 100;

    private String userToken = "";
    private boolean isFBShare = false;
    private boolean isTwitterShare = false;
    private CallbackManager callbackManager;

    // Twitter
    private static Twitter twitter;
    private static RequestToken requestToken;

    /**
     * Register your here app https://dev.twitter.com/apps/new and get your
     * consumer key and secret
     */
    static String TWITTER_CONSUMER_KEY; // place your cosumer key here
    static String TWITTER_CONSUMER_SECRET; // place your consumer secret here
    static String CALLBACK_URL;

    private static final int REQUEST_BLE_LIST = 1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inVRMode = false;
        inVRPositionSince = null;
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mydb = new DBHelper(getContext());
        setHasOptionsMenu(true);

        // fb initialization
        FacebookSdk.sdkInitialize(getActivity());
        callbackManager = CallbackManager.Factory.create();

        // twitter
        TWITTER_CONSUMER_KEY = getString(R.string.twitter_consumer_key);
        TWITTER_CONSUMER_SECRET = getString(R.string.twitter_consumer_secret);
        CALLBACK_URL = getString(R.string.twitter_callback_url);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.dialog_network_retry));
        builder.setCancelable(true);

        userToken = cache.getString(Cache.USER_TOKEN);
        isFBShare = cache.getBoolean(Cache.POST_OPTO_TO_FB, false);
        isTwitterShare = cache.getBoolean(Cache.POST_OPTO_TO_TWITTER, false);

        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        networkProblemAlert = builder.create();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.profileBtn.setOnClickListener(this);
        binding.cameraBtn.setOnClickListener(this);
        binding.settingsBtn.setOnClickListener(this);
        binding.thetaBtn.setOnClickListener(this);
        binding.headerLogo.setOnClickListener(this);
        binding.numberLocalImage.setOnClickListener(this);
        binding.numberImage.setOnClickListener(this);
        binding.searchButton.setOnClickListener(this);
        binding.tapToHide.setOnClickListener(this);
        binding.loadingScreen.setOnClickListener(this);

        Animation clockwiseRotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_clockwise);
        binding.circleBig.startAnimation(clockwiseRotateAnimation);
        Animation counterClockwiseRotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_counterclockwise);
        binding.circleSmall.startAnimation(counterClockwiseRotateAnimation);

        PackageInfo pInfo = null;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String version = pInfo.versionName;
            binding.version.setText("v" + version);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

//        Settings start
        initializeButtons();

        binding.a3dButton.setOnClickListener(this);
        binding.gyroButton.setOnClickListener(this);
        binding.settingsGyro.setOnClickListener(this);
        binding.littlePlanetButton.setOnClickListener(this);
        binding.settingsLittlePlanet.setOnClickListener(this);
        binding.oneRingButton.setOnClickListener(this);
        binding.settingsOneRing.setOnClickListener(this);
        binding.threeRingButton.setOnClickListener(this);
        binding.settingsThreeRing.setOnClickListener(this);
//        binding.manualButton.setOnClickListener(this);
//        binding.settingsManual.setOnClickListener(this);
//        binding.motorButton.setOnClickListener(this);
//        binding.settingsMotor.setOnClickListener(this);
        binding.fbShare.setOnClickListener(this);
        binding.twitterShare.setOnClickListener(this);
        binding.motorOffButton.setOnClickListener(this);
        binding.settingsMotorOff.setOnClickListener(this);
        binding.motorOnButton.setOnClickListener(this);
        binding.settingsMotorOn.setOnClickListener(this);
//        Settings end

        binding.slidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                binding.profileBtn.setVisibility(View.GONE);
                binding.searchButton.setVisibility(View.GONE);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                switch (newState) {
                    case EXPANDED:
                        binding.pullUpButton.setVisibility(View.VISIBLE);
                        if (previousState == SlidingUpPanelLayout.PanelState.DRAGGING) binding.barTransparent.setBackgroundColor(getResources().getColor(R.color.settings_bg));
                        binding.profileBtn.setVisibility(View.GONE);
                        binding.searchButton.setVisibility(View.GONE);
                        ((MainActivity)getActivity()).dragSettingPage(true);
                        binding.notifBadge.setVisibility(View.GONE);
                        break;
                    case COLLAPSED:
                    case ANCHORED:
                    case HIDDEN:
                        binding.pullUpButton.setVisibility(View.GONE);
                        binding.profileBtn.setVisibility(View.VISIBLE);
                        binding.searchButton.setVisibility(View.VISIBLE);
                        binding.barTransparent.setBackgroundColor(getResources().getColor(R.color.transparentOverlay));
                        ((MainActivity)getActivity()).dragSettingPage(false);
                        if(cache.getInt(Cache.NOTIF_COUNT) > 0) binding.notifBadge.setVisibility(View.VISIBLE);
                        else binding.notifBadge.setVisibility(View.GONE);
                        break;
                    case DRAGGING:
                        if (previousState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                            binding.barTransparent.setBackgroundColor(getResources().getColor(R.color.transparentOverlay));
                            binding.pullUpButton.setVisibility(View.GONE);
                        } else if (previousState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                            binding.barTransparent.setBackgroundColor(getResources().getColor(R.color.settings_bg));
                            binding.pullUpButton.setVisibility(View.VISIBLE);
                        }
                        binding.profileBtn.setVisibility(View.GONE);
                        binding.searchButton.setVisibility(View.GONE);
                        binding.notifBadge.setVisibility(View.GONE);
                        break;
                }
            }
        });

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        inVRMode = false;

        if(GlobalState.isAnyJobRunning) {
            binding.cameraBtn.setEnabled(false);
            binding.recordProgress.setVisibility(View.VISIBLE);
        } else {
            binding.cameraBtn.setEnabled(true);
            binding.recordProgress.setVisibility(View.GONE);
        }

//        optographs = new ArrayList<>();
//        localImagesUIUpdate();
        BusProvider.getInstance().register(this);
        if (GlobalState.shouldHardRefreshFeed) {
            initializeFeed(false);
        }

        refresh();
        initializeButtons();
        initializeShareButtons();
    }

    @Override
    public void initializeFeed(boolean fromList) {
        Cursor curs = mydb.getFeedsData(5);
        Log.d("MARK","initializeFeed curs.getCount() = "+curs.getCount());
        if (curs.getCount() > 0) {
            Log.d("myTag"," Subscribe1");
            new DBHelper2(getContext()).getOptographs(curs, "opto")//cur2Json(curs)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() ->{
                        Log.d("myTag"," Subscribe2");
                        api2Consumer.getStoryFeeds(5)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .onErrorReturn(throwable -> {
                                    throwable.printStackTrace();
                                    networkProblemAlert.show();
                                    return null;
                                })
                                .subscribe(optographFeedAdapter::addItem);
                    })
                    .onErrorReturn(throwable -> {
                        throwable.printStackTrace();
                        networkProblemAlert.show();
                        return null;
                    })
                    .subscribe(optographFeedAdapter::addItem);
        } else {
            binding.loadingScreen.setVisibility(View.VISIBLE);
            Log.d("myTag"," Subscribe3");
            api2Consumer.getStoryFeeds(5)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() -> binding.loadingScreen.setVisibility(View.GONE) )
                    .onErrorReturn(throwable -> {
                        binding.loadingScreen.setVisibility(View.GONE);
                        networkProblemAlert.show();
                        return null;
                    })
                    .subscribe(optographFeedAdapter::addItem);
        }

        Log.d("myTag"," Subscribe4");
        LocalOptographManager.getOptographs()
                .observeOn(AndroidSchedulers.mainThread())
                .filter(e -> !mydb.checkIfAllImagesUploaded(e.getId()))
                .subscribe(this::countLocal);
        GlobalState.shouldHardRefreshFeed = false;

        refreshNotifBadge();

    }

    public void refreshNotifBadge() {

        // refresh notification badge
        if(cache.getInt(Cache.NOTIF_COUNT) > 0) binding.notifBadge.setVisibility(View.VISIBLE);
        else binding.notifBadge.setVisibility(View.GONE);

    }

//    public Observable<Optograph> cur2Json(Cursor cursor) {
////        JSONArray resultSet = new JSONArray();
//        List<Optograph> optographs = new LinkedList<>();
//        cursor.moveToFirst();
//        String locId = "";
//
//        for(int a=0; a < cursor.getCount(); a++){
//            Optograph opto = null;
//            String personId = null;
//            try {
//                opto = new Optograph(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_ID)));
//                opto.setCreated_at(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_CREATED_AT)));
//                opto.setIs_starred(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_STARRED)) == 1 ? true : false);
//                opto.setDeleted_at(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_DELETED_AT)));
//                opto.setStitcher_version(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_STITCHER_VERSION)));
//                opto.setText(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_TEXT)));
//                opto.setViews_count(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_STARS_COUNT)));
//                opto.setIs_staff_picked(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_STAFF_PICK)) == 1 ? true : false);
//                opto.setShare_alias(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_SHARE_ALIAS)));
//                opto.setIs_private(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_PRIVATE)) == 1 ? true : false);
//                opto.setIs_published(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_PUBLISHED)) == 1 ? true : false);
//                opto.setOptograph_type(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_TYPE)));
//                opto.setStars_count(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_STARS_COUNT)));
//                opto.setShould_be_published(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) == 1 ? true : false);
//                opto.setIs_local(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_LOCAL)) == 1 ? true : false);
//                opto.setIs_data_uploaded(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_DATA_UPLOADED)) == 1 ? true : false);
//                locId = cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_LOCATION_ID));
//                personId = cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_PERSON_ID));
//            } catch (Exception e) {
//                Log.d(TAG, e.getMessage());
//            }
//
//            Person person = new Person();
//            if(personId !=null && !personId.equals("")){
//                Cursor res = mydb.getData(personId, DBHelper.PERSON_TABLE_NAME,"id");
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
////                if(!person.is_followed()){
////                    continue;
////                }
//            opto.setPerson(person);
//
//            Location location = new Location();
//            if(opto != null && locId !=null && !locId.equals("")){
//                Cursor res = mydb.getData(locId, DBHelper.LOCATION_TABLE_NAME,"id");
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
//            optographs.add(opto);
//
//            Timber.d("FROMDB : " + opto.getPerson().getUser_name() + " " + opto.is_starred());
//
//            cursor.moveToNext();
//        }
//
//        cursor.close();
//        return Observable.from(optographs);
//    }

    private void countLocal(Optograph optograph) {
        if (optograph == null) {
            return;
        }

        Log.d("myTag","countLocal user equal? "+(optograph.getPerson().getId().equals(cache.getString(Cache.USER_ID))));
        if (optograph.getPerson().getId().equals(cache.getString(Cache.USER_ID))) {
            new DBHelper2(getContext()).saveToSQLite(optograph);
        }
        Log.d("myTag","countLocal isLocal? "+optograph.is_local()+" isAllUploaded? "+mydb.checkIfAllImagesUploaded(optograph.getId())+" " +
                "isShouldbepublished? "+mydb.checkIfShouldBePublished(optograph.getId()));
        if (optograph.is_local() && (mydb.checkIfAllImagesUploaded(optograph.getId()) || mydb.checkIfShouldBePublished(optograph.getId()))) {
            return;
        }

        // skip if optograph is already in list
        if (optographs.contains(optograph)) {
            return;
        }

        // if list is empty, simply add new optograph
        if (optographs.isEmpty()) {
            optographs.add(optograph);
            localImagesUIUpdate();
            return;
        }

        optographs.add(optograph);

        localImagesUIUpdate();
    }

    private void localImagesUIUpdate() {
        if (optographs.size()==0) {
            binding.numberLocalImage.setVisibility(View.GONE);
            binding.numberImage.setVisibility(View.GONE);
        } else {
            binding.numberLocalImage.setText(String.valueOf(optographs.size()));
//            binding.numberLocalImage.setVisibility(View.VISIBLE);//uncmment if the number of un-uploaded images is needed
//            binding.numberImage.setVisibility(View.VISIBLE);// uncomment if this is gonna use
        }
    }

    @Override
    public void loadMore() {
        Timber.d("loadMore");
        if(apiConsumer == null) return;
        Cursor curs = mydb.getFeedsData(5, optographFeedAdapter.getOldest().getCreated_at());
        if (curs.getCount() > 0) {
            new DBHelper2(getContext()).getOptographs(curs, "opto")//cur2Json(curs)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() ->{
                        api2Consumer.getStoryFeeds(5, optographFeedAdapter.getOldest().getCreated_at())
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .onErrorReturn(throwable -> {
//                                    if (!networkProblemDialog.isAdded())
//                                        networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                                    return null;
                                })
                                .subscribe(optographFeedAdapter::addItem);
                    })
                    .onErrorReturn(throwable -> {
//                        if (!networkProblemDialog.isAdded())networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                        return null;
                    })
                    .subscribe(optographFeedAdapter::addItem);
        } else {
            Timber.d("LoadMore. No more cache data.");
            api2Consumer.getStoryFeeds(5, optographFeedAdapter.getOldest().getCreated_at())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorReturn(throwable -> {
                        networkProblemAlert.show();
                        return null;
                    })
                    .subscribe(optographFeedAdapter::addItem);
        }

        LocalOptographManager.getOptographs()
                .observeOn(AndroidSchedulers.mainThread())
                .filter(e -> !mydb.checkIfAllImagesUploaded(e.getId()))
                .subscribe(this::countLocal);
    }

    @Override
    public void refresh() {
        refresh(false);
    }

    public void refresh(boolean scrollToTop) {
        Timber.d("Refresh");
        if(apiConsumer == null) return;
        Cursor curs = mydb.getFeedsData(5);
        Log.d("MARK","refresh cursCount - "+curs.getCount());

        if(!scrollToTop) binding.loadingScreen.setVisibility(View.VISIBLE);

        if (curs.getCount() > 0) {
            Log.d("myTag"," Subscribe9");
            new DBHelper2(getContext()).getOptographs(curs, "opto")//cur2Json(curs)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() ->{
                        if(scrollToTop) mLayoutManager.scrollToPosition(0);
                        Log.d("myTag"," Subscribe10");
                        api2Consumer.getStoryFeeds(5)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnCompleted(() -> {
                                    if(scrollToTop) mLayoutManager.scrollToPosition(0);
                                    else binding.loadingScreen.setVisibility(View.GONE);
                                })
                                .onErrorReturn(throwable -> {
                                    if(!scrollToTop) binding.loadingScreen.setVisibility(View.GONE);
                                    networkProblemAlert.show();
                                    return null;
                                })
                                .subscribe(optographFeedAdapter::addItem);
                    })
                    .onErrorReturn(throwable -> {
                        networkProblemAlert.show();
                        return null;
                    })
                    .subscribe(optographFeedAdapter::addItem);
        }else{
            Log.d("myTag"," Subscribe11");
            api2Consumer.getStoryFeeds(5)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
//                                  .doOnCompleted(() -> MixpanelHelper.trackViewViewer2D(getActivity()))
                    .doOnCompleted(() -> {
                        if(scrollToTop) mLayoutManager.scrollToPosition(0);
                        else binding.loadingScreen.setVisibility(View.GONE);
                    })
                    .onErrorReturn(throwable -> {
                        networkProblemAlert.show();
                        return null;
                    })
                    .subscribe(optographFeedAdapter::addItem);
        }

        Log.d("myTag"," Subscribe12");
        LocalOptographManager.getOptographs()
                .observeOn(AndroidSchedulers.mainThread())
                .filter(e -> !mydb.checkIfAllImagesUploaded(e.getId()))
                .subscribe(this::countLocal);

        disableDrag();
        binding.swipeRefreshLayout.setRefreshing(false);

        refreshNotifBadge();

    }

    public void disableDrag() {
        if(optographFeedAdapter != null)
            optographFeedAdapter.disableDraggingPage(firstVisible);
    }

    public void hideSettingPage() {
        ((MainActivity) getActivity()).dragSettingPage(false);
        binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    @Subscribe
    public void recordFinished(RecordFinishedEvent event) {
        initializeFeed(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loading_screen:
            case R.id.tap_to_hide:
                binding.loadingScreen.setVisibility(View.GONE);
                break;
            case R.id.number_image:
            case R.id.number_local_image:
            case R.id.profile_btn:
                cache.save(Cache.NOTIF_COUNT, 0);
                ShortcutBadger.removeCount(getActivity());
                ((MainActivity) getActivity()).setPage(MainActivity.PROFILE_MODE);
                break;
            case R.id.search_button:
                Intent intent2 = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent2);
                break;
            case R.id.camera_btn:
                if (cache.getString(Cache.USER_TOKEN).isEmpty()) {
                    Snackbar.make(v,getActivity().getString(R.string.profile_login_first),Snackbar.LENGTH_SHORT).show();
                    return;
                } else if (GlobalState.isAnyJobRunning) {
                    Snackbar.make(binding.cameraBtn, R.string.dialog_wait_on_record_finish, Snackbar.LENGTH_LONG).show();
                    return;
                }

                Intent intent;
                if(!cache.getString(Cache.GATE_CODE).equals("")) {
                    intent = new Intent(getActivity(), RingOptionActivity.class);
                    startActivity(intent);
                } else
                    ((MainActivity) getActivity()).setPage(MainActivity.PROFILE_MODE);


                break;
            case R.id.header_logo:
            case R.id.settings_btn:
                ((MainActivity) getActivity()).dragSettingPage(true);
                binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                break;
            case R.id.theta_btn:
                Intent intent1;
//                if(!cache.getString(Cache.GATE_CODE).equals("")) {
                    intent = new Intent(getActivity(), ImagePickerActivity.class);
                    intent.putExtra(ImagePickerActivity.PICKER_MODE, ImagePickerActivity.UPLOAD_OPTO_MODE);
                    startActivity(intent);
//                } else
//                    ((MainActivity) getActivity()).setPage(MainActivity.PROFILE_MODE);
                break;
            case R.id.a3d_button:
                cache.save(Cache.VR_3D_ENABLE,!cache.getBoolean(Cache.VR_3D_ENABLE,false));
                binding.a3dButton.setBackgroundResource(cache.getBoolean(Cache.VR_3D_ENABLE, false) ? R.drawable.a3dvr_active_icn : R.drawable.a3dvr_inactive_icn);
                break;
            case R.id.settings_gyro:
            case R.id.gyro_button:
                gyroValidation();
                instatiateFeedDisplayButton();
                break;
            case R.id.settings_little_planet:
            case R.id.little_planet_button:
                littlePlanetValidation();
                instatiateFeedDisplayButton();
                break;
            case R.id.settings_one_ring:
            case R.id.one_ring_button:
                if (cache.getInt(Cache.CAMERA_MODE) != Constants.ONE_RING_MODE) {
                    cache.save(Cache.CAMERA_MODE, Constants.ONE_RING_MODE);
                    activeOneRing();
                }
                break;
            case R.id.settings_three_ring:
            case R.id.three_ring_button:
                if (cache.getInt(Cache.CAMERA_MODE) != Constants.THREE_RING_MODE) {
                    cache.save(Cache.CAMERA_MODE, Constants.THREE_RING_MODE);
                    activeThreeRing();
                }
                break;
            case R.id.motor_off_button:
            case R.id.settings_motor_off:
                if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE)!= Constants.MANUAL_MODE) {
                    cache.save(Cache.CAMERA_CAPTURE_TYPE, Constants.MANUAL_MODE);
                    activeManualType();
                }
                break;
            case R.id.settings_motor_on:
            case R.id.motor_on_button:
                if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE) != Constants.MOTOR_MODE) {
                    cache.save(Cache.CAMERA_CAPTURE_TYPE, Constants.MOTOR_MODE);
                    activeMotorType();
                }
                break;
            case R.id.fb_share:
                Set<String> permissions = null;
                if(com.facebook.AccessToken.getCurrentAccessToken() == null)
                    sharedNotLoginDialog();
                else {
                    permissions = com.facebook.AccessToken.getCurrentAccessToken().getPermissions();

                    if (permissions.contains("publish_actions")) {
                        Log.d("myTag", "Contains publish actions.");
                        isFBShare = !cache.getBoolean(Cache.POST_OPTO_TO_FB, false);
                        cache.save(Cache.POST_OPTO_TO_FB, isFBShare);
                        initializeShareButtons();
                        PersonManager.updatePerson();
                    } else {
                        Log.d("myTag", "No publish actions.");
                        loginFacebook();
                    }
                }
                break;
            case R.id.twitter_share:
                userToken = cache.getString(Cache.USER_TOKEN);
                if (userToken == null || userToken.equals("")) {
                    sharedNotLoginDialog();
                    return;
                } else if (cache.getBoolean(Cache.USER_TWITTER_LOGGED_IN, false)) {
                    isTwitterShare = !cache.getBoolean(Cache.POST_OPTO_TO_TWITTER, false);
                    cache.save(Cache.POST_OPTO_TO_TWITTER, isTwitterShare);
                    initializeShareButtons();
                    PersonManager.updatePerson();
                    return;
                }
                loginTwitter();
                break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);

                MainActivity activity = (MainActivity) getActivity();
                activity.startImagePreview(UUID.randomUUID(), new GeneralUtils().getRealPathFromURI(getActivity(), uri));

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else Log.d("myTag", " resultCode " + Activity.RESULT_OK + " = " + resultCode + "? requestCode: " + requestCode);
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            String verifier = data.getExtras().getString("oauth_verifier");
            if (verifier != null) {
                new TwitterLoggedIn().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, verifier);
            } else {
                cache.save(Cache.POST_OPTO_TO_TWITTER, isTwitterShare);
                initializeShareButtons();
                binding.twitterProgress.setVisibility(View.GONE);
                binding.twitterShare.setClickable(true);
                PersonManager.updatePerson();
            }
        } else if (requestCode == 100) {
            binding.twitterProgress.setVisibility(View.GONE);
            binding.twitterShare.setClickable(true);
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    //    Settings start
    private void initializeButtons() {
        binding.a3dButton.setBackgroundResource(cache.getBoolean(Cache.VR_3D_ENABLE, false) ? R.drawable.a3dvr_active_icn : R.drawable.a3dvr_inactive_icn);

        instatiateFeedDisplayButton();

        if (cache.getInt(Cache.CAMERA_MODE) == Constants.THREE_RING_MODE)
            activeThreeRing();
        else activeOneRing();

        if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE)== Constants.MANUAL_MODE)
            activeManualType();
        else activeMotorType();
    }

    private void gyroValidation() {
        boolean gyro = cache.getBoolean(Cache.GYRO_ENABLE,false);
        boolean lilPlanet = cache.getBoolean(Cache.LITTLE_PLANET_ENABLE,false);
        if (!gyro && lilPlanet) cache.save(Cache.LITTLE_PLANET_ENABLE, false);
        cache.save(Cache.GYRO_ENABLE,!gyro);
    }

    private void littlePlanetValidation() {
        boolean gyro = cache.getBoolean(Cache.GYRO_ENABLE,false);
        boolean lilPlanet = cache.getBoolean(Cache.LITTLE_PLANET_ENABLE,false);
        if (!lilPlanet && gyro) cache.save(Cache.GYRO_ENABLE,false);
        cache.save(Cache.LITTLE_PLANET_ENABLE, !lilPlanet);
    }

    private void instatiateFeedDisplayButton() {
        boolean gyro = cache.getBoolean(Cache.GYRO_ENABLE,false);
        boolean lilPlanet = cache.getBoolean(Cache.LITTLE_PLANET_ENABLE,false);

        binding.gyroButton.setBackgroundResource(gyro?R.drawable.gyro_big_active_icn:R.drawable.gyro_big_inactive_icn);
        binding.settingsGyro.setTextColor(gyro?getResources().getColor(R.color.text_active):getResources().getColor(R.color.text_inactive));
        binding.littlePlanetButton.setBackgroundResource(lilPlanet?R.drawable.little_planet_big_active_icn:R.drawable.little_planet_big_inactive_icn);
        binding.settingsLittlePlanet.setTextColor(lilPlanet ? getResources().getColor(R.color.text_active) : getResources().getColor(R.color.text_inactive));
    }

    private void activeOneRing() {
        binding.oneRingButton.setBackgroundResource(R.drawable.one_ring_active_icn);
        binding.settingsOneRing.setTextColor(getResources().getColor(R.color.text_active));
        binding.threeRingButton.setBackgroundResource(R.drawable.three_ring_inactive_icn);
        binding.settingsThreeRing.setTextColor(getResources().getColor(R.color.text_inactive));
    }

    private void activeThreeRing() {
        binding.threeRingButton.setBackgroundResource(R.drawable.three_ring_active_icn);
        binding.settingsThreeRing.setTextColor(getResources().getColor(R.color.text_active));
        binding.oneRingButton.setBackgroundResource(R.drawable.one_ring_inactive_icn);
        binding.settingsOneRing.setTextColor(getResources().getColor(R.color.text_inactive));
    }

    private void activeManualType() {
        binding.motorOffButton.setBackgroundResource(R.drawable.motor_off_active_icn);
        binding.settingsMotorOff.setTextColor(getResources().getColor(R.color.text_active));
        binding.motorOnButton.setBackgroundResource(R.drawable.motor_on_inactive_icn);
        binding.settingsMotorOn.setTextColor(getResources().getColor(R.color.text_inactive));
        cache.save(Cache.MOTOR_ON, false);
        cache.save(Cache.BLE_DEVICE_ADDRESS, "");
        cache.save(Cache.BLE_DEVICE_NAME, "");
    }

    private void activeMotorType() {
        binding.motorOnButton.setBackgroundResource(R.drawable.motor_on_active);
        binding.settingsMotorOn.setTextColor(getResources().getColor(R.color.text_active));
        binding.motorOffButton.setBackgroundResource(R.drawable.motor_off_inactive_icn);
        binding.settingsMotorOff.setTextColor(getResources().getColor(R.color.text_inactive));
        cache.save(Cache.MOTOR_ON, true);
        if(cache.getString(Cache.BLE_DEVICE_ADDRESS).equals("")){
            Intent intent = new Intent(getActivity(), BLEListActivity.class);
            startActivityForResult(intent,REQUEST_BLE_LIST);
        }
    }

    private void initializeShareButtons() {
        Log.d("myTag", "initializeShare: fb: " + cache.getBoolean(Cache.POST_OPTO_TO_FB, false) + " twitter: " + cache.getBoolean(Cache.POST_OPTO_TO_TWITTER, false));
        if (cache.getBoolean(Cache.POST_OPTO_TO_FB, false)) {
//            fbShareButton.setBackgroundColor(getResources().getColor(R.color.debugView1));
            binding.fbShare.setBackgroundResource(R.drawable.facebook_share_active);
        } else /*fbShareButton.setBackgroundColor(getResources().getColor(R.color.timeAgoFontColor));*/ binding.fbShare.setBackgroundResource(R.drawable.facebook_share_inactive);
        if (cache.getBoolean(Cache.POST_OPTO_TO_TWITTER, false)) {
//            twitterShareButton.setBackgroundColor(getResources().getColor(R.color.debugView1));
            binding.twitterShare.setBackgroundResource(R.drawable.twitter_share_active);
        } else
//            twitterShareButton.setBackgroundColor(getResources().getColor(R.color.timeAgoFontColor));
            binding.twitterShare.setBackgroundResource(R.drawable.twitter_share_inactive);
    }

    private void sharedNotLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_login_needed)
                .setMessage(R.string.profile_login_first)
                .setNegativeButton(getResources().getString(R.string.dialog_continue), (dialog, which) -> {
                    dialog.dismiss();
                });
        builder.create().show();
    }

    private void loginFacebook() {
        Log.d("myTag", "loginFacebook");
        final List<String> PUBLISH_PERMISSIONS = Arrays.asList("publish_actions");
        LoginManager.getInstance().logInWithPublishPermissions(this, PUBLISH_PERMISSIONS);
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("myTag", " share: success login on fb: " + loginResult.getAccessToken().getUserId());

                cache.save(Cache.USER_FB_ID, loginResult.getAccessToken().getUserId());
                cache.save(Cache.USER_FB_TOKEN, loginResult.getAccessToken().getToken());
                cache.save(Cache.USER_FB_LOGGED_IN, true);
                isFBShare = true;
                cache.save(Cache.POST_OPTO_TO_FB, isFBShare);
                PersonManager.updatePerson();
                initializeShareButtons();
            }

            @Override
            public void onCancel() {
                Log.d("myTag", "oncancel login on fb.");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("myTag", "onError login on fb.");
            }
        });
    }

    private void loginTwitter() {
        binding.twitterProgress.setVisibility(View.VISIBLE);
        binding.twitterShare.setClickable(false);
        final ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
        builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

        final Configuration configuration = builder.build();
        final TwitterFactory factory = new TwitterFactory(configuration);
        twitter = factory.getInstance();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    requestToken = twitter.getOAuthRequestToken(CALLBACK_URL);

                    /**
                     *  Loading twitter login page on webview for authorization
                     *  Once authorized, results are received at onActivityResult
                     *  */
                    final Intent intent = new Intent(getActivity().getApplicationContext(), WebViewActivity.class);
                    intent.putExtra(WebViewActivity.EXTRA_URL, requestToken.getAuthenticationURL());
                    startActivityForResult(intent, WEBVIEW_REQUEST_CODE);
//                    new UpdatePersonSocialData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    PersonManager.updatePerson();
                    Log.d("myTag"," share: twitter try");
                } catch (TwitterException e) {
                    Log.d("myTag"," share: twitter catch message: "+e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    class TwitterLoggedIn extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            for (String verifier : params) {
                try {
                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

                    long userID = accessToken.getUserId();
                    final User user = twitter.showUser(userID);
                    String username = user.getName();
                    cache.save(Cache.USER_TWITTER_TOKEN, accessToken.getToken());
                    cache.save(Cache.USER_TWITTER_SECRET, accessToken.getTokenSecret());
                    cache.save(Cache.USER_TWITTER_LOGGED_IN, true);

                    Log.d("myTag", " screenName: " + accessToken.getScreenName() + " userId: " + accessToken.getUserId() + " " + accessToken.getTokenSecret());

                    Log.d("myTag", "Hello " + username);
                } catch (Exception e) {
                    Log.e("Twitter Login Failed", " Error: " + e.toString());
                    Snackbar.make(binding.twitterShare, "Twitter Login Failed.", Snackbar.LENGTH_SHORT).show();isTwitterShare = !cache.getBoolean(Cache.POST_OPTO_TO_TWITTER, false);
                    cache.save(Cache.POST_OPTO_TO_TWITTER, isTwitterShare);
                    initializeShareButtons();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isTwitterShare = true;
            cache.save(Cache.POST_OPTO_TO_TWITTER, isTwitterShare);
            binding.twitterProgress.setVisibility(View.GONE);
            binding.twitterShare.setClickable(true);
            initializeShareButtons();
        }
    }

    // Settings end

    @Subscribe
    public void receiveFinishedImage(RecordFinishedEvent recordFinishedEvent) {
        Timber.d("receiveFinishedImage");
        binding.recordProgress.setVisibility(View.GONE);
        binding.cameraBtn.setEnabled(true);
    }

    public void refreshAfterDelete(String id,boolean isLocal) {
        optographFeedAdapter.refreshAfterDelete(id, isLocal);
    }

}