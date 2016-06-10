package com.iam360.iam360.viewmodels;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iam360.iam360.NewFeedItemBinding;
import com.iam360.iam360.R;
import com.volokh.danylo.video_player_manager.ui.VideoPlayerView;

public class OptographVideoViewHolder extends RecyclerView.ViewHolder {

    public NewFeedItemBinding binding;
    public RelativeLayout profileBar;
    public RelativeLayout descriptionBar;
    //        private Optograph2DCubeView optograph2DCubeView;
    public TextView heart_label;
    public ImageButton followButton;
    public boolean isNavigationModeCombined;
    public VideoPlayerView videoView;
    public ImageView previewImage;

    public OptographVideoViewHolder(View view) {
        super(view);
        this.binding = DataBindingUtil.bind(view);
        profileBar = (RelativeLayout) itemView.findViewById(R.id.profile_bar);
        descriptionBar = (RelativeLayout) itemView.findViewById(R.id.description_bar);
        videoView = (VideoPlayerView) itemView.findViewById(R.id.video_view);
        heart_label = (TextView) itemView.findViewById(R.id.heart_label);
        followButton = (ImageButton) itemView.findViewById(R.id.follow);
        previewImage = (ImageView) itemView.findViewById(R.id.preview_image);
    }

    public NewFeedItemBinding getBinding() {
        return binding;
    }
}
