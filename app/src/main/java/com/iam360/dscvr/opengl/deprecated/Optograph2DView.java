package com.iam360.dscvr.opengl.deprecated;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
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
    private SensorManager sensorManager;
    private OptographRenderer optographRenderer;
    private Bitmap texture;
    private static int maxId = 0;
    private int id = 0;
    private String optographTextureId;


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
        id = maxId++;
        optographRenderer = new OptographRenderer(context, id);
        setRenderer(optographRenderer);

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        registerRotationVectorListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (texture != null) {
            setTexture();
        }
        registerRotationVectorListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterRotationVectorListener();
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        // Log.d(Constants.DEBUG_TAG, "from " + from + " into view " + String.valueOf(id));
        texture = bitmap;
        setTexture();
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        Log.d("Optonaut", "Could not load bitmap");
        //Log.d("Optonaut", errorDrawable.toString());
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        if (texture != null) {
            queueClearTexture();
        }
    }

    private void queueClearTexture() {
        queueEvent(optographRenderer::clearTexture);
    }

    private void setTexture() {
        optographRenderer.setTexture(texture);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Optograph2DView that = (Optograph2DView) o;

        if (sensorManager != null ? !sensorManager.equals(that.sensorManager) : that.sensorManager != null)
            return false;
        if (optographRenderer != null ? !optographRenderer.equals(that.optographRenderer) : that.optographRenderer != null)
            return false;
        return !(texture != null ? !texture.equals(that.texture) : that.texture != null);
    }

    @Override
    public int hashCode() {
        int result = sensorManager != null ? sensorManager.hashCode() : 0;
        result = 31 * result + (optographRenderer != null ? optographRenderer.hashCode() : 0);
        result = 31 * result + (texture != null ? texture.hashCode() : 0);
        return result;
    }

    @Override
    public int getId() {
        return id;
    }

    public void hardResetContent() {
        optographRenderer.resetContent();
    }

    public void rebindTexture() {
        // Log.d(Constants.DEBUG_TAG, "Queue rebindTexture in view " + id);
       setTexture();
    }

    public void setOptographTextureId(String optographTextureId) {
        this.optographTextureId = optographTextureId;
    }

    public String getOptographTextureId() {
        return optographTextureId;
    }
}
