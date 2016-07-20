package com.iam360.iam360.views.new_design;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.iam360.iam360.BR;
import com.iam360.iam360.NewFeedItemBinding;
import com.iam360.iam360.R;
import com.iam360.iam360.model.LogInReturn;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.Constants;
import com.iam360.iam360.util.DBHelper;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import im.ene.lab.toro.ToroAdapter;
import im.ene.lab.toro.ToroViewHolder;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class OptographVideoFeedAdapter extends ToroAdapter<OptographVideoHolder> {
    private static final int ITEM_HEIGHT = Constants.getInstance().getDisplayMetrics().heightPixels;
    private static final float ITEM_WIDTH = Constants.getInstance().getDisplayMetrics().widthPixels;
    private static final float DENSITY = Constants.getInstance().getDisplayMetrics().density;
    private List<Optograph> optographs;

    protected ApiConsumer apiConsumer;
    private Cache cache;
    private Context context;
    private DBHelper mydb;

    private boolean isCurrentUser = false;
    private boolean draggingPage = false;

    public OptographVideoFeedAdapter(Context context) {
        this.context = context;
        this.optographs = new ArrayList<>();

        cache = Cache.open();

        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        mydb = new DBHelper(context);
    }

    @Override
    public OptographVideoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_feed_item, parent, false);
        return new OptographVideoHolder(view, context);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(OptographVideoHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            holder.getBinding().swipeLayout.close(true);
        }
    }

    @Override
    public void onBindViewHolder(OptographVideoHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Optograph optograph = optographs.get(position);

        if (!optograph.equals(holder.getBinding().getOptograph())) {
            int width = (int) ITEM_WIDTH;
            int height = (int)(((ITEM_WIDTH / 3) * 2) + (5 * DENSITY));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            holder.itemView.setLayoutParams(params);

            Timber.d("onBindViewHolder " + holder.itemView.getWidth() + " " + width + " " + height + " " + DENSITY);

                    holder.getBinding().frame.setOnClickListener(v -> callDetailsPage(optograph));
            holder.getBinding().videoView.setOnClickListener(v -> callDetailsPage(optograph));
            holder.getBinding().previewImage.setOnClickListener(v -> callDetailsPage(optograph));

            holder.getBinding().heartLabel.setTypeface(Constants.getInstance().getIconTypeface());
            holder.getBinding().heartLabel.setOnClickListener(v -> setHeart(optograph, holder, v));
            holder.getBinding().heartContainer.setOnClickListener(v -> { setHeart(optograph, holder, v); });

            isCurrentUser = optograph.getPerson().getId().equals(cache.getString(Cache.USER_ID));
            holder.getBinding().follow.setVisibility(isCurrentUser ? View.GONE : View.VISIBLE);
            holder.getBinding().follow.setOnClickListener(v -> followOrUnfollow(optograph, holder, v));
            holder.getBinding().followContainer.setVisibility(isCurrentUser ? View.GONE : View.VISIBLE);
            holder.getBinding().followContainer.setOnClickListener(v -> followOrUnfollow(optograph, holder, v));

            holder.getBinding().personLocationInformation.setOnClickListener(v -> startProfile(optograph.getPerson()));
            holder.getBinding().personAvatarAsset.setOnClickListener(v -> startProfile(optograph.getPerson()));

            updateHeartLabel(optograph, holder);
            followPerson(optograph, optograph.getPerson().is_followed(), holder);

            // setup sharing
            TextView settingsLabel = (TextView) holder.itemView.findViewById(R.id.settings_button);
            settingsLabel.setOnClickListener(v -> Snackbar.make(v, v.getResources().getString(R.string.feature_soon), Snackbar.LENGTH_SHORT).show());

            SwipeLayout swipeLayout = (SwipeLayout) holder.itemView.findViewById(R.id.swipe_layout);
            swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
            swipeLayout.setBottomSwipeEnabled(false);
            swipeLayout.setTopSwipeEnabled(false);
            swipeLayout.setRightSwipeEnabled(false);

            View shareButton = swipeLayout.findViewById(R.id.bottom_wrapper);
//            swipeLayout.addDrag(SwipeLayout.DragEdge.Right, shareButton);

            LinearLayout barSwipe = (LinearLayout) holder.itemView.findViewById(R.id.bar_swipe);

            swipeLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    ViewParent parent = v.getParent();

                    if(event.getAction() == MotionEvent.ACTION_UP) {
                        swipeLayout.close(true);
                    }
                    if(draggingPage) {
                        parent.requestDisallowInterceptTouchEvent(false);
                        return true;
                    } else {
                        parent.requestDisallowInterceptTouchEvent(true);
                        return false;
                    }

                }
            });

            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                    barSwipe.setVisibility(View.GONE);
                    //((MainActivity) context).setOptograph(optograph);
                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    ((MainActivity) context).setOptograph(optograph);
                    draggingPage = true;
                }

                @Override
                public void onStartClose(SwipeLayout layout) {}

                @Override
                public void onClose(SwipeLayout layout) {
                    barSwipe.setVisibility(View.VISIBLE);
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {}
            });

            barSwipe.setOnClickListener(v -> {
                swipeLayout.bounce(300, shareButton);
                ((MainActivity) context).setOptograph(optograph);
            });

            holder.getBinding().setVariable(BR.optograph, optograph);
            holder.getBinding().setVariable(BR.person, optograph.getPerson());
            holder.getBinding().setVariable(BR.location, optograph.getLocation());

            holder.getBinding().executePendingBindings();
        } else {
        }

    }

    @Nullable
    @Override
    protected Object getItem(int position) {
        return optographs.get(position);
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

    private void callDetailsPage(Optograph optograph) {
        Intent intent = new Intent(context, OptographDetailsActivity.class);
        intent.putExtra("opto", optograph);
        context.startActivity(intent);
    }

    private void followOrUnfollow(Optograph optograph, OptographVideoHolder holder, View v) {

        if (!cache.getString(Cache.USER_TOKEN).equals("")) {
            if (optograph.getPerson().is_followed()) {
                followPerson(optograph, false, holder);
                apiConsumer.unfollow(optograph.getPerson().getId(), new Callback<LogInReturn.EmptyResponse>() {
                    @Override
                    public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                        // revert follow count on failure
                        if (!response.isSuccess()) {
                            followPerson(optograph, true, holder);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        followPerson(optograph, true, holder);
                        Timber.e("Error on unfollowing.");
                    }
                });
            } else if (!optograph.getPerson().is_followed()) {
                followPerson(optograph, true, holder);
                apiConsumer.follow(optograph.getPerson().getId(), new Callback<LogInReturn.EmptyResponse>() {
                    @Override
                    public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                        // revert follow count on failure
                        if (!response.isSuccess()) {
                            followPerson(optograph, false, holder);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        followPerson(optograph, false, holder);
                        Timber.e("Error on following.");
                    }
                });
            }
        } else {
            Snackbar.make(v,context.getString(R.string.profile_login_first),Snackbar.LENGTH_SHORT).show();
        }
    }

    private void setHeart(Optograph optograph, OptographVideoHolder holder, View v) {

        if(!cache.getString(Cache.USER_TOKEN).equals("")) {
            if (!optograph.is_starred()) {
                mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, 1);
                optograph.setIs_starred(true);
                optograph.setStars_count(optograph.getStars_count() + 1);
                updateHeartLabel(optograph, holder);
                apiConsumer.postStar(optograph.getId(), new Callback<LogInReturn.EmptyResponse>() {
                    @Override
                    public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                        if (!response.isSuccess()) {
                            mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, 0);
                            optograph.setIs_starred(response.isSuccess());
                            optograph.setStars_count(optograph.getStars_count() - 1);
                            updateHeartLabel(optograph, holder);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, 0);
                        optograph.setIs_starred(false);
                        optograph.setStars_count(optograph.getStars_count() - 1);
                        updateHeartLabel(optograph, holder);
                    }
                });
            } else if (optograph.is_starred()) {
                mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, 0);
                optograph.setIs_starred(false);
                optograph.setStars_count(optograph.getStars_count() - 1);
                updateHeartLabel(optograph, holder);
                apiConsumer.deleteStar(optograph.getId(), new Callback<LogInReturn.EmptyResponse>() {
                    @Override
                    public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                        if (!response.isSuccess()) {
                            mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, 1);
                            optograph.setIs_starred(response.isSuccess());
                            optograph.setStars_count(optograph.getStars_count() + 1);
                            updateHeartLabel(optograph, holder);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, 1);
                        optograph.setIs_starred(true);
                        optograph.setStars_count(optograph.getStars_count() + 1);
                        updateHeartLabel(optograph, holder);
                    }
                });
            }
        } else {
            Snackbar.make(v, context.getString(R.string.profile_login_first), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void updateHeartLabel(Optograph optograph, OptographVideoHolder holder) {
        holder.getBinding().heartLabel.setText(String.valueOf(optograph.getStars_count()));
        if(optograph.is_starred()) {
            holder.getBinding().heartLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.liked_icn, 0);
        } else {
            holder.getBinding().heartLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.like_icn, 0);
        }
    }

    private void followPerson(Optograph optograph,boolean isFollowed, OptographVideoHolder holder) {
        if(isFollowed) {
            optograph.getPerson().setIs_followed(true);
            optograph.getPerson().setFollowers_count(optograph.getPerson().getFollowers_count() + 1);
            holder.getBinding().follow.setImageResource(R.drawable.feed_following_icn);
        } else {
            optograph.getPerson().setIs_followed(false);
            optograph.getPerson().setFollowers_count(optograph.getPerson().getFollowers_count() - 1);
            holder.getBinding().follow.setImageResource(R.drawable.feed_follow_icn);
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
            optographs.add(optographs.size(), optograph);
//            notifyItemInserted(getItemCount());
            notifyItemInserted(optographs.size() - 1);
            return;
        }

        // if optograph is oldest, simply append to list
        if (created_at != null && created_at.isBefore(getOldest().getCreated_atDateTime())) {
            optographs.add(optograph);
//            notifyItemInserted(getItemCount());
            notifyDataSetChanged();
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

    public void saveToSQLite(Optograph opto) {
        Cursor res = mydb.getData(opto.getId(),DBHelper.OPTO_TABLE_NAME,DBHelper.OPTOGRAPH_ID);
        res.moveToFirst();
        if (res.getCount()!=0) return;
        String loc = opto.getLocation()==null?"":opto.getLocation().getId();
        mydb.insertOptograph(opto.getId(),opto.getText(),opto.getPerson().getId(),opto.getLocation()==null?"":opto.getLocation().getId(),
                opto.getCreated_at(),opto.getDeleted_at()==null?"":opto.getDeleted_at(),opto.is_starred()?1:0,opto.getStars_count(),opto.is_published()?1:0,
                opto.is_private()?1:0,opto.getStitcher_version(),1,opto.is_on_server()?1:0,"",opto.isShould_be_published()?1:0,
                opto.is_place_holder_uploaded()?1:0,opto.isPostFacebook()?1:0,opto.isPostTwitter()?1:0,opto.isPostInstagram()?1:0,
                opto.is_data_uploaded()?1:0,opto.getOptograph_type());
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

    public void disableDraggingPage(int position) {
        this.draggingPage = false;
        notifyItemRangeChanged(position - 1, 3, "test");
    }
}
