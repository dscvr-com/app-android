package com.iam360.iam360.viewmodels;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.iam360.iam360.BR;
import com.iam360.iam360.GridFollowerBinding;
import com.iam360.iam360.GridItemLocalBinding;
import com.iam360.iam360.GridItemServerBinding;
import com.iam360.iam360.GridNotificationFollowBinding;
import com.iam360.iam360.GridNotificationStarBinding;
import com.iam360.iam360.ProfileHeaderBinding;
import com.iam360.iam360.ProfileTabBinding;
import com.iam360.iam360.R;
import com.iam360.iam360.model.Follower;
import com.iam360.iam360.model.LogInReturn;
import com.iam360.iam360.model.Notification;
import com.iam360.iam360.model.OptoData;
import com.iam360.iam360.model.OptoDataUpdate;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.network.PersonManager;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.CameraUtils;
import com.iam360.iam360.util.Constants;
import com.iam360.iam360.util.DBHelper;
import com.iam360.iam360.util.GeneralUtils;
import com.iam360.iam360.util.NotificationSender;
import com.iam360.iam360.util.RFC3339DateFormatter;
import com.iam360.iam360.util.TimeUtils;
import com.iam360.iam360.views.activity.MainActivity;
import com.iam360.iam360.views.activity.OptographDetailsActivity;
import com.iam360.iam360.views.activity.ProfileActivity;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Created by Mariel on 6/14/2016.
 */
public class OptographLocalGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_WIDTH= Constants.getInstance().getDisplayMetrics().widthPixels;
    public static final int VIEW_HEADER = 0;
    public static final int SECOND_HEADER = 1;
    public static final int VIEW_LOCAL = 2;
    public static final int VIEW_SERVER = 3;
    public static final int VIEW_FOLLOWER = 4;
    public static final int VIEW_NOTIFICATION_STAR = 5;
    public static final int VIEW_NOTIFICATION_FOLLOW = 6;
    public static final int VIEW_NOTIFICATION_COMMENT = 7;
    public static final int VIEW_NOTIFICATION_VIEWS = 8;
    public static final int ON_IMAGE=0;
    public static final int ON_FOLLOWER=1;
    public static final int ON_NOTIFICATION =2;
    public static final int PICK_IMAGE_REQUEST = 1;
    public static final int DELETE_IMAGE = 2;
    public static final int COLUMNS=3;

    public final static String optoType360 = "optograph";
    public final static String optoTypeTheta = "theta";

    public static final String NOTIF_STAR_TYPE = "star";
    public static final String NOTIF_FOLLOW_TYPE = "follow";
    public static final String NOTIF_COMMENT_TYPE = "comment";
    public static final String NOTIF_VIEWS_TYPE = "view";

    private String tab_text_active_color = "#ff7602";
    private String tab_text_inactive_color = "#ffffff";

    List<Optograph> optographs;
    List<Follower> followers;
    List<Notification> notifications;

    private Person person;
    protected Cache cache;

    private DBHelper mydb;

    protected ApiConsumer apiConsumer;
    private Context context;

    private int count;
    private boolean isCurrentUser=false;
    private boolean isEditMode = false;
    private boolean needSave = false;
    private boolean avatarChange = false;
    private boolean fromCancelEdit = false;

    private int onTab;
    private String follow, following;
    private String origPersonName, origPersonDesc;
    private String personName, personDesc;
    private Bitmap avatarImage;
    private String avatarId;
    private String message;

    public OptographLocalGridAdapter(Context context,int tab) {
        this.context = context;
        this.optographs = new ArrayList<>();
        this.onTab = tab;
        optographs.add(0,null);
        optographs.add(1,null);

        this.followers = new ArrayList<>();
//        followers.add(0,null);
//        followers.add(1,null);

        this.notifications = new ArrayList<Notification>();
        this.notifications.add(0,null);
        this.notifications.add(1,null);

        cache = Cache.open();
        mydb = new DBHelper(context);


        follow = context.getResources().getString(R.string.profile_follow);
        following = context.getResources().getString(R.string.profile_following);

        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("")?null:token);

        if (isTab(ON_NOTIFICATION)) setNotifications();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType==VIEW_HEADER) {
            final View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.grid_item_header, parent, false);

            final HeaderOneViewHolder viewHolder = new HeaderOneViewHolder(itemView);
            return viewHolder;
        } else if (viewType==SECOND_HEADER) {
            final View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.grid_item_tabs, parent, false);

            final HeaderSecondViewHolder viewHolder = new HeaderSecondViewHolder(itemView);
            return viewHolder;
        } else if (viewType==VIEW_LOCAL) {
            final View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.grid_item_local, parent, false);

            final LocalViewHolder viewHolder = new LocalViewHolder(itemView);
            return viewHolder;
        } else if (viewType == VIEW_SERVER) {
            final View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.grid_item_server, parent, false);

            final ServerViewHolder viewHolder = new ServerViewHolder(itemView);
            return viewHolder;
        } else if (viewType == VIEW_FOLLOWER) {
            final View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.grid_item_follower, parent, false);

            final FollowerViewHolder viewHolder = new FollowerViewHolder(itemView);
            return viewHolder;
        } else if (viewType==VIEW_NOTIFICATION_STAR) {
            final View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.grid_item_notification_star,parent,false);

            final NotificationStarViewHolder viewHolder = new NotificationStarViewHolder(itemView);
            return viewHolder;
        } else {
            final View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.grid_item_notification_follow,parent,false);

            final NotificationFollowViewHolder viewHolder = new NotificationFollowViewHolder(itemView);
            return viewHolder;
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (onTab==ON_IMAGE) {
            Optograph optograph = optographs.get(position);

            if (optograph == null && position == 0) {
                HeaderOneViewHolder mHolder1 = (HeaderOneViewHolder) holder;
                initializeHeaderOne(mHolder1);
            } else if (optograph == null && position == 1) {
                HeaderSecondViewHolder mHolder = (HeaderSecondViewHolder) holder;
                initializeHeaderSecond(mHolder);
            } else if (optograph.is_local()) {
                LocalViewHolder mHolder2 = (LocalViewHolder) holder;
                if (!optograph.equals(mHolder2.getBinding().getOptograph())) {
                    if (mHolder2.getBinding().getOptograph() != null) {

                    }

                    if (optograph.is_local()) count += 1;

                    GeneralUtils utils = new GeneralUtils();
                    utils.setFont(context, mHolder2.getBinding().uploadLocalBtn, Typeface.NORMAL);
//                    mHolder2.getBinding().uploadLocal.setVisibility(optograph.is_local() ? View.VISIBLE : View.GONE);
                    mHolder2.getBinding().uploadLocalBtn.setVisibility(optograph.is_local() ? View.VISIBLE : View.GONE);
                    mHolder2.getBinding().uploadProgressLocal.setVisibility(optograph.is_local() ? View.GONE : View.GONE);
                    mHolder2.getBinding().uploadLocalBtn.setText(optograph.isIs_uploading()?context.getString(R.string.profile_uploading):context.getString(R.string.profile_upload));

                    mHolder2.getBinding().optograph2dviewLocal.getLayoutParams().height = (ITEM_WIDTH) / 5;
                    mHolder2.getBinding().optograph2dviewLocal.getLayoutParams().width = (ITEM_WIDTH - 30) / 4;
                    mHolder2.getBinding().optograph2dviewLocal.requestLayout();

                    mHolder2.getBinding().textview.setText(String.valueOf(position) + " " + optograph.getText() + " star: " + optograph.getStars_count());

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ITEM_WIDTH, ViewGroup.LayoutParams.WRAP_CONTENT); //ITEM_WIDTH / OptographGridFragment.NUM_COLUMNS); // (width, height)
                    holder.itemView.setLayoutParams(params);

                    mHolder2.getBinding().setVariable(BR.optograph, optograph);
                    mHolder2.getBinding().executePendingBindings();

                    mHolder2.getBinding().optograph2dviewLocal.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            callDetailsPage(position);
                        }
                    });

                    mHolder2.getBinding().uploadLocalBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (optograph.isIs_uploading()) return;
                            if (cache.getString(Cache.USER_TOKEN).equals("")) {
                                Snackbar.make(v, "Must login to upload.", Snackbar.LENGTH_SHORT);
                            } else {
                                optograph.setIs_uploading(true);
                                apiConsumer = new ApiConsumer(cache.getString(Cache.USER_TOKEN));
                                mHolder2.getBinding().uploadProgressLocal.setVisibility(View.VISIBLE);
//                                mHolder2.getBinding().uploadLocal.setVisibility(View.GONE);
                                mHolder2.getBinding().uploadLocalBtn.setVisibility(View.GONE);
//                                mHolder2.getBinding().uploadLocalBtn.setText(context.getString(R.string.profile_uploading));
                                if (!optograph.is_data_uploaded()) {
                                    Log.d("myTag", "upload the data first. position: " + position);
                                    uploadOptonautData(position, mHolder2.getBinding().uploadProgressLocal);
                                } else if (!optograph.is_place_holder_uploaded()) {
                                    Log.d("myTag", "upload the placeholder first. position: " + position);
                                    uploadPlaceHolder(position, mHolder2.getBinding().uploadProgressLocal);
                                } else {
                                    Log.d("myTag", "upload the 12 images position: " + position);
                                    updateOptograph(position, mHolder2.getBinding().uploadProgressLocal);
//                            getLocalImage(optograph);
                                }
                            }
                        }
                    });
                }
            } else {
                ServerViewHolder mHolder3 = (ServerViewHolder) holder;
                if (!optograph.equals(mHolder3.getBinding().getOptograph())) {
                    if (mHolder3.getBinding().getOptograph() != null) {

                    }
//                mHolder3.getBinding().optograph2dviewServer.getLayoutParams().height = (ITEM_WIDTH - 30) / 4;
                    mHolder3.getBinding().optograph2dviewServer.getLayoutParams().width = (ITEM_WIDTH - 30) / COLUMNS;
                    mHolder3.getBinding().optograph2dviewServer.requestLayout();

                    mHolder3.getBinding().setVariable(BR.optograph, optograph);
                    mHolder3.getBinding().executePendingBindings();

                    mHolder3.getBinding().optograph2dviewServer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            callDetailsPage(position);
                        }
                    });
                }
            }
        } else if (onTab == ON_FOLLOWER) {
            Follower follower = followers.get(position);

            if (follower == null && position == 0) {
                HeaderOneViewHolder mHolder = (HeaderOneViewHolder) holder;
                initializeHeaderOne(mHolder);
            } else if (follower == null && position==1) {
                HeaderSecondViewHolder mHolder1 = (HeaderSecondViewHolder) holder;
                initializeHeaderSecond(mHolder1);
            } else {
                FollowerViewHolder mHolder2 = (FollowerViewHolder) holder;
                if (!follower.equals(mHolder2.getBinding().getFollower())) {
                    if (mHolder2.getBinding().getFollower()!=null) {

                    }

                    mHolder2.getBinding().setVariable(BR.follower, follower);
                    mHolder2.getBinding().executePendingBindings();

                    if (follower.is_followed()) mHolder2.getBinding().followUnfollowBtn.setBackgroundResource(R.drawable.following_btn);
                    else mHolder2.getBinding().followUnfollowBtn.setBackgroundResource(R.drawable.follow_btn);

                    mHolder2.getBinding().badgeLayout.setVisibility(follower.isElite_status()?View.VISIBLE:View.GONE);

                    mHolder2.getBinding().followUnfollowBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mHolder2.getBinding().getFollower().is_followed()) {
                                apiConsumer.unfollow(follower.getId(), new Callback<LogInReturn.EmptyResponse>() {
                                    @Override
                                    public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                                        if (response.isSuccess()) {
                                            mHolder2.getBinding().getFollower().setIs_followed(false);
                                            mHolder2.getBinding().getFollower().setFollowers_count(mHolder2.getBinding().getFollower().getFollowers_count() - 1);
                                            mHolder2.getBinding().invalidateAll();
                                            notifyItemChanged(position);

                                            Cursor res = mydb.getData(mHolder2.getBinding().getFollower().getId(), DBHelper.PERSON_TABLE_NAME, "id");
                                            res.moveToFirst();
                                            if (res.getCount() > 0) {
                                                mydb.updateTableColumn(DBHelper.PERSON_TABLE_NAME,"id", mHolder2.getBinding().getFollower().getId(), "is_followed", String.valueOf(false));
                                                mydb.updateTableColumn(DBHelper.PERSON_TABLE_NAME,"id", mHolder2.getBinding().getFollower().getId(), "followers_count", String.valueOf(mHolder2.getBinding().getFollower().getFollowers_count()));
                                            }
                                        }

                                    }

                                    @Override
                                    public void onFailure(Throwable t) {
                                        Log.d("myTag","Error unfollow: "+t.getMessage());
                                    }
                                });
                            } else {
                                apiConsumer.follow(follower.getId(), new Callback<LogInReturn.EmptyResponse>() {
                                    @Override
                                    public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                                        if (response.isSuccess()) {
                                            mHolder2.getBinding().getFollower().setIs_followed(true);
                                            mHolder2.getBinding().getFollower().setFollowers_count(mHolder2.getBinding().getFollower().getFollowers_count() + 1);
                                            mHolder2.getBinding().invalidateAll();
                                            if(position < optographs.size()){
                                                Optograph optograph = optographs.get(position);
                                                NotificationSender.triggerSendNotification(optograph.getPerson(), "follow");
                                                Cursor res = mydb.getData(optograph.getPerson().getId(), DBHelper.PERSON_TABLE_NAME, "id");
                                                res.moveToFirst();
                                                if (res.getCount() > 0) {
                                                    mydb.updateTableColumn(DBHelper.PERSON_TABLE_NAME,"id", optograph.getPerson().getId(), "is_followed", String.valueOf(true));
                                                    mydb.updateTableColumn(DBHelper.PERSON_TABLE_NAME,"id", optograph.getPerson().getId(), "followers_count", String.valueOf(optograph.getPerson().getFollowers_count()));
                                                }
                                            }
                                            notifyItemChanged(position);
                                        }

                                    }

                                    @Override
                                    public void onFailure(Throwable t) {
                                        Log.d("myTag","Error follow: "+t.getMessage());
                                    }
                                });
                            }
                        }
                    });

                    mHolder2.getBinding().followerItemLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startProfile(follower.getId());
                        }
                    });
                }
            }
        } else {
            Notification notif = notifications.get(position);

            if (notif == null && position == 0) {
                HeaderOneViewHolder mHolder = (HeaderOneViewHolder) holder;
                initializeHeaderOne(mHolder);
            } else if (notif == null && position ==1) {
                HeaderSecondViewHolder mHolder1 = (HeaderSecondViewHolder) holder;
                initializeHeaderSecond(mHolder1);
            } else if (notif.getType().equals(NOTIF_STAR_TYPE)) {
                NotificationStarViewHolder mHolder2 = (NotificationStarViewHolder) holder;
                if (!notif.equals(mHolder2.getBinding().getNotification())) {
                    if (mHolder2.getBinding().getNotification()!=null) {

                    }

                    if (notif.getDeleted_at()!=null) return;

                    mHolder2.getBinding().setVariable(BR.notification, notif);
                    mHolder2.getBinding().setVariable(BR.person, notif.getActivity_resource_star().getCausing_person());
                    mHolder2.getBinding().setVariable(BR.optograph, notif.getActivity_resource_star().getOptograph());
                    mHolder2.getBinding().executePendingBindings();

                    mHolder2.getBinding().starLayout.setBackgroundColor(notif.isIs_read()?context.getResources().getColor(R.color.profile_bg):context.getResources().getColor(R.color.profile_notif_not_read));
                    mHolder2.getBinding().badgeLayout.setVisibility(notif.getActivity_resource_star().getCausing_person().isElite_status()?View.VISIBLE:View.GONE);
                    mHolder2.getBinding().timeAgo.setText(TimeUtils.getTimeAgo(RFC3339DateFormatter.fromRFC3339String(notif.getCreated_at())));

                    mHolder2.getBinding().personUsername.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!notif.isIs_read()) {
                                notif.setIs_read(true);
                                setRead(notif);
                            }
                            startProfile(notif.getActivity_resource_star().getCausing_person());
                        }
                    });

                    mHolder2.getBinding().starLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!notif.isIs_read()) {
                                notif.setIs_read(true);
                                setRead(notif);
                            }
                            Optograph opto = notif.getActivity_resource_star().getOptograph();
                            opto.setPerson(person);
                            callDetailsPage(position);
                        }
                    });
                }
            } else if (notif.getType().equals(NOTIF_FOLLOW_TYPE)) {
                NotificationFollowViewHolder mHolder3 = (NotificationFollowViewHolder) holder;
                if (!notif.equals(mHolder3.getBinding().getNotification())) {
                    if (mHolder3.getBinding().getNotification()!=null) {

                    }

                    if (notif.getDeleted_at()!=null) return;

                    mHolder3.getBinding().setVariable(BR.notification, notif);
                    mHolder3.getBinding().setVariable(BR.person, notif.getActivity_resource_follow().getCausing_person());
                    mHolder3.getBinding().executePendingBindings();

                    mHolder3.getBinding().followLayout.setBackgroundColor(notif.isIs_read() ? context.getResources().getColor(R.color.profile_bg) : context.getResources().getColor(R.color.profile_notif_not_read));
                    mHolder3.getBinding().badgeLayout.setVisibility(notif.getActivity_resource_follow().getCausing_person().isElite_status() ? View.VISIBLE : View.GONE);
                    mHolder3.getBinding().timeAgo.setText(TimeUtils.getTimeAgo(RFC3339DateFormatter.fromRFC3339String(notif.getCreated_at())));
                    mHolder3.getBinding().followNotif.setImageResource(notif.getActivity_resource_follow().getCausing_person().is_followed() ? R.drawable.feed_following_icn : R.drawable.feed_follow_icn);

                    mHolder3.getBinding().followLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!notif.isIs_read()) {
                                notif.setIs_read(true);
                                setRead(notif);
                            }
                            startProfile(notif.getActivity_resource_follow().getCausing_person());
                        }
                    });
                }
            }
        }
    }

    private void callDetailsPage(int position) {
        Intent intent = new Intent(context, OptographDetailsActivity.class);
        intent.putParcelableArrayListExtra("opto_list", getNextOptographList(position, 5));
        if(context instanceof ProfileActivity)
            ((ProfileActivity) context).startActivityForResult(intent, DELETE_IMAGE);
        else if(context instanceof MainActivity)
            ((MainActivity) context).startActivityForResult(intent, DELETE_IMAGE);

    }

    private ArrayList<Optograph> getNextOptographList(int position, int count) {
        int optoListCount = optographs.size();
        count = (count < optoListCount) ? count : optoListCount;

        ArrayList<Optograph> optographList = new ArrayList<Optograph>();

        for(int i = 0; i < count; i++) {
            optographList.add(optographs.get((position) % optoListCount));
            position++;
        }

        return optographList;
    }

    private void setRead(Notification notif) {
        apiConsumer.setNotificationToRead(notif.getId(), new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                Log.d("myTag"," onResponse setNotificationToRead success? "+response.isSuccess());
                if (response.isSuccess()) notif.setIs_read(true);
                else notif.setIs_read(false);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag"," onFailure setNotificationToRead message: "+t.getMessage());
                notif.setIs_read(false);
            }
        });
    }

    private void startProfile(Person person) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra("person", person);
        context.startActivity(intent);
    }

    private void startProfile(String id) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra("id", id);
        context.startActivity(intent);
    }

    private void setTab(HeaderSecondViewHolder mHolder) {
        if (onTab==ON_IMAGE) {
            mHolder.getBinding().imageText.setTextColor(Color.parseColor(tab_text_active_color));
            mHolder.getBinding().imageSelector.setVisibility(View.VISIBLE);
            mHolder.getBinding().followerText.setTextColor(Color.parseColor(tab_text_inactive_color));
            mHolder.getBinding().followerSelector.setVisibility(View.INVISIBLE);
            mHolder.getBinding().notificationText.setTextColor(Color.parseColor(tab_text_inactive_color));
            mHolder.getBinding().notificationSelector.setVisibility(View.INVISIBLE);
        } else if (onTab == ON_FOLLOWER) {
            mHolder.getBinding().imageText.setTextColor(Color.parseColor(tab_text_inactive_color));
            mHolder.getBinding().imageSelector.setVisibility(View.INVISIBLE);
            mHolder.getBinding().followerText.setTextColor(Color.parseColor(tab_text_active_color));
            mHolder.getBinding().followerSelector.setVisibility(View.VISIBLE);
            mHolder.getBinding().notificationText.setTextColor(Color.parseColor(tab_text_inactive_color));
            mHolder.getBinding().notificationSelector.setVisibility(View.INVISIBLE);
        } else {
            mHolder.getBinding().imageText.setTextColor(Color.parseColor(tab_text_inactive_color));
            mHolder.getBinding().imageSelector.setVisibility(View.INVISIBLE);
            mHolder.getBinding().followerText.setTextColor(Color.parseColor(tab_text_inactive_color));
            mHolder.getBinding().followerSelector.setVisibility(View.INVISIBLE);
            mHolder.getBinding().notificationText.setTextColor(Color.parseColor(tab_text_active_color));
            mHolder.getBinding().notificationSelector.setVisibility(View.VISIBLE);
        }
    }

    public boolean isTab(int tab) {
        if (onTab==tab) return true;
        return false;
    }

    public void setMessage(String message) {
        this.message = message;
        updateMenuOptions();
    }

    private void initializeHeaderSecond(HeaderSecondViewHolder mHolder) {

        setTab(mHolder);

        if (!isCurrentUser && onTab == ON_IMAGE) {
            mHolder.getBinding().followerTab.setVisibility(View.GONE);
            mHolder.getBinding().notificationTab.setVisibility(View.GONE);
            mHolder.getBinding().imageText.setTextColor(Color.parseColor(tab_text_inactive_color));
            mHolder.getBinding().imageSelector.setVisibility(View.INVISIBLE);
        } else if (onTab == ON_IMAGE){
            mHolder.getBinding().followerTab.setVisibility(View.VISIBLE);
            mHolder.getBinding().notificationTab.setVisibility(View.VISIBLE);
            mHolder.getBinding().imageText.setTextColor(Color.parseColor(tab_text_active_color));
            mHolder.getBinding().imageSelector.setVisibility(View.VISIBLE);
        }

        mHolder.getBinding().notificationTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTab = ON_NOTIFICATION;
                setTab(mHolder);
                setNotifications();
            }
        });
        mHolder.getBinding().followerTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTab = ON_FOLLOWER;
                setTab(mHolder);
                followers = new ArrayList<Follower>();
                followers.add(0, null);
                followers.add(1, null);
                notifyDataSetChanged();
                setMessage("");
//                apiConsumer.getFollowers()
//                        .subscribeOn(Schedulers.newThread())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .onErrorReturn(throwable -> {
//                            if (throwable.getMessage().contains("iterable must not be null")) {
//                                Toast.makeText(context, "You have no Follower.", Toast.LENGTH_LONG).show();
//                            } else {
//                                Toast.makeText(context, "Network Problem", Toast.LENGTH_SHORT).show();
//                            }
//                            return null;
//                        })
//                        .subscribe(OptographLocalGridAdapter.this::addItem);
                apiConsumer.getFollowersCall(new Callback<List<Follower>>() {
                    @Override
                    public void onResponse(Response<List<Follower>> response, Retrofit retrofit) {
                        if (response.isSuccess() && response.body() != null) {
                            followers = response.body();
                            followers.add(0, null);
                            followers.add(1, null);
                            notifyDataSetChanged();
                        } else {
                            notifyDataSetChanged();
//                            message = context.getResources().getString(R.string.profile_no_follower);
//                            updateMenuOptions();
//                            Toast.makeText(context, "You have no Follower.", Toast.LENGTH_LONG).show();
                        }
                        if (getItemCount()-2==0 && isTab(ON_FOLLOWER))
                            setMessage(context.getResources().getString(R.string.profile_no_follower));
                        else if (isTab(ON_FOLLOWER)) setMessage("");
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        notifyDataSetChanged();
                        setMessage(context.getResources().getString(R.string.profile_net_prob));
//                        Toast.makeText(context, "Network Problem", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        mHolder.getBinding().imageTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCurrentUser) return;
                onTab = ON_IMAGE;
                setTab(mHolder);
                if (context instanceof MainActivity) ((MainActivity) context).refresh();
                notifyDataSetChanged();
                Log.d("myTag"," setMessage: itemcount: "+getItemCount()+" MainActivity? "+(context instanceof MainActivity));
                setMessage(getItemCount()-2==0?context.getResources().getString(R.string.profile_no_image):"");
            }
        });
    }

    private void setNotifications() {
        notifications = new ArrayList<Notification>();
        notifications.add(0,null);
        notifications.add(1,null);
        notifyDataSetChanged();
        setMessage("");
        apiConsumer.getNotifications(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Response<List<Notification>> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body() != null) {
                    List<Notification> notifs = response.body();
                    notifications = new ArrayList<Notification>();
                    notifications.add(0, null);
                    notifications.add(1,null);
                    for (Notification not : notifs) {
                        if (not.getDeleted_at()==null) notifications.add(not);
                    }
                    notifyDataSetChanged();
                } else {
                    notifyDataSetChanged();
//                            Toast.makeText(context, "You have no Notification.", Toast.LENGTH_LONG).show();
                }
                if (getItemCount()-2==0 && isTab(ON_NOTIFICATION))
                    setMessage(context.getResources().getString(R.string.profile_no_notif));
                else if (isTab(ON_NOTIFICATION)) setMessage("");
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag"," notif: onFailure message."+t.getMessage());
                notifyDataSetChanged();
                setMessage(context.getResources().getString(R.string.profile_net_prob));
//                        Toast.makeText(context,"Network Problem",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeHeaderOne(HeaderOneViewHolder mHolder1) {
        mHolder1.getBinding().setVariable(BR.person, person);
        mHolder1.getBinding().executePendingBindings();
        if (avatarChange) {
            if (avatarImage!=null) {
                mHolder1.getBinding().personAvatarAsset.setImageBitmap(avatarImage);
                mHolder1.getBinding().getPerson().setAvatar_asset_id(avatarId);
            }
            avatarChange = false;
        }

        if (fromCancelEdit) {
            mHolder1.getBinding().getPerson().setUser_name(origPersonName);
            mHolder1.getBinding().getPerson().setText(origPersonDesc);
            mHolder1.getBinding().personName.setText(origPersonName);
            mHolder1.getBinding().personDesc.setText(origPersonDesc);
            mHolder1.getBinding().personNameEdit.setText(origPersonName);
            mHolder1.getBinding().personDescEdit.setText(origPersonDesc);
            fromCancelEdit = false;
        }

        if (needSave) {
            mHolder1.getBinding().personName.setText(mHolder1.getBinding().personNameEdit.getText().toString());
            mHolder1.getBinding().personDesc.setText(mHolder1.getBinding().personDescEdit.getText().toString());
            mHolder1.getBinding().getPerson().setText(mHolder1.getBinding().personDescEdit.getText().toString());
            mHolder1.getBinding().getPerson().setUser_name(mHolder1.getBinding().personNameEdit.getText().toString());
            PersonManager.updatePerson(mHolder1.getBinding().personNameEdit.getText().toString(), mHolder1.getBinding().personDescEdit.getText().toString(), mHolder1.getBinding().personNameEdit.getText().toString());
            needSave=false;
        }

//        mHolder1.getBinding().executePendingBindings();
        if (person==null) {
            mHolder1.getBinding().editBtn.setVisibility(View.GONE);
            mHolder1.getBinding().personIsFollowed.setVisibility(View.GONE);
        } else if (!isCurrentUser) {
            mHolder1.getBinding().editBtn.setVisibility(View.GONE);
            mHolder1.getBinding().personIsFollowed.setVisibility(View.VISIBLE);
            mHolder1.getBinding().personIsFollowed.setBackgroundResource((person!=null && person.is_followed())?R.drawable.following_btn:R.drawable.follow_btn);
        }

        if (isCurrentUser && isEditMode) {
            mHolder1.getBinding().editBtn.setVisibility(View.GONE);
            mHolder1.getBinding().personDesc.setVisibility(View.INVISIBLE);
            mHolder1.getBinding().personDescEdit.setVisibility(View.VISIBLE);
            // siince edit username is not applicable
//            mHolder1.getBinding().personName.setVisibility(View.INVISIBLE);
//            mHolder1.getBinding().personNameEdit.setVisibility(View.VISIBLE);
        } else if (isCurrentUser) {
            if(context instanceof MainActivity) mHolder1.getBinding().editBtn.setVisibility(View.VISIBLE);
            else mHolder1.getBinding().editBtn.setVisibility(View.GONE);

            mHolder1.getBinding().personDesc.setVisibility(View.VISIBLE);
            mHolder1.getBinding().personDescEdit.setVisibility(View.INVISIBLE);
//            mHolder1.getBinding().personName.setVisibility(View.VISIBLE);
//            mHolder1.getBinding().personNameEdit.setVisibility(View.INVISIBLE);
            mHolder1.getBinding().personIsFollowed.setVisibility(View.GONE);
        }

        mHolder1.getBinding().personNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                personName = s.toString();
                mHolder1.getBinding().getPerson().setUser_name(s.toString());
            }
        });

        mHolder1.getBinding().personDescEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("myTag", "needSave beforeTextChanged Desc " + s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("myTag", "needSave onTextChanged Desc " + s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                mHolder1.getBinding().getPerson().setText(s.toString());
//                personDesc = s.toString();
                Log.d("myTag", "needSave afterTextChanged personDesc "+personDesc);
            }
        });

        mHolder1.getBinding().editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCurrentUser) {
                    mHolder1.getBinding().personDesc.setVisibility(View.INVISIBLE);
                    mHolder1.getBinding().personDescEdit.setVisibility(View.VISIBLE);
//                    mHolder1.getBinding().personName.setVisibility(View.INVISIBLE);
//                    mHolder1.getBinding().personNameEdit.setVisibility(View.VISIBLE);
                    origPersonName = mHolder1.getBinding().getPerson().getDisplay_name();
//                    mHolder1.getBinding().personNameEdit.setVisibility(View.VISIBLE);
                    origPersonName = mHolder1.getBinding().getPerson().getUser_name();
                    origPersonDesc = mHolder1.getBinding().getPerson().getText();
                    isEditMode = true;
                    updateMenuOptions();
                    mHolder1.getBinding().editBtn.setVisibility(View.GONE);
                }
            }
        });

        mHolder1.getBinding().personIsFollowed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHolder1.getBinding().getPerson().is_followed()) {
                    apiConsumer.unfollow(person.getId(), new Callback<LogInReturn.EmptyResponse>() {
                        @Override
                        public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                            if(response.isSuccess()){
                                mHolder1.getBinding().getPerson().setIs_followed(false);
                                mHolder1.getBinding().getPerson().setFollowers_count(mHolder1.getBinding().getPerson().getFollowers_count()-1);
                                mHolder1.getBinding().invalidateAll();

                                Cursor res = mydb.getData(mHolder1.getBinding().getPerson().getId(), DBHelper.PERSON_TABLE_NAME, "id");
                                res.moveToFirst();
                                if (res.getCount() > 0) {
                                    mydb.updateTableColumn(DBHelper.PERSON_TABLE_NAME,"id", mHolder1.getBinding().getPerson().getId(), "is_followed", String.valueOf(false));
                                    mydb.updateTableColumn(DBHelper.PERSON_TABLE_NAME,"id", mHolder1.getBinding().getPerson().getId(), "followers_count", String.valueOf(mHolder1.getBinding().getPerson().getFollowers_count()));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Log.d("myTag","Error unfollow: "+t.getMessage());
                        }
                    });
                } else {
                    apiConsumer.follow(person.getId(), new Callback<LogInReturn.EmptyResponse>() {
                        @Override
                        public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                            if(response.isSuccess()) {
                                mHolder1.getBinding().getPerson().setIs_followed(true);
                                mHolder1.getBinding().getPerson().setFollowers_count(mHolder1.getBinding().getPerson().getFollowers_count() + 1);
                                mHolder1.getBinding().invalidateAll();
                                NotificationSender.triggerSendNotification(mHolder1.getBinding().getPerson(), "follow");

                                Cursor res = mydb.getData(mHolder1.getBinding().getPerson().getId(), DBHelper.PERSON_TABLE_NAME, "id");
                                res.moveToFirst();
                                if (res.getCount() > 0) {
                                    mydb.updateTableColumn(DBHelper.PERSON_TABLE_NAME,"id", mHolder1.getBinding().getPerson().getId(), "is_followed", String.valueOf(true));
                                    mydb.updateTableColumn(DBHelper.PERSON_TABLE_NAME,"id", mHolder1.getBinding().getPerson().getId(), "followers_count", String.valueOf(mHolder1.getBinding().getPerson().getFollowers_count()));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Log.d("myTag", "Error follow: " + t.getMessage());
                        }
                    });
                }
            }
        });

        mHolder1.getBinding().personAvatarAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    Intent intent = new Intent();
                    // Show only images, no vieos or anything else
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    // Always show the chooser (if there are multiple options available)
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                    }
                }
            }
        });

    }

    public void refreshAfterDelete(String id, boolean isLocal) {
        for (Optograph opto:optographs) {
            if (opto!=null && opto.getId().equals(id) && opto.is_local()==isLocal) {
                int position = optographs.indexOf(opto);
                optographs.remove(opto);
                notifyItemRemoved(position);
                return;
            }
        }
    }

    public void avatarUpload(Bitmap bitmap) {
        String avatar = UUID.randomUUID().toString();
        avatarChange = true;
        avatarImage = bitmap;
        avatarId = avatar;
        notifyItemChanged(0);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bos);
        byte[] data = bos.toByteArray();

        RequestBody fbody = RequestBody.create(MediaType.parse("image/jpeg"), data);
        RequestBody fbodyMain = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("avatar_asset", "avatar.jpg", fbody)
                .addFormDataPart("avatar_asset_id", avatar)
                .build();

        Timber.d("Avatar " + avatar);
        apiConsumer.uploadAvatar(fbodyMain, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                Timber.d("Response : " + response.message());
//                binding.getPerson().setAvatar_asset_id(avatar);
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("OnFailure: " + t.getMessage());
            }
        });
    }

    //    @Override
//    public void onBindViewHolder(LocalViewHolder holder, int position) {
//        Optograph optograph = optographs.get(position);
//
//        Log.d("myTag","position: "+position);
//        if (!optograph.equals(holder.getBinding().getOptograph())) {
//            if (holder.getBinding().getOptograph() != null) {
//
//            }
//
//            if (optograph.is_local()) count+=1;
//
//            holder.getBinding().uploadLocal.setVisibility(optograph.is_local()?View.VISIBLE:View.GONE);
//            holder.getBinding().uploadProgressLocal.setVisibility(optograph.is_local()?View.GONE:View.GONE);
//
////                holder.getBinding().optograph2dviewLocal.getLayoutParams().height = (ITEM_WIDTH-30) / 4;
//                holder.getBinding().optograph2dviewLocal.getLayoutParams().width = (ITEM_WIDTH-30) / 4;
//                holder.getBinding().optograph2dviewLocal.requestLayout();
//
//            holder.getBinding().textview.setText(String.valueOf(position) + " " + optograph.getText() + " star: " + optograph.getStars_count());
//
//            Log.d("myTag","adapter image: "+ITEM_WIDTH/4+" local opto count: "+count+" position: "+position);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ITEM_WIDTH, ViewGroup.LayoutParams.WRAP_CONTENT); //ITEM_WIDTH / OptographGridFragment.NUM_COLUMNS); // (width, height)
//            holder.itemView.setLayoutParams(params);
//
//            holder.getBinding().setVariable(BR.optograph, optograph);
//            holder.getBinding().executePendingBindings();
//
//            holder.getBinding().optograph2dviewLocal.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(context, OptographDetailsActivity.class);
//                    intent.putExtra("opto", optograph);
//                    context.startActivity(intent);
//                }
//            });
//
//            holder.getBinding().uploadLocal.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    if (cache.getString(Cache.USER_TOKEN).equals("")) {
//                        Snackbar.make(v, "Must login to upload.", Snackbar.LENGTH_SHORT);
//                    } else {
//                        apiConsumer = new ApiConsumer(cache.getString(Cache.USER_TOKEN));
//                        holder.getBinding().uploadProgressLocal.setVisibility(View.VISIBLE);
//                        holder.getBinding().uploadLocal.setVisibility(View.GONE);
//                        if (!optograph.is_data_uploaded()) {
//                            Log.d("myTag", "upload the data first. position: " + position);
//                            uploadOptonautData(position);
//                        } else if (!optograph.is_place_holder_uploaded()) {
//                            Log.d("myTag", "upload the placeholder first. position: " + position);
//                            uploadPlaceHolder(position);
//                        } else {
//                            Log.d("myTag", "upload the 12 images position: " + position);
//                            updateOptograph(position);
////                            getLocalImage(optograph);
//                        }
//                    }
//                }
//            });
//
////            holder.getBinding().uploadLocal.setVisibility(View.GONE);
////            holder.getBinding().uploadProgressLocal.setVisibility(View.GONE);
////            holder.getBinding().optograph2dviewLocal.setVisibility(View.INVISIBLE);
//        }
//    }

    public void saveUpdate() {
        needSave = true;
        avatarImage = null;
        avatarId = null;
        isEditMode = false;
        notifyItemChanged(0);
    }

    public boolean isOnEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean mode) {
        fromCancelEdit = true;
        isEditMode = mode;
        notifyItemChanged(0);
    }

    public String getMessage() {
        return message;
    }

    private void updateMenuOptions() {
        if (context instanceof MainActivity) {
            ((MainActivity) context).invalidateOptionsMenu();
        }
    }

    private void uploadOptonautData(int position, ProgressBar progressBar) {
        Optograph optograph = optographs.get(position);
        OptoData data = new OptoData(optograph.getId(), optograph.getStitcher_version(), optograph.getCreated_atRFC3339(),optograph.getOptograph_type(), Constants.PLATFORM+" "+Build.VERSION.RELEASE, Build.MODEL,Build.MANUFACTURER);
        apiConsumer.uploadOptoData(data, new Callback<Optograph>() {
            @Override
            public void onResponse(Response<Optograph> response, Retrofit retrofit) {
                if (!response.isSuccess()) {
                    Toast.makeText(context, "Failed to upload.", Toast.LENGTH_SHORT).show();
                    optograph.setIs_uploading(false);
                    notifyItemChanged(position);
                    return;
                }
                Optograph opto = response.body();
                if (opto == null) {
                    Toast.makeText(context, "Failed to upload.", Toast.LENGTH_SHORT).show();
                    optograph.setIs_uploading(false);
                    notifyItemChanged(position);
                    return;
                }
                // do things for success
                uploadPlaceHolder(position, progressBar);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag", " onFailure: " + t.getMessage());
                Toast.makeText(context, "Failed to upload.", Toast.LENGTH_SHORT).show();
                optograph.setIs_uploading(false);
                notifyItemChanged(position);
            }
        });

        Cursor res = mydb.getData(optograph.getId(), DBHelper.OPTO_TABLE_NAME, DBHelper.OPTOGRAPH_ID);
        if (res==null || res.getCount()==0) return;
        res.moveToFirst();
    }

    private void uploadPlaceHolder(int position, ProgressBar progressBar) {
        Optograph opto = optographs.get(position);
        Log.d("myTag", "Path: " + CameraUtils.PERSISTENT_STORAGE_PATH + opto.getId());
        File dir = new File(CameraUtils.PERSISTENT_STORAGE_PATH + opto.getId());

        String holder = "";

        if (dir.exists()) {// remove the not notation here
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory() && file.getName().equals("preview")) {
                    for (String s : file.list()) {
                        Log.d("myTag"," placeholder path to upload.");
                        holder = file.getPath()+"/"+s+"^"+position;
                        break;
                    }
                } else {
                    // ignore
                }
            }
        }

        UploadPlaceHolder uploadPlaceHolderTask = new UploadPlaceHolder();
        uploadPlaceHolderTask.setProgressBar(progressBar);
        uploadPlaceHolderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, holder);

//        new UploadPlaceHolder().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, holder);
    }

    class UploadPlaceHolder extends AsyncTask<String,Void,Void> {
        int mPosition = 0;
        ProgressBar progressBar;

        public void setProgressBar(ProgressBar progressBar) {
            this.progressBar = progressBar;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            for (String s : params) {
                String[] s1 = s.split("^");
                mPosition = Integer.valueOf(s1[1]);
                String[] s3 = s1[0].split("/");
                String face = s3[s3.length - 1];

                uploadPlaceHolderImage(mPosition, s1[0], face);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateOptograph(mPosition, progressBar);
        }
    }

    private int flag=2;
    private boolean uploadPlaceHolderImage(int position, String filePath,String fileName) {
        flag = 2;
        Optograph opto = optographs.get(position);
//        String[] s2 = filePath.split("/");
//        String fileName = s2[s2.length-1];

        Bitmap bm = null;

        try {
            bm = BitmapFactory.decodeFile(filePath);
        } catch (Exception e) {
            Log.e(e.getClass().getName(), e.getMessage());
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 75, bos);
        byte[] data = bos.toByteArray();

        RequestBody fbody = RequestBody.create(MediaType.parse("image/jpeg"), data);
        RequestBody fbodyMain = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("asset", fileName, fbody)
                .addFormDataPart("key", fileName.replace(".jpg",""))
                .build();
        apiConsumer.uploadOptoImage(opto.getId(), fbodyMain, optoType360, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                flag = response.isSuccess() ? 1 : 0;
                opto.setIs_place_holder_uploaded(response.isSuccess());
                mydb.updateColumnOptograph(opto.getId(), DBHelper.OPTOGRAPH_IS_PLACEHOLDER_UPLOADED, flag);
                opto.setIs_place_holder_uploaded(response.isSuccess());
                Toast.makeText(context, "Failed to upload.", Toast.LENGTH_SHORT).show();
                opto.setIs_uploading(false);
                notifyItemChanged(position);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag", "onFailure uploadImage: " + t.getMessage());
                flag = 0;
                Toast.makeText(context, "Failed to upload.", Toast.LENGTH_SHORT).show();
                opto.setIs_uploading(false);
                notifyItemChanged(position);
            }
        });
        while (flag==2) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        return (flag == 1);
    }

    private void updateOptograph(int position, ProgressBar progressBar) {
        Optograph opto = optographs.get(position);
        OptoDataUpdate data = new OptoDataUpdate(opto.getText(),opto.is_private(),opto.is_published(),opto.isPostFacebook(),opto.isPostTwitter());
        apiConsumer.updateOptoData(opto.getId(), data, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
//                Log.d("myTag", " onResponse isSuccess: " + response.isSuccess());
//                Log.d("myTag", " onResponse body: " + response.body());
//                Log.d("myTag", " onResponse message: " + response.message());
//                Log.d("myTag", " onResponse raw: " + response.raw().toString());
                if (!response.isSuccess()) {
                    Log.d("myTag", "response errorBody: " + response.errorBody());
                    Toast.makeText(context, "Failed to upload.", Toast.LENGTH_SHORT).show();
                    cache.save(Cache.UPLOAD_ON_GOING, false);
                    opto.setIs_uploading(false);
                    notifyItemChanged(position);
                    return;
                }
                getLocalImage(position, progressBar);
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(context, "No Internet Connection.", Toast.LENGTH_SHORT).show();
                cache.save(Cache.UPLOAD_ON_GOING, false);
                opto.setIs_uploading(false);
                notifyItemChanged(position);
            }
        });
    }

    private void getLocalImage(int position, ProgressBar progressBar) {
        Optograph opto = optographs.get(position);
        cache.save(Cache.UPLOAD_ON_GOING, true);
        Log.d("myTag", "Path: " + CameraUtils.PERSISTENT_STORAGE_PATH + opto.getId());
        File dir = new File(CameraUtils.PERSISTENT_STORAGE_PATH + opto.getId());

        List<String> filePathList = new ArrayList<>();
        filePathList.add(String.valueOf(position));

        if (dir.exists()) {// remove the not notation here
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory() && !file.getName().contains("preview")) {
                    Log.d("myTag", "getName: " + file.getName() + " getPath: " + file.getPath());

                    for (String s : file.list()) {
                        filePathList.add(file.getPath()+"/"+s);
                    }
                } else {
                    // ignore
                }
            }
        }

        UploadCubeImages uploadCubeImagesTask = new UploadCubeImages();
        uploadCubeImagesTask.setProgressBar(progressBar);
        uploadCubeImagesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filePathList);
//        new UploadCubeImages().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,filePathList);
    }

    // try using AbstractQueuedSynchronizer
    class UploadCubeImages extends AsyncTask<List<String>,Integer,Void> {
        int mPosition = 0;
        Optograph optograph;
        ProgressBar progressBar;

        public void setProgressBar(ProgressBar progressBar) {
            this.progressBar = progressBar;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setProgress(5);
        }

        @Override
        protected Void doInBackground(List<String>... params) {
            int faceCount = 1;
            for (List<String> sL : params) {
                int ctr = 0;
                for (String s:sL) {
                    if (ctr==0) {
                        mPosition = Integer.valueOf(s);
                        optograph = optographs.get(mPosition);
                        ctr++;
                        continue;
                    }
                    String[] s3 = s.split("/");
                    int side = Integer.valueOf((s3[s3.length - 1].split("\\."))[0]);
                    String face = s.contains("right") ? "r" : "l";

                    if (face.equals("l") && optograph.getLeftFace().getStatus()[side]) {
                        //pass
                    } else if (optograph.getRightFace().getStatus()[side]) {
                        //pass
                    } else if (!cache.getString(Cache.USER_TOKEN).equals("")) {
                        if (apiConsumer==null) apiConsumer = new ApiConsumer(cache.getString(Cache.USER_TOKEN));
                        uploadImage(mPosition, s, face, side);
                        int percentage = (int)(((double)faceCount++ / 12) * 100);
                        optograph.setUploadPercentage(percentage);
                        publishProgress(percentage);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if(progressBar != null) {
                progressBar.setProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Cursor res = mydb.getData(optograph.getId(), DBHelper.FACES_TABLE_NAME, DBHelper.FACES_ID);
            if (res==null || res.getCount()==0) return;
            res.moveToFirst();
            /*if (mydb.checkIfAllImagesUploaded(optoUpload.getId())) {
                mydb.deleteEntry(DBHelper.FACES_TABLE_NAME,DBHelper.FACES_ID,optoUpload.getId());
                mydb.deleteEntry(DBHelper.OPTO_TABLE_NAME,DBHelper.OPTOGRAPH_ID,optoUpload.getId());
            }*/
            optograph.setIs_uploading(false);
            cache.save(Cache.UPLOAD_ON_GOING, false);
            if (mydb.checkIfAllImagesUploaded(optograph.getId())) {
                mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_ON_SERVER, 1);
                optograph.setIs_on_server(true);
                optographs.remove(optograph);
                notifyItemRemoved(mPosition);
//                optograph.setIs_local(true);
//                optographs.add(,optograph);
//                notifyItemInserted();
                if (context instanceof ProfileActivity) ((ProfileActivity) context).refresh();
                else if (context instanceof MainActivity) ((MainActivity) context).refresh();
            } else {
                notifyItemChanged(mPosition);
            }
        }
    }

    private boolean uploadImage(int position, String filePath,String face,int side) {
        Optograph opto = optographs.get(position);
        flag = 2;
        String[] s2 = filePath.split("/");
        String fileName = s2[s2.length-1];

        if (face.equals("l") && opto.getLeftFace().getStatus()[side]) return true;
        else if (opto.getRightFace().getStatus()[side]) return true;

        Bitmap bm = null;

        try {
            bm = BitmapFactory.decodeFile(filePath);
        } catch (Exception e) {
            Log.e(e.getClass().getName(), e.getMessage());
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 75, bos);
        byte[] data = bos.toByteArray();

        RequestBody fbody = RequestBody.create(MediaType.parse("image/jpeg"), data);
        RequestBody fbodyMain = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("asset", face + fileName, fbody)
                .addFormDataPart("key", face + side)
                .build();
        Log.d("myTag","asset: "+face+fileName+" key: "+face+ fileName.replace(".jpg",""));
        apiConsumer.uploadOptoImage(opto.getId(), fbodyMain, optoType360, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
//                Log.d("myTag", "onResponse uploadImage isSuccess? " + response.isSuccess());
//                Log.d("myTag", "onResponse message: " + response.message());
//                Log.d("myTag", "onResponse body: " + response.body());
//                Log.d("myTag", "onResponse raw: " + response.raw());
                if (face.equals("l"))
                    opto.getLeftFace().setStatusByIndex(side, response.isSuccess());
                else opto.getRightFace().setStatusByIndex(side, response.isSuccess());
                updateFace(opto, face, side, response.isSuccess() ? 1 : 0);

                flag = response.isSuccess() ? 1 : 0;
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag", "onFailure uploadImage: " + t.getMessage());
                if (face.equals("l")) opto.getLeftFace().setStatusByIndex(side, false);
                else opto.getRightFace().setStatusByIndex(side, false);
//                notifyItemRangeChanged(position,1);
                opto.setIs_uploading(false);
                notifyItemChanged(position);
                flag = 0;
            }
        });
        while (flag==2) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        return (flag == 1);
    }

    private void updateFace(Optograph opto, String face, int side, int value) {
        String column = "faces_";
        if (face.equals("l")) column +="left_";
        else column += "right_";

        if (side == 0) column += "zero";
        else if (side == 1) column += "one";
        else if (side == 2) column += "two";
        else if (side == 3) column += "three";
        else if (side == 4) column += "four";
        else if (side == 5) column += "five";

        mydb.updateFace(opto.getId(), column, value);
    }

    @Override
    public int getItemCount() {
        if (onTab==ON_IMAGE) return optographs.size();
        if (onTab==ON_FOLLOWER) return  followers.size();
        return notifications.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position==0) return VIEW_HEADER;
        if (position==1) return SECOND_HEADER;
        if (isTab(ON_IMAGE)) return get(position).is_local()?VIEW_LOCAL:VIEW_SERVER;
        else if (isTab(ON_FOLLOWER)) return VIEW_FOLLOWER;
        else if (isTab(ON_NOTIFICATION) && getNotif(position).getType().equals(NOTIF_STAR_TYPE)) return VIEW_NOTIFICATION_STAR;
        else if (isTab(ON_NOTIFICATION) && getNotif(position).getType().equals(NOTIF_FOLLOW_TYPE)) return VIEW_NOTIFICATION_FOLLOW;
        else if (isTab(ON_NOTIFICATION) && getNotif(position).getType().equals(NOTIF_COMMENT_TYPE)) return VIEW_NOTIFICATION_COMMENT;
        else return VIEW_NOTIFICATION_VIEWS;
    }

    public Notification getNotif(int position) {
        return notifications.get(position);
    }

    public Optograph get(int position) {
        return optographs.get(position);
    }

    public Optograph getOldest() {
        return get(getItemCount() - 1);
    }

    public List<Optograph> getOptographs() {
        return this.optographs;
    }

    public boolean isEmpty() {
        return optographs.isEmpty();
    }

    public void setPerson(Person person) {
        this.person = person;
        isCurrentUser = (person.getId().equals(cache.getString(Cache.USER_ID)));
        notifyItemRangeChanged(0, 2);
    }

    public void addItem(Follower follower) {
        if (follower == null) {
            return;
        }

        if (followers.contains(follower)) {
            return;
        }

        followers.add(follower);
//        notifyItemInserted(getItemCount());
        notifyDataSetChanged();
    }

    public void addItem(Optograph optograph) {
        if (optograph == null || onTab!=ON_IMAGE) {
            return;
        }

        DateTime created_at = optograph.getCreated_atDateTime();

        if (optographs.contains(optograph)) {
            return;
        }

        if (optograph.getPerson().getId().equals(cache.getString(Cache.USER_ID))) {
            saveToSQLite(optograph);
        }
        Log.d("myTag"," setMessage: isLocal? "+optograph.is_local());
        if (optograph.is_local()) {
            optograph = checkToDB(optograph);
            if (optograph != null) optograph.setPerson(person);
        }
        Log.d("myTag"," setMessage: opto null? "+(optograph==null));
        if (optograph==null) {
            return;
        }

        if (optograph.getDeleted_at()!=null && !optograph.getDeleted_at().isEmpty()) return;

        // if list is empty, simply add new optograph
        if (optographs.isEmpty() || optographs.size()==2) {
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
            if (current!=null && created_at != null && created_at.isAfter(current.getCreated_atDateTime())) {
                optographs.add(i, optograph);
                notifyItemInserted(i);
                return;
            }
        }
    }

    public void saveToSQLite(Optograph opto) {
        Cursor res = mydb.getData(opto.getId(), DBHelper.OPTO_TABLE_NAME, DBHelper.OPTOGRAPH_ID);
        res.moveToFirst();
        if (res.getCount()!=0) return;
        String loc = opto.getLocation()==null?"":opto.getLocation().getId();
        mydb.insertOptograph(opto.getId(),opto.getText(),opto.getPerson().getId(),opto.getLocation()==null?"":opto.getLocation().getId(),
                opto.getCreated_at(),opto.getDeleted_at()==null?"":opto.getDeleted_at(),opto.is_starred()?1:0,opto.getStars_count(),opto.is_published()?1:0,
                opto.is_private()?1:0,opto.getStitcher_version(),1,opto.is_on_server()?1:0,"",opto.isShould_be_published()?1:0,
                opto.is_place_holder_uploaded()?1:0,opto.isPostFacebook()?1:0,opto.isPostTwitter()?1:0,opto.isPostInstagram()?1:0,
                opto.is_data_uploaded()?1:0,opto.getOptograph_type(),"Optograph");
    }

    public Optograph checkToDB(Optograph optograph) {
        Cursor res = mydb.getData(optograph.getId(), DBHelper.OPTO_TABLE_NAME, DBHelper.OPTOGRAPH_ID);
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

        if (mydb.checkIfAllImagesUploaded(optograph.getId())) return null;
        optograph.setStitcher_version(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_STITCHER_VERSION)));
        optograph.setText(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_TEXT)));
        optograph.setOptograph_type(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_TYPE)));
//        optograph.setCreated_at(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_CREATED_AT)));
        optograph.setIs_on_server(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_ON_SERVER)) != 0);
        optograph.setShould_be_published(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) != 0);
        optograph.setIs_place_holder_uploaded(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_PLACEHOLDER_UPLOADED)) != 0);
        optograph.setIs_data_uploaded(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_DATA_UPLOADED)) != 0);
        optograph.setPostFacebook(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_FACEBOOK)) != 0);
        optograph.setPostTwitter(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_TWITTER)) != 0);
        optograph.setPostInstagram(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_INSTAGRAM)) != 0);
        Cursor face = mydb.getData(optograph.getId(), DBHelper.FACES_TABLE_NAME, DBHelper.FACES_ID);
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

        return optograph;
    }

    public static String getTimeNow() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss.SSS'Z'");
        return format.format(new Date());
    }

    public static class LocalViewHolder extends RecyclerView.ViewHolder {
        private GridItemLocalBinding binding;
        private ImageView optograph2DCubeView_local;
        private ImageButton upload_Local;

        public LocalViewHolder(View rowView) {
            super(rowView);
            this.binding = DataBindingUtil.bind(rowView);
//            optograph2DCubeView_local = (ImageView) itemView.findViewById(R.id.optograph2dview_local);
//            upload_Local = (ImageButton) itemView.findViewById(R.id.upload_local);
        }

        public GridItemLocalBinding getBinding() {return binding;}
    }

    public static class ServerViewHolder extends RecyclerView.ViewHolder {
        private GridItemServerBinding bindingServer;

        public ServerViewHolder(View rowView) {
            super(rowView);
            this.bindingServer = DataBindingUtil.bind(rowView);
        }

        public GridItemServerBinding getBinding() {return bindingServer;}
    }

    public static class HeaderOneViewHolder extends RecyclerView.ViewHolder {
        private ProfileHeaderBinding bindingHeader;

        public HeaderOneViewHolder(View rowView) {
            super(rowView);
            this.bindingHeader = DataBindingUtil.bind(rowView);
        }

        public ProfileHeaderBinding getBinding() {
            return bindingHeader;
        }
    }

    public static class HeaderSecondViewHolder extends RecyclerView.ViewHolder {
        private ProfileTabBinding bindingTab;

        public HeaderSecondViewHolder(View rowView) {
            super(rowView);
            this.bindingTab = DataBindingUtil.bind(rowView);
        }

        public ProfileTabBinding getBinding() {
            return bindingTab;
        }
    }

    public static class FollowerViewHolder extends RecyclerView.ViewHolder {
        private GridFollowerBinding bindingFollower;

        public FollowerViewHolder(View rowView) {
            super(rowView);
            this.bindingFollower = DataBindingUtil.bind(rowView);
        }

        public GridFollowerBinding getBinding() {
            return bindingFollower;
        }
    }

    public static class NotificationStarViewHolder extends RecyclerView.ViewHolder {
        public GridNotificationStarBinding bindingStar;

        public NotificationStarViewHolder(View rowView) {
            super(rowView);
            this.bindingStar = DataBindingUtil.bind(rowView);
        }

        public GridNotificationStarBinding getBinding() {
            return bindingStar;
        }
    }

    public static class NotificationFollowViewHolder extends RecyclerView.ViewHolder {
        public GridNotificationFollowBinding bindingFollow;

        public NotificationFollowViewHolder(View rowView) {
            super(rowView);
            this.bindingFollow = DataBindingUtil.bind(rowView);
        }

        public GridNotificationFollowBinding getBinding() {
            return bindingFollow;
        }
    }
}
