package com.iam360.dscvr.viewmodels;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.danikula.videocache.HttpProxyCacheServer;
import com.iam360.dscvr.BR;
import com.iam360.dscvr.NewFeedItemBinding;
import com.iam360.dscvr.DscvrApp;
import com.iam360.dscvr.R;
import com.iam360.dscvr.model.LogInReturn;
import com.iam360.dscvr.model.OptoData;
import com.iam360.dscvr.model.OptoDataUpdate;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.Person;
import com.iam360.dscvr.network.ApiConsumer;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.CameraUtils;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.DBHelper;
import com.iam360.dscvr.util.NotificationSender;
import com.iam360.dscvr.views.activity.MainActivity;
import com.iam360.dscvr.views.activity.OptoImagePreviewActivity;
import com.iam360.dscvr.views.activity.OptographDetailsActivity;
import com.iam360.dscvr.views.activity.ProfileActivity;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import im.ene.lab.toro.ToroAdapter;
import im.ene.lab.toro.ToroViewHolder;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */
public class OptographFeedAdapter extends ToroAdapter<OptographFeedAdapter.OptographViewHolder> {
    private static final int ITEM_HEIGHT = Constants.getInstance().getDisplayMetrics().heightPixels;
    private List<Optograph> optographs;

    protected ApiConsumer apiConsumer;
    private Cache cache;
    private Optograph optoUpload;
    private Context context;
    private DBHelper mydb;
    private HttpProxyCacheServer proxy;

    private ProgressBar upload_progress;
    private TextView uploadButton;
    private boolean userLikesOptograph = false;
    private boolean isCurrentUser = false;
    private int currentFullVisibilty = 0;

    public OptographFeedAdapter(Context context) {
        this.context = context;
        this.optographs = new ArrayList<>();

        cache = Cache.open();
        proxy = DscvrApp.getProxy(context);

        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        mydb = new DBHelper(context);
    }

    @Override
    public OptographViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.new_feed_item, parent, false);

        final OptographViewHolder viewHolder = new OptographViewHolder(itemView);

        return viewHolder;
    }

    private void initializeDescriptionBar(View itemView) {
        RelativeLayout rl = (RelativeLayout) itemView.findViewById(R.id.description_bar);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) rl.getLayoutParams();

//        int newMarginBottom = ITEM_HEIGHT - ((MainActivityRedesign) itemView.getContext()).getLowerBoundary() + lp.bottomMargin;
//        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, newMarginBottom);
        rl.setLayoutParams(lp);

        rl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // don't pipe click events to views below description bar
                Timber.v("touch description bar");
                return true;
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(OptographViewHolder holder, int position, List<Object> payloads) {

    }

    @Override
    public void onBindViewHolder(OptographViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Optograph optograph = optographs.get(position);//original

        if (!optograph.equals(holder.getBinding().getOptograph())) {
            if (holder.getBinding().getOptograph() != null) {
                // TODO: cancel request
            }
            // span half screen
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ((int)(ITEM_HEIGHT * 0.6))); // (width, height)
            holder.itemView.setLayoutParams(params);

            holder.getBinding().personLocationInformation.setOnClickListener(v -> callDetailsPage(optograph));
//            holder.getBinding().videoView.setOnClickListener(v -> callDetailsPage(optograph));
//            holder.optograph2DCubeView.setOnClickListener(v -> callDetailsPage(optograph));

            holder.heart_label.setVisibility(optograph.getPerson().isElite_status()?View.VISIBLE:View.GONE);

            holder.heart_label.setTypeface(Constants.getInstance().getIconTypeface());
            holder.heart_label.setOnClickListener(v -> {
                setHeart(optograph, holder, v);
            });
            holder.getBinding().heartContainer.setOnClickListener(v -> { setHeart(optograph, holder, v); });

            isCurrentUser = optograph.getPerson().getId().equals(cache.getString(Cache.USER_ID));
            holder.followButton.setVisibility(isCurrentUser ? View.GONE : View.VISIBLE);
            holder.followButton.setOnClickListener(v -> followOrUnfollow(optograph, holder, v));
            holder.getBinding().followContainer.setVisibility(isCurrentUser ? View.GONE : View.VISIBLE);
            holder.getBinding().followContainer.setOnClickListener(v -> followOrUnfollow(optograph, holder, v));

            holder.getBinding().personLocationInformation.setOnClickListener(v -> startProfile(optograph.getPerson()));
            holder.getBinding().personAvatarAsset.setOnClickListener(v -> startProfile(optograph.getPerson()));

            upload_progress = (ProgressBar) holder.itemView.findViewById(R.id.feed_upload_progress);
            uploadButton = (TextView) holder.itemView.findViewById(R.id.feed_upload_label);

//            if (optograph.is_local() && !mydb.checkIfAllImagesUploaded(optograph.getId())) {//original
            if (optograph.is_local() && !optograph.is_on_server() && !optograph.isShould_be_published()) {
                holder.heart_label.setVisibility(View.GONE);
                uploadButton.setVisibility(View.VISIBLE);
            } else {
                holder.heart_label.setVisibility(View.VISIBLE);
                uploadButton.setVisibility(View.GONE);
            }

            uploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cache.getString(Cache.USER_TOKEN).equals("")) {
                        Snackbar.make(v,"Must login to upload.",Snackbar.LENGTH_SHORT);
                    } else {
                        apiConsumer = new ApiConsumer(cache.getString(Cache.USER_TOKEN));
                        upload_progress.setVisibility(View.VISIBLE);
                        uploadButton.setVisibility(View.GONE);
                        /*Cursor res = mydb.getData(optograph.getId(),DBHelper.OPTO_TABLE_NAME,DBHelper.OPTOGRAPH_ID);
                        res.moveToFirst();
                        optograph.setPostFacebook(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_FACEBOOK)) != 0);
                        optograph.setPostTwitter(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_TWITTER)) != 0);
                        optograph.setPostInstagram(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_INSTAGRAM)) != 0);
                        optograph.setIs_data_uploaded(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_DATA_UPLOADED)) != 0);
                        optograph.setIs_place_holder_uploaded(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_PLACEHOLDER_UPLOADED)) != 0);*/
                        optoUpload = optograph;
                        if (!optograph.is_data_uploaded()) {
                            Log.d("myTag", "upload the data first.");
                            uploadOptonautData(optograph);
                        } else if (!optograph.is_place_holder_uploaded()) {
                            Log.d("myTag", "upload the placeholder first.");
                            optoUpload = optograph;
                            uploadPlaceHolder(optograph);
                        } else {
                            Log.d("myTag","upload the 12 images");
                            updateOptograph(optograph);
//                            getLocalImage(optograph);
                        }
                    }
                }
            });

            updateHeartLabel(optograph, holder);
            followPerson(optograph, optograph.getPerson().is_followed(), holder);

            // setup sharing
            TextView settingsLabel = (TextView) holder.itemView.findViewById(R.id.settings_button);
            settingsLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Snackbar.make(v, v.getResources().getString(R.string.feature_soon), Snackbar.LENGTH_SHORT).show();
                }
            });

            SwipeLayout swipeLayout = (SwipeLayout) holder.itemView.findViewById(R.id.swipe_layout);
            swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
            swipeLayout.setBottomSwipeEnabled(false);
            swipeLayout.setTopSwipeEnabled(false);
            swipeLayout.setRightSwipeEnabled(false);
            
            View shareButton = swipeLayout.findViewById(R.id.bottom_wrapper);
            swipeLayout.addDrag(SwipeLayout.DragEdge.Right, shareButton);

            LinearLayout barSwipe = (LinearLayout) holder.itemView.findViewById(R.id.bar_swipe);


            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                    barSwipe.setVisibility(View.GONE);
                    //((MainActivity) context).setOptograph(optograph);

                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    Timber.d("PREVIEW SETOPTOGRAPH1 OPEN " + optograph.getId());

                    ((MainActivity) context).setOptograph(optograph);
                    ((MainActivity) context).dragSharePage();
                    swipeLayout.close();
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

            barSwipe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    swipeLayout.bounce(300, shareButton);
                    ((MainActivity) context).setOptograph(optograph);
                }
            });

            holder.getBinding().setVariable(BR.optograph, optograph);
            holder.getBinding().setVariable(BR.person, optograph.getPerson());
            holder.getBinding().setVariable(BR.location, optograph.getLocation());

            holder.getBinding().executePendingBindings();
        } else {
            Timber.d("rebinding of OptographViewHolder at position %s", position);
        }
    }

    @Nullable
    @Override
    protected Object getItem(int position) {
        return null;
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

    private void followOrUnfollow(Optograph optograph, OptographViewHolder holder, View v) {

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

    private void setHeart(Optograph optograph, OptographViewHolder holder, View v) {

        if(!cache.getString(Cache.USER_TOKEN).equals("")) {
            if (!optograph.is_starred()) {
                mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, true);
                optograph.setIs_starred(true);
                optograph.setStars_count(optograph.getStars_count() + 1);
                updateHeartLabel(optograph, holder);
                apiConsumer.postStar(optograph.getId(), new Callback<LogInReturn.EmptyResponse>() {
                    @Override
                    public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                        if (!response.isSuccess()) {
                            mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, false);
                            optograph.setIs_starred(response.isSuccess());
                            optograph.setStars_count(optograph.getStars_count() - 1);
                            updateHeartLabel(optograph, holder);
                        }else{
                            Cursor res = mydb.getData(optograph.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
                            res.moveToFirst();
                            if (res.getCount() > 0) {
                                mydb.updateTableColumn(DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID, optograph.getId(), "optograph_is_starred", true);
                                mydb.updateTableColumn(DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID, optograph.getId(), "optograph_stars_count", optograph.getStars_count());
                            }
                            NotificationSender.triggerSendNotification(optograph, "like", optograph.getId());
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, false);
                        optograph.setIs_starred(false);
                        optograph.setStars_count(optograph.getStars_count() - 1);
                        updateHeartLabel(optograph, holder);
                    }
                });
            } else if (optograph.is_starred()) {
                mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, false);
                optograph.setIs_starred(false);
                optograph.setStars_count(optograph.getStars_count() - 1);
                updateHeartLabel(optograph, holder);
                apiConsumer.deleteStar(optograph.getId(), new Callback<LogInReturn.EmptyResponse>() {
                    @Override
                    public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
//                            userLikesOptograph = !response.isSuccess();
                        if (!response.isSuccess()) {
                            mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, true);
                            optograph.setIs_starred(response.isSuccess());
                            optograph.setStars_count(optograph.getStars_count() + 1);
                            updateHeartLabel(optograph, holder);
                        }else{
                            Cursor res = mydb.getData(optograph.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
                            res.moveToFirst();
                            if (res.getCount() > 0) {
                                mydb.updateTableColumn(DBHelper.OPTO_TABLE_NAME_FEEDS,DBHelper.OPTOGRAPH_ID, optograph.getId(), "optograph_is_starred", false);
                                mydb.updateTableColumn(DBHelper.OPTO_TABLE_NAME_FEEDS,DBHelper.OPTOGRAPH_ID, optograph.getId(), "optograph_stars_count", optograph.getStars_count());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_STARRED, true);
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

    private void updateOptograph(Optograph opto) {
        Timber.d("isFBShare? "+opto.isPostFacebook()+" isTwitShare? "+opto.isPostTwitter()+" optoId: "+opto.getId());
        OptoDataUpdate data = new OptoDataUpdate(opto.getText(),opto.is_private(),opto.is_published(),opto.isPostFacebook(),opto.isPostTwitter(),null);// TODO: send location value here
        apiConsumer.updateOptoData(opto.getId(), data, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                Log.d("myTag", " onResponse isSuccess: " + response.isSuccess());
                Log.d("myTag"," onResponse body: "+response.body());
                Log.d("myTag", " onResponse message: " + response.message());
                Log.d("myTag", " onResponse raw: " + response.raw().toString());
                if (!response.isSuccess()) {
                    Log.d("myTag", "response errorBody: " + response.errorBody());
                    Snackbar.make(uploadButton, "Failed to upload.", Snackbar.LENGTH_SHORT).show();
                    uploadButton.setVisibility(View.VISIBLE);
                upload_progress.setVisibility(View.GONE);
                cache.save(Cache.UPLOAD_ON_GOING, false);
                    return;
                }
                getLocalImage(opto);
            }

            @Override
            public void onFailure(Throwable t) {
                Snackbar.make(uploadButton, "No Internet Connection.", Snackbar.LENGTH_SHORT).show();
                uploadButton.setVisibility(View.VISIBLE);
                upload_progress.setVisibility(View.GONE);
                cache.save(Cache.UPLOAD_ON_GOING, false);
            }
        });
    }
    private void toUploadOrToHeart(Optograph optograph) {
        if (optograph.is_local() && !optograph.is_on_server() && !optograph.isShould_be_published()) {
//            holder.heart_label.setVisibility(View.GONE);
            uploadButton.setVisibility(View.VISIBLE);
            upload_progress.setVisibility(View.GONE);
        } else {
//            holder.heart_label.setVisibility(View.VISIBLE);
            uploadButton.setVisibility(View.GONE);
            upload_progress.setVisibility(View.GONE);
        }
    }

    private void updateHeartLabel(Optograph optograph, OptographViewHolder holder) {
        holder.heart_label.setText(String.valueOf(optograph.getStars_count()));
        if(optograph.is_starred()) {
            holder.heart_label.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.liked_icn, 0);
        } else {
            holder.heart_label.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.like_icn, 0);
        }
    }

    private void followPerson(Optograph optograph, boolean isFollowed, OptographViewHolder holder) {
        Cursor res = mydb.getData(optograph.getPerson().getId(), DBHelper.PERSON_TABLE_NAME, "id");
        res.moveToFirst();
        if(isFollowed) {
            optograph.getPerson().setIs_followed(true);
            optograph.getPerson().setFollowers_count(optograph.getPerson().getFollowers_count() + 1);
            holder.followButton.setImageResource(R.drawable.feed_following_icn);
            NotificationSender.triggerSendNotification(optograph.getPerson(), "follow");
        } else {
            optograph.getPerson().setIs_followed(false);
            optograph.getPerson().setFollowers_count(optograph.getPerson().getFollowers_count() - 1);
            holder.followButton.setImageResource(R.drawable.feed_follow_icn);
        }
        if (res.getCount() > 0) {
            mydb.updateTableColumn(DBHelper.PERSON_TABLE_NAME,"id", optograph.getPerson().getId(), "is_followed", optograph.getPerson().is_followed());
            mydb.updateTableColumn(DBHelper.PERSON_TABLE_NAME,"id", optograph.getPerson().getId(), "followers_count", optograph.getPerson().getFollowers_count());
        }
    }

    private void uploadOptonautData(Optograph optograph) {
        Log.d("myTag", "uploadOptonautData id: " + optograph.getId() + " created_at: " + optograph.getCreated_atRFC3339());
        OptoData data = new OptoData(optograph.getId(), "0.7.0", optograph.getCreated_atRFC3339(),"optograph", Constants.PLATFORM, Build.MODEL,Build.MANUFACTURER);
        apiConsumer.uploadOptoData(data, new Callback<Optograph>() {
            @Override
            public void onResponse(Response<Optograph> response, Retrofit retrofit) {
                Log.d("myTag", " onResponse isSuccess: " + response.isSuccess());
                Log.d("myTag", " onResponse message: " + response.message());
                Log.d("myTag", " onResponse raw: " + response.raw().toString());
                if (!response.isSuccess()) {
                    Log.d("myTag", "response errorBody: " + response.errorBody());
                    Snackbar.make(uploadButton, "Failed to upload.", Snackbar.LENGTH_SHORT).show();
                    toUploadOrToHeart(optograph);
                    return;
                }
                Optograph opto = response.body();
                if (opto == null) {
                    Log.d("myTag", "parsing the JSON body failed.");
                    Snackbar.make(uploadButton, "Failed to upload.", Snackbar.LENGTH_SHORT).show();
                    toUploadOrToHeart(optograph);
                    return;
                }
                Log.d("myTag", " success: id: " + opto.getId() + " personName: " + opto.getPerson().getUser_name());
                // do things for success
                optoUpload = optograph;
                uploadPlaceHolder(optograph);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag", " onFailure: " + t.getMessage());
                toUploadOrToHeart(optograph);
            }
        });

        Cursor res = mydb.getData(optograph.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
        if (res==null || res.getCount()==0) return;
        res.moveToFirst();
    }

    private void uploadPlaceHolder(Optograph opto) {
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
                        holder = file.getPath()+"/"+s;
                        break;
                    }
                } else {
                    // ignore
                }
            }
        }

        new UploadPlaceHolder().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, holder);
    }

    class UploadPlaceHolder extends AsyncTask<String,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            for (String s : params) {
                String[] s3 = s.split("/");
                String face = s3[s3.length - 1];

                uploadPlaceHolderImage(optoUpload, s, face);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateOptograph(optoUpload);
        }
    }

    private int flag=2;
    private boolean uploadPlaceHolderImage(Optograph opto, String filePath, String fileName) {
        flag = 2;
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
        Log.d("myTag", "asset: " + fileName + " key: " + fileName.replace(".jpg", ""));
        apiConsumer.uploadOptoImage(opto.getId(), fbodyMain, OptoImagePreviewActivity.optoType360, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                Log.d("myTag", "onResponse uploadPlaceHolderImage isSuccess? " + response.isSuccess());
                Log.d("myTag", "onResponse message: " + response.message());
                Log.d("myTag", "onResponse body: " + response.body());
                Log.d("myTag", "onResponse raw: " + response.raw());

                flag = response.isSuccess() ? 1 : 0;
                optoUpload.setIs_place_holder_uploaded(response.isSuccess());
                mydb.updateColumnOptograph(optoUpload.getId(), DBHelper.OPTOGRAPH_IS_PLACEHOLDER_UPLOADED, response.isSuccess());
                toUploadOrToHeart(optoUpload);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag", "onFailure uploadImage: " + t.getMessage());
                flag = 0;
                toUploadOrToHeart(optoUpload);
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

    private void getLocalImage(Optograph opto) {
        cache.save(Cache.UPLOAD_ON_GOING, true);
        Log.d("myTag", "Path: " + CameraUtils.PERSISTENT_STORAGE_PATH + opto.getId());
        File dir = new File(CameraUtils.PERSISTENT_STORAGE_PATH + opto.getId());

        List<String> filePathList = new ArrayList<>();

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

        new UploadCubeImages().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,filePathList);
    }

    // try using AbstractQueuedSynchronizer
    class UploadCubeImages extends AsyncTask<List<String>,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(List<String>... params) {
            for (List<String> sL : params) {
                for (String s:sL) {
                    String[] s3 = s.split("/");
                    int side = Integer.valueOf((s3[s3.length - 1].split("\\."))[0]);
                    String face = s.contains("right") ? "r" : "l";

                    if (face.equals("l") && optoUpload.getLeftFace().getStatus()[side]) {
                        //pass
                    } else if (optoUpload.getRightFace().getStatus()[side]) {
                        //pass
                    } else if (!cache.getString(Cache.USER_TOKEN).equals("")) {
                        if (apiConsumer==null) apiConsumer = new ApiConsumer(cache.getString(Cache.USER_TOKEN));
                        uploadImage(optoUpload, s, face, side);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Cursor res = mydb.getData(optoUpload.getId(), DBHelper.FACES_TABLE_NAME, DBHelper.FACES_ID);
            if (res==null || res.getCount()==0) return;
            res.moveToFirst();
            /*if (mydb.checkIfAllImagesUploaded(optoUpload.getId())) {
                mydb.deleteEntry(DBHelper.FACES_TABLE_NAME,DBHelper.FACES_ID,optoUpload.getId());
                mydb.deleteEntry(DBHelper.OPTO_TABLE_NAME,DBHelper.OPTOGRAPH_ID,optoUpload.getId());
            }*/
            if (mydb.checkIfAllImagesUploaded(optoUpload.getId())) {
                mydb.updateColumnOptograph(optoUpload.getId(), DBHelper.OPTOGRAPH_IS_ON_SERVER, true);
//                holder.heart_label.setVisibility(View.VISIBLE);
            } else {
                uploadButton.setVisibility(View.VISIBLE);
            }
            upload_progress.setVisibility(View.GONE);
            cache.save(Cache.UPLOAD_ON_GOING, false);
        }
    }

    private boolean uploadImage(Optograph opto, String filePath, String face, int side) {
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
        apiConsumer.uploadOptoImage(opto.getId(), fbodyMain, OptoImagePreviewActivity.optoType360, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                Log.d("myTag", "onResponse uploadImage isSuccess? " + response.isSuccess());
                Log.d("myTag", "onResponse message: " + response.message());
                Log.d("myTag", "onResponse body: " + response.body());
                Log.d("myTag", "onResponse raw: " + response.raw());
                if (face.equals("l")) opto.getLeftFace().setStatusByIndex(side,response.isSuccess());
                else opto.getRightFace().setStatusByIndex(side,response.isSuccess());
                updateFace(opto,face,side,response.isSuccess()?1:0);

                flag = response.isSuccess()?1:0;
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag", "onFailure uploadImage: " + t.getMessage());
                if (face.equals("l")) opto.getLeftFace().setStatusByIndex(side,false);
                else opto.getRightFace().setStatusByIndex(side,false);
                toUploadOrToHeart(opto);
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

        mydb.updateFace(opto.getId(),column,value);
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

        String urlItem = "https://s3-ap-southeast-1.amazonaws.com/resources.staging-iam360.io/textures/"+optograph.getId()+"/pan.mp4";

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
        Cursor res = mydb.getData(opto.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
        res.moveToFirst();
        if (res.getCount()!=0) return;
        String loc = opto.getLocation()==null?"":opto.getLocation().getId();
        mydb.insertOptograph(opto.getId(),opto.getText(),opto.getPerson().getId(),opto.getLocation()==null?"":opto.getLocation().getId(),
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

    private void deleteOptographFromPhone(String id) {
        File dir = new File(CameraUtils.PERSISTENT_STORAGE_PATH + id);

        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    for (File file1: file.listFiles()) {
                        boolean result = file1.delete();
                    }
                    boolean result = file.delete();
                } else {
                    // ignore
                }
            }
            boolean result = dir.delete();
        }
    }

    public static class OptographViewHolder extends ToroViewHolder {
        private NewFeedItemBinding binding;
        RelativeLayout profileBar;
        RelativeLayout descriptionBar;
//        private Optograph2DCubeView optograph2DCubeView;
        private TextView heart_label;
        private ImageButton followButton;
        private boolean isNavigationModeCombined;
//        private VideoPlayerView videoView;


        public OptographViewHolder(View rowView) {
            super(rowView);
            this.binding = DataBindingUtil.bind(rowView);
            profileBar = (RelativeLayout) itemView.findViewById(R.id.profile_bar);
            descriptionBar = (RelativeLayout) itemView.findViewById(R.id.description_bar);
//            optograph2DCubeView = (Optograph2DCubeView) itemView.findViewById(R.id.optograph2dview);
//            videoView = (VideoPlayerView) itemView.findViewById(R.id.video_view);
            heart_label = (TextView) itemView.findViewById(R.id.heart_label);
            followButton = (ImageButton) itemView.findViewById(R.id.follow);
//            setInformationBarsVisible();
        }

        @Override
        public void bind(@Nullable Object object) {

        }

        private void setInformationBarsVisible() {
            profileBar.setVisibility(View.VISIBLE);
            descriptionBar.setVisibility(View.VISIBLE);
//            ((MainActivityRedesign) itemView.getContext()).setOverlayVisibility(View.VISIBLE);
            // todo: unregister touch listener
//            optograph2DCubeView.registerRendererOnSensors();
            isNavigationModeCombined = false;
        }

        private void setInformationBarsInvisible() {
            profileBar.setVisibility(View.INVISIBLE);
            descriptionBar.setVisibility(View.INVISIBLE);
//            ((MainActivityRedesign) itemView.getContext()).setOverlayVisibility(View.INVISIBLE);
            // todo: register touch listener
//            optograph2DCubeView.unregisterRendererOnSensors();
            isNavigationModeCombined = true;
        }

        public NewFeedItemBinding getBinding() {
            return binding;
        }

        public void toggleNavigationMode() {
            if (isNavigationModeCombined) {
                setInformationBarsVisible();
            } else {
                setInformationBarsInvisible();
            }
        }

        public boolean isNavigationModeCombined() {
            return isNavigationModeCombined;
        }

        @Override
        public boolean wantsToPlay() {
            return false;
        }

        @Override
        public boolean isAbleToPlay() {
            return false;
        }

        @Nullable
        @Override
        public String getVideoId() {
            return null;
        }

        @NonNull
        @Override
        public View getVideoView() {
            return null;
        }

        @Override
        public void start() {

        }

        @Override
        public void pause() {

        }

        @Override
        public int getDuration() {
            return 0;
        }

        @Override
        public int getCurrentPosition() {
            return 0;
        }

        @Override
        public void seekTo(int pos) {

        }

        @Override
        public boolean isPlaying() {
            return false;
        }
    }
}
