package co.optonaut.optonaut.record;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import co.optonaut.optonaut.util.CameraUtils;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-02-12
 */
public class FinishRecorderJob extends Job {
    protected FinishRecorderJob() {
        // TODO: persist job?
        super(new Params(1));
    }

    @Override
    public void onAdded() {
        Timber.v("FinishRecorderJob added to disk");
    }

    @Override
    public void onRun() throws Throwable {
        Timber.v("finishing Recorder...");
        if (!Recorder.isFinished()) {
            Timber.e("Recorder is not finished yet, but was tried to finish and dispose!");
            throw new RuntimeException();
        }
        Recorder.finish();
        Timber.v("disposing Recorder...");
        Recorder.dispose();
        Timber.v("Stitcher is getting result...");
        Stitcher.getResult(CameraUtils.STORAGE_PATH + "left", CameraUtils.STORAGE_PATH + "shared");
        Timber.v("FinishRecorderJob finished");
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
        Timber.e("FinishRecorderJob has been canceled");
    }
}
