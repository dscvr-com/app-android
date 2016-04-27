package co.optonaut.optonaut.views;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.bus.BusProvider;
import co.optonaut.optonaut.bus.RecordFinishedEvent;
import co.optonaut.optonaut.bus.RecordFinishedPreviewEvent;
import co.optonaut.optonaut.database.DBHelper;
import co.optonaut.optonaut.model.LogInReturn;
import co.optonaut.optonaut.model.OptoData;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.network.ApiConsumer;
import co.optonaut.optonaut.util.Cache;
import co.optonaut.optonaut.util.CameraUtils;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.views.redesign.MainActivityRedesign;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Created by Mariel on 4/13/2016.
 */
public class OptoImagePreviewFragment extends Fragment {

    @Bind(R.id.statusbar) RelativeLayout statusbar;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.exit_button) Button exitButton;
    @Bind(R.id.retry_button) Button retryButton;
//    @Bind(R.id.description_box) TextView descBox;
    @Bind(R.id.post_later_group) RelativeLayout postLaterButton;
    @Bind(R.id.post_later_progress) ProgressBar postLaterProgress;
    @Bind(R.id.upload_progress) ProgressBar uploadProgress;
    @Bind(R.id.upload_group) RelativeLayout uploadButton;
    @Bind(R.id.preview_image) KenBurnsView previewImage;

    private Optograph optographGlobal;
    private String optographId;
    protected ApiConsumer apiConsumer;

    DBHelper mydb;
    boolean doneUpload;
    private Cache cache;
    private String userToken="";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_image_preview,container,false);
        cache = Cache.open();

        mydb = new DBHelper(getActivity());
        userToken = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(userToken);
        doneUpload = false;
        optographId = getArguments().getString("id");//randomUUID
        Optograph optograph = new Optograph(optographId);
        optographGlobal = optograph;
        /*final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 1min
                if (userToken != null && !userToken.isEmpty()) {
                    uploadOptonautData(optograph);
                } else {
                    Snackbar.make(view, "Must login to upload.", Snackbar.LENGTH_SHORT);
                }
                createDefaultOptograph(optograph);
            }
        }, 60000);*/

        ButterKnife.bind(this, view);

        initializeToolbar();

        exitButton = (Button) view.findViewById(R.id.exit_button);
        retryButton = (Button) view.findViewById(R.id.retry_button);
//        descBox = (TextView) view.findViewById(R.id.description_box);
        postLaterButton = (RelativeLayout) view.findViewById(R.id.post_later_group);
        postLaterProgress = (ProgressBar) view.findViewById(R.id.post_later_progress);
        uploadProgress = (ProgressBar) view.findViewById(R.id.upload_progress);
        uploadButton = (RelativeLayout) view.findViewById(R.id.upload_group);

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        statusbar = (RelativeLayout) view.findViewById(R.id.statusbar);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Timber.v("kitkat");
            statusbar.setVisibility(View.VISIBLE);
        } else {
            statusbar.setVisibility(View.GONE);
        }

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userToken==null || userToken.isEmpty()) {
                    Snackbar.make(v,"Must login to upload.",Snackbar.LENGTH_SHORT);
                } else if (doneUpload) {
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED, 1);
                    mydb.updateColumnOptograph(optographId,DBHelper.OPTOGRAPH_PERSON_ID,cache.getString(Cache.USER_ID));
                    getLocalImage(optograph);
                }
            }
        });

        postLaterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (doneUpload) {
                    mydb.updateColumnOptograph(optographId,DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED,0);
                    ((MainActivityRedesign) getActivity()).backToFeed();
                }
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitDialog();
            }
        });

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                retryDialog();
                exitDialog();
            }
        });

        return view;
    }

    private boolean createDefaultOptograph(Optograph opto) {
        return mydb.insertOptograph(opto.getId(),"",cache.getString(Cache.USER_ID),"",opto.getCreated_atRFC3339(),
                opto.getDeleted_at(),0,0,0,0,opto.getStitcher_version(),0,0,"",1,0);
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
                    Toast toast = Toast.makeText(getActivity(), "Failed to upload.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                Optograph opto = response.body();
                if (opto == null) {
                    Log.d("myTag", "parsing the JSON body failed.");
                    Toast toast = Toast.makeText(getActivity(), "Failed to upload", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                Log.d("myTag", " success: id: " + opto.getId() + " personName: " + opto.getPerson().getUser_name());
                // do things for success
                optographGlobal = optograph;
                uploadPlaceHolder(optograph);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag", " onFailure: " + t.getMessage());
                uploadProgress.setVisibility(View.INVISIBLE);
                uploadButton.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(),"No Internet Connection.",Toast.LENGTH_SHORT);
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

                    uploadImage(optographGlobal, s, face);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            postLaterProgress.setVisibility(View.GONE);
            uploadProgress.setVisibility(View.GONE);
            doneUpload = true;
        }
    }

    private int flag=2;
    private boolean uploadImage(Optograph opto, String filePath,String face) {
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
                .addFormDataPart("key", face)
                .build();
        Log.d("myTag","asset: "+face+fileName+" key: "+face+ fileName.replace(".jpg",""));
        apiConsumer.uploadOptoImage(opto.getId(), fbodyMain, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                Log.d("myTag", "onResponse uploadPlaceHolderImage isSuccess? " + response.isSuccess());
                Log.d("myTag", "onResponse message: " + response.message());
                Log.d("myTag", "onResponse body: " + response.body());
                Log.d("myTag", "onResponse raw: " + response.raw());

                flag = response.isSuccess() ? 1 : 0;
                optographGlobal.setIs_place_holder_uploaded(true);
                doneUpload = true;
                mydb.updateColumnOptograph(optographId,DBHelper.OPTOGRAPH_IS_PLACEHOLDER_UPLOADED,1);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag", "onFailure uploadImage: " + t.getMessage());
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

    private void initializeToolbar() {
        float scale = Constants.getInstance().getDisplayMetrics().density;
        int marginTop = (int) (25 * scale + 0.5f);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
        lp.setMargins(0, marginTop, 0, 0);
        toolbar.setLayoutParams(lp);
//        FrameLayout.LayoutParams lp1 = (FrameLayout.LayoutParams) previewImage.getLayoutParams();
//        lp1.setMargins(0,marginTop+,0,0);
//        previewImage.setLayoutParams(lp1);
    }

    private void deleteOptographFromDB() {
        mydb.deleteEntry(DBHelper.OPTO_TABLE_NAME, DBHelper.OPTOGRAPH_ID, optographGlobal.getId());
        mydb.deleteEntry(DBHelper.FACES_TABLE_NAME, DBHelper.FACES_ID, optographGlobal.getId());
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
                    /*for (String s : file.list()) {
//                        Log.d("myTag", "list of file: " + s);
                        (new File(file.getPath()+"/"+s)).delete();
                    }*/
//                    optographs.add(new Optograph(file.getName()));
                } else {
                    // ignore
                }
            }
            boolean result = dir.delete();
            Log.d("myTag", "getName: " + dir.getName() + " getPath: " + dir.getPath()+" delete: "+result);
        }
    }

    private void exitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_exit_from_preview)
                .setPositiveButton(getResources().getString(R.string.dialog_discard), (dialog, which) -> {
                    //how can i call an Activity here???
//                    deleteOptograph();// error occurred with this line because the fragment was unattached before the execution finished.
                    deleteOptographFromPhone(optographId);
                    ((MainActivityRedesign) getActivity()).backToFeed();
                }).setNegativeButton(getResources().getString(R.string.dialog_keep), (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void retryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_retry_recording)
                .setPositiveButton(getResources().getString(R.string.dialog_retry), (dialog, which) -> {
                    //how can i call the RecordingFragment here???
                    MainActivityRedesign activity = (MainActivityRedesign) getActivity();
                    activity.retryRecording();
                }).setNegativeButton(getResources().getString(R.string.dialog_keep), (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }

    public void onBackPressed() {
        exitDialog();
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
                if (file.isDirectory()) {
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

        new UploadCubeImages().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filePathList);
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

                    uploadFaceImage(optographGlobal, s, face, side);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Cursor res = mydb.getData(optographGlobal.getId(),DBHelper.FACES_TABLE_NAME,DBHelper.FACES_ID);
            res.moveToFirst();
            if (res.getCount()==0) return;
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
            cache.save(Cache.UPLOAD_ON_GOING, false);
            if (mydb.checkIfAllImagesUploaded(optographId)) {
                mydb.updateColumnOptograph(optographId,DBHelper.OPTOGRAPH_IS_ON_SERVER,1);
            }
        }
    }

    private boolean uploadFaceImage(Optograph opto, String filePath,String face,int side) {
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

        mydb.updateFace(opto.getId(), column, value);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void receivePreviewImage(RecordFinishedPreviewEvent recordFinishedPreviewEvent) {
        Timber.d("receivePreviewImage");
//https://github.com/flavioarfaria/KenBurnsView
        previewImage.setImageBitmap(recordFinishedPreviewEvent.getPreviewImage());

        createDefaultOptograph(optographGlobal);
        if (userToken != null && !userToken.isEmpty()) {
            uploadOptonautData(optographGlobal);
        } else {
            Log.d("myTag"," must login to upload data");
        }
    }

    @Subscribe
    public void receiveFinishedImage(RecordFinishedEvent recordFinishedEvent) {
        Timber.d("receiveFinishedImage");
        postLaterProgress.setVisibility(View.GONE);
        uploadProgress.setVisibility(View.GONE);
        doneUpload = true;
    }

    @Subscribe
    public void receiveFinishEvent(RecordFinishedEvent recordFinishedEvent) {
        Timber.d("recordFinishedEvent");
    }

}
