package com.iam360.iam360.viewmodels;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.iam360.iam360.BR;
import com.iam360.iam360.ProfileHeaderBinding;
import com.iam360.iam360.R;
import com.iam360.iam360.StoryFeedItemBinding;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.DBHelper;
import com.iam360.iam360.views.activity.MainActivity;
import com.iam360.iam360.views.activity.OptographDetailsActivity;
import com.iam360.iam360.views.activity.ProfileActivity;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

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

        holder.getBinding().storyPreview.setOnClickListener(v -> callDetailsPage(optograph));
        holder.getBinding().personName.setOnClickListener(v -> startProfile(optograph.getPerson()));

        holder.getBinding().setVariable(BR.optograph, optograph);
        holder.getBinding().setVariable(BR.person, optograph.getPerson());
        holder.getBinding().setVariable(BR.location, optograph.getLocation());

        holder.getBinding().executePendingBindings();

    }

    private void callDetailsPage(Optograph optograph) {
        Intent intent = new Intent(context, OptographDetailsActivity.class);
        intent.putExtra("opto", optograph);
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

        if (optograph == null) {
            return;
        }

        DateTime created_at = optograph.getCreated_atDateTime();

        // skip if optograph is already in list
        if (optographs.contains(optograph)) {
            return;
        }

        optographs.add(optographs.size(), optograph);
        notifyItemInserted(optographs.size() - 1);

//        // if list is empty, simply add new optograph
//        if (optographs.isEmpty()) {
//            optographs.add(optographs.size(), optograph);
////            notifyItemInserted(getItemCount());
//            Timber.d("ADD ITEM iteminserted 1 ");
//            notifyItemInserted(optographs.size() - 1);
//            return;
//        }
//
//        // if optograph is oldest, simply append to list
//        if (created_at != null && created_at.isBefore(getOldest().getCreated_atDateTime())) {
//            optographs.add(optograph);
//            Timber.d("ADD ITEM iteminserted 2 ");
////            notifyItemInserted(getItemCount());
//            notifyDataSetChanged();
//            return;
//        }
//
//        // find correct position of optograph
//        // TODO: allow for "breaks" between new optograph and others...
//        for (int i = 0; i < optographs.size(); i++) {
//            Optograph current = optographs.get(i);
//            Timber.d("ADD ITEM " + current.getCreated_atDateTime() + " : " + created_at);
//
//            if (created_at != null && (created_at.isEqual(current.getCreated_atDateTime()) || created_at.isAfter(current.getCreated_atDateTime())) ) {
//                optographs.add(i, optograph);
//                Timber.d("ADD ITEM iteminserted 3 ");
//                notifyItemInserted(i);
//                return;
//            }
//        }
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

}
