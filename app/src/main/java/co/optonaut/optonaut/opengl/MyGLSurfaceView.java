package co.optonaut.optonaut.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ScaleGestureDetector;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * @author Nilan Marktanner
 * @date 2015-12-23
 */
public class MyGLSurfaceView extends GLSurfaceView implements Target {
    private GL2Renderer gl2Renderer;
    private Bitmap texture;
    // TODO: add scale support
    //private final ScaleGestureDetector scaleGestureDetector;

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    private void initialize(Context context) {
        setEGLContextClientVersion(2);
        gl2Renderer = new GL2Renderer(context);
        setRenderer(gl2Renderer);
    }

    public MyGLSurfaceView(Context context) {
        super(context);
        initialize(context);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        texture = bitmap;
        queueBitmap();
    }

    private void queueBitmap() {
        Log.d("Optonaut", "Queue Event");
        queueEvent(() -> {
            Log.d("Optonaut", "Execute Event");
            gl2Renderer.updateTexture(texture);
        });
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {

    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (texture != null) {
            queueBitmap();
        }
    }
    ;
        /*
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            return scaleGestureDetector.onTouchEvent(motionEvent);
        }


    private class ScaleDetectorListener implements ScaleGestureDetector.OnScaleGestureListener {

        float zoomScale = 1;

        float scaleFocusX = 0;
        float scaleFocusY = 0;

        public boolean onScale(ScaleGestureDetector arg0) {
            gl2Renderer.setScale(gl2Renderer.getScale() * arg0.getScaleFactor());
            return true;
        }

        public boolean onScaleBegin(ScaleGestureDetector arg0) {
            invalidate();

            scaleFocusX = arg0.getFocusX();
            scaleFocusY = arg0.getFocusY();

            return true;
        }

        public void onScaleEnd(ScaleGestureDetector arg0) {
            scaleFocusX = 0;
            scaleFocusY = 0;
        }
    }
    */
}