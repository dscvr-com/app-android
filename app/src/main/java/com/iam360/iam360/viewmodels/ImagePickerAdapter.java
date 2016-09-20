package com.iam360.iam360.viewmodels;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iam360.iam360.R;
import com.iam360.iam360.util.Constants;
import com.iam360.iam360.views.activity.ImagePickerActivity;
import com.iam360.iam360.views.activity.OptoImagePreviewActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.UUID;

import timber.log.Timber;

public class ImagePickerAdapter extends RecyclerView.Adapter<ImagePickerAdapter.ViewHolder> {

    private static final int ITEM_WIDTH= Constants.getInstance().getDisplayMetrics().widthPixels;
    private ArrayList<String> mDataset;
    private Context context;
    private ImageLoader imageLoader;
    private int mode;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView text;

        public ViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            text = (TextView) v.findViewById(R.id.text);
        }
    }

    public void add(int position, String item) {
        mDataset.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(String item) {
        int position = mDataset.indexOf(item);
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    public ImagePickerAdapter(Context context, ArrayList<String> myDataset, int mode) {
        mDataset = myDataset;
        this.context = context;
        this.mode = mode;
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public ImagePickerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_picker_adapter, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String path = mDataset.get(position);

        int height = (mode == ImagePickerActivity.UPLOAD_OPTO_MODE ? ImagePickerActivity.NUM_COLUMNS_OPTO : ImagePickerActivity.NUM_COLUMNS_STORY);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ITEM_WIDTH / height);
        holder.itemView.setLayoutParams(params);

        imageLoader.displayImage("file://" + path, holder.image);

        holder.text.setText(path);
        holder.itemView.setOnClickListener(v -> startUploadOrStory(path));

    }

    private void startUploadOrStory(String path) {
        Timber.d("Image path : " + path);

        switch (mode) {
            case ImagePickerActivity.UPLOAD_OPTO_MODE:
                Intent intent = new Intent(context, OptoImagePreviewActivity.class);
                intent.putExtra("id", UUID.randomUUID().toString());
                intent.putExtra("path", path);
                context.startActivity(intent);
                ((Activity) context).finish();
                break;
            case ImagePickerActivity.CREATE_STORY_MODE:
                // TODO add actions here
                ((Activity) context).finish();
                break;
            case ImagePickerActivity.ADD_SCENE_MODE:
                // TODO add actions here
                ((Activity) context).finish();
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
