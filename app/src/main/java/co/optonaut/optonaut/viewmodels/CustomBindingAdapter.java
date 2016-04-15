package co.optonaut.optonaut.viewmodels;

import android.databinding.BindingAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.model.Person;
import co.optonaut.optonaut.opengl.Cube;
import co.optonaut.optonaut.opengl.Optograph2DCubeView;
import co.optonaut.optonaut.util.ImageUrlBuilder;
import co.optonaut.optonaut.util.RFC3339DateFormatter;
import co.optonaut.optonaut.util.TimeUtils;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */

/**
 * For more information read http://developer.android.com/reference/android/databinding/BindingAdapter.html
 */
public class CustomBindingAdapter {
    @BindingAdapter("app:person")
    public static void loadMediumImage(ImageView imageView, Person person) {
        String personId = person.getId();
        String assetId = person.getAvatar_asset_id();
        Picasso.with(imageView.getContext())
                .load(ImageUrlBuilder.buildImageUrl(personId, assetId, 500, 500))
                .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE) // don't store avatars in memory
                .into(imageView);
    }

    @BindingAdapter("app:person")
    public static void loadSmallImage(ImageView imageView, Person person) {
        String personId = person.getId();
        String assetId = person.getAvatar_asset_id();
        Picasso.with(imageView.getContext())
                .load(ImageUrlBuilder.buildImageUrl(personId, assetId, 150, 150))
//                .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE) // don't store avatars in memory
                .into(imageView);
    }

    @BindingAdapter("app:optograph")
    public static void loadOptograph(Optograph2DCubeView optograph2DCubeView, Optograph optograph) {
        optograph2DCubeView.setOptograph(optograph);
    }

    @BindingAdapter("app:optograph_preview")
    public static void loadOptographFace(ImageView imageView, Optograph optograph) {

        String uri = ImageUrlBuilder.buildCubeUrl(optograph, true, Cube.FACES[Cube.FACE_AHEAD]);
        if (optograph.is_local()) {
            Picasso.with(imageView.getContext())
                    .load(new File(uri))
//                    .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE) // don't store in memory
                    .into(imageView);
        } else {
            Picasso.with(imageView.getContext())
                    .load(uri)
//                    .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE) // don't store in memory
                    .into(imageView);
        }

    }

    @BindingAdapter("app:createdAt")
    public static void setTimeAgo(TextView textViev, String created_at) {
        textViev.setText(TimeUtils.getTimeAgo(RFC3339DateFormatter.fromRFC3339String(created_at)));
    }
}