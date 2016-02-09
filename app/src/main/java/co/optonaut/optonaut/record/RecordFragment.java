package co.optonaut.optonaut.record;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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


    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = CameraUtils.getOutputMediaFile(CameraUtils.MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Timber.d("Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Timber.d(e, "File not found!");
            } catch (IOException e) {
                Timber.d(e, "Error accessing file!");
            }
        }
    };

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            File pictureFile = CameraUtils.getOutputMediaFile(CameraUtils.MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Timber.d("Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Timber.d(e, "File not found!");
            } catch (IOException e) {
                Timber.d(e, "Error accessing file!");
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

    public void startRecord() {
        // TODO: rotate camera coordinates like this http://stackoverflow.com/a/18874394/1176596
        // TODO: set size like this http://stackoverflow.com/a/11009422/1176596
        if (camera != null) {
            camera.setPreviewCallback(previewCallback);
        }
    }


}
