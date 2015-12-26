package co.optonaut.optonaut.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * @author Nilan Marktanner
 * @date 2015-12-23
 */
public class Optograph2DView extends GLSurfaceView implements Target {
    private OptographRenderer optographRenderer;
    private Bitmap texture;


    public Optograph2DView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public Optograph2DView(Context context) {
        super(context);
        initialize(context);
    }

    private void initialize(Context context) {
        setEGLContextClientVersion(2);
        optographRenderer = new OptographRenderer(context);
        setRenderer(optographRenderer);
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
            optographRenderer.updateTexture(texture);
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

    @Override
    public void onPause() {
        super.onPause();
    }

    public OptographRenderer getOptographRenderer() {
        return optographRenderer;
    }

    /*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float curX, curY;
        float mX, mY;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // TODO: scroll view
                mX = event.getX();
                mY = event.getY();

                unregisterRotationVectorListener();

                return true;
            case MotionEvent.ACTION_UP:
                curX = event.getX();
                curY = event.getY();

                // TODO: scroll

                registerRotationVectorListener();

                return true;

            case MotionEvent.ACTION_MOVE:
                curX = event.getX();
                curY = event.getY();

                // TODO: scroll
                mX = curX;
                mY = curY;

                return true;
            default:
                return false;
        }
    }
    */
}