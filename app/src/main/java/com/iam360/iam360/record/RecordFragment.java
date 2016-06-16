package com.iam360.iam360.record;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.iam360.iam360.OptonautApp;
import com.iam360.iam360.R;
import com.iam360.iam360.sensors.CoreMotionListener;
import com.iam360.iam360.util.CameraUtils;
import com.iam360.iam360.util.Constants;
import com.iam360.iam360.util.Maths;
import com.iam360.iam360.util.Vector3;
import com.iam360.iam360.views.dialogs.CancelRecordingDialog;
import com.iam360.iam360.views.MainActivityRedesign;
import timber.log.Timber;
// EJ 16.6.16 - This is an old implementation, don't work here.

/**
 * @author Nilan Marktanner
 * @date 2016-02-08
 */
public class RecordFragment extends Fragment {
    private String TAG = RecordFragment.class.getSimpleName();
    private Camera camera;
    private RecordPreview recordPreview;
    private RecorderOverlayView recorderOverlayView;
    private Vector3 ballPosition = new Vector3();
    private Vector3 ballSpeed = new Vector3();
    private SelectionPoint lastKeyframe;

    private float exposureDuration;
    private float sensorWidthInMeters = 0.004f;
    private long time = -1;
    private int captureWidth;

    private boolean fromPause = false;

    // TODO: use this map
    private Map<Edge, LineNode> edgeLineNodeMap = new HashMap<>();

    // Map globalIds of the edge's selection points : LineNode
    private Map<String, LineNode> edgeLineNodeGlobalIdMap = new HashMap<>();

    private CancelRecordingDialog cancelRecordingDialog;

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            int imageFormat = parameters.getPreviewFormat();
            if (imageFormat == ImageFormat.NV21) {
                if (Recorder.isFinished()) {
                    // sync hack
                    return;
                }
                // TODO - get matrix as int
                int[] imageAsARGB8888 = CameraUtils.convertYUV420_NV21toARGB8888(data,
                        parameters.getPreviewSize().width,
                        parameters.getPreviewSize().height);

                Bitmap bitmap = Bitmap.createBitmap(Constants.getInstance().getDisplayMetrics(), imageAsARGB8888, parameters.getPreviewSize().width, parameters.getPreviewSize().height, Bitmap.Config.ARGB_8888);

                // build extrinsics
                float[] coreMotionMatrix = CoreMotionListener.getInstance().getRotationMatrix();
                double[] extrinsicsData = Maths.convertFloatsToDoubles(coreMotionMatrix);

                captureWidth = parameters.getPictureSize().width;

                //Recorder.push(bitmap, extrinsicsData);

                // progress bar
                ((MainActivityRedesign) getActivity()).setProgressLocation((float) (Recorder.getRecordedImagesCount()) / (float) (Recorder.getImagesToRecordCount()));

                // normal towards ring
                float angle = Recorder.getAngularDistanceToBall()[2];
                ((MainActivityRedesign) getActivity()).setAngleRotation(angle);

                float[] unit = {0, 0, 1, 0};
                Vector3 ballHeading = new Vector3(ballPosition);
                ballHeading.normalize();

                float[] currentHeading = new float[4];
                Matrix.multiplyMV(currentHeading, 0, Recorder.getCurrentRotation(), 0, unit, 0);
                Vector3 currentHeadingVec = new Vector3(currentHeading[0], currentHeading[1], currentHeading[2]);

                // Use 3D diff as dist
                Vector3 diff = Vector3.subtract(ballHeading, currentHeadingVec);
                float distXY = diff.length();

                // Helpers for bearing and distance. Relative to ball.
                float[] angularBallHeading = recorderOverlayView.getPointOnScreen(new float[]{ballPosition.x, ballPosition.y, ballPosition.z, 0});
                float[] angularCurrentHeading = recorderOverlayView.getPointOnScreen(currentHeading);

                float[] angularDiff = new float[2];
                angularDiff[0] = angularBallHeading[0] - angularCurrentHeading[0];
                angularDiff[1] = angularBallHeading[1] - angularCurrentHeading[1];

                ((MainActivityRedesign) getActivity()).setArrowRotation((float) Math.atan2(angularDiff[0], angularDiff[1]));
                ((MainActivityRedesign) getActivity()).setArrowVisible(distXY > 0.15 ? true : false);
                ((MainActivityRedesign) getActivity()).setGuideLinesVisible((Math.abs(angle) > 0.05 && distXY < 0.15)? true : false);

                updateBallPosition();

                // shading of recorded nodes
                if (Recorder.hasStarted()) {
                    SelectionPoint currentKeyframe = Recorder.lastKeyframe();

                    if (lastKeyframe == null) {
                        lastKeyframe = currentKeyframe;
                    } else if (currentKeyframe.getGlobalId() != lastKeyframe.getGlobalId()) {
                        Edge recordedEdge = new Edge(lastKeyframe, currentKeyframe);
                        if(edgeLineNodeGlobalIdMap.get(recordedEdge.getGlobalIds()) != null)
                            recorderOverlayView.colorChildNode(edgeLineNodeGlobalIdMap.get(recordedEdge.getGlobalIds()));
                        lastKeyframe = currentKeyframe;
                    }
                }

                if (Recorder.isFinished()) {
                    // TODO: change mode to POST_RECORD
                    Snackbar.make(recordPreview, "Recording is finished, please wait for the result!", Snackbar.LENGTH_LONG).show();

                    // queue finishing on main thread
                    queueFinishRecording();
                }

            } else {
                throw new UnsupportedOperationException("Wrong preview format.");
            }
        }
    };

    private void queueFinishRecording() {
        // see: http://stackoverflow.com/a/11125271/1176596

        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(getActivity().getMainLooper());
        mainHandler.post(this::finishRecording);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        int mode = getArguments().getInt("mode");
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        // Create an instance of Camera
        tryToInitializeCamera();

        // initialize recorder
        float[] size = CameraUtils.getCameraResolution(view.getContext(), 0);
        Recorder.initializeRecorder(CameraUtils.CACHE_PATH, size[0], size[1], camera.getParameters().getFocalLength(), mode);

        // Create our Preview view and set it as the content of our activity.
        recordPreview = new RecordPreview(getActivity(), camera);
        recorderOverlayView = new RecorderOverlayView(getActivity());
        FrameLayout preview = (FrameLayout) view.findViewById(R.id.record_preview);
        preview.addView(recordPreview);
        preview.addView(recorderOverlayView);

        setupSelectionPoints();

        cancelRecordingDialog = new CancelRecordingDialog();

        return view;
    }

    private void tryToInitializeCamera() {
        if (camera == null) {
            // Create an instance of Camera
            camera = CameraUtils.getCameraInstance();
        }

        if (camera == null) {
            throw new RuntimeException("Could not access camera in Record fragment");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        CoreMotionListener.register();

        // Create an instance of Camera
        tryToInitializeCamera();

//        if(fromPause) {
////            recorderOverlayView.getRecorderOverlayRenderer().startRendering();
//
////            Camera.Parameters parameters = camera.getParameters();
////            parameters.setAutoExposureLock(true);
////            parameters.setAutoWhiteBalanceLock(true);
////            camera.setParameters(parameters);
//
//            camera.setPreviewCallback(previewCallback);
//
//            Recorder.setIdle(false);
//            fromPause = false;
//        }
//
//        recordPreview.setCamera(camera);

    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
        CoreMotionListener.unregister();
        fromPause = true;
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    /**
     *
     */
    public void startRecording() {
        Timber.v("Starting recording...");

        recorderOverlayView.getRecorderOverlayRenderer().startRendering();

        Camera.Parameters parameters = camera.getParameters();
        parameters.setAutoExposureLock(true);
        parameters.setAutoWhiteBalanceLock(true);
        camera.setParameters(parameters);

        camera.setPreviewCallback(previewCallback);

        Recorder.setIdle(false);
    }

    private void setupSelectionPoints() {
        SelectionPoint[] rawPoints = Recorder.getSelectionPoints();
        List<SelectionPoint> points = new LinkedList<>();
        List<SelectionPoint> points2 = new LinkedList<>();

        for (SelectionPoint p : rawPoints) {
            points.add(p);
            points2.add(p);
        }

        points2.remove(0);

        for (int i = 0; i < points2.size(); ++i) {
            SelectionPoint a = points.get(i);
            SelectionPoint b = points2.get(i);
            if (a.getRingId() == b.getRingId()) {
                Edge edge = new Edge(a, b);
                float[] vector = {0, 0, 1, 0};
                float[] posA = new float[4];
                float[] posB = new float[4];
                Matrix.multiplyMV(posA, 0, a.getExtrinsics(), 0, vector, 0);
                Matrix.multiplyMV(posB, 0, b.getExtrinsics(), 0, vector, 0);

                LineNode edgeNode = new LineNode(posA, posB);
                edgeLineNodeMap.put(edge, edgeNode);
                edgeLineNodeGlobalIdMap.put(edge.getGlobalIds(), edgeNode);
                recorderOverlayView.addChildNode(edgeNode);
            }
        }
    }

    public void finishRecording() {
        GlobalState.isAnyJobRunning = true;
        releaseCamera();
        UUID id = UUID.randomUUID();

        // start a background thread to finish recorder
        OptonautApp.getInstance().getJobManager().addJobInBackground(new FinishRecorderJob(id));

//        ((MainActivityRedesign) getActivity()).backToFeed(false);//original
        ((MainActivityRedesign) getActivity()).startPreview(id);
    }

    public void cancelRecording() {
        GlobalState.isAnyJobRunning = true;
        releaseCamera();

        // start background thread to cancel recorder
        OptonautApp.getInstance().getJobManager().addJobInBackground(new CancelRecorderJob());
    }

    private void updateBallPosition() {
        // TODO: use -0.9f - error must be somewhere else
        float[] vector = {0, 0, 1, 0};
        float[] newPosition = new float[4];
        Matrix.multiplyMV(newPosition, 0, Recorder.getBallPosition(), 0, vector, 0);

        recorderOverlayView.getRecorderOverlayRenderer().setSpherePosition(newPosition[0], newPosition[1], newPosition[2]);
        ballPosition.set(newPosition[0], newPosition[1], newPosition[2]);

         // Quick hack to limit expo duration in calculations, due to unexpected results of CACurrentMediaTime
//         float exposureDuration = (float)Math.max(this.exposureDuration, 0.006);
//
//         float ballSphereRadius = 1.0f; // Don't put it on 1, since it would overlap with the rings then.
//         float movementPerFrameInPixels = 6000;
//
//         Calendar calendar = Calendar.getInstance();
//         long newTime = calendar.getTimeInMillis();
//
//        float[] vector = {0, 0, 1, 0};
//        float[] newPosition = new float[4];
//        Matrix.multiplyMV(newPosition, 0, Recorder.getBallPosition(), 0, vector, 0);
//
//        Vector3 target = new Vector3(newPosition[0], newPosition[1], newPosition[2]);
//        Vector3 ball = new Vector3(ballPosition);
//
//         if (!Recorder.hasStarted()) {
//             ballPosition.set(newPosition[0], newPosition[1], newPosition[2]);
//         } else {
//
//             float timeDiff = (float)(newTime - time) / 1000f;
//             // Speed per second
//             float maxRecordingSpeedInRadiants = sensorWidthInMeters * movementPerFrameInPixels / ((float)(captureWidth) * exposureDuration);
//
//             float maxRecordingSpeed = ballSphereRadius * maxRecordingSpeedInRadiants;
//
//             float maxSpeed = maxRecordingSpeed * timeDiff;
//
//             float accelleration = (!Recorder.isIdle() ? (maxRecordingSpeed / 10) : (maxRecordingSpeed)) / 30;
//
//             Vector3 newHeading = Vector3.subtract(target, ballPosition);
//
//             float dist = Vector3.length(newHeading);
//             float curSpeed = Vector3.length(ballSpeed);
//
//             // We have to actually break.
//             if (Math.sqrt(dist / accelleration) >= dist / curSpeed)
//                curSpeed -= accelleration;
//             else curSpeed += accelleration;
//
//             // Limit speed
//             if (curSpeed < 0) curSpeed = 0;
//
//             if (curSpeed > maxSpeed)
//                curSpeed = Math.signum(curSpeed) * maxSpeed;
//
//             if (curSpeed > dist)
//                curSpeed = dist;
//
//             if (newHeading.length() != 0)
//                ballSpeed = Vector3.multiply(Vector3.normalize(newHeading), curSpeed);
//             else
//                ballSpeed = newHeading;
//
//             ballPosition = Vector3.add(ball, ballSpeed);
//         }
//
//         time = newTime;
//
//        // use ball position
////        recorderOverlayView.getRecorderOverlayRenderer().setSpherePosition(newPosition[0], newPosition[1], newPosition[2]);
//        recorderOverlayView.getRecorderOverlayRenderer().setSpherePosition(ballPosition.x, ballPosition.y, ballPosition.z);


    }

}
