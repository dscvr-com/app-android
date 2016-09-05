package com.iam360.iam360.views.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ClipDrawable;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.gson.Gson;
import com.iam360.iam360.R;
import com.iam360.iam360.bus.BusProvider;
import com.iam360.iam360.bus.RecordFinishedEvent;
import com.iam360.iam360.gcm.Optographs;
import com.iam360.iam360.model.Location;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.record.GlobalState;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.Constants;
import com.iam360.iam360.util.DBHelper;
import com.iam360.iam360.util.GeneralUtils;
import com.iam360.iam360.viewmodels.LocalOptographManager;
import com.iam360.iam360.views.activity.ImagePickerActivity;
import com.iam360.iam360.views.activity.MainActivity;
import com.iam360.iam360.views.activity.RecorderActivity;
import com.iam360.iam360.views.activity.SearchActivity;
import com.iam360.iam360.views.dialogs.NetworkProblemDialog;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import me.leolin.shortcutbadger.ShortcutBadger;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inVRMode = false;
        inVRPositionSince = null;
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mydb = new DBHelper(getContext());
        setHasOptionsMenu(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.dialog_network_retry));
        builder.setCancelable(true);

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
        binding.manualButton.setOnClickListener(this);
        binding.settingsManual.setOnClickListener(this);
        binding.motorButton.setOnClickListener(this);
        binding.settingsMotor.setOnClickListener(this);
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
                        break;
                    case COLLAPSED:
                    case ANCHORED:
                    case HIDDEN:
                        binding.pullUpButton.setVisibility(View.GONE);
                        binding.profileBtn.setVisibility(View.VISIBLE);
                        binding.searchButton.setVisibility(View.VISIBLE);
                        binding.barTransparent.setBackgroundColor(getResources().getColor(R.color.transparentOverlay));
                        ((MainActivity)getActivity()).dragSettingPage(false);
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

    }

    @Override
    public void initializeFeed(boolean fromList) {
        Cursor curs = mydb.getFeedsData(5);
        Log.d("MARK","initializeFeed curs.getCount() = "+curs.getCount());
        if (curs.getCount() > 0) {
            cur2Json(curs)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() ->{
                        apiConsumer.getOptographs(5)
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
            apiConsumer.getOptographs(5)
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

        LocalOptographManager.getOptographs()
                .observeOn(AndroidSchedulers.mainThread())
                .filter(e -> !mydb.checkIfAllImagesUploaded(e.getId()))
                .subscribe(this::countLocal);
        GlobalState.shouldHardRefreshFeed = false;

        // refresh notification badge
        if(cache.getInt(Cache.NOTIF_COUNT) > 0) binding.notifBadge.setVisibility(View.VISIBLE);
        else binding.notifBadge.setVisibility(View.GONE);

    }

    public Observable<Optograph> cur2Json(Cursor cursor) {
//        JSONArray resultSet = new JSONArray();
        List<Optograph> optographs = new LinkedList<>();
        cursor.moveToFirst();

        for(int a=0; a < cursor.getCount(); a++){
                    Optograph opto = null;
                    String personId = null;
                    try {
                        opto = new Optograph(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_ID)));
                        opto.setCreated_at(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_CREATED_AT)));
                        opto.setIs_starred(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_STARRED)) == 1 ? true : false);
                        opto.setDeleted_at(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_DELETED_AT)));
                        opto.setStitcher_version(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_STITCHER_VERSION)));
                        opto.setText(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_TEXT)));
                        opto.setViews_count(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_STARS_COUNT)));
                        opto.setIs_staff_picked(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_STAFF_PICK)) == 1 ? true : false);
                        opto.setShare_alias(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_SHARE_ALIAS)));
                        opto.setIs_private(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_PRIVATE)) == 1 ? true : false);
                        opto.setIs_published(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_PUBLISHED)) == 1 ? true : false);
                        opto.setOptograph_type(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_TYPE)));
                        opto.setStars_count(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_STARS_COUNT)));
                        opto.setShould_be_published(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) == 1 ? true : false);
                        opto.setIs_local(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_LOCAL)) == 1 ? true : false);
                        personId = cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_PERSON_ID));

                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                    }

            Person person = new Person();
            if(personId !=null && !personId.equals("")){
                Cursor res = mydb.getData(personId, DBHelper.PERSON_TABLE_NAME,"id");
                res.moveToFirst();
                if (res.getCount()!= 0) {
                    person.setId(res.getString(res.getColumnIndex("id")));
                    person.setCreated_at(res.getString(res.getColumnIndex("created_at")));
                    person.setDeleted_at(res.getString(res.getColumnIndex("deleted_at")));
                    person.setDisplay_name(res.getString(res.getColumnIndex("display_name")));
                    person.setUser_name(res.getString(res.getColumnIndex("user_name")));
                    person.setText(res.getString(res.getColumnIndex("email")));
                    person.setEmail(res.getString(res.getColumnIndex("text")));
                    person.setElite_status(res.getInt(res.getColumnIndex("elite_status")) == 1 ? true : false);
                    person.setAvatar_asset_id(res.getString(res.getColumnIndex("avatar_asset_id")));
                    person.setOptographs_count(res.getInt(res.getColumnIndex("optographs_count")));
                    person.setFollowers_count(res.getInt(res.getColumnIndex("followers_count")));
                    person.setFollowed_count(res.getInt(res.getColumnIndex("followed_count")));
                    person.setIs_followed(res.getInt(res.getColumnIndex("is_followed")) == 1 ? true : false);
                    person.setFacebook_user_id(res.getString(res.getColumnIndex("facebook_user_id")));
                    person.setFacebook_token(res.getString(res.getColumnIndex("facebook_token")));
                    person.setTwitter_token(res.getString(res.getColumnIndex("twitter_token")));
                    person.setTwitter_secret(res.getString(res.getColumnIndex("twitter_secret")));
                }
            }
//                if(!person.is_followed()){
//                    continue;
//                }
            opto.setPerson(person);

            Location location = new Location();
            if(opto != null && opto.getLocation().getId() !=null && !opto.getLocation().getId().equals("")){
                Cursor res = mydb.getData(opto.getLocation().getId(), DBHelper.LOCATION_TABLE_NAME,"id");
                res.moveToFirst();
                if (res.getCount()!= 0) {
                    location.setId(res.getString(res.getColumnIndex("id")));
                    location.setCreated_at(res.getString(res.getColumnIndex("created_at")));
                    location.setText(res.getString(res.getColumnIndex("text")));
                    location.setCountry(res.getString(res.getColumnIndex("id")));
                    location.setCountry_short(res.getString(res.getColumnIndex("country")));
                    location.setPlace(res.getString(res.getColumnIndex("place")));
                    location.setRegion(res.getString(res.getColumnIndex("region")));
                    location.setPoi(Boolean.parseBoolean(res.getString(res.getColumnIndex("poi"))));
                    location.setLatitude(res.getString(res.getColumnIndex("latitude")));
                    location.setLongitude(res.getString(res.getColumnIndex("longitude")));
                }
            }
            opto.setLocation(location);
            optographs.add(opto);

            Timber.d("FROMDB : " + opto.getPerson().getUser_name() + " " + opto.is_starred());

            cursor.moveToNext();
        }

        cursor.close();
        return Observable.from(optographs);
    }

    private void countLocal(Optograph optograph) {
        if (optograph == null) {
            return;
        }

        Log.d("myTag","countLocal user equal? "+(optograph.getPerson().getId().equals(cache.getString(Cache.USER_ID))));
        if (optograph.getPerson().getId().equals(cache.getString(Cache.USER_ID))) {
            optographFeedAdapter.saveToSQLite(optograph);
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
        Log.d("MARK", "load cursCount - " + curs.getCount());
        if (curs.getCount() > 0) {
            cur2Json(curs)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() ->{
                        apiConsumer.getOptographs(5, optographFeedAdapter.getOldest().getCreated_at())
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
            apiConsumer.getOptographs(5, optographFeedAdapter.getOldest().getCreated_at())
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

        // TODO: prefetch textures
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
            cur2Json(curs)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() ->{
                        if(scrollToTop) mLayoutManager.scrollToPosition(0);
                        apiConsumer.getOptographs(5)
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
            apiConsumer.getOptographs(5)
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

        LocalOptographManager.getOptographs()
                .observeOn(AndroidSchedulers.mainThread())
                .filter(e -> !mydb.checkIfAllImagesUploaded(e.getId()))
                .subscribe(this::countLocal);

        disableDrag();
        binding.swipeRefreshLayout.setRefreshing(false);

        // refresh notification badge
        if(cache.getInt(Cache.NOTIF_COUNT) > 0) binding.notifBadge.setVisibility(View.VISIBLE);
        else binding.notifBadge.setVisibility(View.GONE);

    }

    public void disableDrag() {
        if(optographFeedAdapter != null)
            optographFeedAdapter.disableDraggingPage(firstVisible);
    }

    @Subscribe
    public void recordFinished(RecordFinishedEvent event) {
        initializeFeed(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
                    intent = new Intent(getActivity(), RecorderActivity.class);
                    startActivity(intent);
                } else
                    ((MainActivity) getActivity()).setPage(MainActivity.PROFILE_MODE);


                break;
            case R.id.settings_btn:
                ((MainActivity) getActivity()).dragSettingPage(true);
                binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                break;
            case R.id.header_logo:
//                binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                break;
            case R.id.theta_btn:
                Intent intent1;
                if(!cache.getString(Cache.GATE_CODE).equals("")) {
                    intent = new Intent(getActivity(), ImagePickerActivity.class);
                    startActivity(intent);
                } else
                    ((MainActivity) getActivity()).setPage(MainActivity.PROFILE_MODE);

//                Intent intent1 = new Intent();
//                intent1.setType("image/*");
//                intent1.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent1, "Select Image"), PICK_IMAGE_REQUEST);
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
            case R.id.settings_manual:
            case R.id.manual_button:
                if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE)!= Constants.MANUAL_MODE) {
                    cache.save(Cache.CAMERA_CAPTURE_TYPE, Constants.MANUAL_MODE);
                    activeManualType();
                }
                break;
            case R.id.settings_motor:
            case R.id.motor_button:
                if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE) != Constants.MOTOR_MODE) {
                    cache.save(Cache.CAMERA_CAPTURE_TYPE, Constants.MOTOR_MODE);
                    activeMotorType();
                }
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
        }

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
        binding.manualButton.setBackgroundResource(R.drawable.manual_active_icn);
        binding.settingsManual.setTextColor(getResources().getColor(R.color.text_active));
        binding.motorButton.setBackgroundResource(R.drawable.motor_inactive_icn);
        binding.settingsMotor.setTextColor(getResources().getColor(R.color.text_inactive));
    }

    private void activeMotorType() {
        binding.motorButton.setBackgroundResource(R.drawable.motor_active_icn);
        binding.settingsMotor.setTextColor(getResources().getColor(R.color.text_active));
        binding.manualButton.setBackgroundResource(R.drawable.manual_inactive_icn);
        binding.settingsManual.setTextColor(getResources().getColor(R.color.text_inactive));
    } // Settings end

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
