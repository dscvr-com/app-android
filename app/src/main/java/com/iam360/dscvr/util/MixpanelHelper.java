package com.iam360.dscvr.util;

import android.content.Context;

import com.iam360.dscvr.BuildConfig;
import com.iam360.dscvr.model.Person;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import timber.log.Timber;

public class MixpanelHelper {
    public static final String MIXPANEL_TOKEN = "905eb49cf2c78af5ceb307939f02c092";//"a032e56b2c02dc758053f6f23173b5e3";

    private static final String LAUNCH_NOTIFICATION = "Launch.Notification";
    private static final String ACTION_CAMERA_CANCEL_RECORDING = "Action.Camera.CancelRecording";
    private static final String ACTION_CAMERA_FINISH_RECORDING = "Action.Camera.FinishRecording";
    private static final String ACTION_CAMERA_START_RECORDING = "Action.Camera.StartRecording";
    private static final String ACTION_CREATE_OPTOGRAPH_POST = "Action.CreateOptograph.Post";
    private static final String ACTION_ENTER_GATE_PASS = "Action.EnterGatePass";
    private static final String ACTION_REQUEST_GATE_PASS = "Action.RequestGatePass";
    private static final String ACTION_STITCHING_FINISH = "Action.Stitching.Finish";
    private static final String ACTION_STITCHING_START = "Action.Stitching.Start";
    private static final String VIEW_CAMERA = "View.Camera";
    private static final String VIEW_CARDBOARD_SELECTION = "View.CardboardSelection";
    private static final String VIEW_OPTOGRAPH_DETAILS = "View.OptographDetails";
    private static final String VIEW_VIEWER = "View.Viewer";

    // MixpanelHelper.trackAppLaunch(getContext());

    public static void track(Context context, String eventName) {
        Timber.d("Tracker : " + eventName);
        MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(context, MIXPANEL_TOKEN);

        if (!BuildConfig.DEBUG) mixpanelAPI.track(eventName);
        else mixpanelAPI.track(eventName);
    }

    public static void identify(Context context, Person person) {
        Timber.d("Identify " + person.getId());
        MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(context, MIXPANEL_TOKEN);
        mixpanelAPI.identify(person.getId());
        mixpanelAPI.getPeople().identify(person.getId());
        mixpanelAPI.getPeople().set("$first", person.getDisplay_name());
        mixpanelAPI.getPeople().set("$username", person.getUser_name());
        mixpanelAPI.getPeople().set("$email", person.getEmail());
        mixpanelAPI.getPeople().set("$created", person.getCreated_at());
        mixpanelAPI.getPeople().set("Followers", person.getFollowers_count());
        mixpanelAPI.getPeople().set("Followed", person.getFollowed_count());
        mixpanelAPI.getPeople().set("EliteStatus", person.isElite_status() ? 1 : 0);
    }

    public static void trackAppLaunch(Context context) {
        track(context, LAUNCH_NOTIFICATION);
    }

    public static void trackCameraCancelRecording(Context context) { track(context, ACTION_CAMERA_CANCEL_RECORDING); }

    public static void trackCameraFinishRecording(Context context) { track(context, ACTION_CAMERA_FINISH_RECORDING); }

    public static void trackCameraStartRecording(Context context) { track(context, ACTION_CAMERA_START_RECORDING); }

    public static void trackCreateOptographPost(Context context) { track(context, ACTION_CREATE_OPTOGRAPH_POST); }

    public static void trackEnterGatePass(Context context) { track(context, ACTION_ENTER_GATE_PASS); }

    public static void trackRequestGatePass(Context context) { track(context, ACTION_REQUEST_GATE_PASS); }

    public static void trackStitchingFinish(Context context) { track(context, ACTION_STITCHING_FINISH); }

    public static void trackStitchingStart(Context context) { track(context, ACTION_STITCHING_START); }

    public static void trackViewCamera(Context context) {
        track(context, VIEW_CAMERA);
    }

    public static void trackViewCardboardSelection(Context context) { track(context, VIEW_CARDBOARD_SELECTION); }

    public static void trackViewOptographDetails(Context context) { track(context, VIEW_OPTOGRAPH_DETAILS); }

    public static void trackViewViewer(Context context) {
        track(context, VIEW_VIEWER);
    }

}
