package com.iam360.iam360.viewmodels;

import android.content.Context;
import android.content.Intent;
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
