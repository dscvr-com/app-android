package com.iam360.iam360.viewmodels;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import com.iam360.iam360.BR;
import com.iam360.iam360.GridItemBinding;
import com.iam360.iam360.R;
import com.iam360.iam360.model.LogInReturn;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.Constants;
import com.iam360.iam360.util.DBHelper;
import com.iam360.iam360.views.new_design.OptographDetailsActivity;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */
public class OptographGridAdapter extends RecyclerView.Adapter<OptographGridAdapter.OptographViewHolder> {
    private static final int ITEM_HEIGHT = Constants.getInstance().getDisplayMetrics().heightPixels;
    private static final int ITEM_WIDTH= Constants.getInstance().getDisplayMetrics().widthPixels;
    List<Optograph> optographs;

    protected Cache cache;
    private DBHelper mydb;
//    private RecyclerView snappyRecyclerView;

    protected ApiConsumer apiConsumer;

    private Context context;

    public OptographGridAdapter(Context context) {
        this.context = context;
        this.optographs = new ArrayList<>();

        cache = Cache.open();
        mydb = new DBHelper(context);

        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);
    }

    private void setHeart(Optograph optograph, TextView heartLabel, boolean liked, int count) {

        heartLabel.setText(String.valueOf(count));
        if(liked) {
            mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, 1);
            heartLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.liked_icn, 0);
        } else {
            mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, 0);
            heartLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.like_icn, 0);
        }

        optograph.setIs_starred(liked);
        optograph.setStars_count(count);
    }

    @Override
    public OptographViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.grid_item, parent, false);

        final OptographViewHolder viewHolder = new OptographViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

//        snappyRecyclerView = recyclerView;
    }

    @Override
    public void onBindViewHolder(OptographViewHolder holder, int position) {
        Optograph optograph = optographs.get(position);

        // reset view holder if we got new optograh
        if (!optograph.equals(holder.getBinding().getOptograph())) {
            // cancel the request for the old texture
            if (holder.getBinding().getOptograph() != null) {
                // TODO: cancel request
            }

            // span complete screen
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ITEM_WIDTH); //ITEM_WIDTH / OptographGridFragment.NUM_COLUMNS); // (width, height)
            holder.itemView.setLayoutParams(params);

            holder.getBinding().setVariable(BR.optograph, optograph);
            holder.getBinding().executePendingBindings();

            holder.getBinding().optograph2dview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, OptographDetailsActivity.class);
                    intent.putExtra("opto", optograph);
                    context.startActivity(intent);

//                    MainActivityRedesign activity = (MainActivityRedesign) context;
//                    activity.startProfileFeed(optograph.getPerson(), position);
                }
            });

            setHeart(optograph, holder.getBinding().heartLabel, optograph.is_starred(), optograph.getStars_count());
            holder.getBinding().heartLabel.setOnClickListener(v -> {
                if (!cache.getString(Cache.USER_TOKEN).equals("") && !optograph.is_starred()) {

                    setHeart(optograph, holder.getBinding().heartLabel, true, optograph.getStars_count() + 1);
                    apiConsumer.postStar(optograph.getId(), new Callback<LogInReturn.EmptyResponse>() {
                        @Override
                        public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                            // revert star count on failure
                            if (!response.isSuccess()) {
                                setHeart(optograph, holder.getBinding().heartLabel, false, optograph.getStars_count() - 1);
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            // revert star count on failure
                            setHeart(optograph, holder.getBinding().heartLabel, false, optograph.getStars_count() - 1);
                        }
                    });
                } else if (!cache.getString(Cache.USER_TOKEN).equals("") && optograph.is_starred()) {
                    setHeart(optograph, holder.getBinding().heartLabel, false, optograph.getStars_count() - 1);

                    apiConsumer.deleteStar(optograph.getId(), new Callback<LogInReturn.EmptyResponse>() {
                        @Override
                        public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                            // revert star count on failure
                            if (!response.isSuccess()) {
                                setHeart(optograph, holder.getBinding().heartLabel, true, optograph.getStars_count() + 1);
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            // revert star count on failure
                            setHeart(optograph, holder.getBinding().heartLabel, true, optograph.getStars_count() + 1);
                        }
                    });
                } else {
                    // TODO show login page
//                    Snackbar.make(v,"Login first.",Snackbar.LENGTH_SHORT).show();
//                    MainActivityRedesign activity = (MainActivityRedesign) context;
//                    activity.prepareProfile(false);
                }

            });

        } else {
            Timber.d("rebinding of OptographViewHolder at position %s", position);
        }
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

        if (optograph.getPerson().getId().equals(cache.getString(Cache.USER_ID))) {
            saveToSQLite(optograph);
        }
        if (optograph.is_local()) optograph = checkToDB(optograph);
        if (optograph==null) {
            return;
        }

        // if list is empty, simply add new optograph
        if (optographs.isEmpty()) {
            optographs.add(optograph);
            notifyItemInserted(getItemCount());
            return;
        }

        // if optograph is oldest, simply append to list
        if (created_at != null && created_at.isBefore(getOldest().getCreated_atDateTime())) {
            optographs.add(optograph);
            notifyItemInserted(getItemCount());
            return;
        }

        // find correct position of optograph
        // TODO: allow for "breaks" between new optograph and others...
        for (int i = 0; i < optographs.size(); i++) {
            Optograph current = optographs.get(i);
            if (created_at != null && created_at.isAfter(current.getCreated_atDateTime())) {
                optographs.add(i, optograph);
                notifyItemInserted(i);
                return;
            }
        }
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


    public void saveToSQLite(Optograph opto) {
        Cursor res = mydb.getData(opto.getId(),DBHelper.OPTO_TABLE_NAME,DBHelper.OPTOGRAPH_ID);
        res.moveToFirst();
        if (res.getCount()!=0) return;
        String loc = opto.getLocation()==null?"":opto.getLocation().getId();
        mydb.insertOptograph(opto.getId(),opto.getText(),opto.getPerson().getId(),opto.getLocation()==null?"":opto.getLocation().getId(),
                opto.getCreated_at(),opto.getDeleted_at()==null?"":opto.getDeleted_at(),opto.is_starred()?1:0,opto.getStars_count(),opto.is_published()?1:0,
                opto.is_private()?1:0,opto.getStitcher_version(),1,opto.is_on_server()?1:0,"",opto.isShould_be_published()?1:0,
                opto.is_place_holder_uploaded()?1:0,opto.isPostFacebook()?1:0,opto.isPostTwitter()?1:0,opto.isPostInstagram()?1:0,
                opto.is_data_uploaded()?1:0);
    }

    public Optograph checkToDB(Optograph optograph) {
        Cursor res = mydb.getData(optograph.getId(),DBHelper.OPTO_TABLE_NAME,DBHelper.OPTOGRAPH_ID);
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
//        optograph.setCreated_at(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_CREATED_AT)));
        optograph.setIs_on_server(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_ON_SERVER)) != 0);
        optograph.setShould_be_published(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) != 0);
        optograph.setIs_place_holder_uploaded(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_PLACEHOLDER_UPLOADED)) != 0);
        optograph.setIs_data_uploaded(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_DATA_UPLOADED)) != 0);
        Timber.d("checkToDB isFBShare? "+(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_FACEBOOK)) != 0)+" Twit? "+(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_TWITTER)) != 0)+" optoId: "+optograph.getId());
        optograph.setPostFacebook(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_FACEBOOK)) != 0);
        optograph.setPostTwitter(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_TWITTER)) != 0);
        optograph.setPostInstagram(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_INSTAGRAM)) != 0);
        Cursor face = mydb.getData(optograph.getId(),DBHelper.FACES_TABLE_NAME,DBHelper.FACES_ID);
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
        return optograph;
    }


    public static class OptographViewHolder extends RecyclerView.ViewHolder {
        private GridItemBinding binding;
        private ImageView optograph2DCubeView;
        private boolean isNavigationModeCombined;


        public OptographViewHolder(View rowView) {
            super(rowView);
            this.binding = DataBindingUtil.bind(rowView);
            optograph2DCubeView = (ImageView) itemView.findViewById(R.id.optograph2dview);
        }

        public GridItemBinding getBinding() {
            return binding;
        }

    }
}
