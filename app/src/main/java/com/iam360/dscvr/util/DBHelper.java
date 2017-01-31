package com.iam360.dscvr.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.iam360.dscvr.model.Location;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.Person;

import org.joda.time.DateTime;

import timber.log.Timber;

/**
 * Created by Mariel on 4/18/2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "OptoData.db";
    public static final String OPTO_TABLE_NAME_FEEDS = "Optograph_Feeds";
    public static final String PERSON_TABLE_NAME = "Person";
    public static final String STORY_TABLE_NAME = "Story";
    public static final String STORY_CHILDREN_TABLE_NAME = "Story_Children";
    public static final String LOCATION_TABLE_NAME = "Location";
    public static final String FACES_TABLE_NAME = "OptoCubeFaces";

    public static final String OPTOGRAPH_ID = "optograph_id";
    public static final String OPTOGRAPH_TEXT = "optograph_text";
    public static final String OPTOGRAPH_PERSON_ID = "optograph_person_id";
    public static final String OPTOGRAPH_LOCATION_ID = "optograph_location_id";
    public static final String OPTOGRAPH_CREATED_AT = "optograph_created_at";
    public static final String OPTOGRAPH_DELETED_AT = "optograph_deleted_at";
    public static final String OPTOGRAPH_IS_STARRED = "optograph_is_starred";
    public static final String OPTOGRAPH_IS_STAFF_PICK = "optograph_is_staff_pick";
    public static final String OPTOGRAPH_STARS_COUNT = "optograph_stars_count";
    public static final String OPTOGRAPH_IS_PUBLISHED = "optograph_is_published";
    public static final String OPTOGRAPH_IS_PRIVATE = "optograph_is_private";
    public static final String OPTOGRAPH_IS_STITCHER_VERSION = "optograph_is_stitcher_version";
    public static final String OPTOGRAPH_IS_IN_FEED = "optograph_is_in_feed";
    public static final String OPTOGRAPH_IS_ON_SERVER = "optograph_is_on_server";
    public static final String OPTOGRAPH_UPDATED_AT = "optograph_updated_at";
    public static final String OPTOGRAPH_SHOULD_BE_PUBLISHED = "optograph_should_be_published";
    public static final String OPTOGRAPH_IS_LOCAL = "optograph_is_local";
    public static final String OPTOGRAPH_IS_DATA_UPLOADED = "optograph_is_data_uploaded";
    public static final String OPTOGRAPH_IS_PLACEHOLDER_UPLOADED = "optograph_is_place_holder_uploaded";
    public static final String OPTOGRAPH_POST_FACEBOOK = "post_facebook";
    public static final String OPTOGRAPH_POST_TWITTER = "post_twitter";
    public static final String OPTOGRAPH_POST_INSTAGRAM = "post_instagram";
    public static final String OPTOGRAPH_TYPE = "optograph_type";
    public static final String OPTOGRAPH_SHARE_ALIAS = "optograph_share_alias";
    public static final String OPTOGRAPH_STORY_ID = "optograph_story_id";


    public static final String FACES_ID = "faces_optograph_id";
    public static final String FACES_LEFT_ZERO = "faces_left_zero";
    public static final String FACES_LEFT_ONE = "faces_left_one";
    public static final String FACES_LEFT_TWO = "faces_left_two";
    public static final String FACES_LEFT_THREE = "faces_left_three";
    public static final String FACES_LEFT_FOUR = "faces_left_four";
    public static final String FACES_LEFT_FIVE = "faces_left_five";
    public static final String FACES_RIGHT_ZERO = "faces_right_zero";
    public static final String FACES_RIGHT_ONE = "faces_right_one";
    public static final String FACES_RIGHT_TWO = "faces_right_two";
    public static final String FACES_RIGHT_THREE = "faces_right_three";
    public static final String FACES_RIGHT_FOUR = "faces_right_four";
    public static final String FACES_RIGHT_FIVE = "faces_right_five";

    public static final String PERSON_ID = "id";
    public static final String PERSON_CREATED_AT = "created_at";
    public static final String PERSON_DELETED_AT = "deleted_at";
    public static final String PERSON_DISPLAY_NAME = "display_name";
    public static final String PERSON_USERNAME = "user_name";
    public static final String PERSON_EMAIL = "email";
    public static final String PERSON_TEXT = "text";
    public static final String PERSON_ELITE_STATUS = "elite_status";
    public static final String PERSON_AVATAR_ASSET_ID = "avatar_asset_id";
    public static final String PERSON_OPTOGRAPH_COUNT = "optographs_count";
    public static final String PERSON_FOLLOWER_COUNT = "followers_count";
    public static final String PERSON_FOLLOWED_COUNT = "followed_count";
    public static final String PERSON_IS_FOLLOWED = "is_followed";
    public static final String PERSON_FB_USER_ID = "facebook_user_id";
    public static final String PERSON_FB_TOKEN = "facebook_token";
    public static final String PERSON_TWITTER_TOKEN = "twitter_token";
    public static final String PERSON_TWITTER_SECRET_TEXT = "twitter_secret";

    public static final String LOCATION_ID = "id";
    public static final String LOCATION_CREATED_AT = "created_at";
    public static final String LOCATION_UPDATED_AT = "updated_at";
    public static final String LOCATION_DELETED_AT = "deleted_at";
    public static final String LOCATION_LATITUDE = "latitude";
    public static final String LOCATION_LONGITUDE = "longitude";
    public static final String LOCATION_TEXT = "text";
    public static final String LOCATION_COUNTRY = "country";
    public static final String LOCATION_COUNTRY_SHORT = "country_short";
    public static final String LOCATION_PLACE = "place";
    public static final String LOCATION_REGION = "region";
    public static final String LOCATION_POI = "poi";


    public static final String STORY_ID = "id";
    public static final String STORY_CREATED_AT = "created_at";
    public static final String STORY_UPDATED_AT = "updated_at";
    public static final String STORY_DELETED_AT = "deleted_at";
    public static final String STORY_OPTOGRAPH_ID = "optograph_id";
    public static final String STORY_PERSON_ID = "person_id";

    public static final String STORY_CHILDREN_OBJECT_ID = "story_object_id";
    public static final String STORY_CHILDREN_STORY_ID = "story_object_story_id";
    public static final String STORY_CHILDREN_MEDIA_TYPE = "story_object_media_type";
    public static final String STORY_CHILDREN_MEDIA_FACE = "story_object_media_face";
    public static final String STORY_CHILDREN_MEDIA_DESCRIPTION = "story_object_media_description";
    public static final String STORY_CHILDREN_MEDIA_ADDITIONAL_DATA = "story_object_media_additional_data";
    public static final String STORY_CHILDREN_POSITION = "story_object_position";
    public static final String STORY_CHILDREN_ROTATION = "story_object_rotation";
    public static final String STORY_CHILDREN_PHI = "story_object_phi";
    public static final String STORY_CHILDREN_THETA = "story_object_theta";
    public static final String STORY_CHILDREN_CREATED_AT = "story_object_created_at";
    public static final String STORY_CHILDREN_UPDATED_AT = "story_object_updated_at";
    public static final String STORY_CHILDREN_DELETED_AT = "story_object_deleted_at";
    public static final String STORY_CHILDREN_MEDIA_FILENAME = "story_object_media_filename";
    public static final String STORY_CHILDREN_MEDIA_FILEURL = "story_object_media_fileurl";


    String[] facesList = {FACES_LEFT_ZERO,FACES_LEFT_ONE,FACES_LEFT_TWO,FACES_LEFT_THREE,FACES_LEFT_FOUR,
            FACES_LEFT_FIVE,FACES_RIGHT_ZERO,FACES_RIGHT_ONE,FACES_RIGHT_TWO,FACES_RIGHT_THREE,
            FACES_RIGHT_FOUR,FACES_RIGHT_FIVE};

    private Cache cache;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        cache = Cache.open();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "create table " + OPTO_TABLE_NAME_FEEDS +
                        " (optograph_id text primary key not null, optograph_text text not null," +
                        "optograph_person_id text not null,optograph_location_id text not null," +
                        "optograph_created_at text not null,optograph_deleted_at text not null," +
                        "optograph_is_starred boolean not null, optograph_stars_count integer not null," +
                        "optograph_is_published boolean not null, optograph_is_private boolean not null," +
                        "optograph_is_stitcher_version text not null, optograph_is_in_feed boolean not null," +
                        "optograph_is_on_server boolean not null, optograph_updated_at text not null," +
                        "optograph_is_staff_pick boolean not null, optograph_is_data_uploaded boolean not null," +
                        "optograph_should_be_published boolean not null, optograph_is_place_holder_uploaded boolean not null," +
                        "optograph_is_local boolean not null, " +
                        "post_facebook boolean not null, post_twitter boolean not null, post_instagram boolean not null," +
                        "optograph_share_alias text not null, optograph_type text not null, optograph_story_id text)"

        );

        db.execSQL(
                "create table " +LOCATION_TABLE_NAME+
                        " (id text primary key not null, created_at text not null," +
                        "updated_at text,deleted_at text," +
                        "latitude text not null, longitude text not null," +
                        "text text,"+
                        "country text not null, country_short text not null," +
                        "place text not null, region text not null," +
                        "poi text not null )"

        );

        db.execSQL(
                "create table " + PERSON_TABLE_NAME +
                        " (id text primary key not null, created_at text not null," +
                        "deleted_at text, display_name text not null, user_name text not null," +
                        "email text not null, text text not null," +
                        "elite_status boolean not null, avatar_asset_id text," +
                        "optographs_count integer not null, followers_count integer not null," +
                        "followed_count integer not null, is_followed boolean not null," +
                        "facebook_user_id text not null, facebook_token text not null," +
                        "twitter_token text not null, twitter_secret text not null)"
        );

        db.execSQL("create table " + FACES_TABLE_NAME +
                " (faces_optograph_id text primaty key not null, faces_left_zero integer not null," +
                "faces_left_one integer not null, faces_left_two integer not null," +
                "faces_left_three integer not null, faces_left_four integer not null," +
                "faces_left_five integer not null, faces_right_zero integer not null," +
                "faces_right_one integer not null, faces_right_two integer not null," +
                "faces_right_three integer not null, faces_right_four integer not null," +
                "faces_right_five integer not null)");

        db.execSQL(
                "create table " +STORY_TABLE_NAME+
                        " (id text primary key not null,"+
                        "created_at text not null," +
                        "updated_at text,deleted_at text," +
                        "optograph_id text not null,"+
                        "person_id text not null," +
                        "story_object_id text )"
        );

        db.execSQL(
                "create table " +STORY_CHILDREN_TABLE_NAME+
                        "(story_object_id text primary key not null,"+
                        "story_object_story_id text not null,"+
                        "story_object_media_type text not null,"+
                        "story_object_media_face text not null,"+
                        "story_object_media_description text not null,"+
                        "story_object_media_additional_data text,"+
                        "story_object_position text,"+
                        "story_object_rotation text,"+
                        "story_object_phi text,"+
                        "story_object_theta text,"+
                        "story_object_created_at text not null," +
                        "story_object_updated_at text," +
                        "story_object_deleted_at text,"+
                        "story_object_media_filename text," +
                        "story_object_media_fileurl text )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + OPTO_TABLE_NAME_FEEDS);
        db.execSQL("DROP TABLE IF EXISTS " + PERSON_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FACES_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + STORY_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + STORY_CHILDREN_TABLE_NAME);
        onCreate(db);
    }

    // insert a new optograph data with initial values
    public boolean insertOptograph(String id, String text, String pId, String lId, String cAt, String dAt,
                                   boolean isStarred, int sCount, boolean isPub, boolean isPri, String stitchVer, boolean isFeed,
                                   boolean onServer, String uAt, boolean shouldPublished, boolean isLocal, boolean isPHUploaded, boolean postFB,
                                   boolean postTwit, boolean postInsta, boolean isDataUploaded, boolean isStaffPick, String shareAlias, String type, String storyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(OPTOGRAPH_ID, nullChecker(id));
        contentValues.put(OPTOGRAPH_TEXT, nullChecker(text));
        contentValues.put(OPTOGRAPH_PERSON_ID, nullChecker(pId));
        contentValues.put(OPTOGRAPH_LOCATION_ID, nullChecker(lId));
        contentValues.put(OPTOGRAPH_CREATED_AT, nullChecker(cAt));
        contentValues.put(OPTOGRAPH_DELETED_AT, nullChecker(dAt));
        contentValues.put(OPTOGRAPH_IS_STARRED, isStarred);
        contentValues.put(OPTOGRAPH_STARS_COUNT, sCount);
        contentValues.put(OPTOGRAPH_IS_PUBLISHED,isPub);
        contentValues.put(OPTOGRAPH_IS_PRIVATE, isPri);
        contentValues.put(OPTOGRAPH_IS_STITCHER_VERSION, nullChecker(stitchVer));
        contentValues.put(OPTOGRAPH_IS_IN_FEED, isFeed);
        contentValues.put(OPTOGRAPH_IS_ON_SERVER, onServer);
        contentValues.put(OPTOGRAPH_IS_STAFF_PICK, isStaffPick);
        contentValues.put(OPTOGRAPH_UPDATED_AT, nullChecker(uAt));
        contentValues.put(OPTOGRAPH_SHOULD_BE_PUBLISHED,shouldPublished);
        contentValues.put(OPTOGRAPH_IS_LOCAL, isLocal);
        contentValues.put(OPTOGRAPH_IS_DATA_UPLOADED,isDataUploaded);
        contentValues.put(OPTOGRAPH_IS_PLACEHOLDER_UPLOADED,isPHUploaded);
        contentValues.put(OPTOGRAPH_POST_FACEBOOK,postFB);
        contentValues.put(OPTOGRAPH_POST_TWITTER,postTwit);
        contentValues.put(OPTOGRAPH_POST_INSTAGRAM,postInsta);
        contentValues.put(OPTOGRAPH_TYPE, nullChecker(type));
        contentValues.put(OPTOGRAPH_SHARE_ALIAS, nullChecker(shareAlias));
        contentValues.put(OPTOGRAPH_STORY_ID, nullChecker(storyId));
        db.insert(OPTO_TABLE_NAME_FEEDS, null, contentValues);
        ContentValues contentValues1 = new ContentValues();
        contentValues1.put(FACES_ID,id);
        contentValues1.put(FACES_LEFT_ZERO,0);
        contentValues1.put(FACES_LEFT_ONE,0);
        contentValues1.put(FACES_LEFT_TWO,0);
        contentValues1.put(FACES_LEFT_THREE,0);
        contentValues1.put(FACES_LEFT_FOUR,0);
        contentValues1.put(FACES_LEFT_FIVE,0);
        contentValues1.put(FACES_RIGHT_ZERO,0);
        contentValues1.put(FACES_RIGHT_ONE,0);
        contentValues1.put(FACES_RIGHT_TWO,0);
        contentValues1.put(FACES_RIGHT_THREE,0);
        contentValues1.put(FACES_RIGHT_FOUR,0);
        contentValues1.put(FACES_RIGHT_FIVE,0);
        db.insert(FACES_TABLE_NAME,null,contentValues1);
        return true;
    }



    public boolean insertPerson(String id, String created_at, String email, String deleted_at, boolean elite_status,
                                String display_name, String user_name, String text, String avatar_asset_id, String facebook_user_id,
                                int optographs_count, int followers_count, int followed_count, boolean is_followed, String facebook_token, String twitter_token, String twitter_secret){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PERSON_ID, nullChecker(id));
        contentValues.put(PERSON_CREATED_AT, nullChecker(created_at));
        contentValues.put(PERSON_DELETED_AT, nullChecker(deleted_at));
        contentValues.put(PERSON_DISPLAY_NAME, nullChecker(display_name));
        contentValues.put(PERSON_USERNAME, nullChecker(user_name));
        contentValues.put(PERSON_EMAIL, nullChecker(email));
        contentValues.put(PERSON_TEXT, nullChecker(text));
        contentValues.put(PERSON_ELITE_STATUS, elite_status);
        contentValues.put(PERSON_AVATAR_ASSET_ID, nullChecker(avatar_asset_id));
        contentValues.put(PERSON_OPTOGRAPH_COUNT, optographs_count);
        contentValues.put(PERSON_FOLLOWER_COUNT, followers_count);
        contentValues.put(PERSON_FOLLOWED_COUNT, followed_count);
        contentValues.put(PERSON_IS_FOLLOWED, is_followed);
        contentValues.put(PERSON_FB_USER_ID, nullChecker(facebook_user_id));
        contentValues.put(PERSON_FB_TOKEN, nullChecker(facebook_token));
        contentValues.put(PERSON_TWITTER_TOKEN, nullChecker(twitter_token));
        contentValues.put(PERSON_TWITTER_SECRET_TEXT, nullChecker(twitter_secret));
        db.insert(PERSON_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean updatePerson(String id, String created_at, String email, String deleted_at, boolean elite_status,
                                String display_name, String user_name, String text, String avatar_asset_id, String facebook_user_id,
                                int optographs_count, int followers_count, int followed_count, boolean is_followed, String facebook_token, String twitter_token, String twitter_secret){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("created_at", created_at);
        contentValues.put("deleted_at", deleted_at);
        contentValues.put("display_name", display_name);
        contentValues.put("user_name", user_name);
        contentValues.put("email", email);
        contentValues.put("text", text);
        contentValues.put("elite_status", elite_status);
        contentValues.put("avatar_asset_id", avatar_asset_id);
        contentValues.put("optographs_count", optographs_count);
        contentValues.put("followers_count", followers_count);
        contentValues.put("followed_count", followed_count);
        contentValues.put("is_followed", is_followed);
        contentValues.put("facebook_user_id", facebook_user_id);
        contentValues.put("facebook_token", facebook_token);
        contentValues.put("twitter_token", twitter_token);
        contentValues.put("twitter_secret", twitter_secret);
//        db.insert(PERSON_TABLE_NAME, null, contentValues);
        db.update(PERSON_TABLE_NAME, contentValues, PERSON_ID + " = ? ", new String[]{id});
        Cursor res = getData(id,PERSON_TABLE_NAME,PERSON_ID);
        res.moveToFirst();
        Log.d("myTag"," follower: updatePersonCheck name: "+res.getString(res.getColumnIndex(PERSON_DISPLAY_NAME))
                +" isFollowed: "+res.getInt(res.getColumnIndex(PERSON_IS_FOLLOWED)));
        res.close();
        return true;
    }

    public boolean insertLocation(String id, String created_at, String updated_at, String deleted_at, double latitude,
                                double longitude, String country, String text, String country_short, String place,
                                  String region, boolean poi){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LOCATION_ID, nullChecker(id));
        contentValues.put(LOCATION_CREATED_AT, nullChecker(created_at));
        contentValues.put(LOCATION_UPDATED_AT, nullChecker(updated_at));
        contentValues.put(LOCATION_DELETED_AT, nullChecker(deleted_at));
        contentValues.put(LOCATION_LATITUDE, latitude);
        contentValues.put(LOCATION_LONGITUDE, longitude);
        contentValues.put(LOCATION_TEXT, nullChecker(text));
        contentValues.put(LOCATION_COUNTRY, nullChecker(country));
        contentValues.put(LOCATION_COUNTRY_SHORT, nullChecker(country_short));
        contentValues.put(LOCATION_PLACE, nullChecker(place));
        contentValues.put(LOCATION_REGION, nullChecker(region));
        contentValues.put(LOCATION_POI, poi);
        db.insert(LOCATION_TABLE_NAME, null, contentValues);
        return true;
    }

    private boolean updateLocation(String id, String created_at, String updated_at, String deleted_at, double latitude,
                                   double longitude, String country, String text, String country_short, String place,
                                   String region, boolean poi){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("created_at", created_at);
        contentValues.put("updated_at", updated_at);
        contentValues.put("deleted_at", deleted_at);
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);
        contentValues.put("text", text);
        contentValues.put("country", country);
        contentValues.put("country_short", country_short);
        contentValues.put("place", place);
        contentValues.put("region", region);
        contentValues.put("poi", poi);
        db.update(LOCATION_TABLE_NAME, contentValues, LOCATION_ID + " = ? ", new String[]{id});
        return true;
    }

    public boolean insertStory(String id, String created_at, String updated_at, String deleted_at, String optograph_id, String person_id, String story_object_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(STORY_ID, nullChecker(id));
        contentValues.put(STORY_CREATED_AT, nullChecker(created_at));
        contentValues.put(STORY_UPDATED_AT, nullChecker(updated_at));
        contentValues.put(STORY_DELETED_AT, nullChecker(deleted_at));
        contentValues.put(STORY_OPTOGRAPH_ID, nullChecker(optograph_id));
        contentValues.put(STORY_PERSON_ID, nullChecker(person_id));
        contentValues.put(STORY_CHILDREN_OBJECT_ID, nullChecker(story_object_id));
        db.insert(STORY_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertStoryChildren(String id, String story_id, String media_type, String media_face, String media_desc, String media_add_data, String pos, String rot,
                                       String phi, String theta,
                                       String created_at, String updated_at, String deleted_at, String media_fname, String media_fUrl){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(STORY_CHILDREN_OBJECT_ID, nullChecker(id));
        contentValues.put(STORY_CHILDREN_STORY_ID, nullChecker(story_id));
        contentValues.put(STORY_CHILDREN_MEDIA_TYPE, nullChecker(media_type));
        contentValues.put(STORY_CHILDREN_MEDIA_FACE, nullChecker(media_face));
        contentValues.put(STORY_CHILDREN_MEDIA_DESCRIPTION, nullChecker(media_desc));
        contentValues.put(STORY_CHILDREN_MEDIA_ADDITIONAL_DATA, nullChecker(media_add_data));
        contentValues.put(STORY_CHILDREN_POSITION, nullChecker(pos));
        contentValues.put(STORY_CHILDREN_ROTATION, nullChecker(rot));
        contentValues.put(STORY_CHILDREN_PHI, nullChecker(phi));
        contentValues.put(STORY_CHILDREN_THETA, nullChecker(theta));
        contentValues.put(STORY_CHILDREN_CREATED_AT, nullChecker(created_at));
        contentValues.put(STORY_CHILDREN_UPDATED_AT, nullChecker(updated_at));
        contentValues.put(STORY_CHILDREN_DELETED_AT, nullChecker(deleted_at));
        contentValues.put(STORY_CHILDREN_MEDIA_FILENAME, nullChecker(media_fname));
        contentValues.put(STORY_CHILDREN_MEDIA_FILEURL, nullChecker(media_fUrl));
        db.insert(STORY_CHILDREN_TABLE_NAME, null, contentValues);
        return true;
    }

    public Cursor getData(String id,String table,String column) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from " + table + " where " + column + "=\'" + id + "\'", null);
    }

    public Cursor getUserOptographs(String id, String table, int limit) {
        return getUserOptographs(id, table, limit, getNow());
    }

    public Cursor getUserOptographs(String id, String table, int limit, String older_than) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + table
                + " WHERE " + OPTOGRAPH_PERSON_ID + " = \'" + id
                + "\' AND " + OPTOGRAPH_CREATED_AT + " < \'" + older_than + "\' "
                + " AND " + OPTOGRAPH_DELETED_AT + " = \'\' "
                + " ORDER BY " + OPTOGRAPH_CREATED_AT + " DESC "
                + " LIMIT " + limit;
        return db.rawQuery(query, null);
    }

    public Cursor getUserStories(String id, String table, int limit) {
        return getUserStories(id, table, limit, getNow());
    }
    public Cursor getUserStories2(String id, String table, int limit) {
        return getUserStories2(id, table, limit, getNow());
    }

    public Cursor getUserStories2(String id, String table, int limit, String older_than) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + table
                + " WHERE " + OPTOGRAPH_ID + " = \'" + id
                + "\' AND " + STORY_CREATED_AT + " < \'" + older_than + "\' "
                + " AND " + STORY_DELETED_AT + " = \'\' "
                + " ORDER BY " + STORY_CREATED_AT + " DESC "
                + " LIMIT " + limit;
        Log.d("getUserStories2","query = "+query);
        return db.rawQuery(query, null);
    }

    public Cursor getUserStories(String id, String table, int limit, String older_than) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + table
                + " WHERE " + STORY_PERSON_ID + " = \'" + id
                + "\' AND " + STORY_CREATED_AT + " < \'" + older_than + "\' "
                + " AND " + STORY_DELETED_AT + " = \'\' "
                + " ORDER BY " + STORY_CREATED_AT + " DESC "
                + " LIMIT " + limit;
        Log.d("getUserStories","query = "+query);
        return db.rawQuery(query, null);
    }

    public Cursor getUserOptoList(String id, String table) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from "+table+" where "+OPTOGRAPH_PERSON_ID+"=\'"+id+"\' and "+OPTOGRAPH_DELETED_AT+" =\'\'", null );
    }

    public Cursor getAllFeedsData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from " + OPTO_TABLE_NAME_FEEDS + " where " + OPTOGRAPH_DELETED_AT + " = \'\'", null);
    }

    public Cursor getFeedsData(int limit) {
        return getFeedsData(limit, getNow());
    }

    public Cursor getFeedsData(int limit, String older_than) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + OPTO_TABLE_NAME_FEEDS
                + " WHERE " + OPTOGRAPH_CREATED_AT + " < \'" + older_than + "\' AND " + OPTOGRAPH_DELETED_AT + " = \'\'"
                + " AND NOT " + OPTOGRAPH_IS_LOCAL
                + " AND ( (" + OPTOGRAPH_PERSON_ID +  " IN ( SELECT ID FROM " + PERSON_TABLE_NAME + " WHERE IS_FOLLOWED OR " + PERSON_ID + " = \'" + cache.getString(Cache.USER_ID) + "\') )"
                + " OR " + OPTOGRAPH_IS_STAFF_PICK + " )"
                + " ORDER BY " + OPTOGRAPH_CREATED_AT + " DESC "
                + " LIMIT " + limit;
        Timber.d(query);
        return db.rawQuery(query, null);
    }

    public long getLocalOptoCount(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + OPTO_TABLE_NAME_FEEDS
                + " WHERE " + OPTOGRAPH_PERSON_ID + " = \'" + id + "\' AND " + OPTOGRAPH_DELETED_AT + " = \'\' "
                + " AND " + OPTOGRAPH_IS_LOCAL;

        Timber.d(query);
        SQLiteStatement s = db.compileStatement( query);
        return s.simpleQueryForLong();
    }

    public boolean updateFace(String id,String column,int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column, value);
        db.update(FACES_TABLE_NAME, contentValues, FACES_ID + "   = ? ", new String[]{id});
        return true;
    }

    public void updateColumnOptograph(String id, String column, boolean value) {
        updateTableColumn(OPTO_TABLE_NAME_FEEDS, OPTOGRAPH_ID, id, column, value);
    }

    public void updateColumnOptograph(String id, String column, int value) {
        updateTableColumn(OPTO_TABLE_NAME_FEEDS, OPTOGRAPH_ID, id, column, value);
    }

    public void updateColumnOptograph(String id, String column, String value) {
        updateTableColumn(OPTO_TABLE_NAME_FEEDS, OPTOGRAPH_ID, id, column, value);
    }

    public void updateColumnPerson(String id, String column, boolean value) {
        updateTableColumn(PERSON_TABLE_NAME, PERSON_ID, id, column, value);
    }

    public void updateColumnPerson(String id, String column, int value) {
        updateTableColumn(PERSON_TABLE_NAME, PERSON_ID, id, column, value);
    }

    public void updateColumnPerson(String id, String column, String value) {
        updateTableColumn(PERSON_TABLE_NAME, PERSON_ID, id, column, value);
    }

    public void updateColumnLocation(String id, String column, boolean value) {
        updateTableColumn(LOCATION_TABLE_NAME, LOCATION_ID, id, column, value);
    }

    public void updateColumnLocation(String id, String column, int value) {
        updateTableColumn(LOCATION_TABLE_NAME, LOCATION_ID, id, column, value);
    }

    public void updateColumnLocation(String id, String column, String value) {
        updateTableColumn(LOCATION_TABLE_NAME, LOCATION_ID, id, column, value);
    }

    public void updateColumnStory(String id, String column, String value) {
        updateTableColumn(STORY_TABLE_NAME, STORY_ID, id, column, value);
    }

    public boolean updateOptograph(String id,String text,String pId,String lId,String cAt,String dAt,
                                   boolean isStarred, int sCount, boolean isPub, boolean isPri,String stitchVer,boolean isFeed,
                                   boolean onServer,String uAt,String shouldPublished, boolean isLocal,
                                   boolean isStaffPick, String shareAlias, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        if(id != null) contentValues.put(OPTOGRAPH_ID, id);
        if(text != null) contentValues.put(OPTOGRAPH_TEXT, text);
        if(pId != null) contentValues.put(OPTOGRAPH_PERSON_ID, pId);
        if(lId != null) contentValues.put(OPTOGRAPH_LOCATION_ID, lId);
        if(cAt != null) contentValues.put(OPTOGRAPH_CREATED_AT,cAt);
        if(dAt != null) contentValues.put(OPTOGRAPH_DELETED_AT,dAt);
        if(stitchVer != null) contentValues.put(OPTOGRAPH_IS_STITCHER_VERSION, stitchVer);
        if(uAt != null) contentValues.put(OPTOGRAPH_UPDATED_AT, uAt);
        if(shareAlias != null) contentValues.put(OPTOGRAPH_SHARE_ALIAS, shareAlias);
        if(type != null) contentValues.put(OPTOGRAPH_TYPE,type);
        contentValues.put(OPTOGRAPH_IS_STARRED, isStarred);
        contentValues.put(OPTOGRAPH_STARS_COUNT, sCount);
        contentValues.put(OPTOGRAPH_IS_PUBLISHED, isPub);
        contentValues.put(OPTOGRAPH_IS_PRIVATE, isPri);
        contentValues.put(OPTOGRAPH_IS_IN_FEED, isFeed);
        contentValues.put(OPTOGRAPH_IS_ON_SERVER, onServer);
        contentValues.put(OPTOGRAPH_SHOULD_BE_PUBLISHED, shouldPublished);
        contentValues.put(OPTOGRAPH_IS_LOCAL, isLocal);
        contentValues.put(OPTOGRAPH_IS_STAFF_PICK, isStaffPick);

        db.update(OPTO_TABLE_NAME_FEEDS, contentValues, OPTOGRAPH_ID+" = ? ", new String[] { id } );
        return true;
    }

    // if optograph is existing -- it updates the data(opto data, person and location)
    // else insert optograph(opto data, person, location)
    public boolean updateOptograph(Optograph optograph) {
        boolean isExist = false;
        Cursor res = getData(optograph.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
        res.moveToFirst();
        if (res.getCount()>0) isExist = true;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        if(optograph.getId() != null) contentValues.put(OPTOGRAPH_ID, optograph.getId());
        if(optograph.getText() != null) contentValues.put(OPTOGRAPH_TEXT, optograph.getText());
        if(optograph.getPerson() != null) contentValues.put(OPTOGRAPH_PERSON_ID, optograph.getPerson().getId());
        if(optograph.getLocation() != null) contentValues.put(OPTOGRAPH_LOCATION_ID, optograph.getLocation().getId());
        if(optograph.getCreated_at() != null) contentValues.put(OPTOGRAPH_CREATED_AT,optograph.getCreated_at());
        if(optograph.getDeleted_at() != null) contentValues.put(OPTOGRAPH_DELETED_AT,optograph.getDeleted_at());
        if(optograph.getStitcher_version() != null) contentValues.put(OPTOGRAPH_IS_STITCHER_VERSION, optograph.getStitcher_version());
//        if(uAt != null) contentValues.put(OPTOGRAPH_UPDATED_AT, uAt);
        if(optograph.getShare_alias() != null) contentValues.put(OPTOGRAPH_SHARE_ALIAS, optograph.getShare_alias());
        if(optograph.getOptograph_type() != null) contentValues.put(OPTOGRAPH_TYPE,optograph.getOptograph_type());
        contentValues.put(OPTOGRAPH_IS_STARRED, optograph.is_starred());
        contentValues.put(OPTOGRAPH_STARS_COUNT, optograph.getStars_count());
        contentValues.put(OPTOGRAPH_IS_PUBLISHED, optograph.is_published());
        contentValues.put(OPTOGRAPH_IS_PRIVATE, optograph.is_private());
//        contentValues.put(OPTOGRAPH_IS_IN_FEED, isFeed);
        contentValues.put(OPTOGRAPH_IS_ON_SERVER, optograph.is_on_server());
        contentValues.put(OPTOGRAPH_SHOULD_BE_PUBLISHED, optograph.isShould_be_published());
        contentValues.put(OPTOGRAPH_IS_LOCAL, optograph.is_local());
        contentValues.put(OPTOGRAPH_IS_STAFF_PICK, optograph.is_staff_picked());
        contentValues.put(OPTOGRAPH_IS_DATA_UPLOADED, optograph.is_data_uploaded());

        if (isExist) db.update(OPTO_TABLE_NAME_FEEDS, contentValues, OPTOGRAPH_ID+" = ? ", new String[] { optograph.getId() } );
        else db.insert(OPTO_TABLE_NAME_FEEDS, null, contentValues);

        if (optograph.getPerson()!=null) {
            Person person = optograph.getPerson();
            res = getData(person.getId(), DBHelper.PERSON_TABLE_NAME, DBHelper.PERSON_ID);
            if (res.getCount()>0) {
                updatePerson(person.getId(),person.getCreated_at(),person.getEmail(),person.getDeleted_at(),
                        person.isElite_status(),person.getDisplay_name(),person.getUser_name(),person.getText()
                        ,person.getAvatar_asset_id(),person.getFacebook_user_id(),person.getOptographs_count(),
                        person.getFollowers_count(),person.getFollowed_count(),person.is_followed(),
                        person.getFacebook_token(),person.getTwitter_token(),person.getTwitter_secret());
            } else {
                insertPerson(person.getId(),person.getCreated_at(),person.getEmail(),person.getDeleted_at(),
                        person.isElite_status(),person.getDisplay_name(),person.getUser_name(),person.getText()
                        ,person.getAvatar_asset_id(),person.getFacebook_user_id(),person.getOptographs_count(),
                        person.getFollowers_count(),person.getFollowed_count(),person.is_followed(),
                        person.getFacebook_token(),person.getTwitter_token(),person.getTwitter_secret());
            }
        }

        if (optograph.getLocation()==null) {
            res.close();
            return true;
        }
        Location location = optograph.getLocation();
        res = getData(location.getId(), DBHelper.LOCATION_TABLE_NAME, DBHelper.LOCATION_ID);
        if (res.getCount()>0) {
            updateLocation(location.getId(),location.getCreated_at(),location.getUpdated_at(),
                    location.getDeleted_at(),location.getLatitude(),location.getLongitude(),
                    location.getCountry(),location.getText(),location.getCountry_short(),
                    location.getPlace(),location.getRegion(),location.isPoi());
        } else {
            insertLocation(location.getId(),location.getCreated_at(),location.getUpdated_at(),
                    location.getDeleted_at(),location.getLatitude(),location.getLongitude(),
                    location.getCountry(),location.getText(),location.getCountry_short(),
                    location.getPlace(),location.getRegion(),location.isPoi());
        }
        res.close();
        return true;
    }

    public int deleteEntry(String tableName,String columnNameOfId,String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(tableName,
                columnNameOfId+" = ? ",
                new String[] { id });
    }

    public boolean inInLocalDB(String id) {
        Timber.d("inInLocalDB : " + id);
        Cursor res = getData(id, DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
            if (res == null || res.getCount() <= 0) {
                res.close();
                Timber.d("inInLocalDB : " + false);
                return false;
            }
        res.close();
        Timber.d("inInLocalDB : " + true);

        return true;
    }

    public boolean checkIfAllImagesUploaded(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "select * from "+FACES_TABLE_NAME+" where "+FACES_ID+"=\'"+id+"\'", null );
        res.moveToFirst();
        Log.d("myTag","res: "+res+" getCount: "+res.getCount()+" getColumnCount: "+res.getColumnCount()+" getPosition: "+res.getPosition());
        if (res == null || res.getCount() <= 0) {
            res.close();
            return false;
        }
        for (String i: facesList) {
            if (res.getInt(res.getColumnIndex(i))==0) {
                res.close();
                return false;
            }
        }
        res.close();
        return true;
    }

    public boolean checkIfShouldBePublished(String id) {
        Cursor res = getData(id, DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
        res.moveToFirst();

        boolean result = (res.getCount()==0 || (res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) == 1
                || !res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_DELETED_AT)).equals("")));
        res.close();
        return result;
    }

    public void deleteAllTable() {
        deleteTable(OPTO_TABLE_NAME_FEEDS);
        deleteTable(PERSON_TABLE_NAME);
        deleteTable(LOCATION_TABLE_NAME);
        deleteTable(FACES_TABLE_NAME);
    }

    public void deleteTable(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + tableName);
    }

    public boolean updateTableColumn(String tableName,String primaryColumn, String primaryColumnValue,String column, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column,value);
        db.update(tableName, contentValues, primaryColumn + " =  ? ", new String[]{String.valueOf(primaryColumnValue)});
        return  true;
    }

    public boolean updateTableColumn(String tableName, String primaryColumn, String primaryColumnValue, String column, int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column,value);
        db.update(tableName, contentValues, primaryColumn + " =  ? ", new String[]{String.valueOf(primaryColumnValue)});
        return  true;
    }

    public boolean updateTableColumn(String tableName, String primaryColumn, String primaryColumnValue, String column, boolean value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column,value);
        db.update(tableName, contentValues, primaryColumn + " =  ? ", new String[]{String.valueOf(primaryColumnValue)});
        return  true;
    }

    public boolean updateTableColumn(String tableName, String primaryColumn, String primaryColumnValue, String column, double value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column,value);
        db.update(tableName, contentValues, primaryColumn + " =  ? ", new String[]{String.valueOf(primaryColumnValue)});
        return  true;
    }

    private String getNow() {
        return RFC3339DateFormatter.toRFC3339String(DateTime.now());
    }

    public Optograph checkOptoDetails(Optograph optograph) {

        Cursor res = getData(optograph.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
        res.moveToFirst();
        if (res.getCount()==0) {
//            deleteOptographFromPhone(optograph.getId());
            return null;
        }
        Log.d("myTag"," setMessage: checkToDb shouldPub? "+(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) == 1)+
                " delAt: "+res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_DELETED_AT))+" onserver? "+(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_ON_SERVER)) != 0));
        if (res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) == 1 || !res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_DELETED_AT)).equals("")) {
//            deleteOptographFromPhone(optograph.getId());
            return null;
        }

        if (checkIfAllImagesUploaded(optograph.getId())) return null;
        optograph.setStitcher_version(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_STITCHER_VERSION)));
        optograph.setText(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_TEXT)));
        optograph.setOptograph_type(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_TYPE)));
//        optograph.setCreated_at(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_CREATED_AT)));
        optograph.setIs_on_server(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_ON_SERVER)) != 0);
        optograph.setShould_be_published(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) != 0);
        optograph.setIs_place_holder_uploaded(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_PLACEHOLDER_UPLOADED)) != 0);
        optograph.setIs_data_uploaded(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_DATA_UPLOADED)) != 0);
        Log.d("myTag", " Profile upload: checkToDB id: " + optograph.getId() + " isDataUploaded? " + res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_DATA_UPLOADED)));
        optograph.setPostFacebook(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_FACEBOOK)) != 0);
        optograph.setPostTwitter(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_TWITTER)) != 0);
        optograph.setPostInstagram(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_INSTAGRAM)) != 0);
        Cursor face = getData(optograph.getId(), DBHelper.FACES_TABLE_NAME, DBHelper.FACES_ID);
        face.moveToFirst();
        if (face.getCount()==0) return optograph;

        if(optograph.getLeftFace() != null) {
            optograph.getLeftFace().setStatusByIndex(0, face.getInt(face.getColumnIndex(DBHelper.FACES_LEFT_ZERO)) != 0);
            optograph.getLeftFace().setStatusByIndex(1, face.getInt(face.getColumnIndex(DBHelper.FACES_LEFT_ONE)) != 0);
            optograph.getLeftFace().setStatusByIndex(2, face.getInt(face.getColumnIndex(DBHelper.FACES_LEFT_TWO)) != 0);
            optograph.getLeftFace().setStatusByIndex(3, face.getInt(face.getColumnIndex(DBHelper.FACES_LEFT_THREE)) != 0);
            optograph.getLeftFace().setStatusByIndex(4, face.getInt(face.getColumnIndex(DBHelper.FACES_LEFT_FOUR)) != 0);
            optograph.getLeftFace().setStatusByIndex(5, face.getInt(face.getColumnIndex(DBHelper.FACES_LEFT_FIVE)) != 0);
        }

        if(optograph.getRightFace() != null) {
            optograph.getRightFace().setStatusByIndex(0,face.getInt(face.getColumnIndex(DBHelper.FACES_RIGHT_ZERO))!=0);
            optograph.getRightFace().setStatusByIndex(1,face.getInt(face.getColumnIndex(DBHelper.FACES_RIGHT_ONE))!=0);
            optograph.getRightFace().setStatusByIndex(2,face.getInt(face.getColumnIndex(DBHelper.FACES_RIGHT_TWO))!=0);
            optograph.getRightFace().setStatusByIndex(3,face.getInt(face.getColumnIndex(DBHelper.FACES_RIGHT_THREE))!=0);
            optograph.getRightFace().setStatusByIndex(4,face.getInt(face.getColumnIndex(DBHelper.FACES_RIGHT_FOUR))!=0);
            optograph.getRightFace().setStatusByIndex(5,face.getInt(face.getColumnIndex(DBHelper.FACES_RIGHT_FIVE))!=0);
        }
        face.close();
        String locId = res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_LOCATION_ID));
        Log.d("myTag"," checkDetails location: locId: "+locId);
        if (locId != null && !locId.equals("")) {
            Cursor loc = getData(locId, DBHelper.LOCATION_TABLE_NAME, DBHelper.LOCATION_ID);
            loc.moveToFirst();
            if (loc.getCount()==0) {
                res.close();
                loc.close();
                return optograph;
            }
            Log.d("myTag"," checkDetails location: location Id is existing in database.");
            Location location = new Location();
            location.setId(loc.getString(loc.getColumnIndex(DBHelper.LOCATION_ID)));
            location.setText(loc.getString(loc.getColumnIndex(DBHelper.LOCATION_TEXT)));
            location.setPlace(loc.getString(loc.getColumnIndex(DBHelper.LOCATION_PLACE)));
            location.setRegion(loc.getString(loc.getColumnIndex(DBHelper.LOCATION_REGION)));
            location.setPoi(loc.getInt(loc.getColumnIndex(DBHelper.LOCATION_POI))!=0);
            location.setCountry(loc.getString(loc.getColumnIndex(DBHelper.LOCATION_COUNTRY)));
            location.setCountry_short(loc.getString(loc.getColumnIndex(DBHelper.LOCATION_COUNTRY_SHORT)));
            location.setLatitude(loc.getDouble(loc.getColumnIndex(DBHelper.LOCATION_LATITUDE)));
            location.setLongitude(loc.getDouble(loc.getColumnIndex(DBHelper.LOCATION_LONGITUDE)));
            location.setCreated_at(loc.getString(loc.getColumnIndex(DBHelper.LOCATION_CREATED_AT)));
            location.setDeleted_at(loc.getString(loc.getColumnIndex(DBHelper.LOCATION_DELETED_AT)));
            location.setUpdated_at(loc.getString(loc.getColumnIndex(DBHelper.LOCATION_UPDATED_AT)));
            optograph.setLocation(location);
            loc.close();
        }
        res.close();
        return optograph;
    }

    public Optograph getOptoDataFromLocalDB(Cursor cursor){
        cursor.moveToFirst();

        Optograph opto = new Optograph(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_ID)));
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
        opto.setIs_data_uploaded(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_DATA_UPLOADED)) == 1 ? true : false);
        String locId = cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_LOCATION_ID));
        String personId = cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_PERSON_ID));


        Person person = new Person();
        if(personId !=null && !personId.equals("")){
            Cursor res = getData(personId, DBHelper.PERSON_TABLE_NAME,"id");
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

        Location location = new Location();
        if(opto != null && locId !=null && !locId.equals("")){
            Cursor res = getData(locId, DBHelper.LOCATION_TABLE_NAME,"id");
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
                location.setLatitude(res.getDouble(res.getColumnIndex("latitude")));
                location.setLongitude(res.getDouble(res.getColumnIndex("longitude")));
            }
        }

        opto.setPerson(person);
        opto.setLocation(location);

        cursor.close();

        return opto;
    }

    public static String nullChecker(String data){
        if(data == null || data.trim().equals("")){
            data = "";
        }
        return data;
    }
}