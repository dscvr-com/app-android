package co.optonaut.optonaut.record;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.drawable.RotateDrawable;
import android.hardware.Camera;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import co.optonaut.optonaut.OptonautApp;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.sensors.CoreMotionListener;
import co.optonaut.optonaut.util.CameraUtils;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.util.Maths;
import co.optonaut.optonaut.util.Vector3;
import co.optonaut.optonaut.views.dialogs.CancelRecordingDialog;
import co.optonaut.optonaut.views.redesign.MainActivityRedesign;
import timber.log.Timber;

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

                Recorder.push(bitmap, extrinsicsData);

                // progress bar
                ((MainActivityRedesign) getActivity()).setProgressLocation((float)(Recorder.getRecordedImagesCount()) / (float)(Recorder.getImagesToRecordCount()));

                // tilt angle
//                Log.d(TAG, "Ball Distance : " + Recorder.getDistanceToBall());
//                float[] angularDistanceToBall = Recorder.getAngularDistanceToBall();
//                for(int i = 0; i < angularDistanceToBall.length; i++)
//                    Log.d(TAG, "Ball Angular Distance " + i + " : "+ angularDistanceToBall[i]);

                ((MainActivityRedesign) getActivity()).setAngleRotation(Recorder.getAngularDistanceToBall()[2]);
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

        View view = inflater.inflate(R.layout.fragment_record, container, false);

        // Create an instance of Camera
        tryToInitializeCamera();

        // initialize recorder
        float[] size = CameraUtils.getCameraResolution(view.getContext(), 0);
        Recorder.initializeRecorder(CameraUtils.CACHE_PATH, size[0], size[1], camera.getParameters().getFocalLength());

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
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
        CoreMotionListener.unregister();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

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

        // start a background thread to finish recorder
        OptonautApp.getInstance().getJobManager().addJobInBackground(new FinishRecorderJob());

        ((MainActivityRedesign) getActivity()).backToFeed(false);
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

//        float maxSpeed = Recorder.hasStarted() ? 0.008f : 0.08f;
//        float accelleration = Recorder.hasStarted() ? 0.1f : 0.5f;
//        float[] vector = {0, 0, 1, 0};
//        float[] newPosition = new float[4];
//        Matrix.multiplyMV(newPosition, 0, Recorder.getBallPosition(), 0, vector, 0);
//        Vector3 target = new Vector3(newPosition[0], newPosition[1], newPosition[2]);
//
//        if(ballPosition.x == 0 && ballPosition.y == 0 && ballPosition.z == 0) {
//            ballPosition = target;
//        } else {
//            Vector3 newSpeed = Vector3.subtract(target, ballPosition);
//            float dist = Vector3.length(newSpeed);
//
//            if(dist > maxSpeed) {
//                newSpeed = Vector3.multiply(Vector3.normalize(newSpeed), maxSpeed);
//            }
//
//            newSpeed.subtract(ballSpeed);
//            newSpeed.multiply(accelleration);
//            newSpeed.add(ballSpeed);
//            ballSpeed = newSpeed;
//            ballPosition.add(ballSpeed);
//        }
//
//        recorderOverlayView.getRecorderOverlayRenderer().setSpherePosition(ballPosition.x, ballPosition.y, ballPosition.z);

/** iOS code
   let maxSpeed = recorder.hasStarted() ? Float(0.008) : Float(0.08)
        let accelleration = recorder.hasStarted() ? Float(0.1) : Float(0.5)
        let vec = GLKVector3Make(0, 0, -1)
        let target = GLKMatrix4MultiplyVector3(recorder.getNextKeyframePosition(), vec)
        let ball = SCNVector3ToGLKVector3(ballNode.position)
        if ball.x == 0 && ball.y == 0 && ball.z == 0 {
            ballNode.position = SCNVector3FromGLKVector3(target)
        } else {
            var newSpeed = GLKVector3Subtract(target, ball)
            let dist = GLKVector3Length(newSpeed)
            if dist > maxSpeed
                newSpeed = GLKVector3MultiplyScalar(GLKVector3Normalize(newSpeed), maxSpeed)
            newSpeed = GLKVector3Subtract(newSpeed, ballSpeed)
            newSpeed = GLKVector3MultiplyScalar(newSpeed, accelleration)
            newSpeed = GLKVector3Add(newSpeed, ballSpeed)
            ballSpeed = newSpeed;
            ballNode.position = SCNVector3FromGLKVector3(GLKVector3Add(ball, ballSpeed))
        }
**/
    }

}
