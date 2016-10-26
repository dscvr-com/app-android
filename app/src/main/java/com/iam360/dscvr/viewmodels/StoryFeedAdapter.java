package com.iam360.dscvr.viewmodels;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
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


        saveToSQLite(optograph);

    }


    public void saveToSQLite(Optograph opto) {
        Cursor res = mydb.getData(opto.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
        res.moveToFirst();
        if (res.getCount()!=0) return;
        String loc = opto.getLocation()==null?"":opto.getLocation().getId();
        mydb.insertOptograph(opto.getId(),opto.getText(),opto.getPerson().getId(),loc,
                opto.getCreated_at(),opto.getDeleted_at()==null?"":opto.getDeleted_at(),opto.is_starred(),opto.getStars_count(),opto.is_published(),
                opto.is_private(), opto.getStitcher_version(),true,opto.is_on_server(),"",opto.isShould_be_published(), opto.is_local(),
                opto.is_place_holder_uploaded(),opto.isPostFacebook(),opto.isPostTwitter(),opto.isPostInstagram(),
                opto.is_data_uploaded(), opto.is_staff_picked(), opto.getShare_alias(), opto.getOptograph_type());
    }


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
//        if (optographs.contains(optograph)) {
//            return;
//        }

        if (optograph.getPerson().getId().equals(cache.getString(Cache.USER_ID))) {
            saveToSQLite(optograph);
        }
        if (optograph.is_local()) optograph = checkToDB(optograph);
        if (optograph==null) {
            return;
        }

        optographs.add(optographs.size(), optograph);
        notifyItemInserted(optographs.size() - 1);

//        // if list is empty, simply add new optograph
//        if (optographs.isEmpty()) {
//            optographs.add(optographs.size(), optograph);
////            notifyItemInserted(getItemCount());
//            Timber.d("ADD ITEM iteminserted 1 ");
//            notifyItemInserted(optographs.size() - 1);
//            return;
//        }
//
//        // if optograph is oldest, simply append to list
//        if (created_at != null && created_at.isBefore(getOldest().getCreated_atDateTime())) {
//            optographs.add(optograph);
//            Timber.d("ADD ITEM iteminserted 2 ");
////            notifyItemInserted(getItemCount());
//            notifyDataSetChanged();
//            return;
//        }
//
//        // find correct position of optograph
//        // TODO: allow for "breaks" between new optograph and others...
//        for (int i = 0; i < optographs.size(); i++) {
//            Optograph current = optographs.get(i);
//            Timber.d("ADD ITEM " + current.getCreated_atDateTime() + " : " + created_at);
//
//            if (created_at != null && (created_at.isEqual(current.getCreated_atDateTime()) || created_at.isAfter(current.getCreated_atDateTime())) ) {
//                optographs.add(i, optograph);
//                Timber.d("ADD ITEM iteminserted 3 ");
//                notifyItemInserted(i);
//                return;
//            }
//        }
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
