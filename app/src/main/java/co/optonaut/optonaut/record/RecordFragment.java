package co.optonaut.optonaut.record;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
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

import co.optonaut.optonaut.R;
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

                Recorder.pushImage(bitmap, extrinsicsData);
            } else {
                throw new UnsupportedOperationException("Wrong preview format.");
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_record, container, false);

        // Create an instance of Camera
        camera = CameraUtils.getCameraInstance();

        if (camera == null) {
            Timber.d("Could not access camera in Record fragment");
        } else {
            // initialize recorder
            float[] size = CameraUtils.getCameraResolution(view.getContext(), 0);
            Recorder.initializeRecorder(CameraUtils.STORAGE_PATH, size[0], size[1], camera.getParameters().getFocalLength());
        }

        // Create our Preview view and set it as the content of our activity.
        recordPreview = new RecordPreview(getActivity(), camera);
        recorderOverlayView = new RecorderOverlayView(getActivity());
        FrameLayout preview = (FrameLayout) view.findViewById(R.id.record_preview);
        preview.addView(recordPreview);
        preview.addView(recorderOverlayView);

        setupSelectionPoints();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        CoreMotionListener.register();
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
        CoreMotionListener.unregister();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public void startRecord() {
        // TODO: rotate camera coordinates like this http://stackoverflow.com/a/18874394/1176596
        // TODO: set size like this http://stackoverflow.com/a/11009422/1176596
        if (camera != null) {
            camera.setPreviewCallback(previewCallback);
        }
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
        // TODO: mixpanel
        camera.stopPreview();
        camera.setPreviewCallback(null);

        Recorder.finish();
        Recorder.dispose();
    }
}
