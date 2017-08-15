package com.iam360.dscvr.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.iam360.dscvr.opengl.Optograph2DCubeView;
import com.iam360.dscvr.sensors.GestureDetectors;

import timber.log.Timber;

/**
 * Created by Emi on 17/07/2017.
 */

public class LockableRecyclerView extends RecyclerView {

    // true if we can scroll (not locked)
    // false if we cannot scroll (locked)
    private boolean mScrollable = true;
    private int lastElement = 0;
    private Optograph2DCubeView.OnScrollLockListener scrollLock = null;

    public LockableRecyclerView(Context context) {
        super(context);

    }

    public void setScrollLock(Optograph2DCubeView.OnScrollLockListener scrollLock){
        this.scrollLock = scrollLock;
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
        if (mScrollable) {
            return super.onTouchEvent(ev);
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (scrollLock!= null && GestureDetectors.singleClickDetector.onTouchEvent(e)) {
            if (scrollLock.isFullscreen()) {
                scrollLock.release();
            } else {
                scrollLock.lock();
            }
            return true;//or false? -- do we need super to stop scrolling?
        }

        boolean b = super.onTouchEvent(e);

        Log.d(this.getClass().getSimpleName(), e.toString());
        Log.d(this.getClass().getSimpleName(), "scrollState: " + getScrollState());
        if (getScrollState() > 0) {
            return b;
        }
        return b;
    }


}