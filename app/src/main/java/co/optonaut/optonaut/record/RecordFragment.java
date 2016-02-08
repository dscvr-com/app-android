package co.optonaut.optonaut.record;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.util.CameraUtils;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-02-08
 */
public class RecordFragment extends Fragment {
    private Camera camera;
    private RecordPreview recordPreview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_record, container, false);

        // Create an instance of Camera
        camera = CameraUtils.getCameraInstance();

        if (camera == null) {
            Timber.d("Could not access camera in Record fragment");
        }

        // Create our Preview view and set it as the content of our activity.
        recordPreview = new RecordPreview(getActivity(), camera);
        FrameLayout preview = (FrameLayout) view.findViewById(R.id.record_preview);
        preview.addView(recordPreview);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
