package co.optonaut.optonaut.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import co.optonaut.optonaut.views.VRModeActivity;

/**
 * @author Nilan Marktanner
 * @date 2015-12-23
 */
public class Optograph2DView extends GLSurfaceView implements Target {
    private SensorManager sensorManager;
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

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        registerRotationVectorListener();
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
        registerRotationVectorListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterRotationVectorListener();
    }

    public OptographRenderer getOptographRenderer() {
        return optographRenderer;
    }

    private void registerRotationVectorListener() {
        sensorManager.registerListener(optographRenderer, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_UI);
    }

    private void unregisterRotationVectorListener() {
        sensorManager.unregisterListener(optographRenderer);
    }
}