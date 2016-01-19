package co.optonaut.optonaut.views;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.squareup.picasso.Picasso;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.util.ImageUrlBuilder;
import co.optonaut.optonaut.opengl.Cube;
import co.optonaut.optonaut.util.Constants;

/**
 * @author Nilan Marktanner
 * @date 2015-12-30
 */
public class VRModeActivity extends CardboardActivity {

    private CardboardRenderer cardboardRenderer;
    private Optograph optograph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    }

    private void initializeTextures() {
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
                throw new RuntimeException("No optograph reveiced in VRActivity!");
            } else {
                Log.d(Constants.DEBUG_TAG, "Creating VRActivity for Optograph " + optograph.getId());
            }
        }
    }
}