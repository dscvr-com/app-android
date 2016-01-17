package co.optonaut.optonaut.opengl;

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

import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.network.ImageHandler;
import co.optonaut.optonaut.util.Constants;

/**
 * @author Nilan Marktanner
 * @date 2015-12-23
 */
public class Optograph2DCubeView extends GLSurfaceView{
    private SensorManager sensorManager;
    private Optograph2DCubeRenderer optograph2DCubeRenderer;
    private static int maxId = 0;
    private int id = 0;

    private Optograph optograph;


    public Optograph2DCubeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public Optograph2DCubeView(Context context) {
        super(context);
        initialize(context);
    }

    private void initialize(Context context) {
        setEGLContextClientVersion(2);
        id = maxId++;
        optograph2DCubeRenderer = new Optograph2DCubeRenderer(context, id);
        setRenderer(optograph2DCubeRenderer);

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        registerRotationVectorListener();
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(Constants.DEBUG_TAG, "onResume");
        // TODO: update texture set
        registerRotationVectorListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(Constants.DEBUG_TAG, "onPause");
        unregisterRotationVectorListener();
    }


    private void registerRotationVectorListener() {
        sensorManager.registerListener(optograph2DCubeRenderer, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_UI);
    }

    private void unregisterRotationVectorListener() {
        sensorManager.unregisterListener(optograph2DCubeRenderer);
    }

    @Override
    public int getId() {
        return id;
    }

    public void hardResetTexture() {
        // TODO: clear texture set
    }

    public void initializeTextures() {
        for (int i = 0; i < Cube.FACES.length; ++i) {
            Picasso.with(getContext())
                    .load(ImageHandler.buildCubeUrl(this.optograph.getLeft_texture_asset_id(), Cube.FACES[i]))
                    .into(optograph2DCubeRenderer.getTextureTarget(Cube.FACES[i]));
        }
    }

    public void setOptograph(Optograph optograph) {
        this.optograph = optograph;
        initializeTextures();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Optograph2DCubeView that = (Optograph2DCubeView) o;

        if (id != that.id) return false;
        if (sensorManager != null ? !sensorManager.equals(that.sensorManager) : that.sensorManager != null)
            return false;
        if (optograph2DCubeRenderer != null ? !optograph2DCubeRenderer.equals(that.optograph2DCubeRenderer) : that.optograph2DCubeRenderer != null)
            return false;
        return !(optograph != null ? !optograph.equals(that.optograph) : that.optograph != null);

    }

    @Override
    public int hashCode() {
        int result = sensorManager != null ? sensorManager.hashCode() : 0;
        result = 31 * result + (optograph2DCubeRenderer != null ? optograph2DCubeRenderer.hashCode() : 0);
        result = 31 * result + id;
        result = 31 * result + (optograph != null ? optograph.hashCode() : 0);
        return result;
    }
}
