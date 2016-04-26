package co.optonaut.optonaut.viewmodels;

import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import co.optonaut.optonaut.BR;
import co.optonaut.optonaut.FeedItemBinding;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.database.DBHelper;
import co.optonaut.optonaut.model.LogInReturn;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.network.ApiConsumer;
import co.optonaut.optonaut.network.ApiEndpoints;
import co.optonaut.optonaut.opengl.Optograph2DCubeView;
import co.optonaut.optonaut.util.Cache;
import co.optonaut.optonaut.util.CameraUtils;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.views.GestureDetectors;
import co.optonaut.optonaut.views.redesign.MainActivityRedesign;
import co.optonaut.optonaut.views.redesign.SnappyRecyclerView;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */
public class OptographFeedAdapter extends RecyclerView.Adapter<OptographFeedAdapter.OptographViewHolder> {
    private static final int ITEM_HEIGHT = Constants.getInstance().getDisplayMetrics().heightPixels;
    List<Optograph> optographs;
    private SnappyRecyclerView snappyRecyclerView;

    ApiEndpoints service;
    protected ApiConsumer apiConsumer;
    Cache cache;
    Boolean[] defaultList = {false,false,false,false,false,false};
    Optograph optoUpload;
    Context context;

    DBHelper mydb;

    ProgressBar upload_progress;
    TextView uploadButton;
    TextView heart_label;

    public class OptoImageData {
        final String key;
        final RequestBody asset;
        OptoImageData(String key,RequestBody asset) {
            this.key = key;
            this.asset = asset;
        }
    }

    public OptographFeedAdapter(Context context) {
        this.context = context;
        this.optographs = new ArrayList<>();

        cache = Cache.open();

        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        mydb = new DBHelper(context);
    }

    @Override
    public OptographViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.feed_item, parent, false);

        Optograph2DCubeView optograph2DCubeView = (Optograph2DCubeView) itemView.findViewById(R.id.optograph2dview);

        final OptographViewHolder viewHolder = new OptographViewHolder(itemView);

        // TODO: add touch navigation and don't allow scrolling
        optograph2DCubeView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (viewHolder.isNavigationModeCombined) {
                    if (GestureDetectors.singleClickDetector.onTouchEvent(event)) {
                        Timber.v("detected single click in combined navigation");
                        viewHolder.toggleNavigationMode();
                        snappyRecyclerView.enableScrolling();
                        // still return optograph2DCubeView for registering end of touching
                        return optograph2DCubeView.getOnTouchListener().onTouch(v, event);
                    } else {
                        Timber.v("pipe touch in combined navigation to optograph view");
                        return optograph2DCubeView.getOnTouchListener().onTouch(v, event);
                    }
                } else {
                    if (GestureDetectors.singleClickDetector.onTouchEvent(event)) {
                        Timber.v("detected single click in simple navigation");
                        viewHolder.toggleNavigationMode();
                        snappyRecyclerView.disableScrolling();
                        return true;
                    } else {
                        // need to return true here to prevent touch-stealing of parent!
                        return true;
                    }
                }
            }
        });

        initializeProfileBar(itemView);
        initializeDescriptionBar(itemView);

        return viewHolder;
    }

    private void initializeDescriptionBar(View itemView) {
        RelativeLayout rl = (RelativeLayout) itemView.findViewById(R.id.description_bar);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) rl.getLayoutParams();

        int newMarginBottom = ITEM_HEIGHT - ((MainActivityRedesign) itemView.getContext()).getLowerBoundary() + lp.bottomMargin;
        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, newMarginBottom);
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
        int newMarginTop = ((MainActivityRedesign) itemView.getContext()).getUpperBoundary();
        lp.setMargins(0, newMarginTop, 0, 0);
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
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ITEM_HEIGHT); // (width, height)
            holder.itemView.setLayoutParams(params);

            heart_label = (TextView) holder.itemView.findViewById(R.id.heart_label);
            heart_label.setTypeface(Constants.getInstance().getIconTypeface());
            heart_label.setOnClickListener(v -> {
                Snackbar.make(holder.itemView, holder.itemView.getResources().getString(R.string.feature_favorites_soon), Snackbar.LENGTH_SHORT).show();
                Log.d("myTag", "id: " + optograph.getId() + " createdAt: " + optograph.getCreated_at() + " createdAtRFC: " + optograph.getCreated_atRFC3339().toString() +
                        " stitchVersion: " + optograph.getStitcher_version() + " previewAssetId: " +
                        optograph.getPreview_asset_id() + " isLocal: " + optograph.is_local() + " isPrivate: " +
                        optograph.is_private() + " isStarred: " + optograph.is_starred() + " leftAssetId: " +
                        optograph.getLeft_texture_asset_id() + " rightAssetId: " + optograph.getRight_texture_asset_id());
                /*if (optograph.is_local()) {
//                    uploadOptonautData(optograph,holder.itemView);
                    getLocalImage(optograph);
                    optoUpload = optograph;
                } else {
                    // star(route)
                }*/
            });


            ImageView profileView = (ImageView) holder.itemView.findViewById(R.id.person_avatar_asset);
            profileView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MainActivityRedesign activity = (MainActivityRedesign) context;
                    activity.startProfile(optograph.getPerson(), null);
                }
            });

            upload_progress = (ProgressBar) holder.itemView.findViewById(R.id.feed_upload_progress);
            uploadButton = (TextView) holder.itemView.findViewById(R.id.feed_upload_label);

            if (optograph.is_local() && !mydb.checkIfAllImagesUploaded(optograph.getId())) {
                heart_label.setVisibility(View.GONE);
                uploadButton.setVisibility(View.VISIBLE);
            } else {
                heart_label.setVisibility(View.VISIBLE);
                uploadButton.setVisibility(View.GONE);
            }

            uploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    upload_progress.setVisibility(View.VISIBLE);
                    uploadButton.setVisibility(View.GONE);
                    getLocalImage(optograph);
                    optoUpload = optograph;
                }
            });

            // TODO: check if user has starred optograph
            boolean userLikesOptograph = false;
            if (userLikesOptograph) {
                heart_label.setText(holder.itemView.getResources().getString(R.string.heart_count, optograph.getStars_count(), String.valueOf((char) 0xe90d)));
            } else {
                // TODO: use empty heart
                heart_label.setText(holder.itemView.getResources().getString(R.string.heart_count, optograph.getStars_count(), String.valueOf((char) 0xe90d)));
            }

            // setup sharing
            TextView settingsLabel = (TextView) holder.itemView.findViewById(R.id.settings_button);
            settingsLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), v);
                    popupMenu.inflate(R.menu.feed_item_menu);

                    //registering popup with OnMenuItemClickListener
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.share_item) {
                                ((MainActivityRedesign) v.getContext()).shareOptograph(optograph);
                                return true;
                            } else if (item.getItemId() == R.id.report_item) {
                                Snackbar.make(v, v.getResources().getString(R.string.feature_soon), Snackbar.LENGTH_SHORT).show();
                                return true;
                            }
                            return false;
                        }
                    });

                    popupMenu.show();
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

    private void getLocalImage(Optograph opto) {
        Log.d("myTag", "Path: " + CameraUtils.PERSISTENT_STORAGE_PATH + opto.getId());
        File dir = new File(CameraUtils.PERSISTENT_STORAGE_PATH + opto.getId());

        List<String> filePathList = new ArrayList<>();

        if (dir.exists()) {// remove the not notation here
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    Log.d("myTag", "getName: " + file.getName() + " getPath: " + file.getPath());

                    for (String s : file.list()) {
//                        Log.d("myTag", "list of file: " + s);
                        filePathList.add(file.getPath()+"/"+s);
                    }
//                    optographs.add(new Optograph(file.getName()));
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

//        Observable<String> myObservable = Observable.from(filePathList);
//        Observer<String> myObserver = new Observer<String>() {
//            @Override
//            public void onCompleted() {
//                Log.d("myTag","onComplete");
//                int ctr = 0;
//                for (boolean i : opto.getLeftFace().getStatus()) {
//                    Log.d("myTag","left "+ctr+": "+i);
//                    ctr+=1;
//                }
//                int ctr2 = 0;
//                for (boolean i : opto.getRightFace().getStatus()) {
//                    Log.d("myTag","left "+ctr2+": "+i);
//                    ctr2+=1;
//                }
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                e.printStackTrace();
//                Log.d("myTag","onError upload: "+e.getMessage());
//            }
//
//            @Override
//            public void onNext(String s) {
//                String[] s3 = s.split("/");
//                Log.d("myTag","onNext s: "+s+" s3 length: "+s3.length+" (s2[s2.length - 1]): "+(s3[s3.length - 1]));
//                Log.d("myTag"," split: "+(s3[s3.length - 1].split("\\."))[0]);
//                int side = Integer.valueOf((s3[s3.length - 1].split("\\."))[0]);
//                String face = s.contains("right")?"r":"l";
//                Log.d("myTag"," face: "+face);
//
////                uploadImage(opto, s,face,side);
//                String[] s2 = s.split("/");
//                String fileName = s2[s2.length-1];
//
//                Bitmap bm = null;
//
//                try {
//                    bm = BitmapFactory.decodeFile(s);
//                } catch (Exception e) {
//                    Log.e(e.getClass().getName(), e.getMessage());
//                }
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                bm.compress(Bitmap.CompressFormat.JPEG, 75, bos);
//                byte[] data = bos.toByteArray();
//
//                RequestBody fbody = RequestBody.create(MediaType.parse("image/jpeg"), data);
//                RequestBody fbodyMain = new MultipartBuilder()
//                        .type(MultipartBuilder.FORM)
//                        .addFormDataPart("asset", face + fileName, fbody)
//                        .addFormDataPart("key", face + side)
//                        .build();
//                Log.d("myTag","asset: "+face+fileName+" key: "+face+ fileName.replace(".jpg",""));
//                apiConsumer.uploadOptoImage(opto.getId(), fbodyMain, new Callback<LogInReturn.EmptyResponse>() {
//                    @Override
//                    public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
//                        Log.d("myTag", "onResponse uploadImage isSuccess? " + response.isSuccess());
//                        Log.d("myTag", "onResponse message: " + response.message());
//                        Log.d("myTag", "onResponse body: " + response.body());
//                        Log.d("myTag", "onResponse raw: " + response.raw());
//                        if (face.equals("l")) opto.getLeftFace().setStatusByIndex(side,response.isSuccess());
//                        else opto.getRightFace().setStatusByIndex(side,response.isSuccess());
//                    }
//
//                    @Override
//                    public void onFailure(Throwable t) {
//                        Log.d("myTag", "onFailure uploadImage: " + t.getMessage());
//                        if (face.equals("l")) opto.getLeftFace().setStatusByIndex(side,false);
//                        else opto.getRightFace().setStatusByIndex(side,false);
//                    }
//                });
//            }
//        };

//        myObservable.subscribe(myObserver);

        /*myObservable.subscribeOn(Schedulers.newThread())
                .map(new Func1<String, Boolean>() {
            @Override
            public Boolean call(String s) {
                String[] s3 = s.split("/");
                Log.d("myTag", "onNext s: " + s + " s3 length: " + s3.length + " (s2[s2.length - 1]): " + (s3[s3.length - 1]));
                Log.d("myTag", " split: " + (s3[s3.length - 1].split("\\."))[0]);
                int side = Integer.valueOf((s3[s3.length - 1].split("\\."))[0]);
                String face = s.contains("right") ? "r" : "l";
                Log.d("myTag", " face: " + face);

                uploadImage(opto, s, face, side);
                return true;
            }
        })
        .subscribe(aBoolean -> Log.d("myTag", "return: " + aBoolean));*/

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

                    uploadImage(optoUpload, s, face, side);
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
                heart_label.setVisibility(View.VISIBLE);
            } else {
                uploadButton.setVisibility(View.VISIBLE);
            }
            upload_progress.setVisibility(View.GONE);
        }
    }

    private int flag=2;
    private boolean uploadImage(Optograph opto, String filePath,String face,int side) {
        flag = 2;
        String[] s2 = filePath.split("/");
        String fileName = s2[s2.length-1];

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
        apiConsumer.uploadOptoImage(opto.getId(), fbodyMain, new Callback<LogInReturn.EmptyResponse>() {
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
        private FeedItemBinding binding;
        RelativeLayout profileBar;
        RelativeLayout descriptionBar;
        private Optograph2DCubeView optograph2DCubeView;
        private boolean isNavigationModeCombined;


        public OptographViewHolder(View rowView) {
            super(rowView);
            this.binding = DataBindingUtil.bind(rowView);
            profileBar = (RelativeLayout) itemView.findViewById(R.id.profile_bar);
            descriptionBar = (RelativeLayout) itemView.findViewById(R.id.description_bar);
            optograph2DCubeView = (Optograph2DCubeView) itemView.findViewById(R.id.optograph2dview);
            setInformationBarsVisible();
        }

        private void setInformationBarsVisible() {
            profileBar.setVisibility(View.VISIBLE);
            descriptionBar.setVisibility(View.VISIBLE);
            ((MainActivityRedesign) itemView.getContext()).setOverlayVisibility(View.VISIBLE);
            // todo: unregister touch listener
            optograph2DCubeView.registerRendererOnSensors();
            isNavigationModeCombined = false;
        }

        private void setInformationBarsInvisible() {
            profileBar.setVisibility(View.INVISIBLE);
            descriptionBar.setVisibility(View.INVISIBLE);
            ((MainActivityRedesign) itemView.getContext()).setOverlayVisibility(View.INVISIBLE);
            // todo: register touch listener
            optograph2DCubeView.unregisterRendererOnSensors();
            isNavigationModeCombined = true;
        }

        public FeedItemBinding getBinding() {
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
