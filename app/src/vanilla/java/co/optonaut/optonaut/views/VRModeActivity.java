package co.optonaut.optonaut.views;


import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.opengl.Cube;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.util.ImageUrlBuilder;
import co.optonaut.optonaut.util.MixpanelHelper;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-12-30
 */
public class VRModeActivity extends CardboardActivity implements SensorEventListener {
    private static final int MILLISECONDS_THRESHOLD_FOR_SWITCH = 500;

    private CardboardRenderer cardboardRenderer;
    private Optograph optograph;

    private DateTime creationTime;
    private boolean thresholdForSwitchReached;

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
        MixpanelHelper.trackViewViewerVR(this);

        creationTime = DateTime.now();
        thresholdForSwitchReached = false;
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
        creationTime = DateTime.now();
        inVRMode = true;
    }

    @Override
    public void onDestroy() {
        MixpanelHelper.flush(this);
        super.onDestroy();
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

        for (int i = 0; i < Cube.FACES.length; ++i) {
            Picasso.with(this)
                    .load(ImageUrlBuilder.buildCubeUrl(optograph.getId(), true, Cube.FACES[i]))
                    .into(cardboardRenderer.getLeftCube().getCubeTextureSet().getTextureTarget(Cube.FACES[i]));
        }

        for (int i = 0; i < Cube.FACES.length; ++i) {
            Picasso.with(this)
                    .load(ImageUrlBuilder.buildCubeUrl(optograph.getId(), false, Cube.FACES[i]))
                    .into(cardboardRenderer.getRightCube().getCubeTextureSet().getTextureTarget(Cube.FACES[i]));
        }
    }

    private void initializeOptograph() {
        Intent intent = getIntent();
        if (intent != null) {
            this.optograph = intent.getExtras().getParcelable("optograph");
            if (optograph == null) {
                //throw new RuntimeException("No optograph reveiced in VRActivity!");
                Timber.e("No optograph reveiced in VRActivity!");
            } else {
                Timber.v("creating VRActivity for Optograph %s", optograph.getId());
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // only listen for Accelerometer if we did not switch to normal mode yet
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && inVRMode) {
            if (!thresholdForSwitchReached) {
                Interval timePassed = new Interval(creationTime, DateTime.now());
                Duration  duration = timePassed.toDuration();
                long millisecondsPassed = duration.getMillis();
                if (millisecondsPassed > MILLISECONDS_THRESHOLD_FOR_SWITCH) {
                    thresholdForSwitchReached = true;
                } else {
                    // don't switch
                    return;
                }
            }

            // switch to normal mode if phone was turned
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float length = (float) Math.sqrt(x*x + y*y);
            if (length < Constants.MINIMUM_AXIS_LENGTH) {
                return;
            }

            if (x + Constants.ACCELERATION_EPSILON < y) {
                switchToNormalMode();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void switchToNormalMode() {
        Timber.v("switching to feed mode");
        inVRMode = false;
        this.finish();
    }

    @Override
    public void onBackPressed() {
        // TODO: stop endless switching loop with timer?
        // switchToNormalMode();
    }
}