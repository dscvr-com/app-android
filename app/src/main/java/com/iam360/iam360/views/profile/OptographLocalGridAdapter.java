package com.iam360.iam360.views.profile;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.iam360.iam360.BR;
import com.iam360.iam360.GridItemLocalBinding;
import com.iam360.iam360.R;
import com.iam360.iam360.model.LogInReturn;
import com.iam360.iam360.model.OptoData;
import com.iam360.iam360.model.OptoDataUpdate;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.CameraUtils;
import com.iam360.iam360.util.Constants;
import com.iam360.iam360.util.DBHelper;
import com.iam360.iam360.views.new_design.OptographDetailsActivity;
import com.iam360.iam360.views.record.OptoImagePreviewFragment;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Created by Mariel on 6/14/2016.
 */
public class OptographLocalGridAdapter extends RecyclerView.Adapter<OptographLocalGridAdapter.LocalViewHolder> {

    private static final int ITEM_WIDTH= Constants.getInstance().getDisplayMetrics().widthPixels;
    List<Optograph> optographs;

    protected Cache cache;
    private DBHelper mydb;

    protected ApiConsumer apiConsumer;
    private Context context;

    public OptographLocalGridAdapter(Context context) {
        this.context = context;
        this.optographs = new ArrayList<>();

        cache = Cache.open();
        mydb = new DBHelper(context);

        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("")?null:token);
    }

    @Override
    public LocalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.grid_item_local,parent,false);

        final LocalViewHolder viewHolder = new LocalViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(LocalViewHolder holder, int position) {
        Optograph optograph = optographs.get(position);

        if (!optograph.equals(holder.getBinding().getOptograph())) {
            if (holder.getBinding().getOptograph() != null) {

            }

            Log.d("myTag","adapter image: "+ITEM_WIDTH/4);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ITEM_WIDTH, ViewGroup.LayoutParams.WRAP_CONTENT); //ITEM_WIDTH / OptographGridFragment.NUM_COLUMNS); // (width, height)
            holder.itemView.setLayoutParams(params);

            holder.getBinding().setVariable(BR.optograph, optograph);
            holder.getBinding().executePendingBindings();

            holder.getBinding().optograph2dviewLocal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, OptographDetailsActivity.class);
                    intent.putExtra("opto", optograph);
                    context.startActivity(intent);
                }
            });

            holder.getBinding().uploadLocal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (cache.getString(Cache.USER_TOKEN).equals("")) {
                        Snackbar.make(v, "Must login to upload.", Snackbar.LENGTH_SHORT);
                    } else {
                        apiConsumer = new ApiConsumer(cache.getString(Cache.USER_TOKEN));
                        holder.getBinding().uploadProgressLocal.setVisibility(View.VISIBLE);
                        holder.getBinding().uploadLocal.setVisibility(View.GONE);
                        if (!optograph.is_data_uploaded()) {
                            Log.d("myTag", "upload the data first. position: " + position);
                            uploadOptonautData(position);
                        } else if (!optograph.is_place_holder_uploaded()) {
                            Log.d("myTag", "upload the placeholder first. position: "+position);
                            uploadPlaceHolder(position);
                        } else {
                            Log.d("myTag","upload the 12 images position: "+position);
                            updateOptograph(position);
//                            getLocalImage(optograph);
                        }
                    }
                }
            });
        }
    }

    private void uploadOptonautData(int position) {
        Optograph optograph = optographs.get(position);
        OptoData data = new OptoData(optograph.getId(), optograph.getStitcher_version(), optograph.getCreated_atRFC3339(),optograph.getOptograph_type());
        apiConsumer.uploadOptoData(data, new Callback<Optograph>() {
            @Override
            public void onResponse(Response<Optograph> response, Retrofit retrofit) {
                if (!response.isSuccess()) {
                    Log.d("myTag", "response errorBody: " + response.errorBody());
                    Toast.makeText(context, "Failed to upload.", Toast.LENGTH_SHORT).show();
                    notifyItemChanged(position);
                    return;
                }
                Optograph opto = response.body();
                if (opto == null) {
                    Log.d("myTag", "parsing the JSON body failed.");
                    Toast.makeText(context, "Failed to upload.", Toast.LENGTH_SHORT).show();
                    notifyItemChanged(position);
                    return;
                }
                Log.d("myTag", " success: id: " + opto.getId() + " personName: " + opto.getPerson().getUser_name());
                // do things for success
                uploadPlaceHolder(position);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag", " onFailure: " + t.getMessage());
                notifyItemChanged(position);
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
        Log.d("myTag", "" + stringRes);
    }


    private void uploadPlaceHolder(int position) {
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

        new UploadPlaceHolder().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, holder);
    }

    class UploadPlaceHolder extends AsyncTask<String,Void,Void> {
        int mPosition = 0;
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
                Log.d("myTag", "onNext s: " + s + " s3 length: " + s3.length + " (s2[s2.length - 1]): " + (s3[s3.length - 1]));
                String face = s3[s3.length - 1];
                Log.d("myTag", " face: " + face);

                uploadPlaceHolderImage(mPosition, s1[0], face);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateOptograph(mPosition);
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
        apiConsumer.uploadOptoImage(opto.getId(), fbodyMain, OptoImagePreviewFragment.optoType360, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                flag = response.isSuccess() ? 1 : 0;
                opto.setIs_place_holder_uploaded(response.isSuccess());
                mydb.updateColumnOptograph(opto.getId(), DBHelper.OPTOGRAPH_IS_PLACEHOLDER_UPLOADED, flag);
                opto.setIs_place_holder_uploaded(response.isSuccess());
                notifyItemChanged(position);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag", "onFailure uploadImage: " + t.getMessage());
                flag = 0;
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


    private void updateOptograph(int position) {
        Optograph opto = optographs.get(position);
        Timber.d("isFBShare? " + opto.isPostFacebook() + " isTwitShare? " + opto.isPostTwitter() + " optoId: " + opto.getId());
        OptoDataUpdate data = new OptoDataUpdate(opto.getText(),opto.is_private(),opto.is_published(),opto.isPostFacebook(),opto.isPostTwitter());
        apiConsumer.updateOptoData(opto.getId(), data, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                Log.d("myTag", " onResponse isSuccess: " + response.isSuccess());
                Log.d("myTag", " onResponse body: " + response.body());
                Log.d("myTag", " onResponse message: " + response.message());
                Log.d("myTag", " onResponse raw: " + response.raw().toString());
                if (!response.isSuccess()) {
                    Log.d("myTag", "response errorBody: " + response.errorBody());
                    Toast.makeText(context, "Failed to upload.", Toast.LENGTH_SHORT).show();
                    cache.save(Cache.UPLOAD_ON_GOING, false);
                    notifyItemChanged(position);
                    return;
                }
                getLocalImage(position);
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(context, "No Internet Connection.", Toast.LENGTH_SHORT).show();
                cache.save(Cache.UPLOAD_ON_GOING, false);
                notifyItemChanged(position);
            }
        });
    }

    private void getLocalImage(int position) {
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
        Log.d("myTag", "before: ");
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
        int mPosition = 0;
        Optograph optograph;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(List<String>... params) {
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
                    Log.d("myTag", "onNext s: " + s + " s3 length: " + s3.length + " (s2[s2.length - 1]): " + (s3[s3.length - 1]));
                    Log.d("myTag", " split: " + (s3[s3.length - 1].split("\\."))[0]);
                    int side = Integer.valueOf((s3[s3.length - 1].split("\\."))[0]);
                    String face = s.contains("right") ? "r" : "l";
                    Log.d("myTag", " face: " + face);

                    if (face.equals("l") && optograph.getLeftFace().getStatus()[side]) {
                        //pass
                    } else if (optograph.getRightFace().getStatus()[side]) {
                        //pass
                    } else if (!cache.getString(Cache.USER_TOKEN).equals("")) {
                        if (apiConsumer==null) apiConsumer = new ApiConsumer(cache.getString(Cache.USER_TOKEN));
                        uploadImage(mPosition, s, face, side);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Cursor res = mydb.getData(optograph.getId(),DBHelper.FACES_TABLE_NAME,DBHelper.FACES_ID);
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
            if (mydb.checkIfAllImagesUploaded(optograph.getId())) {
                mydb.updateColumnOptograph(optograph.getId(), DBHelper.OPTOGRAPH_IS_ON_SERVER, 1);
                optograph.setIs_on_server(true);
            }
            cache.save(Cache.UPLOAD_ON_GOING, false);
            notifyItemChanged(mPosition);
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
        apiConsumer.uploadOptoImage(opto.getId(), fbodyMain, OptoImagePreviewFragment.optoType360, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                Log.d("myTag", "onResponse uploadImage isSuccess? " + response.isSuccess());
                Log.d("myTag", "onResponse message: " + response.message());
                Log.d("myTag", "onResponse body: " + response.body());
                Log.d("myTag", "onResponse raw: " + response.raw());
                if (face.equals("l"))
                    opto.getLeftFace().setStatusByIndex(side, response.isSuccess());
                else opto.getRightFace().setStatusByIndex(side, response.isSuccess());
                updateFace(opto, face, side, response.isSuccess() ? 1 : 0);

                Log.d("myTag", "after: ");
                int ctr = 0;
                for (boolean i : opto.getLeftFace().getStatus()) {
                    Log.d("myTag", "left " + ctr + ": " + i);
                    ctr += 1;
                }
                int ctr2 = 0;
                for (boolean i : opto.getRightFace().getStatus()) {
                    Log.d("myTag", "right " + ctr2 + ": " + i);
                    ctr2 += 1;
                }
                flag = response.isSuccess() ? 1 : 0;
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag", "onFailure uploadImage: " + t.getMessage());
                if (face.equals("l")) opto.getLeftFace().setStatusByIndex(side, false);
                else opto.getRightFace().setStatusByIndex(side, false);
//                notifyItemRangeChanged(position,1);
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

        mydb.updateFace(opto.getId(), column, value);
    }

    @Override
    public int getItemCount() {
        return optographs.size();
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

    public void addItem(Optograph optograph) {
        if (optograph == null) {
            return;
        }

        if (!optograph.is_local() || optograph.is_on_server()) {
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
        optograph = checkToDB(optograph);
        Log.d("myTag"," opto null? "+(optograph==null));
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
        Log.d("myTag","checkToDB getcount: "+res.getCount());
        if (res.getCount()==0) {
//            deleteOptographFromPhone(optograph.getId());
            return null;
        }
        Log.d("myTag","checkToDb shouldPub? "+(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) == 1)+" delAt: "+res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_DELETED_AT)));
        if (res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) == 1 || !res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_DELETED_AT)).equals("")) {
//            deleteOptographFromPhone(optograph.getId());
            return null;
        }
        if (mydb.checkIfAllImagesUploaded(optograph.getId())) return null;
        optograph.setStitcher_version(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_STITCHER_VERSION)));
        optograph.setText(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_TEXT)));
//        optograph.setCreated_at(res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_CREATED_AT)));
        optograph.setIs_on_server(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_ON_SERVER)) != 0);
        optograph.setShould_be_published(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) != 0);
        optograph.setIs_place_holder_uploaded(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_PLACEHOLDER_UPLOADED)) != 0);
        optograph.setIs_data_uploaded(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_DATA_UPLOADED)) != 0);
        optograph.setPostFacebook(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_FACEBOOK)) != 0);
        optograph.setPostTwitter(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_TWITTER)) != 0);
        optograph.setPostInstagram(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_POST_INSTAGRAM)) != 0);
        Cursor face = mydb.getData(optograph.getId(),DBHelper.FACES_TABLE_NAME,DBHelper.FACES_ID);
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

    public static class LocalViewHolder extends RecyclerView.ViewHolder {
        private GridItemLocalBinding binding;
        private ImageView optograph2DCubeView_local;
        private ImageButton upload_Local;

        public LocalViewHolder(View rowView) {
            super(rowView);
            this.binding = DataBindingUtil.bind(rowView);
            optograph2DCubeView_local = (ImageView) itemView.findViewById(R.id.optograph2dview_local);
            upload_Local = (ImageButton) itemView.findViewById(R.id.upload_local);
        }

        public GridItemLocalBinding getBinding() {return binding;}
    }
}
