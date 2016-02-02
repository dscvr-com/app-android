package co.optonaut.optonaut.viewmodels;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import co.optonaut.optonaut.BR;
import co.optonaut.optonaut.FeedItemBinding;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.opengl.Optograph2DCubeView;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.util.ImageUrlBuilder;
import co.optonaut.optonaut.views.GestureDetectors;
import co.optonaut.optonaut.views.redesign.MainActivityRedesign;
import co.optonaut.optonaut.views.redesign.SnappyRecyclerView;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */
public class OptographFeedAdapter extends RecyclerView.Adapter<OptographFeedAdapter.OptographViewHolder> {
    private static final int ITEM_HEIGHT = Constants.getInstance().getDisplayMetrics().heightPixels;
    List<Optograph> optographs;
    private SnappyRecyclerView snappyRecyclerView;


    public OptographFeedAdapter() {
        this.optographs = new ArrayList<>();
    }

    @Override
    public OptographViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.feed_item, parent, false);

        Optograph2DCubeView optograph2DCubeView = (Optograph2DCubeView) itemView.findViewById(R.id.optograph2dview);

        final OptographViewHolder viewHolder = new OptographViewHolder(itemView);

        // TODO: add touch navigation and don't allow scrolling
        optograph2DCubeView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (viewHolder.isNavigationModeCombined) {
                    if (GestureDetectors.singleClickDetector.onTouchEvent(event)) {
                        Timber.v("detected single click in combined navigation");
                        viewHolder.toggleNavigationMode();
                        snappyRecyclerView.enableScrolling();
                        // still return optograph2DCubeView for registering end of touching
                        return optograph2DCubeView.getOnTouchListener().onTouch(v, event);
                    } else {
                        Timber.v("pipe touch in combined navigation to optograph view");
                        return optograph2DCubeView.getOnTouchListener().onTouch(v, event);
                    }
                } else {
                    if (GestureDetectors.singleClickDetector.onTouchEvent(event)) {
                        Timber.v("detected single click in simple navigation");
                        viewHolder.toggleNavigationMode();
                        snappyRecyclerView.disableScrolling();
                        return true;
                    } else {
                        // need to return true here to prevent touch-stealing of parent!
                        return true;
                    }
                }
            }
        });

        initializeProfileBar(itemView);
        initializeDescriptionBar(itemView);

        return viewHolder;
    }

    private void initializeDescriptionBar(View itemView) {
        RelativeLayout rl = (RelativeLayout) itemView.findViewById(R.id.description_bar);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) rl.getLayoutParams();

        int newMarginBottom = ITEM_HEIGHT - ((MainActivityRedesign) itemView.getContext()).getLowerBoundary() + lp.bottomMargin;
        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, newMarginBottom);
        rl.setLayoutParams(lp);

        rl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // don't pipe click events to views below description bar
                return true;
            }
        });
    }

    private void initializeProfileBar(final View itemView) {
        RelativeLayout rl = (RelativeLayout) itemView.findViewById(R.id.profile_bar);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) rl.getLayoutParams();

        // set margin and height
        int newMarginTop = ((MainActivityRedesign) itemView.getContext()).getUpperBoundary();
        lp.setMargins(0, newMarginTop, 0, 0);
        rl.setLayoutParams(lp);

        rl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // don't pipe click events to views below profile bar
                return true;
            }
        });

        ImageView profileView = (ImageView) itemView.findViewById(R.id.person_avatar_asset);
        profileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, v.getResources().getString(R.string.feature_next_version), Snackbar.LENGTH_SHORT).show();
            }
        });

        TextView profileLabel = (TextView) itemView.findViewById(R.id.person_name_label);
        profileLabel.setTypeface(Constants.getInstance().getDefaultRegularTypeFace());

        TextView locationLabel = (TextView) itemView.findViewById(R.id.location_label);
        locationLabel.setTypeface(Constants.getInstance().getDefaultLightTypeFace());

        TextView timeAgoLabel = (TextView) itemView.findViewById(R.id.time_ago);
        timeAgoLabel.setTypeface(Constants.getInstance().getDefaultRegularTypeFace());


        TextView settingsLabel = (TextView) itemView.findViewById(R.id.settings_label);
        settingsLabel.setTypeface(Constants.getInstance().getIconTypeface());
        settingsLabel.setText(String.valueOf((char) 0xe904));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        snappyRecyclerView = (SnappyRecyclerView) recyclerView;
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
            heart_label.setTypeface(Constants.getInstance().getIconTypeface());

            // TODO: check if user has starred optograph
            boolean userLikesOptograph = false;
            if (userLikesOptograph) {
                heart_label.setText(holder.itemView.getResources().getString(R.string.heart_count, optograph.getStars_count(), String.valueOf((char) 0xe90d)));
            } else {
                // TODO: use empty heart
                heart_label.setText(holder.itemView.getResources().getString(R.string.heart_count, optograph.getStars_count(), String.valueOf((char) 0xe90d)));
            }

            // setup sharing
            TextView settingsLabel = (TextView) holder.itemView.findViewById(R.id.settings_label);
            settingsLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), v);
                    popupMenu.inflate(R.menu.feed_item_menu);

                    //registering popup with OnMenuItemClickListener
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.share_item) {
                                ((MainActivityRedesign) v.getContext()).shareOptograph(optograph);
                                return true;
                            } else if (item.getItemId() == R.id.report_item) {
                                Timber.v("clicked report");
                                return true;
                            }
                            return false;
                        }
                    });

                    popupMenu.show();
                }
            });

            holder.getBinding().setVariable(BR.optograph, optograph);
            holder.getBinding().setVariable(BR.person, optograph.getPerson());
            holder.getBinding().setVariable(BR.location, optograph.getLocation());

            holder.getBinding().executePendingBindings();
        } else {
            Timber.d("rebinding of OptographViewHolder at position " + position);
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
        private FeedItemBinding binding;
        RelativeLayout profileBar;
        RelativeLayout descriptionBar;
        private Optograph2DCubeView optograph2DCubeView;
        private boolean isNavigationModeCombined;


        public OptographViewHolder(View rowView) {
            super(rowView);
            this.binding = DataBindingUtil.bind(rowView);
            profileBar = (RelativeLayout) itemView.findViewById(R.id.profile_bar);
            descriptionBar = (RelativeLayout) itemView.findViewById(R.id.description_bar);
            optograph2DCubeView = (Optograph2DCubeView) itemView.findViewById(R.id.optograph2dview);
            setInformationBarsVisible();
        }

        private void setInformationBarsVisible() {
            profileBar.setVisibility(View.VISIBLE);
            descriptionBar.setVisibility(View.VISIBLE);
            ((MainActivityRedesign) itemView.getContext()).setOverlayVisibility(View.VISIBLE);
            // todo: unregister touch listener
            optograph2DCubeView.registerRendererOnSensors();
            isNavigationModeCombined = false;
        }

        private void setInformationBarsInvisible() {
            profileBar.setVisibility(View.INVISIBLE);
            descriptionBar.setVisibility(View.INVISIBLE);
            ((MainActivityRedesign) itemView.getContext()).setOverlayVisibility(View.INVISIBLE);
            // todo: register touch listener
            optograph2DCubeView.unregisterRendererOnSensors();
            isNavigationModeCombined = true;
        }

        public FeedItemBinding getBinding() {
            return binding;
        }


        public void toggleNavigationMode() {
            if (isNavigationModeCombined) {
                setInformationBarsVisible();
            } else {
                setInformationBarsInvisible();
            }
        }

        public boolean isNavigationModeCombined() {
            return isNavigationModeCombined;
        }
    }
}
