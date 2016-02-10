package co.optonaut.optonaut.record;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * @author Nilan Marktanner
 * @date 2016-02-10
 */
public class RecorderOverlayView extends GLSurfaceView {
    private RecorderOverlayRenderer recorderOverlayRenderer;

    public RecorderOverlayView(Context context) {
        super(context);
        initialize();
    }

    public RecorderOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    private void initialize() {
        setEGLContextClientVersion(2);

        // TODO: use as overlay
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        recorderOverlayRenderer = new RecorderOverlayRenderer();
        setRenderer(recorderOverlayRenderer);
    }
}
