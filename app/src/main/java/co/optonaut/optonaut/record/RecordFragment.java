package co.optonaut.optonaut.record;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.JobManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import co.optonaut.optonaut.OptonautApp;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.opengl.Cube;
import co.optonaut.optonaut.sensors.CoreMotionListener;
import co.optonaut.optonaut.util.CameraUtils;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.util.Maths;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-02-08
 */
public class RecordFragment extends Fragment {
    private Camera camera;
    private Cube cube;
    private RecordPreview recordPreview;
    private RecorderOverlayView recorderOverlayView;
    private Map<Edge, LineNode> edgeLineNodeMap = new HashMap<>();

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            int imageFormat = parameters.getPreviewFormat();
            if (imageFormat == ImageFormat.NV21) {
                // TODO - get matrix as int
                int[] imageAsARGB8888 = CameraUtils.convertYUV420_NV21toARGB8888(data,
                        parameters.getPreviewSize().width,
                        parameters.getPreviewSize().height);

                Bitmap bitmap = Bitmap.createBitmap(Constants.getInstance().getDisplayMetrics(), imageAsARGB8888, parameters.getPreviewSize().width, parameters.getPreviewSize().height, Bitmap.Config.ARGB_8888);

                // build extrinsics
                float[] coreMotionMatrix = CoreMotionListener.getInstance().getRotationMatrix();
                double[] extrinsicsData = Maths.convertFloatsToDoubles(coreMotionMatrix);

                if (Recorder.isFinished()) {
                    // TODO: change mode to POST_RECORD
                    Snackbar.make(recordPreview, "Recording is finished, please wait for the result!", Snackbar.LENGTH_LONG).show();

                    // queue finishing on main thread
                    queueFinishRecording();
                } else {
                    Recorder.push(bitmap, extrinsicsData);
                    updateBallPosition();
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
        Recorder.initializeRecorder(CameraUtils.STORAGE_PATH, size[0], size[1], camera.getParameters().getFocalLength());

        // Create our Preview view and set it as the content of our activity.
        recordPreview = new RecordPreview(getActivity(), camera);
        recorderOverlayView = new RecorderOverlayView(getActivity());
        FrameLayout preview = (FrameLayout) view.findViewById(R.id.record_preview);
        preview.addView(recordPreview);
        preview.addView(recorderOverlayView);

        setupSelectionPoints();

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
        // TODO: rotate camera coordinates like this http://stackoverflow.com/a/18874394/1176596
        // TODO: set size like this http://stackoverflow.com/a/11009422/1176596
        Timber.v("Starting recording...");
        recorderOverlayView.getRecorderOverlayRenderer().startRendering();
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
                float[] vector = {0, 0, -1, 0};
                float[] posA = new float[4];
                float[] posB = new float[4];
                Matrix.multiplyMV(posA, 0, a.getExtrinsics(), 0, vector, 0);
                Matrix.multiplyMV(posB, 0, b.getExtrinsics(), 0, vector, 0);

                LineNode edgeNode = new LineNode(posA, posB);
                edgeLineNodeMap.put(edge, edgeNode);
                recorderOverlayView.addChildNode(edgeNode);
            }
        }
    }

    public void finishRecording() {
        releaseCamera();
        // TODO: open VRModeActivity

        // start a background thread to finish recorder
        OptonautApp.getInstance().getJobManager().addJobInBackground(new FinishRecorderJob());
    }

    private void updateBallPosition() {
        // TODO: use -0.9f - error must be somewhere else
        float[] vector = {0, 0, 0.9f, 0};
        float[] newPosition = new float[4];
        Matrix.multiplyMV(newPosition, 0, Recorder.getBallPosition(), 0, vector, 0);
        recorderOverlayView.getRecorderOverlayRenderer().setCubePosition(newPosition[0], newPosition[1], newPosition[2]);
    }


}
