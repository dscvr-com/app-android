package co.optonaut.optonaut.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

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
        queueEvent(() -> {
            optographRenderer.updateTexture(texture);
        });
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        // do nothing
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        // do nothing
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
}