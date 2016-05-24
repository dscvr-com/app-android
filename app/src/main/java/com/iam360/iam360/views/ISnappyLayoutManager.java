package com.iam360.iam360.views;

/**
 * @author Nilan Marktanner
 * @date 2016-01-07
 */

/**
 * An interface that LayoutManagers that should snap to grid should implement.
 */
// source: http://stackoverflow.com/a/26445064/1176596
public interface ISnappyLayoutManager {

    /**
     * @param velocityX
     * @param velocityY
     * @return the resultant position from a fling of the given velocity.
     */
    int getPositionForVelocity(int velocityX, int velocityY);

    /**
     * @return the position this list must scroll to to fix a state where the
     * views are not snapped to grid.
     */
    int getFixScrollPos();

}
