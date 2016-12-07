package com.iam360.dscvr.viewmodels;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iam360.dscvr.BR;
import com.iam360.dscvr.R;
import com.iam360.dscvr.StoryFeedItemBinding;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.Person;
import com.iam360.dscvr.network.ApiConsumer;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.DBHelper;
import com.iam360.dscvr.util.DBHelper2;
import com.iam360.dscvr.views.activity.MainActivity;
import com.iam360.dscvr.views.activity.OptographDetailsActivity;
import com.iam360.dscvr.views.activity.ProfileActivity;
import com.iam360.dscvr.views.activity.StoryCreatorActivity;

import java.util.ArrayList;
import java.util.List;

public class StoryFeedAdapter extends RecyclerView.Adapter<StoryFeedAdapter.StoryFeedItemHolder> {
    private List<Optograph> optographs;

    protected ApiConsumer apiConsumer;
    private Cache cache;
    private Context context;
    private DBHelper mydb;
    private boolean isAllStory = true;

    public StoryFeedAdapter(Context context, boolean isAllStory) {
        this.context = context;
        this.optographs = new ArrayList<>();
        this.isAllStory = isAllStory;

        cache = Cache.open();

        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        mydb = new DBHelper(context);
    }

    @Override
    public StoryFeedItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()) .inflate(R.layout.story_feed_item, parent, false);
        return new StoryFeedItemHolder(view);
    }

    @Override
    public void onBindViewHolder(StoryFeedItemHolder holder, int position) {

        Optograph optograph = optographs.get(position);

        if(isAllStory) holder.getBinding().personName.setVisibility(View.VISIBLE);
        else  holder.getBinding().personName.setVisibility(View.GONE);

        holder.getBinding().storyPreview.setOnClickListener(v -> callDetailsPage(optograph, "view"));
        holder.getBinding().personName.setOnClickListener(v -> startProfile(optograph.getPerson()));
        holder.getBinding().storyPreviewEdit.setOnClickListener(v -> callDetailsPage(optograph, "edit"));

        holder.getBinding().setVariable(BR.optograph, optograph);
        holder.getBinding().setVariable(BR.person, optograph.getPerson());
        holder.getBinding().setVariable(BR.location, optograph.getLocation());

        holder.getBinding().executePendingBindings();

    }

    private void callDetailsPage(Optograph optograph, String type) {
        Intent intent = new Intent(context, OptographDetailsActivity.class);
        if(type.equals("edit")){
            intent = new Intent(context, StoryCreatorActivity.class);
        }
        intent.putExtra("opto", optograph);
        intent.putExtra("story", true);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    private void startProfile(Person person) {
        if(cache.getString(Cache.USER_ID).equals(person)) {
            if(context instanceof MainActivity)
                ((MainActivity) context).setPage(MainActivity.PROFILE_MODE);
        } else {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("person", person);
            context.startActivity(intent);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return optographs.size();
    }

    public void addItem(Optograph optograph) {
        if (optograph == null || optographs.contains(optograph)) {
            return;
        }

        new DBHelper2(context).saveToSQLite(optograph);
        optographs.add(optograph);
        notifyItemInserted(getItemCount());
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

    public static class StoryFeedItemHolder extends RecyclerView.ViewHolder {
        private StoryFeedItemBinding bindingHeader;
        public StoryFeedItemHolder(View rowView) {
            super(rowView);
            this.bindingHeader = DataBindingUtil.bind(rowView);
        }
        public StoryFeedItemBinding getBinding() {
            return bindingHeader;
        }
    }

    public void clearData() {
        int size = optographs.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                optographs.remove(0);
            }
            this.notifyItemRangeRemoved(0, size);
        }
    }
}