package co.optonaut.optonaut.views;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
public class VRModeActivity extends CardboardActivity implements Target {

    private Bitmap texture;
    private CardboardRenderer cardboardRenderer;
    private Optograph optograph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrmode);

        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);

        cardboardRenderer = new CardboardRenderer();

        cardboardView.setRenderer(cardboardRenderer);
        // might use this for performance boost...
        // cardboardView.setRestoreGLStateEnabled(false);
        setCardboardView(cardboardView);

        initalizeOptograph();
        loadTextures();
    }

    private void initalizeOptograph() {
        Intent intent = getIntent();
        if (intent != null) {
            this.optograph = intent.getExtras().getParcelable("optograph");
            if (this.optograph == null) {
                throw new RuntimeException("Failed to initialize optograph!");
            } else {
                Log.d(Constants.DEBUG_TAG, "Loaded optograph " + optograph.getId() + " in VRMode!");
            }
        }
    }

    private void loadTextures() {
        Picasso.with(this)
                .load(ImageHandler.buildTextureUrl(this.optograph.getLeft_texture_asset_id()))
                .into(this);
    }

    private void setTexture() {
        cardboardRenderer.setTexture(Eye.Type.LEFT, texture);
    }
    @Override
    public void onResume() {
        super.onResume();
        if (texture != null) {
            setTexture();
        }
    }


    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        Log.d(Constants.DEBUG_TAG, "Received texture in VRMode");
        texture = bitmap;
        setTexture();
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {

    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
    }
}