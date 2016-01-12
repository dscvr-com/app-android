package co.optonaut.optonaut.views;


import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.util.Constants;

/**
 * @author Nilan Marktanner
 * @date 2015-12-30
 */
public class VRModeActivity extends CardboardActivity {

    private CardboardRenderer cardboardRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrmode);

        AssetManager am = this.getAssets();
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeStream(am.open("lava.bmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);

        cardboardRenderer = new CardboardRenderer(bmp);
        cardboardView.setRenderer(cardboardRenderer);
        // might use this for performance boost...
        // cardboardView.setRestoreGLStateEnabled(false);

        setCardboardView(cardboardView);
    }
}