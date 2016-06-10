package com.iam360.iam360.views.new_design;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.iam360.iam360.NewFeedBinding;
import com.iam360.iam360.R;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.viewmodels.InfiniteScrollListener;
import com.iam360.iam360.views.SnappyLinearLayoutManager;
import com.volokh.danylo.video_player_manager.manager.PlayerItemChangeListener;
import com.volokh.danylo.video_player_manager.manager.SingleVideoPlayerManager;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;
import com.volokh.danylo.visibility_utils.calculator.DefaultSingleItemCalculatorCallback;
import com.volokh.danylo.visibility_utils.calculator.ListItemsVisibilityCalculator;
import com.volokh.danylo.visibility_utils.calculator.SingleListViewItemActiveCalculator;
import com.volokh.danylo.visibility_utils.scroll_utils.ItemsPositionGetter;
import com.volokh.danylo.visibility_utils.scroll_utils.RecyclerViewItemPositionGetter;

import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public abstract class OptographListFragment extends Fragment {
    protected OptographFeedAdapter optographFeedAdapter;
    protected ApiConsumer apiConsumer;
    protected Cache cache;
    protected NewFeedBinding binding;
    private int lastVisible = 0;

    private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
    private ItemsPositionGetter mItemsPositionGetter;
    LinearLayoutManager mLayoutManager;

    /**
     * Here we use {@link SingleVideoPlayerManager}, which means that only one video playback is possible.
     */
    private final VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
        @Override
        public void onPlayerItemChanged(MetaData metaData) {

        }
    });

    private ListItemsVisibilityCalculator mVideoVisibilityCalculator;


    public OptographListFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        optographFeedAdapter = new OptographFeedAdapter(getActivity(), mVideoPlayerManager);
        mVideoVisibilityCalculator = new SingleListViewItemActiveCalculator(new DefaultSingleItemCalculatorCallback(), optographFeedAdapter.getVideoItems());

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.new_feed_fragment, container, false);
//        ButterKnife.bind(this, view);
        binding = DataBindingUtil.inflate(inflater, R.layout.new_feed_fragment, container, false);
        mLayoutManager = new LinearLayoutManager(getContext());

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.optographFeed.setLayoutManager(mLayoutManager);
        binding.optographFeed.setAdapter(optographFeedAdapter);
        binding.optographFeed.setItemViewCacheSize(5);

        DefaultItemAnimator animator = new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
                return true;
            }
        };
        binding.optographFeed.setItemAnimator(animator);


        binding.optographFeed.addOnScrollListener(new InfiniteScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore() {
                loadMore();
            }


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                int firstVisiblePosition = mLayoutManager.findFirstCompletelyVisibleItemPosition();
//
//                if (firstVisiblePosition > -1) {
//                    // mca: got completely visible cubemap
//                    if (lastVisible != firstVisiblePosition) {
//                        optographFeedAdapter.rotateCubeMap(firstVisiblePosition);
//                        lastVisible = firstVisiblePosition;
//                    }
//
//                }

                Timber.d("mVideoVisibilityCalculator " + mVideoVisibilityCalculator + " " + mItemsPositionGetter + " " + mLayoutManager + " " + mLayoutManager.findFirstVisibleItemPosition() + " " + mLayoutManager.findLastVisibleItemPosition());

                // TODO Hi Mariel, dito ko may issue. Nag nanull pointer sya pag scroll.
                if(!optographFeedAdapter.getVideoItems().isEmpty()) {
                    mVideoVisibilityCalculator.onScroll(
                            mItemsPositionGetter,
                            mLayoutManager.findFirstVisibleItemPosition(),
                            mLayoutManager.findLastVisibleItemPosition() - mLayoutManager.findFirstVisibleItemPosition() + 1,
                            mScrollState);
                }

            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                mScrollState = scrollState;

                // TODO ito okay to. pag nagstop ka magscroll, after ilang seconds magpeplay yung video na tinigilan. pwede mo icheck yung ibang states SCROLL_STATE_DRAGGING SCROLL_STATE_SETTLING baka pwede natin magamit.
                if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {

                    mVideoVisibilityCalculator.onScrollStateIdle(
                            mItemsPositionGetter,
                            mLayoutManager.findFirstVisibleItemPosition(),
                            mLayoutManager.findLastVisibleItemPosition());
                }
            }
        });

        mItemsPositionGetter = new RecyclerViewItemPositionGetter(mLayoutManager, binding.optographFeed);

    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // load first few optographs
        initializeFeed();
    }

    public Optograph getCurrentOptograph() {
        Optograph optograph = null;

        if (!optographFeedAdapter.isEmpty()) {
            SnappyLinearLayoutManager lm = ((SnappyLinearLayoutManager) binding.optographFeed.getLayoutManager());
            optograph = optographFeedAdapter.get(lm.findFirstVisibleItemPosition());
        }

        return optograph;
    }

    protected abstract void initializeFeed();
    protected abstract void loadMore();
    protected abstract void refresh();

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("mVideoVisibilityCalculator onresume");
        if(!optographFeedAdapter.getVideoItems().isEmpty()){
            // need to call this method from list view handler in order to have filled list

            binding.optographFeed.post(new Runnable() {
                @Override
                public void run() {

                    mVideoVisibilityCalculator.onScrollStateIdle(
                            mItemsPositionGetter,
                            mLayoutManager.findFirstVisibleItemPosition(),
                            mLayoutManager.findLastVisibleItemPosition());

                }
            });
        }

    }
}
