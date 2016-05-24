package com.iam360.iam360.record;

/**
 * @author Nilan Marktanner
 * @date 2016-02-13
 */
public class GlobalState {
    private GlobalState() {
        // do nothing
    }

    public static boolean isAnyJobRunning = false;
    public static boolean shouldHardRefreshFeed = false;

}
