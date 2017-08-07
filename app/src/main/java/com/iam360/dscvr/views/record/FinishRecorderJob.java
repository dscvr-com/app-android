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

        final String leftPath = CameraUtils.CACHE_PATH + "left/";
        final String rightPath = CameraUtils.CACHE_PATH + "right/";
        final String sharedPath = CameraUtils.CACHE_PATH + "shared/";


        Bitmap leftEQ = Stitcher.getResult(leftPath, sharedPath);
        CameraUtils.savePanoramaToLocationWithExif(leftEQ, CameraUtils.PERSISTENT_STORAGE_PATH + id + "_1.jpg");

        Timber.v("EQ Size: " + leftEQ.getWidth() + "x" + leftEQ.getHeight());

        Bitmap[] leftFaces = Stitcher.getCubeMap(leftEQ);
        leftEQ.recycle();

        for (int i = 0; i < leftFaces.length; ++i) {
            CameraUtils.saveBitmapToLocation(leftFaces[i], CameraUtils.CACHE_PATH + id + "/left/" + i + ".jpg");
            leftFaces[i].recycle();
        }

        Bitmap rightEQ = Stitcher.getResult(rightPath, sharedPath);

        Bitmap[] rightFaces = Stitcher.getCubeMap(rightEQ);
        rightEQ.recycle();

        for (int i = 0; i < rightFaces.length; ++i) {
            CameraUtils.saveBitmapToLocation(rightFaces[i], CameraUtils.CACHE_PATH + id + "/right/" + i + ".jpg");
            rightFaces[i].recycle();
        }

        MixpanelHelper.trackStitchingFinish(getApplicationContext());

        Timber.v("FinishRecorderJob finished");
        Stitcher.clear(leftPath, sharedPath);
        Stitcher.clear(rightPath, sharedPath);


        GlobalState.isAnyJobRunning = false;
        GlobalState.shouldHardRefreshFeed = true;
        BusProvider.getInstance().post(new RecordFinishedEvent(true));
        Timber.v("finish all job");
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
                                                     int maxRunCount) {
        Log.e(TAG,"error while Finishing", throwable);
        GlobalState.isAnyJobRunning = false;
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
