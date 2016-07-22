package com.iam360.iam360.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Mariel on 4/18/2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "OptoData.db";
    public static final String OPTO_TABLE_NAME = "Optograph";
    public static final String OPTO_TABLE_NAME_FEEDS = "Optograph_Feeds";
    public static final String PERSON_TABLE_NAME = "Person";
    public static final String LOCATION_TABLE_NAME = "Location";
    public static final String OPTOGRAPH_ID = "optograph_id";
    public static final String OPTOGRAPH_TEXT = "optograph_text";
    public static final String OPTOGRAPH_PERSON_ID = "optograph_person_id";
    public static final String OPTOGRAPH_LOCATION_ID = "optograph_location_id";
    public static final String OPTOGRAPH_CREATED_AT = "optograph_created_at";
    public static final String OPTOGRAPH_DELETED_AT = "optograph_deleted_at";
    public static final String OPTOGRAPH_IS_STARRED = "optograph_is_starred";
    public static final String OPTOGRAPH_STARS_COUNT = "optograph_stars_count";
    public static final String OPTOGRAPH_IS_PUBLISHED = "optograph_is_published";
    public static final String OPTOGRAPH_IS_PRIVATE = "optograph_is_private";
    public static final String OPTOGRAPH_IS_STITCHER_VERSION = "optograph_is_stitcher_version";
    public static final String OPTOGRAPH_IS_IN_FEED = "optograph_is_in_feed";
    public static final String OPTOGRAPH_IS_ON_SERVER = "optograph_is_on_server";
    public static final String OPTOGRAPH_UPDATED_AT = "optograph_updated_at";
    public static final String OPTOGRAPH_SHOULD_BE_PUBLISHED = "optograph_should_be_published";
    public static final String OPTOGRAPH_IS_DATA_UPLOADED = "optograph_is_data_uploaded";
    public static final String OPTOGRAPH_IS_PLACEHOLDER_UPLOADED = "optograph_is_place_holder_uploaded";
    public static final String OPTOGRAPH_POST_FACEBOOK = "post_facebook";
    public static final String OPTOGRAPH_POST_TWITTER = "post_twitter";
    public static final String OPTOGRAPH_POST_INSTAGRAM = "post_instagram";
    public static final String OPTOGRAPH_TYPE = "optograph_type";
    public static final String FACES_TABLE_NAME = "OptoCubeFaces";
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

    String[] facesList = {FACES_LEFT_ZERO,FACES_LEFT_ONE,FACES_LEFT_TWO,FACES_LEFT_THREE,FACES_LEFT_FOUR,
            FACES_LEFT_FIVE,FACES_RIGHT_ZERO,FACES_RIGHT_ONE,FACES_RIGHT_TWO,FACES_RIGHT_THREE,
            FACES_RIGHT_FOUR,FACES_RIGHT_FIVE};

    public DBHelper(Context context) {
        super(context,DATABASE_NAME,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table " +OPTO_TABLE_NAME+
                        " (optograph_id text primary key not null, optograph_text text not null," +
                        "optograph_person_id text not null,optograph_location_id text not null," +
                        "optograph_created_at text not null,optograph_deleted_at text not null," +
                        "optograph_is_starred integer not null, optograph_stars_count integer not null," +
                        "optograph_is_published integer not null, optograph_is_private integer not null," +
                        "optograph_is_stitcher_version text not null, optograph_is_in_feed integer not null," +
                        "optograph_is_on_server integer not null, optograph_updated_at text not null," +
                        "optograph_is_data_uploaded integer not null,"+
                        "optograph_should_be_published integer not null, optograph_is_place_holder_uploaded integer not null," +
                        "post_facebook integer not null, post_twitter integer not null, post_instagram integer not null," +
                        "optograph_type text not null )"

        );

        db.execSQL(
                "create table " +OPTO_TABLE_NAME_FEEDS+
                        " (optograph_id text primary key not null, optograph_text text not null," +
                        "optograph_person_id text not null,optograph_location_id text not null," +
                        "optograph_created_at text not null,optograph_deleted_at text not null," +
                        "optograph_is_starred text not null, optograph_stars_count integer not null," +
                        "optograph_is_published text not null, optograph_is_private text not null," +
                        "optograph_is_stitcher_version text not null, optograph_is_in_feed text not null," +
                        "optograph_is_on_server text not null, optograph_updated_at text not null," +
                        "optograph_is_data_uploaded text not null,"+
                        "optograph_should_be_published text not null, optograph_is_place_holder_uploaded text not null," +
                        "post_facebook text not null, post_twitter text not null, post_instagram text not null," +
                        "optograph_type text not null )"

        );

        db.execSQL(
                "create table " +LOCATION_TABLE_NAME+
                        " (id text primary key not null, created_at text not null," +
                        "updated_at text not null,deleted_at text not null," +
                        "latitude text not null, longitude text not null," +
                        "text text not null,"+
                        "country text not null, country_short text not null," +
                        "place text not null, region text not null," +
                        "poi text not null )"

        );

        db.execSQL(
                "create table " +PERSON_TABLE_NAME+
                        " (id text primary key not null, created_at text not null," +
                        "deleted_at text, display_name text not null, user_name text not null," +
                        "email text not null, text text not null,"+
                        "elite_status text not null, avatar_asset_id text not null," +
                        "optographs_count integer not null, followers_count integer not null," +
                        "followed_count integer not null, is_followed text not null,"+
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS "+OPTO_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+OPTO_TABLE_NAME_FEEDS);
        db.execSQL("DROP TABLE IF EXISTS "+FACES_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertOptograph(String id,String text,String pId,String lId,String cAt,String dAt,
                                   int isStarred,int sCount,int isPub,int isPri,String stitchVer,int isFeed,
                                   int onServer,String uAt,int shouldPublished,int isPHUploaded,int postFB,
                                   int postTwit,int postInsta, int isDataUploaded, String type, String tableName) {
        if(tableName == null || tableName.equals("")){
            tableName = OPTO_TABLE_NAME;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(OPTOGRAPH_ID, id);
        contentValues.put(OPTOGRAPH_TEXT, text);
        contentValues.put(OPTOGRAPH_PERSON_ID, pId);
        contentValues.put(OPTOGRAPH_LOCATION_ID, lId);
        contentValues.put(OPTOGRAPH_CREATED_AT,cAt);
        contentValues.put(OPTOGRAPH_DELETED_AT,dAt);
        contentValues.put(OPTOGRAPH_IS_STARRED, isStarred);
        contentValues.put(OPTOGRAPH_STARS_COUNT, sCount);
        contentValues.put(OPTOGRAPH_IS_PUBLISHED, isPub);
        contentValues.put(OPTOGRAPH_IS_PRIVATE, isPri);
        contentValues.put(OPTOGRAPH_IS_STITCHER_VERSION, stitchVer);
        contentValues.put(OPTOGRAPH_IS_IN_FEED, isFeed);
        contentValues.put(OPTOGRAPH_IS_ON_SERVER, onServer);
        contentValues.put(OPTOGRAPH_UPDATED_AT, uAt);
        contentValues.put(OPTOGRAPH_SHOULD_BE_PUBLISHED,shouldPublished);
        contentValues.put(OPTOGRAPH_IS_DATA_UPLOADED,isDataUploaded);
        contentValues.put(OPTOGRAPH_IS_PLACEHOLDER_UPLOADED,isPHUploaded);
        contentValues.put(OPTOGRAPH_POST_FACEBOOK,postFB);
        contentValues.put(OPTOGRAPH_POST_TWITTER,postTwit);
        contentValues.put(OPTOGRAPH_POST_INSTAGRAM,postInsta);
        contentValues.put(OPTOGRAPH_TYPE,type);
        db.insert(tableName, null, contentValues);
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
        db.insert(PERSON_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertLocation(String id, String created_at, String updated_at, String deleted_at, String latitude,
                                String longitude, String country, String text, String country_short, String place,
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
        db.insert(LOCATION_TABLE_NAME, null, contentValues);
        return true;
    }

    public Cursor getData(String id,String table,String column) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from "+table+" where "+column+"=\'"+id+"\'", null );
    }

    public Cursor getAllFeedsData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from "+OPTO_TABLE_NAME_FEEDS, null );
    }

    public boolean updateFace(String id,String column,int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column, value);
        db.update(FACES_TABLE_NAME, contentValues, FACES_ID+" = ? ", new String[] { id } );
        return true;
    }

    public boolean updateColumnOptograph(String id,String column, int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column,value);
        db.update(OPTO_TABLE_NAME,contentValues,OPTOGRAPH_ID+" = ? ",new String[] {String.valueOf(id)});
        return  true;
    }

    public boolean updateColumnOptograph(String id,String column, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column,value);
        db.update(OPTO_TABLE_NAME,contentValues,OPTOGRAPH_ID+" = ? ",new String[] {id});
        return  true;
    }

    public boolean updateOptograph(String id,String text,String pId,String lId,String cAt,String dAt,
                                   int isStarred,int sCount,int isPub,int isPri,String stitchVer,int isFeed,
                                   int onServer,String uAt,String shouldPublished,String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(OPTOGRAPH_ID, id);
        contentValues.put(OPTOGRAPH_TEXT, text);
        contentValues.put(OPTOGRAPH_PERSON_ID, pId);
        contentValues.put(OPTOGRAPH_LOCATION_ID, lId);
        contentValues.put(OPTOGRAPH_CREATED_AT,cAt);
        contentValues.put(OPTOGRAPH_DELETED_AT,dAt);
        contentValues.put(OPTOGRAPH_IS_STARRED, isStarred);
        contentValues.put(OPTOGRAPH_STARS_COUNT, sCount);
        contentValues.put(OPTOGRAPH_IS_PUBLISHED, isPub);
        contentValues.put(OPTOGRAPH_IS_PRIVATE, isPri);
        contentValues.put(OPTOGRAPH_IS_STITCHER_VERSION, stitchVer);
        contentValues.put(OPTOGRAPH_IS_IN_FEED, isFeed);
        contentValues.put(OPTOGRAPH_IS_ON_SERVER, onServer);
        contentValues.put(OPTOGRAPH_UPDATED_AT, uAt);
        contentValues.put(OPTOGRAPH_SHOULD_BE_PUBLISHED,shouldPublished);
        contentValues.put(OPTOGRAPH_TYPE,type);
        db.update(OPTO_TABLE_NAME, contentValues, OPTOGRAPH_ID+" = ? ", new String[] { id } );
        return true;
    }

    public int deleteEntry(String tableName,String columnNameOfId,String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(tableName,
                columnNameOfId+" = ? ",
                new String[] { id });
    }

    public boolean checkIfAllImagesUploaded(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "select * from "+FACES_TABLE_NAME+" where "+FACES_ID+"=\'"+id+"\'", null );
        res.moveToFirst();
        Log.d("myTag","res: "+res+" getCount: "+res.getCount()+" getColumnCount: "+res.getColumnCount()+" getPosition: "+res.getPosition());
        if (res == null || res.getCount() <= 0) {
            return false;
        }
        for (String i: facesList) {
            if (res.getInt(res.getColumnIndex(i))==0) {
                return false;
            }
        }
        return true;
    }

    public boolean checkIfShouldBePublished(String id) {
        Cursor res = getData(id, DBHelper.OPTO_TABLE_NAME, DBHelper.OPTOGRAPH_ID);
        res.moveToFirst();
        return (res.getCount()==0 || (res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) == 1
                || !res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_DELETED_AT)).equals("")));
    }

    public void deleteAllTable(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ tableName);
    }


    public boolean updateTableColumn(String tableName,String primaryColumn, String primaryColumnValue,String column, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column,value);
        Log.d("MARK","updateTableColumn tableName="+tableName);
        Log.d("MARK","updateTableColumn contentValues="+contentValues);
        Log.d("MARK","updateTableColumn primaryColumn="+primaryColumn);
        Log.d("MARK","updateTableColumn primaryColumnValue="+primaryColumnValue);
        db.update(tableName,contentValues,primaryColumn+" =  ? ",new String[] {String.valueOf(primaryColumnValue)});
        return  true;
    }
}
