package com.iam360.dscvr.removed_social.viewmodels;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.iam360.dscvr.AAFeedItemBinding;
import com.iam360.dscvr.BR;
import com.iam360.dscvr.R;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.removed_social.views.activity.MainActivity;
import com.iam360.dscvr.sensors.CombinedMotionManager;
import com.iam360.dscvr.sensors.GestureDetectors;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.Constants;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class OptographVideoFeedAdapter extends RecyclerView.Adapter<OptographVideoFeedAdapter.OptographHolder> {
    private static final int ITEM_HEIGHT = Constants.getInstance().getDisplayMetrics().heightPixels;
    private static final float ITEM_WIDTH = Constants.getInstance().getDisplayMetrics().widthPixels;
    private static final float DENSITY = Constants.getInstance().getDisplayMetrics().density;
    private List<Optograph> optographs;

    private Cache cache;
    private Context context;

    public OptographVideoFeedAdapter(Context context) {
        this.context = context;
        this.optographs = new ArrayList<>();

        cache = Cache.open();
    }

    @Override
    public OptographHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.aa_feed_item, parent, false);
        return new OptographHolder(view, context);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
//        super.onAttachedToRecyclerView(recyclerView);

        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(recyclerView);
//        snappyRecyclerView = (SnappyRecyclerView) recyclerView;
    }

    @Override
    public void onBindViewHolder(OptographHolder holder, int position) {
//        super.onBindViewHolder(holder, position);
        Optograph optograph = optographs.get(position);
        Timber.d("onBindViewHolder " + optograph.getId());

//        int height = (int)((ITEM_WIDTH / 1.405) + (5 * DENSITY));
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
//        holder.itemView.setLayoutParams(params);

        holder.bindingHeader.optograph2dview.setSensorMode(CombinedMotionManager.GYRO_MODE);
        holder.bindingHeader.optograph2dview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (GestureDetectors.singleClickDetector.onTouchEvent(event)) {
                    if(context instanceof MainActivity)
                        ((MainActivity) context).toggleFeedFullScreen();
                }

                return holder.bindingHeader.optograph2dview.getOnTouchListener().onTouch(v, event);
            }
        });

//            holder.getBinding().frame.setOnClickListener(v -> callDetailsPage(optograph, position));

            holder.getBinding().setVariable(BR.optograph, optograph);
            holder.getBinding().executePendingBindings();

    }

    private ArrayList<Optograph> getNextOptographList(int position, int count) {
        int optoListCount = optographs.size();
        count = (count < optoListCount) ? count : optoListCount;

        ArrayList<Optograph> optographList = new ArrayList<Optograph>();

        for(int i = 0; i < count; i++) {
            optographList.add(optographs.get((position) % optoListCount));
            position++;
        }

        return optographList;
    }

    @Override
    public int getItemCount() {
        return optographs.size();
    }

    public void addItem(Optograph optograph) {
        if (optograph == null) {
            return;
        }

        // skip if optograph is already in list
        if (optographs.contains(optograph)) {
            return;
        }

        optographs.add(optographs.size(), optograph);
        notifyItemInserted(optographs.size() - 1);
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

    public void refreshAfterDelete(String id, boolean isLocal) {

        for (Optograph opto:optographs) {
            if (opto!=null && opto.getId().equals(id) && opto.is_local()==isLocal) {
                int position = optographs.indexOf(opto);
                optographs.remove(opto);
                notifyItemRemoved(position);
                Timber.d("refreshAfterDelete optoremoved");
                return;
            }
        }
    }

    public static class OptographHolder extends RecyclerView.ViewHolder {
        private AAFeedItemBinding bindingHeader;

        public OptographHolder(View rowView, Context context) {
            super(rowView);
            this.bindingHeader = DataBindingUtil.bind(rowView);
        }

        public AAFeedItemBinding getBinding() {
            return bindingHeader;
        }
    }

}