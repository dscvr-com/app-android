package co.optonaut.optonaut.record;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-02-07
 */
public class RecordPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder;
    private Camera camera;

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
    }

    public RecordPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
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
