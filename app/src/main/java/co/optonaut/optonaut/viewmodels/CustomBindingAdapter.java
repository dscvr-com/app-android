package co.optonaut.optonaut.viewmodels;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import co.optonaut.optonaut.network.ImageHandler;
import co.optonaut.optonaut.opengl.MyGLSurfaceView;

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
                .into(imageView);
    }

    @BindingAdapter("app:avatarId")
    public static void loadSmallImage(ImageView imageView, String asset_id) {
        Picasso.with(imageView.getContext()).
                load(ImageHandler.buildImageUrl(asset_id, 50, 50))
                .into(imageView);
    }

    @BindingAdapter("app:textureId")
    public static void loadTexture(MyGLSurfaceView myGLSurfaceView, String texture_id) {
        Picasso.with(myGLSurfaceView.getContext())
                .load(ImageHandler.buildImageUrl(texture_id, 7000, 2050))
                .networkPolicy(NetworkPolicy.NO_STORE, NetworkPolicy.NO_CACHE) // disable caching
                .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE) // disable caching
                .into(myGLSurfaceView);
    }
}