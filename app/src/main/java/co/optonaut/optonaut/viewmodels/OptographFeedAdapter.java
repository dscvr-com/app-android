package co.optonaut.optonaut.viewmodels;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import co.optonaut.optonaut.BR;
import co.optonaut.optonaut.FeedItemBinding;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.views.MainActivity;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */
public class OptographFeedAdapter extends RecyclerView.Adapter<OptographFeedAdapter.OptographViewHolder> {
    private static final String DEBUG_TAG = "Optonaut";
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


        ImageView optographView = (ImageView) itemView.findViewById(R.id.optograph_preview_asset);
        optographView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) itemView.getContext()).openOptograph2DView(viewHolder.getBinding().getOptograph());
            }
        });

        ImageView profileView = (ImageView) itemView.findViewById(R.id.person_avatar_asset);
        profileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) itemView.getContext()).openProfileFragment(viewHolder.getBinding().getPerson());
            }
        });
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(OptographViewHolder holder, int position) {
        Optograph optograph = optographs.get(position);
        holder.getBinding().setVariable(BR.optograph, optograph);
        holder.getBinding().setVariable(BR.person, optograph.getPerson());
        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return optographs.size();
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

    public void clear() {
        this.optographs.clear();
    }

    public void addItems(List<Optograph> optographs) {
        this.optographs.addAll(optographs);
        notifyDataSetChanged();
    }

    public Optograph get(int position) {
        return optographs.get(position);
    }

    public Optograph last() {
        return get(getItemCount() - 1);
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public List<Optograph> getOptographs() {
        return this.optographs;
    }

    public void setOptographs(List<Optograph> optographs) {
        this.optographs = optographs;
        notifyDataSetChanged();
    }
}
