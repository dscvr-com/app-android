package co.optonaut.optonaut.viewmodels;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import co.optonaut.optonaut.BR;
import co.optonaut.optonaut.FeedItemBinding;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */
public class OptographAdapter extends RecyclerView.Adapter<OptographAdapter.OptographViewHolder> {
    List<Optograph> optographs;


    public OptographAdapter() {
        this.optographs = new ArrayList<>();
    }

    @Override
    public OptographViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.feed_item, parent, false);

        return new OptographViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OptographViewHolder holder, int position) {
        Optograph optograph = optographs.get(position);
        holder.getBinding().setVariable(BR.optograph, optograph);
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
            binding = DataBindingUtil.bind(rowView);
        }
        public FeedItemBinding getBinding() {
            return binding;
        }
    }

    public void addItems(List<Optograph> optographs) {
        this.optographs.addAll(optographs);
        notifyDataSetChanged();
    }
}
