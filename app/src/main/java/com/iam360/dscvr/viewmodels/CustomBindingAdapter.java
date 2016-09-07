package com.iam360.dscvr.viewmodels;

import android.databinding.BindingAdapter;
import android.graphics.Typeface;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.iam360.dscvr.model.Follower;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import com.iam360.dscvr.R;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.Person;
import com.iam360.dscvr.opengl.Cube;
import com.iam360.dscvr.opengl.Optograph2DCubeView;
import com.iam360.dscvr.util.ImageUrlBuilder;
import com.iam360.dscvr.util.RFC3339DateFormatter;
import com.iam360.dscvr.util.TimeUtils;

import timber.log.Timber;

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
        if (person != null) {
            String personId = person.getId();
            String assetId = person.getAvatar_asset_id();
            Picasso.with(imageView.getContext())
                    .load(ImageUrlBuilder.buildImageUrl(personId, assetId, 150, 150))
                            //                .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE) // don't store avatars in memory
                    .into(imageView);
        }
    }

    @BindingAdapter("app:follower")
    public static void loadSmallImage(ImageView imageView, Follower follower) {
        if (follower != null) {
            String followerId = follower.getId();
            String assetId = follower.getAvatar_asset_id();
            Picasso.with(imageView.getContext())
                    .load(ImageUrlBuilder.buildImageUrl(followerId, assetId, 150, 150))
                            //                .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE) // don't store avatars in memory
                    .into(imageView);
        }
    }

    @BindingAdapter("app:optograph")
    public static void loadOptograph(Optograph2DCubeView optograph2DCubeView, Optograph optograph) {
        optograph2DCubeView.setOptograph(optograph);
    }

    @BindingAdapter("app:optograph_profile")
    public static void loadOptographFaceSmall(ImageView imageView, Optograph optograph) {

        String uri = ImageUrlBuilder.buildSmallCubeUrl(optograph, true, Cube.FACES[Cube.FACE_AHEAD]);
        Log.d("myTag","URL: "+uri);

        imageView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    // Wait until layout to call Picasso
                    @Override
                    public void onGlobalLayout() {
                        // Ensure we call this only once
                        imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        if (optograph.is_local()) {
                            Picasso.with(imageView.getContext())
                                    .load(new File(uri))
                                    .resize(200, 0)
                                    .into(imageView);
                        } else {
                            Picasso.with(imageView.getContext())
                                    .load(uri)
                                    .placeholder(R.drawable.placeholder)
                                    .resize(imageView.getWidth(), 0)
                                    .into(imageView);
                        }
                    }
                });
    }

    @BindingAdapter("app:optograph_preview")
    public static void loadOptographFace(ImageView imageView, Optograph optograph) {

//        String uri = ImageUrlBuilder.buildCubeUrl(optograph, true, Cube.FACES[Cube.FACE_AHEAD]);
        String uri = ImageUrlBuilder.buildImagePreviewUrl(optograph.getId());
        Timber.d("Toro URI:" + uri);

        imageView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    // Wait until layout to call Picasso
                    @Override
                    public void onGlobalLayout() {
                        // Ensure we call this only once
                        imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        if (optograph.is_local()) {
                            Picasso.with(imageView.getContext())
                                    .load(new File(uri))
                                    .resize(imageView.getWidth(), 0)
                                    .into(imageView);
                        } else {
                            Picasso.with(imageView.getContext())
                                    .load(uri)
                                    .placeholder(R.drawable.placeholder)
                                    .resize(imageView.getWidth(), 0)
                                    .into(imageView);
                        }
                    }
                });

    }

    @BindingAdapter("app:createdAt")
    public static void setTimeAgo(TextView textViev, String created_at) {
        textViev.setText(TimeUtils.getTimeAgo(RFC3339DateFormatter.fromRFC3339String(created_at)));
    }

    @BindingAdapter("app:font")
    public static void setFont(TextView textView, String font) {
        Typeface typeface = Typeface.createFromAsset(textView.getContext().getAssets(), "fonts/" + "Avenir_LT_45_Book_0.ttf");
        Typeface boldTypeface = Typeface.create(typeface, Typeface.BOLD);
        Typeface italicTypeface = Typeface.create(typeface, Typeface.ITALIC);
        if (font != null && font.equals("bold")) textView.setTypeface(boldTypeface);
        else if (font != null && font.equals("italic")) textView.setTypeface(italicTypeface);
        else if (font != null && !font.equals(""))
            textView.setTypeface(Typeface.createFromAsset(textView.getContext().getAssets(), "fonts/" + font));
        else
            textView.setTypeface(Typeface.createFromAsset(textView.getContext().getAssets(), "fonts/" + "Avenir_LT_45_Book_0.ttf"));
    }
}