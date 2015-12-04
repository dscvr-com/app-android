package co.optonaut.optonaut.viewmodels;

import android.databinding.BindingAdapter;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import co.optonaut.optonaut.network.ImageHandler;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */

/**
 * For more information read http://developer.android.com/reference/android/databinding/BindingAdapter.html
 */
public class CustomBindingAdapter {
    @BindingAdapter("app:assetId")
    public static void loadImage(ImageView imageView, String asset_id) {
        String url = ImageHandler.buildImageUrl(asset_id, 500, 500);
        Picasso.with(imageView.getContext()).load(url).into(imageView);
    }
}