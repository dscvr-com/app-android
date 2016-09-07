package com.iam360.dscvr.util;

import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

/**
 * @author Nilan Marktanner
 * @date 2016-02-02
 */
public class MixpanelHelper {
    public static final String MIXPANEL_TOKEN = "a032e56b2c02dc758053f6f23173b5e3";

    private static final String ACTION_VIEWER2D_SHARE = "Action.Viewer2D.Share";
    private static final String ACTION_VIEWER2D_VRBUTTON = "Action.Viewer2D.VRButton";

    private static final String VIEW_VIEWER2D = "View.Viewer2D";
    private static final String VIEW_VIEWERVR = "View.ViewerVR";

    public static void trackActionViewer2DShare(Context context) {
        MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(context, MIXPANEL_TOKEN);
        mixpanelAPI.track(ACTION_VIEWER2D_SHARE);
    }

    public static void trackActionViewer2DVRButton(Context context) {
        MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(context, MIXPANEL_TOKEN);
        mixpanelAPI.track(ACTION_VIEWER2D_VRBUTTON);
    }

    public static void trackViewViewer2D(Context context) {
        MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(context, MIXPANEL_TOKEN);
        mixpanelAPI.track(VIEW_VIEWER2D);
    }

    public static void trackViewViewerVR(Context context) {
        MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(context, MIXPANEL_TOKEN);
        mixpanelAPI.track(VIEW_VIEWERVR);
    }

    public static void flush(Context context) {
        MixpanelAPI mixpanelAPI= MixpanelAPI.getInstance(context, MIXPANEL_TOKEN);
    }
}
