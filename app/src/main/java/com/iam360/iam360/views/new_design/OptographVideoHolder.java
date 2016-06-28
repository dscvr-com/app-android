package com.iam360.iam360.views.new_design;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.danikula.videocache.HttpProxyCacheServer;
import com.iam360.iam360.NewFeedItemBinding;
import com.iam360.iam360.OptonautApp;
import com.iam360.iam360.R;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.util.ImageUrlBuilder;

import im.ene.lab.toro.ToroVideoViewHolder;
import im.ene.lab.toro.widget.ToroVideoView;
import timber.log.Timber;

public class OptographVideoHolder extends ToroVideoViewHolder {

    private NewFeedItemBinding binding;
    protected ImageView imagePreview;
    protected Context context;

    public OptographVideoHolder(View itemView, Context context) {
        super(itemView);
        this.binding= DataBindingUtil.bind(itemView);
        imagePreview = (ImageView) itemView.findViewById(R.id.preview_image);

        this.context = context;

        Timber.d("ToroView OptographVideoHolder");
    }

    public NewFeedItemBinding getBinding() {
        return binding;
    }

    @Override protected ToroVideoView findVideoView(View itemView) {
        return (ToroVideoView) itemView.findViewById(R.id.video_view);
    }

    @Nullable @Override public String getVideoId() {
        return "my awesome video's id and its order: " + getAdapterPosition();
    }

    @Override public void bind(Object object) {

        if ((object instanceof Optograph) && object != null) {
            Optograph optograph = (Optograph) object;
            String url = ImageUrlBuilder.buildVideoUrl(optograph.getId());
            HttpProxyCacheServer proxy = OptonautApp.getProxy(context);
            String proxyUrl = proxy.getProxyUrl(url);
            mVideoView.setVideoPath(proxyUrl);
        }

    }

    @Override public void onViewHolderBound() {
        super.onViewHolderBound();
    }

    // Playback cycle

    @Override public void onVideoPrepared(MediaPlayer mp) {
        super.onVideoPrepared(mp);
    }

    @Override public boolean onPlaybackError(MediaPlayer mp, int what, int extra) {
        imagePreview.setVisibility(View.VISIBLE);
        return super.onPlaybackError(mp, what, extra);
    }

    @Override public void onPlaybackStarted() {
        super.onPlaybackStarted();
    }

    @Override public void onPlaybackProgress(int position, int duration) {
        if (position > 0) {
            imagePreview.setVisibility(View.GONE);
        }
    }

    @Override public void onPlaybackPaused() {
        imagePreview.setVisibility(View.VISIBLE);
    }

    @Override public void onPlaybackStopped() {
        super.onPlaybackStopped();
        imagePreview.setVisibility(View.VISIBLE);
    }
}