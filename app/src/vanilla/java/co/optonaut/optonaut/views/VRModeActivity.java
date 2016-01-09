package co.optonaut.optonaut.views;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.network.ImageHandler;
import co.optonaut.optonaut.util.Constants;

/**
 * @author Nilan Marktanner
 * @date 2015-12-30
 */
public class VRModeActivity extends CardboardActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrmode);

        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);

        /*
        leftTarget = new SphereTarget(Eye.Type.LEFT);
        rightTarget = new SphereTarget(Eye.Type.RIGHT);
        loadTextures();
        */
        CardboardRendererTest cardboardRenderer = new CardboardRendererTest();

        cardboardView.setRenderer(cardboardRenderer);
        // might use this for performance boost...
        cardboardView.setRestoreGLStateEnabled(false);
        setCardboardView(cardboardView);

    }
}