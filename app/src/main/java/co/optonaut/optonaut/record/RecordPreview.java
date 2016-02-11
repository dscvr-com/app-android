package co.optonaut.optonaut.record;

import android.content.Context;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

import co.optonaut.optonaut.util.CameraUtils;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-02-07
 */
public class RecordPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder;
    private Camera camera;

    private MediaActionSound mediaActionSound;

    public RecordPreview(Context context) {
        super(context);
        initialize();
    }

    public RecordPreview(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        initialize();
    }

    private void initialize() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        if (camera != null) {
            // use in portrait mode
            camera.setDisplayOrientation(90);
        }

        mediaActionSound = new MediaActionSound();
        mediaActionSound.load(MediaActionSound.START_VIDEO_RECORDING);
        mediaActionSound.load(MediaActionSound.STOP_VIDEO_RECORDING);
    }

    public RecordPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Timber.v("surfaceCreated preview");
        startPreview(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Timber.v("surfaceChanged preview");
        if (surfaceHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        Camera.Parameters parameters = camera.getParameters();
        parameters.setRotation(90);
        //parameters.setPictureSize(CameraUtils.getBiggestSupportedPictureSize(camera));

        // start preview with new settings
        startPreview(surfaceHolder);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // do nothing - release camera in activity
    }

    private void startPreview(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Timber.d(e, "Error setting camera preview");
        }
    }
}
