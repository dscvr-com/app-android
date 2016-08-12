package com.iam360.iam360.views.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;

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

    NetworkProblemDialog networkProblemDialog;

    private SensorManager sensorManager;
    private boolean inVRMode;

    private DateTime inVRPositionSince;

    private List<Optograph> optographs = new ArrayList<>();
    private DBHelper mydb;

    private int PICK_IMAGE_REQUEST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkProblemDialog = new NetworkProblemDialog();
        inVRMode = false;
        inVRPositionSince = null;
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mydb = new DBHelper(getContext());
        setHasOptionsMenu(true);
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
                refresh();
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

        Timber.d("ONRESUME");

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
                                    if (!networkProblemDialog.isAdded())networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                                    return null;
                                })
                                .subscribe(optographFeedAdapter::addItem);
                    })
                    .onErrorReturn(throwable -> {
                        throwable.printStackTrace();
                        if (!networkProblemDialog.isAdded())networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                        return null;
                    })
                    .subscribe(optographFeedAdapter::addItem);
        } else {
            ProgressDialog progress = new ProgressDialog(getActivity());
            progress.setTitle("Fetching...");
//            progress.setMessage("Wait while loading...");
            progress.show();
            apiConsumer.getOptographs(5)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() -> progress.dismiss() )
                    .onErrorReturn(throwable -> {
                        if (!networkProblemDialog.isAdded())networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                        progress.dismiss();
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
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        String columnName = cursor.getColumnName(i);
                        rowObject.put(columnName,
                                cursor.getString(i));
                        Timber.d("CURSOR : " + columnName + " " + cursor.getString(i));
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            }
//            resultSet.put(rowObject);

            String json = rowObject.toString();
            Log.d("MARK","List<Optograph> opto = "+json);
            Gson gson = new Gson();
            Optographs data = gson.fromJson(json, Optographs.class);

            Optograph opto = new Optograph(data.optograph_id);

            Person person = new Person();
            if(data.optograph_person_id !=null && !data.optograph_person_id.equals("")){
                Cursor res = mydb.getData(data.optograph_person_id, DBHelper.PERSON_TABLE_NAME,"id");
                res.moveToFirst();
                if (res.getCount()!= 0) {
                    person.setId(res.getString(res.getColumnIndex("id")));
                    person.setCreated_at(res.getString(res.getColumnIndex("created_at")));
                    person.setDisplay_name(res.getString(res.getColumnIndex("display_name")));
//                        Log.d("MARK","cur2Json user_name = "+res.getString(res.getColumnIndex("user_name")));
                    person.setUser_name(res.getString(res.getColumnIndex("user_name")));
                    person.setText(res.getString(res.getColumnIndex("text")));
                    person.setAvatar_asset_id(res.getString(res.getColumnIndex("avatar_asset_id")));
                }
            }
//                if(!person.is_followed()){
//                    continue;
//                }
            opto.setPerson(person);
            opto.setCreated_at(data.optograph_created_at);
            opto.setIs_starred(data.optograph_is_starred);
            opto.setDeleted_at(data.optograph_deleted_at);
            opto.setStitcher_version(data.optograph_stitcher_version);
            opto.setText(data.optograph_text);
            opto.setViews_count(data.optograph_views_count);
            opto.setIs_staff_picked(data.optograph_is_staff_pick);
            opto.setShare_alias(data.optograph_share_alias);
            opto.setIs_private(data.optograph_is_private);
            opto.setIs_published(data.optograph_is_published);
            opto.setLeft_texture_asset_id(data.optograph_left_texture_asset_id);
            opto.setRight_texture_asset_id(data.optograph_right_texture_asset_id);
            opto.setIs_local(false);

            Location location = new Location();
            if(data.optograph_location_id !=null && !data.optograph_location_id.equals("")){
                Cursor res = mydb.getData(data.optograph_location_id, DBHelper.LOCATION_TABLE_NAME,"id");
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

            opto.setOptograph_type(data.optograph_type);
            opto.setStars_count(data.optograph_stars_count);
            opto.setComments_count(data.optograph_comments_count);
            opto.setHashtag_string(data.optograph_hashtag_string);

            optographs.add(opto);
//                Log.d("MARK","cur2Json opto = "+opto.toString());

            cursor.moveToNext();
        }

        cursor.close();


//        List<Optograph> optographs = new LinkedList<>();
//        for(int i=0; i < resultSet.length(); i++){
//            try {
//                String json = resultSet.get(i).toString();
//                Log.d("MARK","List<Optograph> opto = "+json);
//                Gson gson = new Gson();
//                Optographs data = gson.fromJson(json, Optographs.class);
//
//                Optograph opto = new Optograph(data.optograph_id);
//
//                Person person = new Person();
//                if(data.optograph_person_id !=null && !data.optograph_person_id.equals("")){
//                    Cursor res = mydb.getData(data.optograph_person_id, DBHelper.PERSON_TABLE_NAME,"id");
//                    res.moveToFirst();
//                    if (res.getCount()!= 0) {
//                        person.setId(res.getString(res.getColumnIndex("id")));
//                        person.setCreated_at(res.getString(res.getColumnIndex("created_at")));
//                        person.setDisplay_name(res.getString(res.getColumnIndex("display_name")));
////                        Log.d("MARK","cur2Json user_name = "+res.getString(res.getColumnIndex("user_name")));
//                        person.setUser_name(res.getString(res.getColumnIndex("user_name")));
//                        person.setText(res.getString(res.getColumnIndex("text")));
//                        person.setAvatar_asset_id(res.getString(res.getColumnIndex("avatar_asset_id")));
//                    }
//                }
////                if(!person.is_followed()){
////                    continue;
////                }
//                opto.setPerson(person);
//                opto.setCreated_at(data.optograph_created_at);
//                opto.setIs_starred(data.optograph_is_starred);
//                opto.setDeleted_at(data.optograph_deleted_at);
//                opto.setStitcher_version(data.optograph_stitcher_version);
//                opto.setText(data.optograph_text);
//                opto.setViews_count(data.optograph_views_count);
//                opto.setIs_staff_picked(data.optograph_is_staff_pick);
//                opto.setShare_alias(data.optograph_share_alias);
//                opto.setIs_private(data.optograph_is_private);
//                opto.setIs_published(data.optograph_is_published);
//                opto.setLeft_texture_asset_id(data.optograph_left_texture_asset_id);
//                opto.setRight_texture_asset_id(data.optograph_right_texture_asset_id);
//                opto.setIs_local(false);
//
//                Location location = new Location();
//                if(data.optograph_location_id !=null && !data.optograph_location_id.equals("")){
//                    Cursor res = mydb.getData(data.optograph_location_id, DBHelper.LOCATION_TABLE_NAME,"id");
//                    res.moveToFirst();
//                    if (res.getCount()!= 0) {
//                        location.setId(res.getString(res.getColumnIndex("id")));
//                        location.setCreated_at(res.getString(res.getColumnIndex("created_at")));
//                        location.setText(res.getString(res.getColumnIndex("text")));
//                        location.setCountry(res.getString(res.getColumnIndex("id")));
//                        location.setCountry_short(res.getString(res.getColumnIndex("country")));
//                        location.setPlace(res.getString(res.getColumnIndex("place")));
//                        location.setRegion(res.getString(res.getColumnIndex("region")));
//                        location.setPoi(Boolean.parseBoolean(res.getString(res.getColumnIndex("poi"))));
//                        location.setLatitude(res.getString(res.getColumnIndex("latitude")));
//                        location.setLongitude(res.getString(res.getColumnIndex("longitude")));
//                    }
//                }
//                opto.setLocation(location);
//
//                opto.setOptograph_type(data.optograph_type);
//                opto.setStars_count(data.optograph_stars_count);
//                opto.setComments_count(data.optograph_comments_count);
//                opto.setHashtag_string(data.optograph_hashtag_string);
//
//                optographs.add(opto);
////                Log.d("MARK","cur2Json opto = "+opto.toString());
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        Log.d("MARK","cur2Json optographs.size = "+optographs.size());
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
                        if (!networkProblemDialog.isAdded())
                            networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
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
        Timber.d("Refresh");
        if(apiConsumer == null) return;
        Cursor curs = mydb.getFeedsData(5);
        Log.d("MARK","refresh cursCount - "+curs.getCount());
        if (curs.getCount() > 0) {
            cur2Json(curs)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() ->{
                        apiConsumer.getOptographs(5)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .onErrorReturn(throwable -> {
                                    if (!networkProblemDialog.isAdded())networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                                    return null;
                                })
                                .subscribe(optographFeedAdapter::addItem);
                    })
                    .onErrorReturn(throwable -> {
                        if (!networkProblemDialog.isAdded())networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                        return null;
                    })
                    .subscribe(optographFeedAdapter::addItem);
        }else{
            apiConsumer.getOptographs(5)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
//                                  .doOnCompleted(() -> MixpanelHelper.trackViewViewer2D(getActivity()))
                    .onErrorReturn(throwable -> {
                        if (!networkProblemDialog.isAdded())networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
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
