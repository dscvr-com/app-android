package com.iam360.iam360.views.new_design;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import com.iam360.iam360.record.GlobalState;
import com.iam360.iam360.record.Recorder;
import com.iam360.iam360.record.Stitcher;
import com.iam360.iam360.util.CameraUtils;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-02-13
 */
public class CancelRecorderJob extends Job {
    protected CancelRecorderJob() {
        super(new Params(1));
    }

    @Override
    public void onAdded() {
        Timber.v("CancelRecorderJob added to disk");
    }

    @Override
    public void onRun() throws Throwable {
        Timber.v("finishing Recorder...");
        Recorder.finish();
        Timber.v("disposing Recorder...");
        Recorder.disposeRecorder();

        Timber.v("clearing Stitcher");
        Stitcher.clear(CameraUtils.CACHE_PATH + "left", CameraUtils.CACHE_PATH + "shared");
        Stitcher.clear(CameraUtils.CACHE_PATH + "right", CameraUtils.CACHE_PATH + "shared");
        Timber.v("CancelRecorderJobfinished");
        GlobalState.isAnyJobRunning = false;
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
        Timber.e("CancelRecorderJob has been canceled");
    }
}
