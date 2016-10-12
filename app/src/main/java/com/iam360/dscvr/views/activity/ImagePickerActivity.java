package com.iam360.dscvr.views.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.iam360.dscvr.R;
import com.iam360.dscvr.gcm.Optographs;
import com.iam360.dscvr.model.Location;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.Person;
import com.iam360.dscvr.network.ApiConsumer;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.DBHelper;
import com.iam360.dscvr.util.GeneralUtils;
import com.iam360.dscvr.viewmodels.ImagePickerAdapter;
import com.iam360.dscvr.viewmodels.InfiniteScrollListener;
import com.iam360.dscvr.viewmodels.OptographListAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.JSONObject;

public class ImagePickerActivity extends AppCompatActivity {

    public static final int MIN_WIDTH = 4000;
    public static final int NUM_COLUMNS_OPTO = 3;
    public static final int NUM_COLUMNS_STORY = 4;

    public static final int UPLOAD_OPTO_MODE = 0;
    public static final int CREATE_STORY_MODE = 1;
    public static final int CREATE_STORY_MODE2 = 3;
    public static final int ADD_SCENE_MODE = 2;

    public static final String PICKER_MODE = "mode";

    private int MODE = UPLOAD_OPTO_MODE;


    private Person person;
    private Cache cache;
    private ApiConsumer apiConsumer;
    private DBHelper mydb;
    private OptographListAdapter optographLocalGridAdapter;

    GridLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        MODE = getIntent().getExtras().getInt(PICKER_MODE);
        person = getIntent().getExtras().getParcelable("person");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        if(MODE == UPLOAD_OPTO_MODE) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.optonautMain_2));
            title.setText(getResources().getString(R.string.image_picker_your_images));
            title.setTextColor(Color.WHITE);
        }

        new GeneralUtils().setFont(this, title, Typeface.BOLD);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(this, MODE == UPLOAD_OPTO_MODE ? NUM_COLUMNS_OPTO : NUM_COLUMNS_STORY);
        recyclerView.setLayoutManager(layoutManager);

        if(MODE == CREATE_STORY_MODE || MODE == CREATE_STORY_MODE2) {
            cache = Cache.open();
            String token = cache.getString(cache.USER_TOKEN);
            apiConsumer = new ApiConsumer(token.equals("")?null:token);
            mydb = new DBHelper(this);

            optographLocalGridAdapter = new OptographListAdapter(this,MODE);

            if (person != null) {
                setAdapter(recyclerView);
                initializeFeed();
            }
        }else{
            ImagePickerAdapter adapter = new ImagePickerAdapter(this, listImages(), MODE);
            recyclerView.setAdapter(adapter);
        }
    }

    private ArrayList<String> listImages() {
        //where contextObject is your activity
        ContentResolver cr = getContentResolver();

        ArrayList<String> imagePathList = new ArrayList<String>();
        String[] columns = new String[] {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.TITLE,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.MIME_TYPE,
                MediaStore.Images.ImageColumns.WIDTH,
                MediaStore.Images.ImageColumns.HEIGHT,
                MediaStore.Images.ImageColumns.SIZE,
                MediaStore.Images.ImageColumns.DATE_TAKEN };

        Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        if (cursor.moveToFirst()) {
            do {
//                final String title = cursor.getString(cursor.getColumnIndex(columns[1]));
                final String data = cursor.getString(cursor.getColumnIndex(columns[2]));
//                final String mimeType = cursor.getString(cursor.getColumnIndex(columns[3]));
                final String width = cursor.getString(cursor.getColumnIndex(columns[4]));
//                final String height = cursor.getString(cursor.getColumnIndex(columns[5]));
//                final String size = cursor.getString(cursor.getColumnIndex(columns[6]));
//                final String date = cursor.getString(cursor.getColumnIndex(columns[7]));
//                Timber.d("PATH : " + title + ":" + data + ":" + mimeType + ":" + width + ":" + height + ":" + size);

                if(width != null && Integer.parseInt(width) > MIN_WIDTH) {
                    imagePathList.add(data);
//                    Timber.d("360IMAGES " + date);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();

        return imagePathList;

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 11) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("opto_id", data.getStringExtra("opto_id"));
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }



    private void setAdapter(RecyclerView recyclerView) {
        recyclerView.setAdapter(optographLocalGridAdapter);
        recyclerView.setItemViewCacheSize(10);

        recyclerView.addOnScrollListener(new InfiniteScrollListener(layoutManager) {
            int yPos = 0;
            float height01=0;

            @Override
            public void onLoadMore() {
                Log.d("myTag", TAG + " onLoadMore");
                loadMore();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                View view = recyclerView.getChildAt(1);
                if (view==null)return;
                yPos += dy;
                float top = view.getY();
                if((top + view.getHeight())>height01) {
                    height01 = top + view.getHeight();
                }
            }
        });

    }

    public void loadMore() {
        apiConsumer.getOptographsFromPerson(person.getId(), optographLocalGridAdapter.getOldest().getCreated_at())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
//                    getFragmentManager().executePendingTransactions();
//                    if(!networkProblemDialog.isAdded())networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                    return null;
                })
                .subscribe(optographLocalGridAdapter::addItem);
    }

    public void initializeFeed() {
//        optographLocalGridAdapter.setPerson(person);
        Cursor cursor = mydb.getUserOptoList(person.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS);
        cursor.moveToFirst();

        Log.v("MARK","initializeFeed person.getId() = "+person.getId());
        Log.v("MARK","initializeFeed cursor.getCount() = "+cursor.getCount());
        if (cursor.getCount()!=0) {
            cur2Json(cursor, ApiConsumer.PROFILE_GRID_LIMIT)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() -> {
                        apiConsumer.getOptographsFromPerson(person.getId(), ApiConsumer.PROFILE_GRID_LIMIT)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
//                                .doOnCompleted(() -> updateMessage(null))
                                .onErrorReturn(throwable -> {
//                                    updateMessage(getResources().getString(R.string.profile_net_prob));
                                    return null;
                                })
                                .subscribe(optographLocalGridAdapter::addItem);
                    })
                    .onErrorReturn(throwable -> {
                        Log.d("myTag"," Error: message: "+throwable.getMessage());
                        return null;
                    })
                    .subscribe(optographLocalGridAdapter::addItem);
        }
    }

    public Observable<Optograph> cur2Json(Cursor cursor, int limit) {
        List<Optograph> optographs = new LinkedList<>();
        cursor.moveToFirst();
        if(limit > cursor.getCount()){
            limit = cursor.getCount();
        }
        for(int a=0; a < limit; a++){
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        String columnName = cursor.getColumnName(i);
                        rowObject.put(columnName,
                                cursor.getString(i));
//                        Timber.d("CURSOR : " + columnName + " " + cursor.getString(i));
                    } catch (Exception e) {
                        Log.d("MARK", e.getMessage());
                    }
                }
            }

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
                    location.setLatitude(Double.parseDouble(res.getString(res.getColumnIndex("latitude"))));
                    location.setLongitude(Double.parseDouble(res.getString(res.getColumnIndex("longitude"))));
                }
            }
            opto.setLocation(location);

            opto.setOptograph_type(data.optograph_type);
            opto.setStars_count(data.optograph_stars_count);
            opto.setComments_count(data.optograph_comments_count);
            opto.setHashtag_string(data.optograph_hashtag_string);

            optographs.add(opto);

            cursor.moveToNext();
        }

        cursor.close();

        return Observable.from(optographs);
    }

}