package co.optonaut.optonaut.viewmodels;

/**
 * @author Nilan Marktanner
 * @date 2015-11-29
 */
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;


// Source: https://gist.github.com/ssinss/e06f12ef66c51252563e
public abstract class InfiniteScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = InfiniteScrollListener.class.getSimpleName();

    // The total number of items in the dataset after the last load
    private int previousTotal = 0;

    // True if we are still waiting for the last set of data to load.
    private boolean loading = true;

    // The minimum amount of items to have below your current scroll position before loading more.
    private int visibleThreshold = 5;

    private int firstVisibleItem;
    private int visibleItemCount;
    private int totalItemCount;
    private int current_page = 1;

    private LinearLayoutManager llm;

    public InfiniteScrollListener(LinearLayoutManager llm) {
        this.llm = llm;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = llm.getItemCount();
        firstVisibleItem = llm.findFirstVisibleItemPosition();

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            current_page++;
            onLoadMore(current_page);
            loading = true;
        }
    }

    public abstract void onLoadMore(int current_page);
}