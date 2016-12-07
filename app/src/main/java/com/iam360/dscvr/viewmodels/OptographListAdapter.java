package com.iam360.dscvr.viewmodels;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iam360.dscvr.R;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.opengl.Cube;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.DBHelper;
import com.iam360.dscvr.util.ImageUrlBuilder;
import com.iam360.dscvr.views.activity.ImagePickerActivity;
import com.iam360.dscvr.views.activity.StoryCreatorActivity;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Joven on 10/4/2016.
 */
public class OptographListAdapter extends RecyclerView.Adapter<OptographListAdapter.ViewHolder>{

    private static final int ITEM_WIDTH= Constants.getInstance().getDisplayMetrics().widthPixels;
    private List<Optograph> optographs;
    private Context context;
    private int mode;
    private Cache cache;
    private DBHelper mydb;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView text;

        public ViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            text = (TextView) v.findViewById(R.id.text);
        }
    }

    public void add(int position, Optograph item) {
        optographs.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(String item) {
        int position = optographs.indexOf(item);
        optographs.remove(position);
        notifyItemRemoved(position);
    }

    public OptographListAdapter(Context context, int mode) {
        this.context = context;
        this.mode = mode;
        this.cache = Cache.open();
        this.optographs = new ArrayList<>();
        mydb = new DBHelper(context);
    }

    @Override
    public OptographListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.optolist_picker_adapter, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String uri = ImageUrlBuilder.buildSmallCubeUrl(optographs.get(position), true, Cube.FACES[Cube.FACE_AHEAD]);

        int height = (mode == ImagePickerActivity.UPLOAD_OPTO_MODE ? ImagePickerActivity.NUM_COLUMNS_OPTO : ImagePickerActivity.NUM_COLUMNS_STORY);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ITEM_WIDTH / height);
        holder.itemView.setLayoutParams(params);

        Picasso.with(holder.image.getContext())
                .load(uri)
                .into(holder.image);

        holder.text.setText(uri);
        holder.itemView.setOnClickListener(v -> startUploadOrStory(optographs.get(position)));

    }

    private void startUploadOrStory(Optograph optograph) {
        if(mode == ImagePickerActivity.CREATE_STORY_MODE2){
            Intent resultIntent = new Intent();
            resultIntent.putExtra("opto", optograph);
            resultIntent.putExtra("opto_id", optograph.getId());
            ((Activity) context).setResult(Activity.RESULT_OK, resultIntent);
            ((Activity) context).finish();
        }else{
            Intent intent = new Intent(context, StoryCreatorActivity.class);
            intent.putExtra("opto", optograph);
            intent.putExtra("type", "create");
            context.startActivity(intent);
        }
    }

    @Override
    public int getItemCount() {
        return optographs.size();
    }



    public void addItem(Optograph optograph) {
        if (optograph == null || optographs.contains(optograph)) {
            return;
        }

        DateTime created_at = optograph.getCreated_atDateTime();

        if(mode == ImagePickerActivity.CREATE_STORY_MODE2 || mode == ImagePickerActivity.CREATE_STORY_MODE){
            Timber.d("optograph.getStory() = "+optograph.getStory());
            Cursor res =  mydb.getData(optograph.getId(), DBHelper.STORY_TABLE_NAME,"optograph_id");
            res.moveToFirst();
            if (res.getCount()!= 0) {
                Timber.d("optographStoryId = "+res.getString(res.getColumnIndex("id")));
                return;
            }
        }

        if (optograph.getDeleted_at()!=null && !optograph.getDeleted_at().isEmpty()) return;

        // if list is empty, simply add new optograph
        if (optographs.isEmpty() || optographs.size()==2) {
            optographs.add(optograph);
            notifyItemInserted(getItemCount());
            return;
        }

        // if optograph is oldest, simply append to list
        if (created_at != null && created_at.isBefore(getOldest().getCreated_atDateTime())) {
            optographs.add(optograph);
            notifyItemInserted(getItemCount());
            return;
        }

        // find correct position of optograph
        // TODO: allow for "breaks" between new optograph and others...
        for (int i = 0; i < optographs.size(); i++) {
            Optograph current = optographs.get(i);
            if (current!=null && created_at != null && created_at.isAfter(current.getCreated_atDateTime())) {
                optographs.add(i, optograph);
                notifyItemInserted(i);
                return;
            }
        }
    }


    public Optograph getOldest() {
        return get(getItemCount() - 1);
    }

    public Optograph get(int position) {
        return optographs.get(position);
    }
}
