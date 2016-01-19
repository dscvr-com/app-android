package co.optonaut.optonaut.viewmodels;

import android.databinding.BindingAdapter;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.util.ImageUrlBuilder;
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
                .load(ImageUrlBuilder.buildImageUrl(asset_id, 500, 500))
                .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE) // don't store avatars in memory
                .into(imageView);
    }

    @BindingAdapter("app:avatarId")
    public static void loadSmallImage(ImageView imageView, String asset_id) {
        Picasso.with(imageView.getContext())
                .load(ImageUrlBuilder.buildImageUrl(asset_id, 50, 50))
                .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE) // don't store avatars in memory
                .into(imageView);
    }

    @BindingAdapter("app:optograph")
    public static void loadOptograph(Optograph2DCubeView optograph2DCubeView, Optograph optograph) {
        optograph2DCubeView.setOptograph(optograph);
    }
}