package co.optonaut.optonaut.views;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.network.ApiConsumer;
import co.optonaut.optonaut.viewmodels.InfiniteScrollListener;
import co.optonaut.optonaut.viewmodels.OptographFeedAdapter;
import co.optonaut.optonaut.viewmodels.OptographGridAdapter;
import co.optonaut.optonaut.views.redesign.SnappyLinearLayoutManager;
import co.optonaut.optonaut.views.redesign.SnappyRecyclerView;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public abstract class OptographGridFragment extends Fragment {
    protected OptographGridAdapter optographFeedAdapter;
    protected ApiConsumer apiConsumer;
    protected RecyclerView recList;
    public static final int NUM_COLUMNS = 3;


    public OptographGridFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiConsumer = new ApiConsumer(null);
        optographFeedAdapter = new OptographGridAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.grid_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recList = (RecyclerView) view.findViewById(R.id.optographFeed);
        // our children have fixed size
        recList.setHasFixedSize(true);
//        SnappyLinearLayoutManager llm = new SnappyLinearLayoutManager(view.getContext());
//        llm.setOrientation(SnappyLinearLayoutManager.VERTICAL);
        GridLayoutManager llm = new GridLayoutManager(view.getContext(), NUM_COLUMNS);
//        llm.setOrientation(GridLayoutManager.VERTICAL);

//        recList.setLayoutManager(new GridLayoutManager(view.getContext(), NUM_COLUMNS));
//        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(view.getContext(), R.dimen.item_offset);
//        recList.addItemDecoration(itemDecoration);
        recList.setLayoutManager(llm);
        recList.setAdapter(optographFeedAdapter);
//        recList.setItemViewCacheSize(10);

        recList.addOnScrollListener(new InfiniteScrollListener(llm) {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        });
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
            GridLayoutManager lm = ((GridLayoutManager) recList.getLayoutManager());
            optograph = optographFeedAdapter.get(lm.findFirstVisibleItemPosition());
        }

        return optograph;
    }

    protected abstract void initializeFeed();
    protected abstract void loadMore();
    protected abstract void refresh();

    public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        public ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        public ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
        }
    }

}
