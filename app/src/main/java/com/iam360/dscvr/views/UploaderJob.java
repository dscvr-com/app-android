package com.iam360.dscvr.views;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.iam360.dscvr.model.LogInReturn;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.network.ApiConsumer;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.CameraUtils;
import com.iam360.dscvr.util.DBHelper;
import com.iam360.dscvr.views.activity.OptoImagePreviewActivity;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class UploaderJob extends Job {

    private UUID id;
    private DBHelper mydb;
    private Cache cache;
    private ApiConsumer apiConsumer;
    private Optograph optograph;

    public UploaderJob(UUID uuid) {
        super(new Params(1));
        this.id = uuid;
        this.cache = Cache.open();

        String userToken = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(userToken.equals("")? null:userToken);
        optograph = new Optograph(uuid.toString());

    }

    @Override
    public void onAdded() {
        Timber.v("UploaderJob added to disk");
    }

    @Override
    public void onRun() throws Throwable {
        Timber.d("Uploader Job.");
        mydb = new DBHelper(getApplicationContext());

        // upload images only if tagged for upload
        if(checkIfForUpload()) getLocalImage(optograph);

    }

    /**
     *
     * @return true if for upload, false if for postlater
     */
    private boolean checkIfForUpload() {
        Cursor res = mydb.getData(id.toString(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);

        res.moveToFirst();
        if (res.getCount() == 0) return false;

        if(res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) == 0) return false;
        else return true;

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
                                                     int maxRunCount) {
        // An error occurred in onRun.
        // Return value determines whether this job should retry or cancel. You can further
        // specifcy a backoff strategy or change the job's priority. You can also apply the
        // delay to the whole group to preserve jobs' running order.
        return RetryConstraint.CANCEL;
    }

    @Override
    protected void onCancel() {
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
                        filePathList.add(file.getPath() + "/" + s);
                    }
                } else {
                    // ignore
                }
            }
        }

        new UploadCubeImages().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filePathList);
    }

    // try using AbstractQueuedSynchronizer
    class UploadCubeImages extends AsyncTask<List<String>, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(List<String>... params) {
            for (List<String> sL : params) {
                for (String s : sL) {
                    String[] s3 = s.split("/");
                    int side = Integer.valueOf((s3[s3.length - 1].split("\\."))[0]);
                    String face = s.contains("right") ? "r" : "l";

                    uploadFaceImage(optograph, s, face, side);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Cursor res = mydb.getData(id.toString(), DBHelper.FACES_TABLE_NAME, DBHelper.FACES_ID);
            res.moveToFirst();
            if (res.getCount() == 0) return;
            cache.save(Cache.UPLOAD_ON_GOING, false);
            if (mydb.checkIfAllImagesUploaded(id.toString())) {
                mydb.updateColumnOptograph(id.toString(), DBHelper.OPTOGRAPH_IS_ON_SERVER, true);
                mydb.updateColumnOptograph(id.toString(), DBHelper.OPTOGRAPH_IS_LOCAL, false);
            } else {
                mydb.updateColumnOptograph(id.toString(), DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED, false);
                mydb.updateColumnOptograph(id.toString(), DBHelper.OPTOGRAPH_IS_LOCAL, true);
            }
        }
    }



    private void uploadFaceImage(Optograph opto, String filePath, String face, int side) {
        String[] s2 = filePath.split("/");
        String fileName = s2[s2.length - 1];

        if (face.equals("l") && opto.getLeftFace().getStatus()[side]) {
            Log.d("myTag"," already uploaded: "+face+side);
            return;
        }
        else if (opto.getRightFace().getStatus()[side]) {
            Log.d("myTag"," already uploaded: "+face+side);
            return;
        }

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
        Log.d("myTag", "asset: " + face + fileName + " key: " + face + fileName.replace(".jpg", ""));
        apiConsumer.uploadOptoImage(opto.getId(), fbodyMain, (OptoImagePreviewActivity.optoType360), new Callback<LogInReturn.EmptyResponse>() {
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

            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag", "onFailure uploadImage: " + t.getMessage());
                if (face.equals("l")) opto.getLeftFace().setStatusByIndex(side, false);
                else opto.getRightFace().setStatusByIndex(side, false);
            }
        });
//        while (flag == 2) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                Thread.interrupted();
//            }
//        }
    }

    private void updateFace(Optograph opto, String face, int side, int value) {

        String column = "faces_";
        if (face.equals("l")) column += "left_";
        else column += "right_";

        if (side == 0) column += "zero";
        else if (side == 1) column += "one";
        else if (side == 2) column += "two";
        else if (side == 3) column += "three";
        else if (side == 4) column += "four";
        else if (side == 5) column += "five";

        mydb.updateFace(opto.getId(), column, value);
    }

}
