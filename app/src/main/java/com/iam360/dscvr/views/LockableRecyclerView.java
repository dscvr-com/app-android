package com.iam360.dscvr.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import timber.log.Timber;

/**
 * Created by Emi on 17/07/2017.
 */

public class LockableRecyclerView extends RecyclerView {

    // true if we can scroll (not locked)
    // false if we cannot scroll (locked)
    private boolean mScrollable = true;

    public LockableRecyclerView(Context context) {
        super(context);
    }

    public LockableRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LockableRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setIsScrollable(boolean scrollable) {
        mScrollable = scrollable;
        Timber.d("scrollable: " + mScrollable);
    }
    public boolean getIsScrollable() {
        return mScrollable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
                // if we can scroll pass the event to the superclass
                if (mScrollable) return super.onTouchEvent(ev);
                // only continue to handle the touch event if scrolling enabled
                return mScrollable; // mScrollable is always false at this point
    }
}