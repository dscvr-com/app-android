package co.optonaut.optonaut.viewmodels;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import co.optonaut.optonaut.BR;
import co.optonaut.optonaut.FeedItemBinding;
import co.optonaut.optonaut.GridItemBinding;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.LogInReturn;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.network.ApiConsumer;
import co.optonaut.optonaut.network.ApiEndpoints;
import co.optonaut.optonaut.opengl.Optograph2DCubeView;
import co.optonaut.optonaut.util.Cache;
import co.optonaut.optonaut.util.CameraUtils;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.views.GestureDetectors;
import co.optonaut.optonaut.views.OptographGridFragment;
import co.optonaut.optonaut.views.redesign.MainActivityRedesign;
import co.optonaut.optonaut.views.redesign.SnappyRecyclerView;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import rx.Observable;
import rx.Observer;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */
public class OptographGridAdapter extends RecyclerView.Adapter<OptographGridAdapter.OptographViewHolder> {
    private static final int ITEM_HEIGHT = Constants.getInstance().getDisplayMetrics().heightPixels;
    private static final int ITEM_WIDTH= Constants.getInstance().getDisplayMetrics().widthPixels;
    List<Optograph> optographs;
//    private RecyclerView snappyRecyclerView;

    protected ApiConsumer apiConsumer;
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ijg5ODBlZjgzLTgxMzMtNGM4Yy04ZWY5LTdjYjkzYmVlNTM4NCJ9.I4q9b5eHUpY-NUgT-_1xlvDeTU9jItSwkzBnh1nKuP4";

    private Context context;

    public OptographGridAdapter(Context context) {
        this.context = context;
        this.optographs = new ArrayList<>();
        apiConsumer = new ApiConsumer(TOKEN);
    }

    @Override
    public OptographViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.grid_item, parent, false);

        ImageView optograph2DCubeView = (ImageView) itemView.findViewById(R.id.optograph2dview);

        final OptographViewHolder viewHolder = new OptographViewHolder(itemView);


        return viewHolder;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

//        snappyRecyclerView = recyclerView;
    }

    @Override
    public void onBindViewHolder(OptographViewHolder holder, int position) {
        Optograph optograph = optographs.get(position);

        // reset view holder if we got new optograh
        if (!optograph.equals(holder.getBinding().getOptograph())) {
            // cancel the request for the old texture
            if (holder.getBinding().getOptograph() != null) {
                // TODO: cancel request
            }
            // span complete screen
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ITEM_WIDTH / OptographGridFragment.NUM_COLUMNS); // (width, height)
            holder.itemView.setLayoutParams(params);

            holder.getBinding().setVariable(BR.optograph, optograph);
            holder.getBinding().executePendingBindings();

            ImageView optographView = (ImageView) holder.itemView.findViewById(R.id.optograph2dview);
            optographView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivityRedesign activity = (MainActivityRedesign) context;
                    activity.startProfileFeed(optograph.getPerson(), position);
                }
            });

        } else {
            Timber.d("rebinding of OptographViewHolder at position %s", position);
        }
    }

    @Override
    public int getItemCount() {
        return optographs.size();
    }

    public void addItem(Optograph optograph) {
        if (optograph == null) {
            return;
        }

        DateTime created_at = optograph.getCreated_atDateTime();

        // skip if optograph is already in list
        if (optographs.contains(optograph)) {
            return;
        }

        // if list is empty, simply add new optograph
        if (optographs.isEmpty()) {
            optographs.add(optograph);
            notifyItemInserted(getItemCount());
            return;
        }

        // if optograph is oldest, simply append to list
        if (created_at.isBefore(getOldest().getCreated_atDateTime())) {
            optographs.add(optograph);
            notifyItemInserted(getItemCount());
            return;
        }

        // find correct position of optograph
        // TODO: allow for "breaks" between new optograph and others...
        for (int i = 0; i < optographs.size(); i++) {
            Optograph current = optographs.get(i);
            if (created_at.isAfter(current.getCreated_atDateTime())) {
                optographs.add(i, optograph);
                notifyItemInserted(i);
                return;
            }
        }
    }

    public Optograph get(int position) {
        return optographs.get(position);
    }

    public Optograph getOldest() {
        return get(getItemCount() - 1);
    }

    public boolean isEmpty() {
        return optographs.isEmpty();
    }

    public List<Optograph> getOptographs() {
        return this.optographs;
    }


    public static class OptographViewHolder extends RecyclerView.ViewHolder {
        private GridItemBinding binding;
        private ImageView optograph2DCubeView;
        private boolean isNavigationModeCombined;


        public OptographViewHolder(View rowView) {
            super(rowView);
            this.binding = DataBindingUtil.bind(rowView);
            optograph2DCubeView = (ImageView) itemView.findViewById(R.id.optograph2dview);
        }

        public GridItemBinding getBinding() {
            return binding;
        }

    }
}
