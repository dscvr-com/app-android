package com.iam360.iam360.views.new_design;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.SizeF;
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
import com.iam360.iam360.record.Edge;
import com.iam360.iam360.record.GlobalState;
import com.iam360.iam360.record.LineNode;
import com.iam360.iam360.record.RecordPreview;
import com.iam360.iam360.record.Recorder;
import com.iam360.iam360.record.RecorderOverlayView;
import com.iam360.iam360.record.SelectionPoint;
import com.iam360.iam360.sensors.CoreMotionListener;
import com.iam360.iam360.util.CameraUtils;
import com.iam360.iam360.util.Constants;
import com.iam360.iam360.util.Maths;
import com.iam360.iam360.util.Vector3;
import com.iam360.iam360.views.dialogs.CancelRecordingDialog;
import com.iam360.iam360.views.record.RecorderPreviewView;

import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-02-08
 */
public class RecordFragment extends Fragment {
    private String TAG = RecordFragment.class.getSimpleName();
    private RecorderPreviewView recordPreview;
    private RecorderOverlayView recorderOverlayView;
    private Vector3 ballPosition = new Vector3();
    private Vector3 ballSpeed = new Vector3();
    private SelectionPoint lastKeyframe;

    private float exposureDuration;
    private float sensorWidthInMeters = 0.004f;
    private long time = -1;
    private int captureWidth;
    private int mode;

    private boolean fromPause = false;

    // TODO: use this map
    private Map<Edge, LineNode> edgeLineNodeMap = new HashMap<>();

    // Map globalIds of the edge's selection points : LineNode
    private Map<String, LineNode> edgeLineNodeGlobalIdMap = new HashMap<>();

    private CancelRecordingDialog cancelRecordingDialog;

    private RecorderPreviewView.RecorderPreviewListener previewListener = new RecorderPreviewView.RecorderPreviewListener() {
        @Override
        public void imageDataReady(byte[] data, int width, int height, Bitmap.Config colorFormat) {
            if (Recorder.isFinished()) {
                // sync hack
                return;
            }
            assert colorFormat == Bitmap.Config.ARGB_8888;

            // build extrinsics
            float[] coreMotionMatrix = CoreMotionListener.getInstance().getRotationMatrix();
            double[] extrinsicsData = Maths.convertFloatsToDoubles(coreMotionMatrix);

            Recorder.push(data, width, height, extrinsicsData);

            // progress bar
            ((RecorderActivity) getActivity()).setProgressLocation((float) (Recorder.getRecordedImagesCount()) / (float) (Recorder.getImagesToRecordCount()));

            // normal towards ring
            float angle = Recorder.getAngularDistanceToBall()[2];
            ((RecorderActivity) getActivity()).setAngleRotation(angle);

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

            ((RecorderActivity) getActivity()).setArrowRotation((float) Math.atan2(angularDiff[0], angularDiff[1]));
            ((RecorderActivity) getActivity()).setArrowVisible(distXY > 0.15 ? true : false);
            ((RecorderActivity) getActivity()).setGuideLinesVisible((Math.abs(angle) > 0.05 && distXY < 0.15)? true : false);

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
        }

        @Override
        public void cameraOpened(CameraDevice device) {
            CameraManager cameraManager = (CameraManager)getActivity().getSystemService(Context.CAMERA_SERVICE);
            try {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(device.getId());
                // initialize recorder
                SizeF size = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                float focalLength = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0];
                Recorder.initializeRecorder(CameraUtils.CACHE_PATH, size.getWidth(), size.getHeight(), focalLength, mode);

                setupSelectionPoints();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void cameraClosed(CameraDevice device) {

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

        this.mode = getArguments().getInt("mode");
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        // Create our Preview view and set it as the content of our activity.
        recordPreview = new RecorderPreviewView(getActivity());
        recordPreview.setPreviewListener(previewListener);
        recorderOverlayView = new RecorderOverlayView(getActivity());
        FrameLayout preview = (FrameLayout) view.findViewById(R.id.record_preview);
        preview.addView(recordPreview);
        preview.addView(recorderOverlayView);

        cancelRecordingDialog = new CancelRecordingDialog();

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        CoreMotionListener.register();
        recordPreview.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();
        recordPreview.onPause();
        CoreMotionListener.unregister();
        fromPause = true;
    }

    /**
     *
     */
    public void startRecording() {
        Timber.v("Starting recording...");

        recorderOverlayView.getRecorderOverlayRenderer().startRendering();

        // Todo: lock exposure
        /*
        Camera.Parameters parameters = camera.getParameters();
        parameters.setAutoExposureLock(true);
        parameters.setAutoWhiteBalanceLock(true);
        camera.setParameters(parameters);

        camera.setPreviewCallback(previewCallback);
*/
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
        UUID id = UUID.randomUUID();

        // start a background thread to finish recorder
        OptonautApp.getInstance().getJobManager().addJobInBackground(new FinishRecorderJob(id));

//        ((MainActivityRedesign) getActivity()).backToFeed(false);//original
        ((RecorderActivity) getActivity()).startPreview(id);
    }

    public void cancelRecording() {
        GlobalState.isAnyJobRunning = true;

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


    }

}
