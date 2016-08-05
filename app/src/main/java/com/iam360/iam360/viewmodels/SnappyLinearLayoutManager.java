package com.iam360.iam360.viewmodels;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.iam360.iam360.util.Constants;

/**
 * @author Nilan Marktanner
 * @date 2016-01-07
 */

// source: http://stackoverflow.com/a/26445064/1176596
public class SnappyLinearLayoutManager extends LinearLayoutManager implements ISnappyLayoutManager {
    public SnappyLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public int getPositionForVelocity(int velocityX, int velocityY) {
        if (getChildCount() == 0) {
            return 0;
        }
        if (getOrientation() == HORIZONTAL) {
            return calcPosForVelocity(velocityX, getPosition(getChildAt(0)));
        } else {
            return calcPosForVelocity(velocityY, getPosition(getChildAt(0)));
        }
    }

    private int calcPosForVelocity(int velocity, int currPos) {
        if (velocity < 0) {
            return currPos;
        } else {
            return currPos + 1;
        }
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        final LinearSmoothScroller linearSmoothScroller =
                new LinearSmoothScroller(recyclerView.getContext()) {

                    // I want a behavior where the scrolling always snaps to the beginning of
                    // the list. Snapping to end is also trivial given the default implementation.
                    // If you need a different behavior, you may need to override more
                    // of the LinearSmoothScrolling methods.
                    protected int getHorizontalSnapPreference() {
                        return mTargetVector == null || mTargetVector.x == 0 ? SNAP_TO_ANY :
                                mTargetVector.x > 0 ? SNAP_TO_END : SNAP_TO_START;
                    }

                    protected int getVerticalSnapPreference() {
                        return mTargetVector == null || mTargetVector.y == 0 ? SNAP_TO_ANY :
                                mTargetVector.y > Constants.getInstance().getDisplayMetrics().heightPixels / 2.0f ? SNAP_TO_END : SNAP_TO_START;
                    }

                    @Override
                    public PointF computeScrollVectorForPosition(int targetPosition) {
                        return SnappyLinearLayoutManager.this
                                .computeScrollVectorForPosition(targetPosition);
                    }
                };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    /**
     * This implementation obviously doesn't take into account the direction of the
     * that preceded it, but there is no easy way to get that information without more
     * hacking than I was willing to put into it.
     */
    @Override
    public int getFixScrollPos() {
        if (this.getChildCount() == 0) {
            return 0;
        }

        final View child = getChildAt(0);
        final int childPos = getPosition(child);

        if (getOrientation() == HORIZONTAL
                && Math.abs(child.getLeft()) > child.getMeasuredWidth() / 2) {
            // Scrolled first view more than halfway offscreen
            return childPos + 1;
        } else if (getOrientation() == VERTICAL
                && Math.abs(child.getTop()) > child.getMeasuredHeight() / 2) {
            // Scrolled first view more than halfway offscreen
            return childPos + 1;
        }
        return childPos;
    }

}
