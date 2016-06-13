package com.iam360.iam360.views.new_design;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.daimajia.swipe.SwipeLayout;
import com.iam360.iam360.viewmodels.BaseVideoItem;
import com.iam360.iam360.viewmodels.DirectLinkVideoItem;
import com.iam360.iam360.viewmodels.OptographVideoViewHolder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.iam360.iam360.BR;
import com.iam360.iam360.NewFeedItemBinding;
import com.iam360.iam360.R;
import com.iam360.iam360.model.LogInReturn;
import com.iam360.iam360.model.OptoData;
import com.iam360.iam360.model.OptoDataUpdate;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.opengl.Optograph2DCubeView;
import com.iam360.iam360.sensors.CombinedMotionManager;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.CameraUtils;
import com.iam360.iam360.util.Constants;
import com.iam360.iam360.util.DBHelper;
import com.iam360.iam360.views.SnappyRecyclerView;
import com.iam360.iam360.views.record.OptoImagePreviewFragment;
import com.squareup.picasso.Picasso;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.ui.VideoPlayerView;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */
public class OptographFeedAdapter extends RecyclerView.Adapter<OptographVideoViewHolder> {
    private static final int ITEM_HEIGHT = Constants.getInstance().getDisplayMetrics().heightPixels;
    private List<Optograph> optographs;

    // TODO temporary list to add video links
    private List<DirectLinkVideoItem> videoItems;
    private SnappyRecyclerView snappyRecyclerView;
    private final VideoPlayerManager mVideoPlayerManager;

    protected ApiConsumer apiConsumer;
    private Cache cache;
    private Optograph optoUpload;
    private Context context;
    private DBHelper mydb;

    private ProgressBar upload_progress;
    private TextView uploadButton;
    private boolean userLikesOptograph = false;
    private boolean isCurrentUser = false;
    private int currentFullVisibilty = 0;

    private String url = "https://s3-ap-southeast-1.amazonaws.com/resources.staging-iam360.io/textures/6e40d95d-c79e-4ba9-a2d0-789d6b08611f/pan.mp4";

    public OptographFeedAdapter(Context context, VideoPlayerManager videoPlayerManager) {
        this.context = context;
        this.optographs = new ArrayList<>();
        this.videoItems = new ArrayList<>();
        this.mVideoPlayerManager = videoPlayerManager;

        cache = Cache.open();

        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        mydb = new DBHelper(context);
    }

    @Override
    public OptographVideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        final View itemView = LayoutInflater.
//                from(parent.getContext()).
//                inflate(R.layout.new_feed_item, parent, false);

//        Optograph2DCubeView optograph2DCubeView = (Optograph2DCubeView) itemView.findViewById(R.id.optograph2dview);

//        final OptographVideoViewHolder viewHolder = new OptographVideoViewHolder(itemView);

        DirectLinkVideoItem videoItem = new DirectLinkVideoItem("test", url, mVideoPlayerManager, null, 0);
        View resultView = videoItem.createView(parent, context.getResources().getDisplayMetrics().widthPixels);

        // TODO: add touch navigation and don't allow scrolling
//        optograph2DCubeView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (viewHolder.isNavigationModeCombined) {
//                    if (GestureDetectors.singleClickDetector.onTouchEvent(event)) {
//                        Timber.v("detected single click in combined navigation");
//                        viewHolder.toggleNavigationMode();
//                        snappyRecyclerView.enableScrolling();
//                        // still return optograph2DCubeView for registering end of touching
//                        return optograph2DCubeView.getOnTouchListener().onTouch(v, event);
//                    } else {
//                        Timber.v("pipe touch in combined navigation to optograph view");
//                        return optograph2DCubeView.getOnTouchListener().onTouch(v, event);
//                    }
//                } else {
//                    if (GestureDetectors.singleClickDetector.onTouchEvent(event)) {
//                        Timber.v("detected single click in simple navigation");
//                        viewHolder.toggleNavigationMode();
//                        snappyRecyclerView.disableScrolling();
//                        return true;
//                    } else {
//                        // need to return true here to prevent touch-stealing of parent!
//                        return true;
//                    }
//                }
//            }
//        });

//        initializeProfileBar(itemView);
//        initializeDescriptionBar(itemView);

        return new OptographVideoViewHolder(resultView);

//        return viewHolder;
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

    private void initializeProfileBar(final View itemView) {
        RelativeLayout rl = (RelativeLayout) itemView.findViewById(R.id.profile_bar);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) rl.getLayoutParams();

        // set margin and height
//        int newMarginTop = ((MainActivityRedesign) itemView.getContext()).getUpperBoundary();
//        lp.setMargins(0, newMarginTop, 0, 0);
        rl.setLayoutParams(lp);

        rl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // don't pipe click events to views below profile bar
                Timber.v("profilebar touched");
                return true;
            }
        });

//        ImageView profileView = (ImageView) itemView.findViewById(R.id.person_avatar_asset);
//        profileView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Snackbar.make(v, v.getResources().getString(R.string.feature_profiles_soon), Snackbar.LENGTH_SHORT).show();
//            }
//        });

        TextView profileLabel = (TextView) itemView.findViewById(R.id.person_name_label);
        profileLabel.setTypeface(Constants.getInstance().getDefaultRegularTypeFace());

        TextView locationLabel = (TextView) itemView.findViewById(R.id.location_label);
        locationLabel.setTypeface(Constants.getInstance().getDefaultLightTypeFace());

        TextView timeAgoLabel = (TextView) itemView.findViewById(R.id.time_ago);
        timeAgoLabel.setTypeface(Constants.getInstance().getDefaultRegularTypeFace());

        TextView settingsLabel = (TextView) itemView.findViewById(R.id.settings_button);
        settingsLabel.setTypeface(Constants.getInstance().getIconTypeface());
        settingsLabel.setText(String.valueOf((char) 0xe904));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        snappyRecyclerView = (SnappyRecyclerView) recyclerView;
    }


    public void rotateCubeMap (int pos) {
        currentFullVisibilty = pos;

        if (pos > 0) {
            //notifyItemChanged(pos - 1 ,"test" );
            notifyItemRangeChanged(pos - 1, 3, "test");
        } else {
            notifyItemRangeChanged(pos , 2, "test");
        }

    }

    @Override
    public void onBindViewHolder(OptographVideoViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            Log.v("mcandres", "payload empty");
            // Perform a full update
            onBindViewHolder(holder, position);
        } else {
            Log.v("mcandres", "payload not empty");
            if (currentFullVisibilty ==  position) {
                if(cache.getBoolean(Cache.GYRO_ENABLE));
//                    holder.optograph2DCubeView.setSensorMode(CombinedMotionManager.GYRO_MODE);
                else if(!cache.getBoolean(Cache.GYRO_ENABLE) && !cache.getBoolean(Cache.LITTLE_PLANET_ENABLE));
//                    holder.optograph2DCubeView.setSensorMode(CombinedMotionManager.PANNING_MODE);
                else;
//                    holder.optograph2DCubeView.setSensorMode(CombinedMotionManager.STILL_MODE);
            } else {
//                holder.optograph2DCubeView.setSensorMode(CombinedMotionManager.STILL_MODE);
            }

        }
    }

    @Override
    public void onBindViewHolder(OptographVideoViewHolder holder, int position) {
        Optograph optograph = optographs.get(position);//original
        
        String url = "https://s3-ap-southeast-1.amazonaws.com/resources.staging-iam360.io/textures/6e40d95d-c79e-4ba9-a2d0-789d6b08611f/pan.mp4";
        DirectLinkVideoItem videoItem = new DirectLinkVideoItem("test", url, mVideoPlayerManager, null, 0);
//        BaseVideoItem videoItem = optographs.get(position);
        videoItem.update(position, holder, mVideoPlayerManager);

        if (currentFullVisibilty ==  position) {
            if(cache.getBoolean(Cache.GYRO_ENABLE));
//                holder.optograph2DCubeView.setSensorMode(CombinedMotionManager.GYRO_MODE);
            else if(!cache.getBoolean(Cache.GYRO_ENABLE) && !cache.getBoolean(Cache.LITTLE_PLANET_ENABLE));
//                holder.optograph2DCubeView.setSensorMode(CombinedMotionManager.PANNING_MODE);
            else;
//                holder.optograph2DCubeView.setSensorMode(CombinedMotionManager.STILL_MODE);
        } else {
//            holder.optograph2DCubeView.setSensorMode(CombinedMotionManager.STILL_MODE);
        }

//        String url = "http://download.wavetlan.com/SVV/Media/HTTP/H264/Other_Media/H264_test7_voiceclip_mp4_480x360.mp4";
//        String url = "http://s3-ap-southeast-1.amazonaws.com/resources.staging-iam360.io/textures/b146850f-6105-408e-90b4-2ff76dbe88b1/pan.mp4";
//        String url = "https://s3-ap-southeast-1.amazonaws.com/resources.staging-iam360.io/textures/6e40d95d-c79e-4ba9-a2d0-789d6b08611f/pan.mp4";

//        Uri video = Uri.parse("http://download.wavetlan.com/SVV/Media/HTTP/H264/Other_Media/H264_test7_voiceclip_mp4_480x360.mp4");
//        holder.videoView.setVideoURI(video);
//        holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                mp.setLooping(true);
//                holder.videoView.start();
//            }
//        });

//        MediaController mc;
//        try {
//            mc = new MediaController(context);
//            mc.setAnchorView(holder.videoView);
//            mc.setMediaPlayer(holder.videoView);
////            holder.videoView.setMediaController(mc);
//            Uri link = Uri.parse(url.replace(" ","%20"));
//            holder.videoView.setVideoURI(link);
//            holder.videoView.requestFocus();
//            holder.videoView.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        userLikesOptograph = optograph.is_starred();
        // reset view holder if we got new optograh
        if (!optograph.equals(holder.getBinding().getOptograph())) {
            // cancel the request for the old texture
            if (holder.getBinding().getOptograph() != null) {
                // TODO: cancel request
            }
            // span half screen
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ((int)(ITEM_HEIGHT * 0.6))); // (width, height)
            holder.itemView.setLayoutParams(params);

            holder.getBinding().personLocationInformation.setOnClickListener(v -> callDetailsPage(optograph));
            holder.getBinding().videoView.setOnClickListener(v -> callDetailsPage(optograph));
//            holder.optograph2DCubeView.setOnClickListener(v -> callDetailsPage(optograph));

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
                    Log.d("myTag", "isFbShare? " + optograph.isPostFacebook() + " isTwitShare? " + optograph.isPostTwitter()+" optoId: "+optograph.getId());
                    if (cache.getString(Cache.USER_TOKEN).equals("")) {
                        Snackbar.make(v,"Must login to upload.",Snackbar.LENGTH_SHORT);
                    } else {
                        Log.d("myTag","upload bfore  optoId: "+optograph.getId()+" optograhPersonId: " +
                                optograph.getPerson().getId()+" dataUpload? "+optograph.is_data_uploaded()+
                                " placeHolderUpload? "+optograph.is_place_holder_uploaded()+" isFBShare? "+
                                optograph.isPostFacebook()+" isTwitShare? "+optograph.isPostTwitter());
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
                        Log.d("myTag", "upload after  optoId: " + optograph.getId() + " optograhPersonId: " +
                                optograph.getPerson().getId() + " dataUpload? " + optograph.is_data_uploaded() +
                                " placeHolderUpload? " + optograph.is_place_holder_uploaded() + " isFBShare? " +
                                optograph.isPostFacebook() + " isTwitShare? " + optograph.isPostTwitter());
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
//                    PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), v);
//                    popupMenu.inflate(R.menu.feed_item_menu);
//                    Menu menu = popupMenu.getMenu();
//                    MenuItem deleteItem = menu.findItem(R.id.delete_item);
//
//                    if (optograph.is_local() || optograph.getPerson().getId().equals(cache.getString(Cache.USER_ID))) {
//                        deleteItem.setVisible(true);
//                    }
//
//                    //registering popup with OnMenuItemClickListener
//                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                        public boolean onMenuItemClick(MenuItem item) {
//                            if (item.getItemId() == R.id.share_item) {
//                                ((MainActivityRedesign) v.getContext()).shareOptograph(optograph);
//                                return true;
//                            } else if (item.getItemId() == R.id.report_item) {
//                                Snackbar.make(v, v.getResources().getString(R.string.feature_soon), Snackbar.LENGTH_SHORT).show();
//                                return true;
//                            } else if (item.getItemId() == R.id.delete_item) {
//                                mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_DELETED_AT, RFC3339DateFormatter.toRFC3339String(DateTime.now()));
//                                return true;
//                            }
//                            return false;
//                        }
//                    });
//
//                    popupMenu.show();
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
//                    swipeLayout.close(true);
                }

                @Override
                public void onStartClose(SwipeLayout layout) {}

                @Override
                public void onClose(SwipeLayout layout) {
                    barSwipe.setVisibility(View.VISIBLE);
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

                   // Log.v("mcandres", "leftoffset : " + leftOffset + " topoffset :" + topOffset);

                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {}
            });



            barSwipe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    swipeLayout.bounce(300, shareButton);

                    Timber.d("PREVIEW SETOPTOGRAPH1 CLICK " + optograph.getId());

//                    swipeLayout.open();
                    ((MainActivity) context).setOptograph(optograph);
              //      ((MainActivity) context).setPage(MainActivity.SHARING_MODE);
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

    private void followOrUnfollow(Optograph optograph, OptographVideoViewHolder holder, View v) {

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

    private void setHeart(Optograph optograph, OptographVideoViewHolder holder, View v) {

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
//                            userLikesOptograph = !response.isSuccess();
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

    private void updateOptograph(Optograph opto) {
        Timber.d("isFBShare? "+opto.isPostFacebook()+" isTwitShare? "+opto.isPostTwitter()+" optoId: "+opto.getId());
        OptoDataUpdate data = new OptoDataUpdate(opto.getText(),opto.is_private(),opto.is_published(),opto.isPostFacebook(),opto.isPostTwitter());
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

    private void updateHeartLabel(Optograph optograph, OptographVideoViewHolder holder) {
//        if (userLikesOptograph) {
//            heart_label.setText(holder.itemView.getResources().getString(R.string.heart_count, optograph.getStars_count(), String.valueOf((char) 0xe90d)));
//        } else {
//            // TODO: use empty heart
//            heart_label.setText(holder.itemView.getResources().getString(R.string.heart_count, optograph.getStars_count(), String.valueOf((char) 0xe90d)));
//        }
        holder.heart_label.setText(String.valueOf(optograph.getStars_count()));
        if(optograph.is_starred()) {
            holder.heart_label.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.liked_icn, 0);
        } else {
            holder.heart_label.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.like_icn, 0);
        }
    }

    private void followPerson(Optograph optograph,boolean isFollowed, OptographVideoViewHolder holder) {
        if(isFollowed) {
            optograph.getPerson().setIs_followed(true);
            optograph.getPerson().setFollowers_count(optograph.getPerson().getFollowers_count() + 1);
            holder.followButton.setImageResource(R.drawable.feed_following_icn);
        } else {
            optograph.getPerson().setIs_followed(false);
            optograph.getPerson().setFollowers_count(optograph.getPerson().getFollowers_count() - 1);
            holder.followButton.setImageResource(R.drawable.feed_follow_icn);
        }
    }

    private void uploadOptonautData(Optograph optograph) {
        Log.d("myTag", "uploadOptonautData id: " + optograph.getId() + " created_at: " + optograph.getCreated_atRFC3339());
        OptoData data = new OptoData(optograph.getId(), "0.7.0", optograph.getCreated_atRFC3339(),"optograph");
        apiConsumer.uploadOptoData(data, new Callback<Optograph>() {
            @Override
            public void onResponse(Response<Optograph> response, Retrofit retrofit) {
                Log.d("myTag", " onResponse isSuccess: " + response.isSuccess());
//                        Log.d("myTag"," onResponse body: "+response.body());
                Log.d("myTag", " onResponse message: " + response.message());
                Log.d("myTag", " onResponse raw: " + response.raw().toString());
                if (!response.isSuccess()) {
                    Log.d("myTag", "response errorBody: " + response.errorBody());
                    Snackbar.make(uploadButton,"Failed to upload.",Snackbar.LENGTH_SHORT).show();
                    toUploadOrToHeart(optograph);
                    return;
                }
                Optograph opto = response.body();
                if (opto == null) {
                    Log.d("myTag", "parsing the JSON body failed.");
                    Snackbar.make(uploadButton,"Failed to upload.",Snackbar.LENGTH_SHORT).show();
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

        Cursor res = mydb.getData(optograph.getId(),DBHelper.OPTO_TABLE_NAME,DBHelper.OPTOGRAPH_ID);
        if (res==null || res.getCount()==0) return;
        res.moveToFirst();
        String stringRes = ""+DBHelper.OPTOGRAPH_ID+" "+res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_ID))+
                "\n"+DBHelper.OPTOGRAPH_IS_PUBLISHED+" "+res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_PUBLISHED))+
                "\n"+DBHelper.OPTOGRAPH_CREATED_AT+" "+res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_CREATED_AT))+
                "\n"+DBHelper.OPTOGRAPH_IS_ON_SERVER+" "+res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_ON_SERVER))+
                "\n"+DBHelper.OPTOGRAPH_TEXT+" "+res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_TEXT))+
                "\n"+DBHelper.OPTOGRAPH_IS_STITCHER_VERSION+" "+res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_STITCHER_VERSION));
//        descBox.setText(stringRes);
        Log.d("myTag", "" + stringRes);
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
        Log.d("myTag","before: ");
        int ctr = 0;
        for (boolean i : opto.getLeftFace().getStatus()) {
            Log.d("myTag","left "+ctr+": "+i);
            ctr+=1;
        }
        int ctr2 = 0;
        for (boolean i : opto.getRightFace().getStatus()) {
            Log.d("myTag","right "+ctr2+": "+i);
            ctr2+=1;
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
                Log.d("myTag", "onNext s: " + s + " s3 length: " + s3.length + " (s2[s2.length - 1]): " + (s3[s3.length - 1]));
                String face = s3[s3.length - 1];
                Log.d("myTag", " face: " + face);

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
    private boolean uploadPlaceHolderImage(Optograph opto, String filePath,String fileName) {
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
        apiConsumer.uploadOptoImage(opto.getId(), fbodyMain, OptoImagePreviewFragment.optoType360, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                Log.d("myTag", "onResponse uploadPlaceHolderImage isSuccess? " + response.isSuccess());
                Log.d("myTag", "onResponse message: " + response.message());
                Log.d("myTag", "onResponse body: " + response.body());
                Log.d("myTag", "onResponse raw: " + response.raw());

                flag = response.isSuccess() ? 1 : 0;
                optoUpload.setIs_place_holder_uploaded(response.isSuccess());
                mydb.updateColumnOptograph(optoUpload.getId(), DBHelper.OPTOGRAPH_IS_PLACEHOLDER_UPLOADED, flag);
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
        Log.d("myTag","before: ");
        int ctr = 0;
        for (boolean i : opto.getLeftFace().getStatus()) {
            Log.d("myTag","left "+ctr+": "+i);
            ctr+=1;
        }
        int ctr2 = 0;
        for (boolean i : opto.getRightFace().getStatus()) {
            Log.d("myTag","right "+ctr2+": "+i);
            ctr2+=1;
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
                    Log.d("myTag", "onNext s: " + s + " s3 length: " + s3.length + " (s2[s2.length - 1]): " + (s3[s3.length - 1]));
                    Log.d("myTag", " split: " + (s3[s3.length - 1].split("\\."))[0]);
                    int side = Integer.valueOf((s3[s3.length - 1].split("\\."))[0]);
                    String face = s.contains("right") ? "r" : "l";
                    Log.d("myTag", " face: " + face);

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
            Cursor res = mydb.getData(optoUpload.getId(),DBHelper.FACES_TABLE_NAME,DBHelper.FACES_ID);
            if (res==null || res.getCount()==0) return;
            res.moveToFirst();
            String stringRes = ""+DBHelper.FACES_LEFT_ZERO+" "+res.getString(res.getColumnIndex(DBHelper.FACES_LEFT_ZERO))+
                    "\n"+DBHelper.FACES_LEFT_ONE+" "+res.getString(res.getColumnIndex(DBHelper.FACES_LEFT_ONE))+
                    "\n"+DBHelper.FACES_LEFT_TWO+" "+res.getString(res.getColumnIndex(DBHelper.FACES_LEFT_TWO))+
                    "\n"+DBHelper.FACES_LEFT_THREE+" "+res.getString(res.getColumnIndex(DBHelper.FACES_LEFT_THREE))+
                    "\n"+DBHelper.FACES_LEFT_FOUR+" "+res.getString(res.getColumnIndex(DBHelper.FACES_LEFT_FOUR))+
                    "\n"+DBHelper.FACES_LEFT_FIVE+" "+res.getString(res.getColumnIndex(DBHelper.FACES_LEFT_FIVE))+
                    "\n"+DBHelper.FACES_RIGHT_ZERO+" "+res.getString(res.getColumnIndex(DBHelper.FACES_RIGHT_ZERO))+
                    "\n"+DBHelper.FACES_RIGHT_ONE+" "+res.getString(res.getColumnIndex(DBHelper.FACES_RIGHT_ONE))+
                    "\n"+DBHelper.FACES_RIGHT_TWO+" "+res.getString(res.getColumnIndex(DBHelper.FACES_RIGHT_TWO))+
                    "\n"+DBHelper.FACES_RIGHT_THREE+" "+res.getString(res.getColumnIndex(DBHelper.FACES_RIGHT_THREE))+
                    "\n"+DBHelper.FACES_RIGHT_FOUR+" "+res.getString(res.getColumnIndex(DBHelper.FACES_RIGHT_FOUR))+
                    "\n"+DBHelper.FACES_RIGHT_FIVE+" "+res.getString(res.getColumnIndex(DBHelper.FACES_RIGHT_FIVE));
            Log.d("myTag", "" + stringRes);
            /*if (mydb.checkIfAllImagesUploaded(optoUpload.getId())) {
                mydb.deleteEntry(DBHelper.FACES_TABLE_NAME,DBHelper.FACES_ID,optoUpload.getId());
                mydb.deleteEntry(DBHelper.OPTO_TABLE_NAME,DBHelper.OPTOGRAPH_ID,optoUpload.getId());
            }*/
            if (mydb.checkIfAllImagesUploaded(optoUpload.getId())) {
                mydb.updateColumnOptograph(optoUpload.getId(), DBHelper.OPTOGRAPH_IS_ON_SERVER, 1);
//                holder.heart_label.setVisibility(View.VISIBLE);
            } else {
                uploadButton.setVisibility(View.VISIBLE);
            }
            upload_progress.setVisibility(View.GONE);
            cache.save(Cache.UPLOAD_ON_GOING, false);
        }
    }

    private boolean uploadImage(Optograph opto, String filePath,String face,int side) {
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
        apiConsumer.uploadOptoImage(opto.getId(), fbodyMain, OptoImagePreviewFragment.optoType360, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                Log.d("myTag", "onResponse uploadImage isSuccess? " + response.isSuccess());
                Log.d("myTag", "onResponse message: " + response.message());
                Log.d("myTag", "onResponse body: " + response.body());
                Log.d("myTag", "onResponse raw: " + response.raw());
                if (face.equals("l")) opto.getLeftFace().setStatusByIndex(side,response.isSuccess());
                else opto.getRightFace().setStatusByIndex(side,response.isSuccess());
                updateFace(opto,face,side,response.isSuccess()?1:0);

                Log.d("myTag","after: ");
                int ctr = 0;
                for (boolean i : opto.getLeftFace().getStatus()) {
                    Log.d("myTag","left "+ctr+": "+i);
                    ctr+=1;
                }
                int ctr2 = 0;
                for (boolean i : opto.getRightFace().getStatus()) {
                    Log.d("myTag","right "+ctr2+": "+i);
                    ctr2+=1;
                }
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

    private void updateFace(Optograph opto, String face,int side,int value) {
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
        Log.d("myTag","opto null? "+(optograph==null)+" isLocal? "+optograph.is_local());
        if (optograph.is_local()) optograph = checkToDB(optograph);
        if (optograph==null) {
            return;
        }

        String urlItem = "https://s3-ap-southeast-1.amazonaws.com/resources.staging-iam360.io/textures/"+optograph.getId()+"/pan.mp4";

        // if list is empty, simply add new optograph
        if (optographs.isEmpty()) {
            optographs.add(optograph);
            videoItems.add(new DirectLinkVideoItem("test", urlItem, mVideoPlayerManager, null, 0));
            notifyItemInserted(getItemCount());
            return;
        }

        // if optograph is oldest, simply append to list
        if (created_at != null && created_at.isBefore(getOldest().getCreated_atDateTime())) {
            optographs.add(optograph);
            videoItems.add(new DirectLinkVideoItem("test", urlItem, mVideoPlayerManager, null, 0));
            notifyItemInserted(getItemCount());
            return;
        }

        // find correct position of optograph
        // TODO: allow for "breaks" between new optograph and others...
        for (int i = 0; i < optographs.size(); i++) {
            Optograph current = optographs.get(i);
            if (created_at != null && created_at.isAfter(current.getCreated_atDateTime())) {
                optographs.add(i, optograph);
                videoItems.add(new DirectLinkVideoItem("test", urlItem, mVideoPlayerManager, null, 0));
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

    public List<DirectLinkVideoItem> getVideoItems() {
        return this.videoItems;
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
        Timber.d("checkToDB isFBShare? " + (res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_FACEBOOK)) != 0) + " Twit? " + (res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_TWITTER)) != 0) + " optoId: " + optograph.getId());
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

        Person person = new Person();
        person.setFacebook_token(cache.getString(Cache.USER_FB_TOKEN));
        person.setDisplay_name(cache.getString(Cache.USER_NAME));
        person.setFacebook_user_id(cache.getString(Cache.USER_FB_ID));
        person.setUser_name(cache.getString(Cache.USER_NAME));

        optograph.setPerson(person);
        return optograph;
    }

    private void deleteOptographFromPhone(String id) {
        Log.d("myTag", "Path: " + CameraUtils.PERSISTENT_STORAGE_PATH + id);
        File dir = new File(CameraUtils.PERSISTENT_STORAGE_PATH + id);

        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    for (File file1: file.listFiles()) {
                        boolean result = file1.delete();
                        Log.d("myTag", "getName: " + file1.getName() + " getPath: " + file1.getPath()+" delete: "+result);
                    }
                    boolean result = file.delete();
                    Log.d("myTag", "getName: " + file.getName() + " getPath: " + file.getPath()+" delete: "+result);
                } else {
                    // ignore
                }
            }
            boolean result = dir.delete();
            Log.d("myTag", "getName: " + dir.getName() + " getPath: " + dir.getPath()+" delete: "+result);
        }
    }

    public static class OptographViewHolder extends RecyclerView.ViewHolder {
        private NewFeedItemBinding binding;
        RelativeLayout profileBar;
        RelativeLayout descriptionBar;
//        private Optograph2DCubeView optograph2DCubeView;
        private TextView heart_label;
        private ImageButton followButton;
        private boolean isNavigationModeCombined;
        private VideoPlayerView videoView;


        public OptographViewHolder(View rowView) {
            super(rowView);
            this.binding = DataBindingUtil.bind(rowView);
            profileBar = (RelativeLayout) itemView.findViewById(R.id.profile_bar);
            descriptionBar = (RelativeLayout) itemView.findViewById(R.id.description_bar);
//            optograph2DCubeView = (Optograph2DCubeView) itemView.findViewById(R.id.optograph2dview);
            videoView = (VideoPlayerView) itemView.findViewById(R.id.video_view);
            heart_label = (TextView) itemView.findViewById(R.id.heart_label);
            followButton = (ImageButton) itemView.findViewById(R.id.follow);
//            setInformationBarsVisible();
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
    }
}
