package com.iam360.dscvr.util;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.iam360.dscvr.model.Location;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.Person;
import com.iam360.dscvr.model.Story;
import com.iam360.dscvr.model.StoryChild;
import com.iam360.dscvr.network.ApiConsumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import rx.Observable;

/**
 * Created by Joven on 10/28/2016.
 */
public class DBHelper2 {
    private final Context context;
    private DBHelper mydb;
    Cache cache;
    protected ApiConsumer apiConsumer;

    public DBHelper2(Context context){
         this.context = context;
         cache = Cache.open();
         String token = cache.getString(Cache.USER_TOKEN);
         apiConsumer = new ApiConsumer(token.equals("") ? null : token);
         mydb = new DBHelper(context);
    }

    public void saveToSQLite(Optograph opto) {
        if (opto.getId() == null) return;

        saveToSQLiteOpto(opto);

        String loc = opto.getLocation() == null ? "" : opto.getLocation().getId();
        String per = opto.getPerson() == null ? "" : opto.getPerson().getId();
        String stry = opto.getStory() == null ? "" : opto.getStory().getId();

        if (!DBHelper.nullChecker(per).equals("")) {
            saveToSQLitePer(opto.getPerson());
        }

        if (!DBHelper.nullChecker(loc).equals("")) {
            saveToSQLiteLoc(opto.getLocation());
        }

        if(!DBHelper.nullChecker(stry).equals("")){
            saveToSQLiteStry(opto.getStory());
        }
    }

    public void saveToSQLiteOpto(Optograph opto) {
        Cursor res = mydb.getData(opto.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
        res.moveToFirst();
        if (res.getCount() > 0) {
            Log.d("StoryFeedAdapter", "Updating opto " + opto.getId());
            String id = DBHelper.OPTOGRAPH_ID;
            String tb = DBHelper.OPTO_TABLE_NAME_FEEDS;
            if (opto.getText() != null && !opto.getText().equals("")) {
                mydb.updateTableColumn(tb, id, opto.getId(), "optograph_text", opto.getText());
            }
            if (opto.getCreated_at() != null && !opto.getCreated_at().equals("")) {
                mydb.updateTableColumn(tb, id, opto.getId(), "optograph_created_at", opto.getCreated_at());
            }
            if (opto.getDeleted_at() != null && !opto.getDeleted_at().equals("")) {
                mydb.updateTableColumn(tb, id, opto.getId(), "optograph_deleted_at", opto.getDeleted_at());
            }
            mydb.updateTableColumn(tb, id, opto.getId(), "optograph_is_starred", opto.is_starred());
            mydb.updateTableColumn(tb, id, opto.getId(), "optograph_stars_count", opto.getStars_count());
            mydb.updateTableColumn(tb, id, opto.getId(), "optograph_is_published", opto.is_published());
            mydb.updateTableColumn(tb, id, opto.getId(), "optograph_is_private", opto.is_private());

            if (opto.getStitcher_version() != null && !opto.getStitcher_version().equals("")) {
                mydb.updateTableColumn(tb, id, opto.getId(), "optograph_is_stitcher_version", opto.getStitcher_version());
            }
            if (opto.getStitcher_version() != null && !opto.getStitcher_version().equals("")) {
                mydb.updateTableColumn(tb, id, opto.getId(), "optograph_is_data_uploaded", opto.is_data_uploaded());
            }
            mydb.updateTableColumn(tb, id, opto.getId(), "optograph_should_be_published", opto.isShould_be_published());
            mydb.updateTableColumn(tb, id, opto.getId(), "optograph_is_place_holder_uploaded", opto.is_place_holder_uploaded());
            mydb.updateTableColumn(tb, id, opto.getId(), "post_facebook", opto.isPostFacebook());
            mydb.updateTableColumn(tb, id, opto.getId(), "post_twitter", opto.isPostTwitter());
            mydb.updateTableColumn(tb, id, opto.getId(), "post_instagram", opto.isPostInstagram());
            if (opto.getOptograph_type() != null && !opto.getOptograph_type().equals("")) {
                mydb.updateTableColumn(tb, id, opto.getId(), "optograph_type", opto.getOptograph_type());
            }
            if (opto.getLocation() != null && opto.getLocation().getId() != null && !opto.getLocation().getId().equals("")) {
                mydb.updateTableColumn(tb, id, opto.getId(), "optograph_location_id", opto.getLocation().getId());
            }
            res.close();
        } else {
            Log.d("StoryFeedAdapter", "Inserting opto " + opto.getId());
            String stryId = "";
            if(opto.getStory() != null && !DBHelper.nullChecker(opto.getStory().getId()).equals("")){
                stryId = DBHelper.nullChecker(opto.getStory().getId());
            }
            mydb.insertOptograph(opto.getId(), opto.getText(), opto.getPerson().getId(), opto.getLocation() == null ? "" : opto.getLocation().getId(),
                    opto.getCreated_at(), opto.getDeleted_at() == null ? "" : opto.getDeleted_at(), opto.is_starred(), opto.getStars_count(), opto.is_published(),
                    opto.is_private(), opto.getStitcher_version(), true, opto.is_on_server(), "", opto.isShould_be_published(), opto.is_local(),
                    opto.is_place_holder_uploaded(), opto.isPostFacebook(), opto.isPostTwitter(), opto.isPostInstagram(),
                    opto.is_data_uploaded(), opto.is_staff_picked(), opto.getShare_alias(), opto.getOptograph_type(),stryId);
            res.close();
        }
    }

    public void saveToSQLitePer(Person per) {
        Cursor res = mydb.getData(per.getId(), DBHelper.PERSON_TABLE_NAME, "id");
        res.moveToFirst();
        if (per.getId() != null) {
            Person person = per;
            if (res.getCount() > 0) {
                Log.d("StoryFeedAdapter", "Updating person " + person.getId());
                String id = "id";
                String tb = DBHelper.PERSON_TABLE_NAME;
                if (person.getCreated_at() != null && !person.getCreated_at().equals("")) {
                    mydb.updateTableColumn(tb, id, person.getId(), "created_at", person.getCreated_at());
                }
                if (person.getDeleted_at() != null && !person.getDeleted_at().equals("")) {
                    mydb.updateTableColumn(tb, id, person.getId(), "deleted_at", person.getDeleted_at());
                }
                if (person.getDisplay_name() != null && !person.getDisplay_name().equals("")) {
                    mydb.updateTableColumn(tb, id, person.getId(), "display_name", person.getDisplay_name());
                }
                if (person.getUser_name() != null && !person.getUser_name().equals("")) {
                    mydb.updateTableColumn(tb, id, person.getId(), "user_name", person.getUser_name());
                }
                if (person.getEmail() != null && !person.getEmail().equals("")) {
                    mydb.updateTableColumn(tb, id, person.getId(), "email", person.getEmail());
                }
                if (person.getText() != null && !person.getText().equals("")) {
                    mydb.updateTableColumn(tb, id, person.getId(), "text", person.getText());
                }
                if (person.getAvatar_asset_id() != null && !person.getAvatar_asset_id().equals("")) {
                    mydb.updateTableColumn(tb, id, person.getId(), "avatar_asset_id", person.getAvatar_asset_id());
                }
                mydb.updateTableColumn(tb, id, person.getId(), "optographs_count", String.valueOf(person.getOptographs_count()));
                mydb.updateTableColumn(tb, id, person.getId(), "followers_count", String.valueOf(person.getFollowers_count()));
                mydb.updateTableColumn(tb, id, person.getId(), "followed_count", String.valueOf(person.getFollowed_count()));
                mydb.updateTableColumn(tb, id, person.getId(), "is_followed", String.valueOf(person.is_followed()));
                if (person.getFacebook_user_id() != null && !person.getFacebook_user_id().equals("")) {
                    mydb.updateTableColumn(tb, id, person.getId(), "facebook_user_id", String.valueOf(person.getFacebook_user_id()));
                }
                if (person.getFacebook_token() != null && !person.getFacebook_token().equals("")) {
                    mydb.updateTableColumn(tb, id, person.getId(), "facebook_token", String.valueOf(person.getFacebook_token()));
                }
                if (person.getTwitter_token() != null && !person.getTwitter_token().equals("")) {
                    mydb.updateTableColumn(tb, id, person.getId(), "twitter_token", String.valueOf(person.getTwitter_token()));
                }
                if (person.getTwitter_secret() != null && !person.getTwitter_secret().equals("")) {
                    mydb.updateTableColumn(tb, id, person.getId(), "twitter_secret", String.valueOf(person.getTwitter_secret()));
                }
                res.close();
            } else {
                Log.d("StoryFeedAdapter", "Inserting person " + person.getId());
                mydb.insertPerson(person.getId(), person.getCreated_at(), person.getEmail(), person.getDeleted_at(), person.isElite_status(),
                        person.getDisplay_name(), person.getUser_name(), person.getText(), person.getAvatar_asset_id(), person.getFacebook_user_id(), person.getOptographs_count(),
                        person.getFollowers_count(), person.getFollowed_count(), person.is_followed(), person.getFacebook_token(), person.getTwitter_token(), person.getTwitter_secret());
                res.close();
            }
        }
    }

    public void saveToSQLiteLoc(Location loc) {
        Cursor res = mydb.getData(loc.getId(), DBHelper.LOCATION_TABLE_NAME, "id");
        res.moveToFirst();
        if (loc.getId() != null) {
            Location locs = loc;
            if (res.getCount() > 0) {
                Log.d("StoryFeedAdapter", "Updating loc " + locs.getId());
                String id = "id";
                String tb = DBHelper.LOCATION_TABLE_NAME;
                if (locs.getCreated_at() != null && !locs.getCreated_at().equals("")) {
                    mydb.updateTableColumn(tb, id, locs.getId(), "created_at", locs.getCreated_at());
                }
                if (locs.getUpdated_at() != null && !locs.getUpdated_at().equals("")) {
                    mydb.updateTableColumn(tb, id, locs.getId(), "updated_at", locs.getUpdated_at());
                }
                if (locs.getDeleted_at() != null && !locs.getDeleted_at().equals("")) {
                    mydb.updateTableColumn(tb, id, locs.getId(), "deleted_at", locs.getDeleted_at());
                }
                if (locs.getLatitude() != 0) {
                    mydb.updateTableColumn(tb, id, locs.getId(), "latitude", locs.getLatitude());
                }
                if (locs.getLongitude() != 0) {
                    mydb.updateTableColumn(tb, id, locs.getId(), "longitude", locs.getLongitude());
                }
                if (locs.getText() != null && !locs.getText().equals("")) {
                    mydb.updateTableColumn(tb, id, locs.getId(), "text", locs.getText());
                }
                if (locs.getCountry() != null && !locs.getCountry().equals("")) {
                    mydb.updateTableColumn(tb, id, locs.getId(), "country", locs.getCountry());
                }
                if (locs.getCountry_short() != null && !locs.getCountry_short().equals("")) {
                    mydb.updateTableColumn(tb, id, locs.getId(), "country_short", locs.getCountry_short());
                }
                if (locs.getPlace() != null && !locs.getPlace().equals("")) {
                    mydb.updateTableColumn(tb, id, locs.getId(), "place", locs.getPlace());
                }
                if (locs.getRegion() != null && !locs.getRegion().equals("")) {
                    mydb.updateTableColumn(tb, id, locs.getId(), "region", locs.getRegion());
                }
                mydb.updateTableColumn(tb, id, locs.getId(), "poi", String.valueOf(locs.isPoi()));
                res.close();
            } else {
                Log.d("StoryFeedAdapter", "Inserting loc " + locs.getId());
//                    mydb.updateColumnOptograph(opto.getId(),DBHelper.LOCATION_ID,locs.getId());
                mydb.insertLocation(locs.getId(), locs.getCreated_at(), locs.getUpdated_at(), locs.getDeleted_at(),
                        locs.getLatitude(), locs.getLongitude(), locs.getCountry(), locs.getText(),
                        locs.getCountry_short(), locs.getPlace(), locs.getRegion(), locs.isPoi());
                res.close();
            }
        }
    }

    public void saveToSQLiteStry(Story stry) {
        Cursor res = mydb.getData(stry.getId(), DBHelper.STORY_TABLE_NAME, "id");
        res.moveToFirst();
        if (stry.getId() != null) {
            Story story = stry;
            if (res.getCount() > 0) {
                Log.d("StoryFeedAdapter", "Updating story " + story.getId());
                String id = "id";
                String tb = DBHelper.STORY_TABLE_NAME;
                if (story.getCreated_at() != null && !story.getCreated_at().equals("")) {
                    mydb.updateTableColumn(tb, id, story.getId(), "created_at", story.getCreated_at());
                }
                if (story.getUpdated_at() != null && !story.getUpdated_at().equals("")) {
                    mydb.updateTableColumn(tb, id, story.getId(), "updated_at", story.getUpdated_at());
                }
                if (story.getDeleted_at() != null && !story.getDeleted_at().equals("")) {
                    mydb.updateTableColumn(tb, id, story.getId(), "deleted_at", story.getDeleted_at());
                }
                if (story.getOptograph_id() != null && !story.getOptograph_id().equals("")) {
                    mydb.updateTableColumn(tb, id, story.getId(), "optograph_id", story.getOptograph_id());
                }
                if (story.getPerson_id() != null && !story.getPerson_id().equals("")) {
                    mydb.updateTableColumn(tb, id, story.getId(), "person_id", story.getPerson_id());
                }
                List<String> childrenIds = new LinkedList<>();
                if(story.getChildren().size() > 0){
                    List<StoryChild> chldrn = stry.getChildren();
                    String id2 = "story_object_id";
                    Log.d("StoryFeedAdapter", "Updating story chld.size = " + chldrn.size());
                    for (int z=0; z < chldrn.size(); z++){
                        String tb2 = DBHelper.STORY_CHILDREN_TABLE_NAME;
                        StoryChild chld = chldrn.get(z);
                        res = mydb.getData(chld.getStory_object_id(), tb2, "story_object_id");
                        res.moveToFirst();
                        if(chld.getStory_object_id() != null){
                            Log.d("saveToSQLiteStry","chld.getStory_object_id() = "+chld.getStory_object_id());
                            Log.d("saveToSQLiteStry","chld.getStory_object_story_id() = "+chld.getStory_object_story_id());
                            Log.d("saveToSQLiteStry","tb2 = "+tb2);
                            Log.d("saveToSQLiteStry","id2 = "+id2);
                            mydb.updateTableColumn(tb2, id2, chld.getStory_object_id(), "story_object_story_id", chld.getStory_object_story_id());
                        }
                        if(chld.getStory_object_media_type() != null  && !chld.getStory_object_media_type().equals("")){
                            mydb.updateTableColumn(tb2, id2, chld.getStory_object_id(), "story_object_media_type", chld.getStory_object_media_type());
                        }
                        if(chld.getStory_object_media_face() != null && !chld.getStory_object_media_face().equals("")){
                            mydb.updateTableColumn(tb2, id2, chld.getStory_object_id(), "story_object_media_face", chld.getStory_object_media_face());
                        }
                        if(chld.getStory_object_media_description() != null && !chld.getStory_object_media_description().equals("")){
                            mydb.updateTableColumn(tb2, id2, chld.getStory_object_id(), "story_object_media_description", chld.getStory_object_media_description());
                        }
                        if(chld.getStory_object_media_additional_data() != null && !chld.getStory_object_media_additional_data().equals("")){
                            mydb.updateTableColumn(tb2, id2, chld.getStory_object_id(), "story_object_media_additional_data", chld.getStory_object_media_additional_data());
                        }
                        if(chld.getStory_object_position() != null){
                            mydb.updateTableColumn(tb2, id2, chld.getStory_object_id(), "story_object_position", TextUtils.join(",", chld.getStory_object_position()));
                        }
                        if(chld.getStory_object_rotation() != null){
                            mydb.updateTableColumn(tb2, id2, chld.getStory_object_id(), "story_object_rotation", TextUtils.join(",", chld.getStory_object_rotation()));
                        }
                        if(chld.getStory_object_created_at() != null && !chld.getStory_object_created_at().equals("")){
                            mydb.updateTableColumn(tb2, id2, chld.getStory_object_id(), "story_object_created_at", chld.getStory_object_created_at());
                        }
                        if(chld.getStory_object_updated_at() != null && !chld.getStory_object_updated_at().equals("")){
                            mydb.updateTableColumn(tb2, id2, chld.getStory_object_id(), "story_object_updated_at", chld.getStory_object_updated_at());
                        }
                        if(chld.getStory_object_deleted_at() != null && !chld.getStory_object_deleted_at().equals("")){
                            mydb.updateTableColumn(tb2, id2, chld.getStory_object_id(), "story_object_deleted_at", chld.getStory_object_deleted_at());
                        }
                        if(chld.getStory_object_media_filename() != null && !chld.getStory_object_media_filename().equals("")){
                            mydb.updateTableColumn(tb2, id2, chld.getStory_object_id(), "story_object_media_filename", chld.getStory_object_media_filename());
                        }
                        if(chld.getStory_object_media_fileurl() != null && !chld.getStory_object_media_fileurl().equals("")){
                            mydb.updateTableColumn(tb2, id2, chld.getStory_object_id(), "story_object_media_fileurl", chld.getStory_object_media_fileurl());
                        }

                        childrenIds.add(chldrn.get(z).getStory_object_id());
                    }
                }
                mydb.updateTableColumn(tb, id, story.getId(), "story_object_id", TextUtils.join(",", childrenIds));

                res.close();
            }else{
                List<String> childrenIds = new LinkedList<>();
                if(story.getChildren().size() > 0){
                    List<StoryChild> chldrn = stry.getChildren();
                    Log.d("StoryFeedAdapter", "Inserting story chld chldrn.size() = " + chldrn.size());

                    for (int z=0; z < chldrn.size(); z++){
                        StoryChild chld = chldrn.get(z);
                        mydb.insertStoryChildren(chld.getStory_object_id(), chld.getStory_object_story_id(), chld.getStory_object_media_type(), chld.getStory_object_media_face(),
                                chld.getStory_object_media_description(), chld.getStory_object_media_additional_data(), TextUtils.join(",", chld.getStory_object_position()), TextUtils.join(",", chld.getStory_object_rotation()),
                                chld.getStory_object_created_at(), chld.getStory_object_updated_at(), chld.getStory_object_deleted_at(), chld.getStory_object_media_filename(), chld.getStory_object_media_fileurl());

                        childrenIds.add(chldrn.get(z).getStory_object_id());
                    }
                }
                Log.d("StoryFeedAdapter", "Inserting story " + story.getId());
                Log.d("StoryFeedAdapter", "Inserting story.getPerson_id() " +story.getPerson_id());

                mydb.insertStory(story.getId(),story.getCreated_at(),story.getUpdated_at(),story.getDeleted_at(), story.getOptograph_id(), story.getPerson_id(), TextUtils.join(",", childrenIds));
                res.close();
            }
        }
    }

    public Observable<Optograph> getOptographs(Cursor cursor, String type) {
        List<Optograph> optographs = new LinkedList<>();
        cursor.moveToFirst();
        Log.d("DBHelper2","getOptographs cursor.getCount() = "+cursor.getCount());
        for(int a=0; a < cursor.getCount(); a++){
            if(type.equals("story")){
                Story story = createStoryFromCursor(cursor);
                String optoId = cursor.getString(cursor.getColumnIndex(DBHelper.STORY_OPTOGRAPH_ID));

                Cursor res = mydb.getData(optoId, DBHelper.OPTO_TABLE_NAME_FEEDS,DBHelper.OPTOGRAPH_ID);
                res.moveToFirst();
                Optograph opto = createOptoFromCursor(res);
                opto.setStory(story);
                optographs.add(opto);
            }else{ //optographs
                Optograph opto = createOptoFromCursor(cursor);
                String storyId = cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_STORY_ID));
                String optoId = cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_ID));
                Log.d("DBHelper2","storyId = "+storyId);
                if(!storyId.equals("")){
                    Cursor res = mydb.getUserStories2(optoId, DBHelper.STORY_TABLE_NAME, ApiConsumer.PROFILE_GRID_LIMIT);
                    Log.d("DBHelper2","storyId res.count= "+res.getCount());
                    Story story = createStoryFromCursor(res);
                    opto.setStory(story);
                }
                optographs.add(opto);
            }
            cursor.moveToNext();
        }

        cursor.close();

        return Observable.from(optographs);
    }

    public Optograph createOptoFromCursor(Cursor res){
        Optograph opto = null;
        Log.d("DBHelper2","createOptoFromCursor res.getCount() = "+res.getCount());
        if (res.getCount() > 0) {
            Log.d("DBHelper2","createOptoFromCursor OPTOGRAPH_ID = "+res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_ID)));
            opto = new Optograph(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_ID)));
            opto.setCreated_at(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_CREATED_AT)));
            opto.setIs_starred(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_STARRED)) == 1 ? true : false);
            opto.setDeleted_at(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_DELETED_AT)));
            opto.setStitcher_version(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_STITCHER_VERSION)));
            opto.setText(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_TEXT)));
            opto.setViews_count(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_STARS_COUNT)));
            opto.setIs_staff_picked(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_STAFF_PICK)) == 1 ? true : false);
            opto.setShare_alias(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_SHARE_ALIAS)));
            opto.setIs_private(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_PRIVATE)) == 1 ? true : false);
            opto.setIs_published(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_PUBLISHED)) == 1 ? true : false);
            opto.setOptograph_type(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_TYPE)));
            opto.setStars_count(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_STARS_COUNT)));
            opto.setShould_be_published(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) == 1 ? true : false);
            opto.setIs_local(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_LOCAL)) == 1 ? true : false);
            opto.setIs_data_uploaded(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_DATA_UPLOADED)) == 1 ? true : false);

            Cursor locCur = mydb.getData(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_LOCATION_ID)), DBHelper.LOCATION_TABLE_NAME, "id");
            Cursor perCur = mydb.getData(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_PERSON_ID)), DBHelper.PERSON_TABLE_NAME, "id");

            opto.setLocation(createLocationFromCursor(locCur));
            opto.setPerson(createPersonFromCursor(perCur));

        }
        return opto;
    }

    public Person createPersonFromCursor(Cursor res){
        Person person = new Person();
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
        return person;
    }

    public Location createLocationFromCursor(Cursor res){
        Location location = new Location();
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
        return location;
    }

    public Story createStoryFromCursor(Cursor res){
        Story story = new Story();
        res.moveToFirst();
        if (res.getCount() >  0) {
            story.setId(res.getString(res.getColumnIndex(DBHelper.STORY_ID)));
            story.setCreated_at(res.getString(res.getColumnIndex(DBHelper.STORY_CREATED_AT)));
            story.setUpdated_at(res.getString(res.getColumnIndex(DBHelper.STORY_UPDATED_AT)));
            story.setDeleted_at(res.getString(res.getColumnIndex(DBHelper.STORY_DELETED_AT)));
            story.setOptograph_id(res.getString(res.getColumnIndex(DBHelper.STORY_OPTOGRAPH_ID)));
            story.setPerson_id(res.getString(res.getColumnIndex(DBHelper.STORY_PERSON_ID)));

            Cursor chldrenCurs = new DBHelper(context).getData(res.getString(res.getColumnIndex(DBHelper.STORY_ID)), DBHelper.STORY_CHILDREN_TABLE_NAME, DBHelper.STORY_CHILDREN_STORY_ID);
            ArrayList<StoryChild> storyChldren = createStoryChildrenFromCursor(chldrenCurs);
            story.setChildren(storyChldren);
        }
        return story;
    }

    public ArrayList<StoryChild> createStoryChildrenFromCursor(Cursor res){
        ArrayList<StoryChild> storyChldren = new ArrayList<StoryChild>();
        res.moveToFirst();

        if (res.getCount() > 0) {
            for(int a=0; a < res.getCount(); a++){
                StoryChild storyChld = new StoryChild();
                storyChld.setStory_object_id(res.getString(res.getColumnIndex(DBHelper.STORY_CHILDREN_OBJECT_ID)));
                storyChld.setStory_object_story_id(res.getString(res.getColumnIndex(DBHelper.STORY_CHILDREN_STORY_ID)));
                storyChld.setStory_object_media_type(res.getString(res.getColumnIndex(DBHelper.STORY_CHILDREN_MEDIA_TYPE)));
                storyChld.setStory_object_media_face(res.getString(res.getColumnIndex(DBHelper.STORY_CHILDREN_MEDIA_FACE)));
                storyChld.setStory_object_media_description(res.getString(res.getColumnIndex(DBHelper.STORY_CHILDREN_MEDIA_DESCRIPTION)));
                storyChld.setStory_object_media_additional_data(res.getString(res.getColumnIndex(DBHelper.STORY_CHILDREN_MEDIA_ADDITIONAL_DATA)));

                storyChld.setStory_object_position(Arrays.asList(TextUtils.split(res.getString(res.getColumnIndex(DBHelper.STORY_CHILDREN_POSITION)), ",")));
                storyChld.setStory_object_rotation(Arrays.asList(TextUtils.split(res.getString(res.getColumnIndex(DBHelper.STORY_CHILDREN_ROTATION)),",")));

                storyChld.setStory_object_created_at(res.getString(res.getColumnIndex(DBHelper.STORY_CHILDREN_CREATED_AT)));
                storyChld.setStory_object_updated_at(res.getString(res.getColumnIndex(DBHelper.STORY_CHILDREN_UPDATED_AT)));
                storyChld.setStory_object_deleted_at(res.getString(res.getColumnIndex(DBHelper.STORY_CHILDREN_DELETED_AT)));
                storyChld.setStory_object_media_filename(res.getString(res.getColumnIndex(DBHelper.STORY_CHILDREN_MEDIA_FILENAME)));
                storyChld.setStory_object_media_fileurl(res.getString(res.getColumnIndex(DBHelper.STORY_CHILDREN_MEDIA_FILEURL)));

                storyChldren.add(storyChld);
                res.moveToNext();
            }
        }
        return storyChldren;
    }
}