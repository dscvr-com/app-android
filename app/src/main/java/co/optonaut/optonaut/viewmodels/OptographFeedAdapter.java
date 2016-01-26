package co.optonaut.optonaut.viewmodels;

import android.databinding.DataBindingUtil;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import co.optonaut.optonaut.BR;
import co.optonaut.optonaut.FeedItemBinding;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.views.redesign.MainActivityRedesign;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */
public class OptographFeedAdapter extends RecyclerView.Adapter<OptographFeedAdapter.OptographViewHolder> {
    private static final int ITEM_HEIGHT = Constants.getInstance().getDisplayMetrics().heightPixels;
    List<Optograph> optographs;


    public OptographFeedAdapter() {
        this.optographs = new ArrayList<>();
    }

    @Override
    public OptographViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.feed_item, parent, false);

        final OptographViewHolder viewHolder = new OptographViewHolder(itemView);

        initializeProfileBar(itemView);
        initializeDescriptionBar(itemView);

        return viewHolder;
    }

    private void initializeDescriptionBar(View itemView) {
        RelativeLayout rl = (RelativeLayout) itemView.findViewById(R.id.description_bar);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) rl.getLayoutParams();

        int newMarginBottom = ITEM_HEIGHT - ((MainActivityRedesign) itemView.getContext()).getLowerBoundary() + lp.bottomMargin;
        lp.setMargins(0, 0, 0, newMarginBottom);
        rl.setLayoutParams(lp);
    }

    private void initializeProfileBar(final View itemView) {
        RelativeLayout rl = (RelativeLayout) itemView.findViewById(R.id.profile_bar);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) rl.getLayoutParams();

        // set margin and height
        int newMarginTop = ((MainActivityRedesign) itemView.getContext()).getUpperBoundary();
        lp.height = Constants.getInstance().getToolbarHeight();
        lp.setMargins(0, newMarginTop, 0, 0);
        rl.setLayoutParams(lp);

        ImageView profileView = (ImageView) itemView.findViewById(R.id.person_avatar_asset);
        profileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, v.getResources().getString(R.string.feature_next_version), Snackbar.LENGTH_SHORT).show();
            }
        });

        Button settingsButton = (Button) itemView.findViewById(R.id.settings_button);
        settingsButton.setTypeface(Constants.getInstance().getDefaultTypeface());
        settingsButton.setText(String.valueOf((char) 0xe904));
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(itemView.getContext(), v);
                popupMenu.inflate(R.menu.feed_item_menu);

                //registering popup with OnMenuItemClickListener
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Snackbar.make(
                                itemView,
                                itemView.getResources().getString(R.string.feature_next_version),
                                Snackbar.LENGTH_SHORT
                        ).show();
                        return true;
                    }
                });

                popupMenu.show();
            }
        });
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
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ITEM_HEIGHT); // (width, height)
            holder.itemView.setLayoutParams(params);

            TextView heart_label = (TextView) holder.itemView.findViewById(R.id.heart_label);
            heart_label.setTypeface(Constants.getInstance().getDefaultTypeface());

            // TODO: check if user has starred optograph
            boolean userLikesOptograph = false;
            if (userLikesOptograph) {
                heart_label.setText(holder.itemView.getResources().getString(R.string.heart_count, optograph.getStars_count(), String.valueOf((char) 0xe90d)));
            } else {
                // TODO: use empty heart
                heart_label.setText(holder.itemView.getResources().getString(R.string.heart_count, optograph.getStars_count(), String.valueOf((char) 0xe90d)));
            }

            holder.getBinding().setVariable(BR.optograph, optograph);
            holder.getBinding().setVariable(BR.person, optograph.getPerson());
            holder.getBinding().executePendingBindings();
        } else {
            Log.d(Constants.DEBUG_TAG, "Re-Binding of OptographViewHolder at position " + position);
        }
    }

    @Override
    public int getItemCount() {
        return optographs.size();
    }

    public void addItem(Optograph optograph) {
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

    public static class OptographViewHolder extends RecyclerView.ViewHolder {
        private FeedItemBinding binding;

        public OptographViewHolder(View rowView) {
            super(rowView);
            this.binding = DataBindingUtil.bind(rowView);
        }

        public FeedItemBinding getBinding() {
            return binding;
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
}
