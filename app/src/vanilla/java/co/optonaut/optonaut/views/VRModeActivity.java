package co.optonaut.optonaut.views;


import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.squareup.picasso.Picasso;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.opengl.Cube;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.util.ImageUrlBuilder;
import co.optonaut.optonaut.views.redesign.MainActivityRedesign;

/**
 * @author Nilan Marktanner
 * @date 2015-12-30
 */
public class VRModeActivity extends CardboardActivity implements SensorEventListener {
    private CardboardRenderer cardboardRenderer;
    private Optograph optograph;

    private boolean inVRMode;
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Constants.initializeConstants(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrmode);
        initializeOptograph();
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardRenderer = new CardboardRenderer();
        cardboardView.setRenderer(cardboardRenderer);

        // might use this for performance boost...
        // cardboardView.setRestoreGLStateEnabled(false);
        setCardboardView(cardboardView);

        initializeTextures();

        inVRMode = true;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerAccelerationListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterAccelerationListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerAccelerationListener();
        inVRMode = true;
    }

    private void registerAccelerationListener() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterAccelerationListener() {
        sensorManager.unregisterListener(this);
    }

    private void initializeTextures() {
        if (optograph == null) {
            return;
        }
        String leftId = this.optograph.getLeft_texture_asset_id();
        for (int i = 0; i < Cube.FACES.length; ++i) {
            Picasso.with(this)
                    .load(ImageUrlBuilder.buildCubeUrl(leftId, Cube.FACES[i]))
                    .into(cardboardRenderer.getLeftCube().getCubeTextureSet().getTextureTarget(Cube.FACES[i]));
        }

        String rightId = this.optograph.getRight_texture_asset_id();
        for (int i = 0; i < Cube.FACES.length; ++i) {
            Picasso.with(this)
                    .load(ImageUrlBuilder.buildCubeUrl(rightId, Cube.FACES[i]))
                    .into(cardboardRenderer.getRightCube().getCubeTextureSet().getTextureTarget(Cube.FACES[i]));
        }
    }

    private void initializeOptograph() {
        Intent intent = getIntent();
        if (intent != null) {
            this.optograph = intent.getExtras().getParcelable("optograph");
            if (optograph == null) {
                //throw new RuntimeException("No optograph reveiced in VRActivity!");
                Log.d(Constants.DEBUG_TAG, "No optograph reveiced in VRActivity!");
            } else {
                Log.d(Constants.DEBUG_TAG, "Creating VRActivity for Optograph " + optograph.getId());
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // only listen for Accelerometer if we did not switch to normal mode yet
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && inVRMode) {
            // switch to normal mode if phone was turned
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if (x + Constants.ACCELERATION_EPSILON < y) {
                switchToNormalMode();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void switchToNormalMode() {
        Log.d(Constants.DEBUG_TAG, "Switched to Normal Mode");
        inVRMode = false;
        Intent intent = new Intent(this, MainActivityRedesign.class);

        this.finish();

        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // TODO: retain optograph feed adapter in MainFeedFragment! then activate this hack...
        // switchToNormalMode();
    }
}