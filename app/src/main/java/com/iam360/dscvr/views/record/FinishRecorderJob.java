package com.iam360.dscvr.views.record;

import android.graphics.Bitmap;
import android.util.Log;

import com.iam360.dscvr.bus.BusProvider;
import com.iam360.dscvr.bus.RecordFinishedEvent;
import com.iam360.dscvr.record.ConvertToStereo;
import com.iam360.dscvr.record.GlobalState;
import com.iam360.dscvr.record.Recorder;
import com.iam360.dscvr.record.Stitcher;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.CameraUtils;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.MixpanelHelper;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import java.util.UUID;

import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-02-12
 */
public class FinishRecorderJob extends Job {

    private UUID id;
    private Cache cache;
    private static final String TAG = FinishRecorderJob.class.getSimpleName();
    int mode;

    public FinishRecorderJob(UUID uuid) {
        // TODO: persist job?
        super(new Params(1));
        this.id = uuid;
        cache = Cache.open();
        mode = cache.getInt(Cache.CAMERA_MODE);
    }

    @Override
    public void onAdded() {
        Timber.v("FinishRecorderJob added to disk");
    }

    @Override
    public void onRun() throws Throwable {

        MixpanelHelper.trackStitchingStart(getApplicationContext());
        Timber.v("finishing Recorder...");
        Recorder.finish();
        Timber.v("Sending event");

        Timber.v("disposing Recorder...");
        Recorder.disposeRecorder();
        Timber.v("Stitcher is getting result...");

//        Alignment.align(CameraUtils.CACHE_PATH + "post/", CameraUtils.CACHE_PATH + "shared/", CameraUtils.CACHE_PATH);
        if (mode == Constants.THREE_RING_MODE)
            ConvertToStereo.convert(CameraUtils.CACHE_PATH + "post/", CameraUtils.CACHE_PATH + "shared/", CameraUtils.CACHE_PATH);

        Bitmap[] bitmaps = Stitcher.getResult(CameraUtils.CACHE_PATH + "left/", CameraUtils.CACHE_PATH + "shared/", mode);
//        UUID id = UUID.randomUUID();
        for (int i = 0; i < bitmaps.length; ++i) {
            CameraUtils.saveBitmapToLocation(bitmaps[i], CameraUtils.PERSISTENT_STORAGE_PATH + id + "/left/" + i + ".jpg");
            bitmaps[i].recycle();
        }

        Bitmap eqmap = Stitcher.getEQResult(CameraUtils.CACHE_PATH + "left/", CameraUtils.CACHE_PATH + "shared/", mode);
        CameraUtils.saveBitmapToLocationEQ(eqmap, CameraUtils.PERSISTENT_STORAGE_PATH + id + "_1.jpg");

        bitmaps = Stitcher.getResult(CameraUtils.CACHE_PATH + "right/", CameraUtils.CACHE_PATH + "shared/", mode);
        for (int i = 0; i < bitmaps.length; ++i) {
            CameraUtils.saveBitmapToLocation(bitmaps[i], CameraUtils.PERSISTENT_STORAGE_PATH + id + "/right/" + i + ".jpg");
            bitmaps[i].recycle();
        }

        MixpanelHelper.trackStitchingFinish(getApplicationContext());
        Timber.v("FinishRecorderJob finished");
        Stitcher.clear(CameraUtils.CACHE_PATH + "left/", CameraUtils.CACHE_PATH + "shared/");
        Stitcher.clear(CameraUtils.CACHE_PATH + "right/", CameraUtils.CACHE_PATH + "shared/");


        GlobalState.isAnyJobRunning = false;
        GlobalState.shouldHardRefreshFeed = true;
        BusProvider.getInstance().post(new RecordFinishedEvent(true));
        Timber.v("finish all job");



    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
                                                     int maxRunCount) {
        Log.e(TAG,"error while Finishing", throwable);
        BusProvider.getInstance().post(new RecordFinishedEvent(false));
        // An error occurred in onRun.
        // Return value determines whether this job should retry or cancel. You can further
        // specifcy a backoff strategy or change the job's priority. You can also apply the
        // delay to the whole group to preserve jobs' running order.
        return RetryConstraint.CANCEL;
    }

    @Override
    protected void onCancel() {
        GlobalState.isAnyJobRunning = false;
        Timber.e("FinishRecorderJob has been canceled");
    }
}
