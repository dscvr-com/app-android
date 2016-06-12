package com.iam360.iam360.views;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iam360.iam360.BR;
import com.iam360.iam360.ItemAutoCompleteSearchBinding;
import com.iam360.iam360.R;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.views.new_design.ProfileActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mariel on 6/7/2016.
 */
public class AutoCompleteSearchAdapter extends RecyclerView.Adapter<AutoCompleteSearchAdapter.SearchViewHolder> {

    private List<Person> mData = new ArrayList<Person>();

    private Cache cache;

    private Context mContext;
    private SnappyRecyclerView snappyRecyclerView;

    public AutoCompleteSearchAdapter(Context context) {
        this.mContext = context;

        cache = Cache.open();
    }

    public void setObjects(List<Person> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void addItem(Person person) {
        if (person!=null) {
            mData.add(person);
            notifyItemInserted(getItemCount());
            ((SearchActivity)mContext).searchProgressVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_auto_complete_search,parent,false);

        final SearchViewHolder viewHolder = new SearchViewHolder(itemView);

        return viewHolder;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        snappyRecyclerView = (SnappyRecyclerView) recyclerView;
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder,position);
        }
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {
        Person person = mData.get(position);

        if (!person.equals(holder.getBinding().getPerson())) {
            if (holder.getBinding().getPerson() != null) {

            }

            holder.getBinding().personAvatarAsset.setOnClickListener(v -> startProfile(person));
            holder.getBinding().personLocationInformation.setOnClickListener(v -> startProfile(person));

            holder.getBinding().setVariable(BR.person, person);
            holder.getBinding().executePendingBindings();
        }
    }

    private void startProfile(Person person) {
        Intent intent = new Intent(mContext, ProfileActivity.class);
        intent.putExtra("person", person);
        mContext.startActivity(intent);
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        private ItemAutoCompleteSearchBinding binding;

        public SearchViewHolder(View rowView) {
            super(rowView);
            this.binding = DataBindingUtil.bind(rowView);
        }

        public ItemAutoCompleteSearchBinding getBinding() {
            return binding;
        }
    }
//
//    @Override
//    public Filter getFilter() {
//        return new Filter() {
//            @Override
//            protected FilterResults performFiltering(CharSequence constraint) {
//                FilterResults results = new FilterResults();
//                if (constraint == null || constraint.length() == 0) {
//                    results.values = mData;
//                    results.count = mData.size();
//                } else {
//                    List<Person> nList = new ArrayList<Person>();
//                    for (Person p : mData) {
//                        if (p.getDisplay_name().startsWith(constraint.toString())) {
//                            nList.add(p);
//                        }
//                    }
//
//                    results.values = nList;
//                    results.count = nList.size();
//                }
//                return results;
//            }
//
//            @SuppressWarnings("unchecked")
//            @Override
//            protected void publishResults(CharSequence constraint, FilterResults results) {
//                if (results.count == 0) {
//                    notifyDataSetChanged();
//                } else {
//                    mData = (ArrayList<Person>) results.values;
//                    notifyDataSetChanged();
//                }
//            }
//        };
//    }
}
