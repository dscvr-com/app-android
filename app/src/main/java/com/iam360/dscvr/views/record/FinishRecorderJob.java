package com.iam360.dscvr.views.record;

import android.graphics.Bitmap;

import com.iam360.dscvr.DscvrApp;
import com.iam360.dscvr.record.Alignment;
import com.iam360.dscvr.views.UploaderJob;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import java.util.UUID;

import com.iam360.dscvr.bus.BusProvider;
import com.iam360.dscvr.bus.RecordFinishedEvent;
import com.iam360.dscvr.bus.RecordFinishedPreviewEvent;
import com.iam360.dscvr.record.GlobalState;
import com.iam360.dscvr.record.Recorder;
import com.iam360.dscvr.record.Stitcher;
import com.iam360.dscvr.util.CameraUtils;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-02-12
 */
public class FinishRecorderJob extends Job {

    private UUID id;
    public FinishRecorderJob(UUID uuid) {
        // TODO: persist job?
        super(new Params(1));
        this.id = uuid;
    }

    @Override
    public void onAdded() {
        Timber.v("FinishRecorderJob added to disk");
    }

    @Override
    public void onRun() throws Throwable {

        Timber.v("finishing Recorder...");
        Recorder.finish();
        Timber.v("Sending event");

//TODO        http://stackoverflow.com/questions/15431768/how-to-send-event-from-service-to-activity-with-otto-event-bus

        Bitmap previewBitmap = null;
        if(Recorder.previewAvailable()) {
            previewBitmap = Recorder.getPreviewImage();
            BusProvider.getInstance().post(new RecordFinishedPreviewEvent(previewBitmap));
            Timber.v("post of placeholder");
        }

        if(previewBitmap != null)
            CameraUtils.saveBitmapToLocation(previewBitmap, CameraUtils.PERSISTENT_STORAGE_PATH + id + "/preview/placeholder.jpg");
        Timber.v("after save of placeholder");

        Timber.v("disposing Recorder...");
        Recorder.disposeRecorder();
        Timber.v("Stitcher is getting result...");

        Alignment.align(CameraUtils.CACHE_PATH + "post/", CameraUtils.CACHE_PATH + "shared/", CameraUtils.CACHE_PATH);

        Bitmap[] bitmaps = Stitcher.getResult(CameraUtils.CACHE_PATH + "left/", CameraUtils.CACHE_PATH + "shared/");
//        UUID id = UUID.randomUUID();
        for (int i = 0; i < bitmaps.length; ++i) {
            CameraUtils.saveBitmapToLocation(bitmaps[i], CameraUtils.PERSISTENT_STORAGE_PATH + id + "/left/" + i + ".jpg");
            bitmaps[i].recycle();
        }

        Bitmap eqmap = Stitcher.getEQResult(CameraUtils.CACHE_PATH + "left/", CameraUtils.CACHE_PATH + "shared/");
        CameraUtils.saveBitmapToLocationEQ(eqmap, CameraUtils.PERSISTENT_STORAGE_PATH + id + "_1.jpg");

        bitmaps = Stitcher.getResult(CameraUtils.CACHE_PATH + "right/", CameraUtils.CACHE_PATH + "shared/");
        for (int i = 0; i < bitmaps.length; ++i) {
            CameraUtils.saveBitmapToLocation(bitmaps[i], CameraUtils.PERSISTENT_STORAGE_PATH + id + "/right/" + i + ".jpg");
            bitmaps[i].recycle();
        }

        Timber.v("FinishRecorderJob finished");
        Stitcher.clear(CameraUtils.CACHE_PATH + "preview/", CameraUtils.CACHE_PATH + "shared/");
        Stitcher.clear(CameraUtils.CACHE_PATH + "left/", CameraUtils.CACHE_PATH + "shared/");
        Stitcher.clear(CameraUtils.CACHE_PATH + "right/", CameraUtils.CACHE_PATH + "shared/");

        // TODO: fire event or otherwise handle refresh
        BusProvider.getInstance().post(new RecordFinishedEvent());
        GlobalState.isAnyJobRunning = false;
        GlobalState.shouldHardRefreshFeed = true;
        Timber.v("finish all job");

        DscvrApp.getInstance().getJobManager().addJobInBackground(new UploaderJob(id));

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
        GlobalState.isAnyJobRunning = false;
        Timber.e("FinishRecorderJob has been canceled");
    }
}
