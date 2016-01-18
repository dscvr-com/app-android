package co.optonaut.optonaut.viewmodels;

import android.databinding.BindingAdapter;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.network.ImageHandler;
import co.optonaut.optonaut.opengl.Optograph2DCubeView;
import co.optonaut.optonaut.opengl.deprecated.Optograph2DView;
import co.optonaut.optonaut.util.Constants;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */

/**
 * For more information read http://developer.android.com/reference/android/databinding/BindingAdapter.html
 */
public class CustomBindingAdapter {
    @BindingAdapter("app:assetId")
    public static void loadMediumImage(ImageView imageView, String asset_id) {
        Picasso.with(imageView.getContext())
                .load(ImageHandler.buildImageUrl(asset_id, 500, 500))
                .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE) // don't store avatars in memory
                .into(imageView);
    }

    @BindingAdapter("app:avatarId")
    public static void loadSmallImage(ImageView imageView, String asset_id) {
        Picasso.with(imageView.getContext())
                .load(ImageHandler.buildImageUrl(asset_id, 50, 50))
                .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE) // don't store avatars in memory
                .into(imageView);
    }

    @BindingAdapter("app:textureId")
    public static void loadTexture(Optograph2DView optograph2DView, String texture_id) {
        if (texture_id.equals(optograph2DView.getOptographTextureId())) {
            Log.d(Constants.DEBUG_TAG, "Queing same texture...");
        } else {
            optograph2DView.setOptographTextureId(texture_id);
            Picasso.with(optograph2DView.getContext())
                    .load(ImageHandler.buildTextureUrl(texture_id))
                            //.networkPolicy(NetworkPolicy.NO_STORE, NetworkPolicy.NO_CACHE) // disable caching
                            //.memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE) // disable caching
                    .into(optograph2DView);
        }
    }

    @BindingAdapter("app:optograph")
    public static void loadOptograph(Optograph2DCubeView optograph2DCubeView, Optograph optograph) {
        optograph2DCubeView.setOptograph(optograph);
    }
}