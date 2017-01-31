package com.iam360.dscvr.viewmodels;

/**
 * @author Nilan Marktanner
 * @date 2015-11-29
 */
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;


// Source: https://gist.github.com/ssinss/e06f12ef66c51252563e
// and https://github.com/codepath/android_guides/wiki/Endless-Scrolling-with-AdapterViews-and-RecyclerView
public abstract class InfiniteScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = InfiniteScrollListener.class.getSimpleName();

    // The total number of items in the dataset after the getOldest load
    private int previousTotal = 0;

    // True if we are still waiting for the getOldest set of data to load.
    private boolean loading = true;

    // The minimum amount of items to have below your current scroll position before loading more.
    private int visibleThreshold = 3;

    private int firstVisibleItem;
    private int visibleItemCount;
    private int totalItemCount;

    private LinearLayoutManager llm;

    public InfiniteScrollListener(LinearLayoutManager llm) {
        this.llm = llm;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        // don't react on layout changes
        if (dx == 0 && dy == 0) {
            return;
        }

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = llm.getItemCount();
        firstVisibleItem = llm.findFirstVisibleItemPosition();

        /*
         * If the total item count is zero and the previous isn't, assume the
         * list is invalidated and should be reset back to initial state
         */
        if (totalItemCount < previousTotal) {
            this.previousTotal = totalItemCount;
            if (totalItemCount == 0) {
                this.loading = true;
            }
        }

        /*
         * If it’s still loading, we check to see if the dataset count has
         * changed, if so we conclude it has finished loading and update the current page
         * number and total item count.
         */
        if (loading && totalItemCount > previousTotal) {
            loading = false;
            previousTotal = totalItemCount;
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            onLoadMore();
            loading = true;
        }
    }

    public abstract void onLoadMore();
}