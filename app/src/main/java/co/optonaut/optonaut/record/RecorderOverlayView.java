package co.optonaut.optonaut.record;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
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

        // we want to use this as overlay above the camera preview - while not covering other views such as OverlayNavigationFragment
        setZOrderMediaOverlay(true);

        // not sure why we need this, but without it we can't get transparency!
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        recorderOverlayRenderer = new RecorderOverlayRenderer();
        setRenderer(recorderOverlayRenderer);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    public void addChildNode(LineNode edgeNode) {
        recorderOverlayRenderer.addChildNode(edgeNode);
    }

    public void colorChildNode(LineNode edgeNode) {
        recorderOverlayRenderer.colorChildNode(edgeNode);
    }

    public float[] getPointOnScreen(float[] point) {
        return recorderOverlayRenderer.getPointOnScreen(point);
    }

    public RecorderOverlayRenderer getRecorderOverlayRenderer() {
        return recorderOverlayRenderer;
    }


}
