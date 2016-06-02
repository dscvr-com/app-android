package com.iam360.iam360.views.new_design;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iam360.iam360.R;
import com.iam360.iam360.util.Constants;
import com.iam360.iam360.util.GeneralUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class ImagePickerAdapter extends RecyclerView.Adapter<ImagePickerAdapter.ViewHolder> {

    private static final int ITEM_WIDTH= Constants.getInstance().getDisplayMetrics().widthPixels;
    private ArrayList<String> mDataset;
    private Context context;

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

    public ImagePickerAdapter(Context context, ArrayList<String> myDataset) {
        mDataset = myDataset;
        this.context = context;
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

//        holder.image.setImageURI(Uri.parse(path));

//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ITEM_WIDTH, 400); //ITEM_WIDTH / OptographGridFragment.NUM_COLUMNS); // (width, height)
//        holder.itemView.setLayoutParams(params);

        Picasso.with(holder.image.getContext())
                .load(new File(path))
//                .placeholder(R.drawable.placeholder)
                .resize(ITEM_WIDTH, 550)
                .centerCrop()
                .into(holder.image);

        holder.text.setText(path);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OptoImagePreviewActivity.class);
                intent.putExtra("id", UUID.randomUUID().toString());
                intent.putExtra("path", path);
                context.startActivity(intent);
                ((Activity)context).finish();
            }
        });

//        holder.image.setMaxWidth(ITEM_WIDTH / 3);
//        holder.image.getViewTreeObserver()
//                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                    // Wait until layout to call Picasso
//                    @Override
//                    public void onGlobalLayout() {
//                        // Ensure we call this only once
//                        holder.image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                            Picasso.with(holder.image.getContext())
//                                    .load(new File(path))
//                                    .resize(holder.image.getWidth(), 0)
////                                    .centerCrop()
//                                    .into(holder.image);
//                    }
//                });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
