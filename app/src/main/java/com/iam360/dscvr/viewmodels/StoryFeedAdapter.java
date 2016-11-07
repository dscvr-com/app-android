package com.iam360.dscvr.viewmodels;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iam360.dscvr.BR;
import com.iam360.dscvr.R;
import com.iam360.dscvr.StoryFeedItemBinding;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.Person;
import com.iam360.dscvr.network.ApiConsumer;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.DBHelper;
import com.iam360.dscvr.util.DBHelper2;
import com.iam360.dscvr.views.activity.MainActivity;
import com.iam360.dscvr.views.activity.OptographDetailsActivity;
import com.iam360.dscvr.views.activity.ProfileActivity;
import com.iam360.dscvr.views.activity.StoryCreatorActivity;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class StoryFeedAdapter extends RecyclerView.Adapter<StoryFeedAdapter.StoryFeedItemHolder> {
    private List<Optograph> optographs;

    protected ApiConsumer apiConsumer;
    private Cache cache;
    private Context context;
    private DBHelper mydb;
    private boolean isAllStory = true;

    public StoryFeedAdapter(Context context, boolean isAllStory) {
        this.context = context;
        this.optographs = new ArrayList<>();
        this.isAllStory = isAllStory;

        cache = Cache.open();

        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        mydb = new DBHelper(context);
    }

    @Override
    public StoryFeedItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()) .inflate(R.layout.story_feed_item, parent, false);
        return new StoryFeedItemHolder(view);
    }

    @Override
    public void onBindViewHolder(StoryFeedItemHolder holder, int position) {

        Optograph optograph = optographs.get(position);

        if(isAllStory) holder.getBinding().personName.setVisibility(View.VISIBLE);
        else  holder.getBinding().personName.setVisibility(View.GONE);

        holder.getBinding().storyPreview.setOnClickListener(v -> callDetailsPage(optograph, "view"));
        holder.getBinding().personName.setOnClickListener(v -> startProfile(optograph.getPerson()));
        holder.getBinding().storyPreviewEdit.setOnClickListener(v -> callDetailsPage(optograph, "edit"));

        holder.getBinding().setVariable(BR.optograph, optograph);
        holder.getBinding().setVariable(BR.person, optograph.getPerson());
        holder.getBinding().setVariable(BR.location, optograph.getLocation());

        holder.getBinding().executePendingBindings();

    }


//    public void saveToSQLite(Optograph opto) {
//        if (opto.getId() == null) return;
//        Cursor res = mydb.getData(opto.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
//        res.moveToFirst();
//        if (res.getCount() > 0) {
//            Log.d("StoryFeedAdapter", "Updating opto " + opto.getId());
//            String id = DBHelper.OPTOGRAPH_ID;
//            String tb = DBHelper.OPTO_TABLE_NAME_FEEDS;
//            if (opto.getText() != null && !opto.getText().equals("")) {
//                mydb.updateTableColumn(tb, id, opto.getId(), "optograph_text", opto.getText());
//            }
//            if (opto.getCreated_at() != null && !opto.getCreated_at().equals("")) {
//                mydb.updateTableColumn(tb, id, opto.getId(), "optograph_created_at", opto.getCreated_at());
//            }
//            if (opto.getDeleted_at() != null && !opto.getDeleted_at().equals("")) {
//                mydb.updateTableColumn(tb, id, opto.getId(), "optograph_deleted_at", opto.getDeleted_at());
//            }
//            mydb.updateTableColumn(tb, id, opto.getId(), "optograph_is_starred", opto.is_starred());
//            mydb.updateTableColumn(tb, id, opto.getId(), "optograph_stars_count", opto.getStars_count());
//            mydb.updateTableColumn(tb, id, opto.getId(), "optograph_is_published", opto.is_published());
//            mydb.updateTableColumn(tb, id, opto.getId(), "optograph_is_private", opto.is_private());
//
//            if (opto.getStitcher_version() != null && !opto.getStitcher_version().equals("")) {
//                mydb.updateTableColumn(tb, id, opto.getId(), "optograph_is_stitcher_version", opto.getStitcher_version());
//            }
//            if (opto.getStitcher_version() != null && !opto.getStitcher_version().equals("")) {
//                mydb.updateTableColumn(tb, id, opto.getId(), "optograph_is_data_uploaded", opto.is_data_uploaded());
//            }
//            mydb.updateTableColumn(tb, id, opto.getId(), "optograph_should_be_published", opto.isShould_be_published());
//            mydb.updateTableColumn(tb, id, opto.getId(), "optograph_is_place_holder_uploaded", opto.is_place_holder_uploaded());
//            mydb.updateTableColumn(tb, id, opto.getId(), "post_facebook", opto.isPostFacebook());
//            mydb.updateTableColumn(tb, id, opto.getId(), "post_twitter", opto.isPostTwitter());
//            mydb.updateTableColumn(tb, id, opto.getId(), "post_instagram", opto.isPostInstagram());
//            if (opto.getOptograph_type() != null && !opto.getOptograph_type().equals("")) {
//                mydb.updateTableColumn(tb, id, opto.getId(), "optograph_type", opto.getOptograph_type());
//            }
//            if (opto.getLocation() != null && opto.getLocation().getId() != null && !opto.getLocation().getId().equals("")) {
//                mydb.updateTableColumn(tb, id, opto.getId(), "optograph_location_id", opto.getLocation().getId());
//            }
//            res.close();
//        } else {
//            Log.d("StoryFeedAdapter", "Inserting opto " + opto.getId());
//            mydb.insertOptograph(opto.getId(), opto.getText(), opto.getPerson().getId(), opto.getLocation() == null ? "" : opto.getLocation().getId(),
//                    opto.getCreated_at(), opto.getDeleted_at() == null ? "" : opto.getDeleted_at(), opto.is_starred(), opto.getStars_count(), opto.is_published(),
//                    opto.is_private(), opto.getStitcher_version(), true, opto.is_on_server(), "", opto.isShould_be_published(), opto.is_local(),
//                    opto.is_place_holder_uploaded(), opto.isPostFacebook(), opto.isPostTwitter(), opto.isPostInstagram(),
//                    opto.is_data_uploaded(), opto.is_staff_picked(), opto.getShare_alias(), opto.getOptograph_type(),opto.getStory().getId());
//            res.close();
//        }
//        String loc = opto.getLocation() == null ? "" : opto.getLocation().getId();
//        String per = opto.getPerson() == null ? "" : opto.getPerson().getId();
//        String stry = opto.getStory() == null ? "" : opto.getStory().getId();
//
//        if (!per.equals("")) {
//            res = mydb.getData(opto.getPerson().getId(), DBHelper.PERSON_TABLE_NAME, "id");
//            res.moveToFirst();
//            if (opto.getPerson().getId() != null) {
//                Person person = opto.getPerson();
//                if (res.getCount() > 0) {
//                    Log.d("StoryFeedAdapter", "Updating person " + person.getId());
//                    String id = "id";
//                    String tb = DBHelper.PERSON_TABLE_NAME;
//                    if (person.getCreated_at() != null && !person.getCreated_at().equals("")) {
//                        mydb.updateTableColumn(tb, id, person.getId(), "created_at", person.getCreated_at());
//                    }
//                    if (person.getDeleted_at() != null && !person.getDeleted_at().equals("")) {
//                        mydb.updateTableColumn(tb, id, person.getId(), "deleted_at", person.getDeleted_at());
//                    }
//                    if (person.getDisplay_name() != null && !person.getDisplay_name().equals("")) {
//                        mydb.updateTableColumn(tb, id, person.getId(), "display_name", person.getDisplay_name());
//                    }
//                    if (person.getUser_name() != null && !person.getUser_name().equals("")) {
//                        mydb.updateTableColumn(tb, id, person.getId(), "user_name", person.getUser_name());
//                    }
//                    if (person.getEmail() != null && !person.getEmail().equals("")) {
//                        mydb.updateTableColumn(tb, id, person.getId(), "email", person.getEmail());
//                    }
//                    if (person.getText() != null && !person.getText().equals("")) {
//                        mydb.updateTableColumn(tb, id, person.getId(), "text", person.getText());
//                    }
//                    if (person.getAvatar_asset_id() != null && !person.getAvatar_asset_id().equals("")) {
//                        mydb.updateTableColumn(tb, id, person.getId(), "avatar_asset_id", person.getAvatar_asset_id());
//                    }
//                    mydb.updateTableColumn(tb, id, person.getId(), "optographs_count", String.valueOf(person.getOptographs_count()));
//                    mydb.updateTableColumn(tb, id, person.getId(), "followers_count", String.valueOf(person.getFollowers_count()));
//                    mydb.updateTableColumn(tb, id, person.getId(), "followed_count", String.valueOf(person.getFollowed_count()));
//                    mydb.updateTableColumn(tb, id, person.getId(), "is_followed", String.valueOf(person.is_followed()));
//                    if (person.getFacebook_user_id() != null && !person.getFacebook_user_id().equals("")) {
//                        mydb.updateTableColumn(tb, id, person.getId(), "facebook_user_id", String.valueOf(person.getFacebook_user_id()));
//                    }
//                    if (person.getFacebook_token() != null && !person.getFacebook_token().equals("")) {
//                        mydb.updateTableColumn(tb, id, person.getId(), "facebook_token", String.valueOf(person.getFacebook_token()));
//                    }
//                    if (person.getTwitter_token() != null && !person.getTwitter_token().equals("")) {
//                        mydb.updateTableColumn(tb, id, person.getId(), "twitter_token", String.valueOf(person.getTwitter_token()));
//                    }
//                    if (person.getTwitter_secret() != null && !person.getTwitter_secret().equals("")) {
//                        mydb.updateTableColumn(tb, id, person.getId(), "twitter_secret", String.valueOf(person.getTwitter_secret()));
//                    }
//                    res.close();
//                } else {
//                    Log.d("StoryFeedAdapter", "Inserting person " + person.getId());
//                    mydb.insertPerson(person.getId(), person.getCreated_at(), person.getEmail(), person.getDeleted_at(), person.isElite_status(),
//                            person.getDisplay_name(), person.getUser_name(), person.getText(), person.getAvatar_asset_id(), person.getFacebook_user_id(), person.getOptographs_count(),
//                            person.getFollowers_count(), person.getFollowed_count(), person.is_followed(), person.getFacebook_token(), person.getTwitter_token(), person.getTwitter_secret());
//                    res.close();
//                }
//            }
//        }
//
//        if (!loc.equals("")) {
//            res = mydb.getData(opto.getLocation().getId(), DBHelper.LOCATION_TABLE_NAME, "id");
//            res.moveToFirst();
//            if (opto.getLocation().getId() != null) {
//                Location locs = opto.getLocation();
//                if (res.getCount() > 0) {
//                    Log.d("StoryFeedAdapter", "Updating loc " + locs.getId());
//                    String id = "id";
//                    String tb = DBHelper.LOCATION_TABLE_NAME;
//                    if (locs.getCreated_at() != null && !locs.getCreated_at().equals("")) {
//                        mydb.updateTableColumn(tb, id, locs.getId(), "created_at", locs.getCreated_at());
//                    }
//                    if (locs.getUpdated_at() != null && !locs.getUpdated_at().equals("")) {
//                        mydb.updateTableColumn(tb, id, locs.getId(), "updated_at", locs.getUpdated_at());
//                    }
//                    if (locs.getDeleted_at() != null && !locs.getDeleted_at().equals("")) {
//                        mydb.updateTableColumn(tb, id, locs.getId(), "deleted_at", locs.getDeleted_at());
//                    }
//                    if (locs.getLatitude() != 0) {
//                        mydb.updateTableColumn(tb, id, locs.getId(), "latitude", locs.getLatitude());
//                    }
//                    if (locs.getLongitude() != 0) {
//                        mydb.updateTableColumn(tb, id, locs.getId(), "longitude", locs.getLongitude());
//                    }
//                    if (locs.getText() != null && !locs.getText().equals("")) {
//                        mydb.updateTableColumn(tb, id, locs.getId(), "text", locs.getText());
//                    }
//                    if (locs.getCountry() != null && !locs.getCountry().equals("")) {
//                        mydb.updateTableColumn(tb, id, locs.getId(), "country", locs.getCountry());
//                    }
//                    if (locs.getCountry_short() != null && !locs.getCountry_short().equals("")) {
//                        mydb.updateTableColumn(tb, id, locs.getId(), "country_short", locs.getCountry_short());
//                    }
//                    if (locs.getPlace() != null && !locs.getPlace().equals("")) {
//                        mydb.updateTableColumn(tb, id, locs.getId(), "place", locs.getPlace());
//                    }
//                    if (locs.getRegion() != null && !locs.getRegion().equals("")) {
//                        mydb.updateTableColumn(tb, id, locs.getId(), "region", locs.getRegion());
//                    }
//                    mydb.updateTableColumn(tb, id, locs.getId(), "poi", String.valueOf(locs.isPoi()));
//                    res.close();
//                } else {
//                    Log.d("StoryFeedAdapter", "Inserting loc " + locs.getId());
////                    mydb.updateColumnOptograph(opto.getId(),DBHelper.LOCATION_ID,locs.getId());
//                    mydb.insertLocation(locs.getId(), locs.getCreated_at(), locs.getUpdated_at(), locs.getDeleted_at(),
//                            locs.getLatitude(), locs.getLongitude(), locs.getCountry(), locs.getText(),
//                            locs.getCountry_short(), locs.getPlace(), locs.getRegion(), locs.isPoi());
//                    res.close();
//                }
//            }
//        }
//
//        if(!stry.equals("")){
//            res = mydb.getData(opto.getStory().getId(), DBHelper.STORY_TABLE_NAME, "id");
//            res.moveToFirst();
//            if (opto.getStory().getId() != null) {
//                Story story = opto.getStory();
//                if (res.getCount() > 0) {
//                    Log.d("StoryFeedAdapter", "Updating story " + story.getId());
//                    String id = "id";
//                    String tb = DBHelper.LOCATION_TABLE_NAME;
//                    if (story.getCreated_at() != null && !story.getCreated_at().equals("")) {
//                        mydb.updateTableColumn(tb, id, story.getId(), "created_at", story.getCreated_at());
//                    }
//                    if (story.getUpdated_at() != null && !story.getUpdated_at().equals("")) {
//                        mydb.updateTableColumn(tb, id, story.getId(), "updated_at", story.getUpdated_at());
//                    }
//                    if (story.getDeleted_at() != null && !story.getDeleted_at().equals("")) {
//                        mydb.updateTableColumn(tb, id, story.getId(), "deleted_at", story.getDeleted_at());
//                    }
//                    if (story.getOptograph_id() != null && !story.getOptograph_id().equals("")) {
//                        mydb.updateTableColumn(tb, id, story.getId(), "optograph_id", story.getOptograph_id());
//                    }
//                    if (story.getPerson_id() != null && !story.getPerson_id().equals("")) {
//                        mydb.updateTableColumn(tb, id, story.getId(), "person_id", story.getPerson_id());
//                    }
//                    List<String> childrenIds = new LinkedList<>();
//                    if(story.getChildren().size() > 0){
//                        List<StoryChild> chldrn = opto.getStory().getChildren();
//                        Log.d("StoryFeedAdapter", "Updating story chld.size(" + chldrn.size());
//                        for (int z=0; z < chldrn.size(); z++){
//                            tb = DBHelper.STORY_CHILDREN_TABLE_NAME;
//                            StoryChild chld = chldrn.get(z);
//                            res = mydb.getData(chld.getStory_object_id(), tb, "story_object_id");
//                            res.moveToFirst();
//                            if(chld.getStory_object_id() != null){
//                                mydb.updateTableColumn(tb, id, chld.getStory_object_id(), "story_object_story_id", chld.getStory_object_story_id());
//                            }
//                            if(chld.getStory_object_media_type() != null  && !chld.getStory_object_media_type().equals("")){
//                                mydb.updateTableColumn(tb, id, chld.getStory_object_id(), "story_object_media_type", chld.getStory_object_media_type());
//                            }
//                            if(chld.getStory_object_media_face() != null && !chld.getStory_object_media_face().equals("")){
//                                mydb.updateTableColumn(tb, id, chld.getStory_object_id(), "story_object_media_face", chld.getStory_object_media_face());
//                            }
//                            if(chld.getStory_object_media_description() != null && !chld.getStory_object_media_description().equals("")){
//                                mydb.updateTableColumn(tb, id, chld.getStory_object_id(), "story_object_media_description", chld.getStory_object_media_description());
//                            }
//                            if(chld.getStory_object_media_additional_data() != null && !chld.getStory_object_media_additional_data().equals("")){
//                                mydb.updateTableColumn(tb, id, chld.getStory_object_id(), "story_object_media_additional_data", chld.getStory_object_media_additional_data());
//                            }
//                            if(chld.getStory_object_position() != null){
//                                mydb.updateTableColumn(tb, id, chld.getStory_object_id(), "story_object_position", TextUtils.join(",", chld.getStory_object_position()));
//                            }
//                            if(chld.getStory_object_rotation() != null){
//                                mydb.updateTableColumn(tb, id, chld.getStory_object_id(), "story_object_rotation", TextUtils.join(",", chld.getStory_object_rotation()));
//                            }
//                            if(chld.getStory_object_created_at() != null && !chld.getStory_object_created_at().equals("")){
//                                mydb.updateTableColumn(tb, id, chld.getStory_object_id(), "story_object_created_at", chld.getStory_object_created_at());
//                            }
//                            if(chld.getStory_object_updated_at() != null && !chld.getStory_object_updated_at().equals("")){
//                                mydb.updateTableColumn(tb, id, chld.getStory_object_id(), "story_object_updated_at", chld.getStory_object_updated_at());
//                            }
//                            if(chld.getStory_object_deleted_at() != null && !chld.getStory_object_deleted_at().equals("")){
//                                mydb.updateTableColumn(tb, id, chld.getStory_object_id(), "story_object_deleted_at", chld.getStory_object_deleted_at());
//                            }
//                            if(chld.getStory_object_media_filename() != null && !chld.getStory_object_media_filename().equals("")){
//                                mydb.updateTableColumn(tb, id, chld.getStory_object_id(), "story_object_media_filename", chld.getStory_object_media_filename());
//                            }
//                            if(chld.getStory_object_media_fileurl() != null && !chld.getStory_object_media_fileurl().equals("")){
//                                mydb.updateTableColumn(tb, id, chld.getStory_object_id(), "story_object_media_fileurl", chld.getStory_object_media_fileurl());
//                            }
//
//                            childrenIds.add(chldrn.get(z).getStory_object_id());
//                        }
//                    }
//                    mydb.updateTableColumn(tb, id, story.getId(), "story_children_id", TextUtils.join(",", childrenIds));
//
//                    res.close();
//                }else{
//                    List<String> childrenIds = new LinkedList<>();
//                    if(story.getChildren().size() > 0){
//                        List<StoryChild> chldrn = opto.getStory().getChildren();
//                        Log.d("StoryFeedAdapter", "Inserting story chld chldrn.size() = " + chldrn.size());
//
//                        for (int z=0; z < chldrn.size(); z++){
//                            StoryChild chld = chldrn.get(z);
//                            mydb.insertStoryChildren(chld.getStory_object_id(), chld.getStory_object_story_id(), chld.getStory_object_media_type(), chld.getStory_object_media_face(),
//                                    chld.getStory_object_media_description(), chld.getStory_object_media_additional_data(), TextUtils.join(",", chld.getStory_object_position()), TextUtils.join(",", chld.getStory_object_rotation()),
//                                    chld.getStory_object_created_at(), chld.getStory_object_updated_at(), chld.getStory_object_deleted_at(), chld.getStory_object_media_filename(), chld.getStory_object_media_fileurl());
//
//                            childrenIds.add(chldrn.get(z).getStory_object_id());
//                        }
//                    }
//                    Log.d("StoryFeedAdapter", "Inserting story " + story.getId());
//                    Log.d("StoryFeedAdapter", "Inserting story.getPerson_id() " +story.getPerson_id());
//
//                    mydb.insertStory(story.getId(),story.getCreated_at(),story.getUpdated_at(),story.getDeleted_at(), story.getOptograph_id(), story.getPerson_id(), TextUtils.join(",", childrenIds));
//                    res.close();
//                }
//            }
//        }
//    }


    public Optograph checkToDB(Optograph optograph) {
        Cursor res = mydb.getData(optograph.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
        res.moveToFirst();
        if (res.getCount()==0) {
//            deleteOptographFromPhone(optograph.getId());
            return null;
        }
        if (res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) == 1 || !res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_DELETED_AT)).equals("")) {
//            deleteOptographFromPhone(optograph.getId());
            return null;
        }
        optograph.setStitcher_version(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_STITCHER_VERSION)));
        optograph.setText(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_TEXT)));
        optograph.setOptograph_type(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_TYPE)));
//        optograph.setCreated_at(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_CREATED_AT)));
        optograph.setIs_on_server(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_ON_SERVER)) != 0);
        optograph.setShould_be_published(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) != 0);
        optograph.setIs_place_holder_uploaded(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_PLACEHOLDER_UPLOADED)) != 0);
        optograph.setIs_data_uploaded(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_DATA_UPLOADED)) != 0);
        Timber.d("checkToDB isFBShare? " + (res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_FACEBOOK)) != 0) + " Twit? " + (res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_TWITTER)) != 0) + " optoId: " + optograph.getId());
        optograph.setPostFacebook(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_FACEBOOK)) != 0);
        optograph.setPostTwitter(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_TWITTER)) != 0);
        optograph.setPostInstagram(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_INSTAGRAM)) != 0);
        Cursor face = mydb.getData(optograph.getId(), DBHelper.FACES_TABLE_NAME, DBHelper.FACES_ID);
        face.moveToFirst();
        if (face.getCount()==0) return optograph;
        optograph.getLeftFace().setStatusByIndex(0, face.getInt(face.getColumnIndex(DBHelper.FACES_LEFT_ZERO)) != 0);
        optograph.getLeftFace().setStatusByIndex(1, face.getInt(face.getColumnIndex(DBHelper.FACES_LEFT_ONE)) != 0);
        optograph.getLeftFace().setStatusByIndex(2, face.getInt(face.getColumnIndex(DBHelper.FACES_LEFT_TWO)) != 0);
        optograph.getLeftFace().setStatusByIndex(3, face.getInt(face.getColumnIndex(DBHelper.FACES_LEFT_THREE)) != 0);
        optograph.getLeftFace().setStatusByIndex(4, face.getInt(face.getColumnIndex(DBHelper.FACES_LEFT_FOUR)) != 0);
        optograph.getLeftFace().setStatusByIndex(5,face.getInt(face.getColumnIndex(DBHelper.FACES_LEFT_FIVE))!=0);
        optograph.getRightFace().setStatusByIndex(0,face.getInt(face.getColumnIndex(DBHelper.FACES_RIGHT_ZERO))!=0);
        optograph.getRightFace().setStatusByIndex(1,face.getInt(face.getColumnIndex(DBHelper.FACES_RIGHT_ONE))!=0);
        optograph.getRightFace().setStatusByIndex(2,face.getInt(face.getColumnIndex(DBHelper.FACES_RIGHT_TWO))!=0);
        optograph.getRightFace().setStatusByIndex(3,face.getInt(face.getColumnIndex(DBHelper.FACES_RIGHT_THREE))!=0);
        optograph.getRightFace().setStatusByIndex(4,face.getInt(face.getColumnIndex(DBHelper.FACES_RIGHT_FOUR))!=0);
        optograph.getRightFace().setStatusByIndex(5,face.getInt(face.getColumnIndex(DBHelper.FACES_RIGHT_FIVE))!=0);

        Person person = new Person();
        person.setFacebook_token(cache.getString(Cache.USER_FB_TOKEN));
        person.setDisplay_name(cache.getString(Cache.USER_DISPLAY_NAME));
        person.setFacebook_user_id(cache.getString(Cache.USER_FB_ID));
        person.setUser_name(cache.getString(Cache.USER_DISPLAY_NAME));

        optograph.setPerson(person);
        return optograph;
    }

    private void callDetailsPage(Optograph optograph, String type) {
        Intent intent = new Intent(context, OptographDetailsActivity.class);
        if(type.equals("edit")){
            intent = new Intent(context, StoryCreatorActivity.class);
        }
        intent.putExtra("opto", optograph);
        intent.putExtra("story", true);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    private void startProfile(Person person) {
        if(cache.getString(Cache.USER_ID).equals(person)) {
            if(context instanceof MainActivity)
                ((MainActivity) context).setPage(MainActivity.PROFILE_MODE);
        } else {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("person", person);
            context.startActivity(intent);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return optographs.size();
    }

    public void addItem(Optograph optograph) {

        if (optograph == null) {
            return;
        }

        DateTime created_at = optograph.getCreated_atDateTime();

        // skip if optograph is already in list
        if (optographs.contains(optograph)) {
            return;
        }

        new DBHelper2(context).saveToSQLite(optograph);
        if (optograph.is_local()) optograph = checkToDB(optograph);
        Log.d("MARK","addItem optograph.getStory = "+optograph.getStory());
        optographs.add(optograph);
        notifyItemInserted(getItemCount());

    }

    public Optograph get(int position) {
        return optographs.get(position);
    }

    public Optograph getOldest() {
        return get(getItemCount() - 1);
    }

    public boolean isEmpty() {
        return optographs.isEmpty();
    }

    public List<Optograph> getOptographs() {
        return this.optographs;
    }

    public static class StoryFeedItemHolder extends RecyclerView.ViewHolder {
        private StoryFeedItemBinding bindingHeader;

        public StoryFeedItemHolder(View rowView) {
            super(rowView);
            this.bindingHeader = DataBindingUtil.bind(rowView);
        }

        public StoryFeedItemBinding getBinding() {
            return bindingHeader;
        }
    }


    public void clearData() {
        int size = optographs.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                optographs.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }
}
