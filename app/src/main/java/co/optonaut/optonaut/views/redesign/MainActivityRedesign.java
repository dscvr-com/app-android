package co.optonaut.optonaut.views.redesign;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.record.RecordFragment;
import co.optonaut.optonaut.sensors.CoreMotionListener;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.util.ImageUrlBuilder;
import co.optonaut.optonaut.util.MixpanelHelper;
import co.optonaut.optonaut.views.BackStackFragment;
import co.optonaut.optonaut.views.GestureDetectors;
import co.optonaut.optonaut.views.HostFragment;
import co.optonaut.optonaut.views.MainFeedFragment;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-12-29
 */
public class MainActivityRedesign extends AppCompatActivity {
    private static final int REQUEST_SHARE = 1;
    ;
    private MixpanelAPI mixpanelAPI;

    private HostFragment hostFragment;
    private OverlayNavigationFragment overlayFragment;

    private boolean isStatusBarVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initialize constants
        Constants.initializeConstants(this);
        GestureDetectors.initialize(this);
        CoreMotionListener.initialize(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_redesign);

        if (findViewById(R.id.feed_placeholder) != null) {
            if (savedInstanceState != null) {
                return;
            }

            MainFeedFragment mainSnappyFeedFragment = new MainFeedFragment();
            hostFragment = HostFragment.newInstance(mainSnappyFeedFragment, "Main Snappy Feed Fragment");
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.feed_placeholder, hostFragment).commit();

            overlayFragment = new OverlayNavigationFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.feed_placeholder, overlayFragment).commit();
        }

        initializeMixpanel();
        goFullscreen();
    }

    private void initializeMixpanel() {
        this.mixpanelAPI = MixpanelAPI.getInstance(this, MixpanelHelper.MIXPANEL_TOKEN);
    }

    private void goFullscreen() {
        // go full fullscreen mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        isStatusBarVisible = true;
    }

    private void hideStatusBar() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        isStatusBarVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        findFragments();
        restoreUIState();
    }

    @Override
    public void onDestroy() {
        // flush mixpanel events before destroying
        mixpanelAPI.flush();
        super.onDestroy();
    }

    private void restoreUIState() {
        if (isStatusBarVisible) {
            goFullscreen();
        } else {
            hideStatusBar();
        }
    }

    private void findFragments() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (hostFragment == null && fragment instanceof HostFragment) {
                hostFragment = (HostFragment) fragment;
            } else if (overlayFragment == null && fragment instanceof OverlayNavigationFragment) {
                overlayFragment = (OverlayNavigationFragment) fragment;
            } else {
                Timber.w("unknown fragment in SupportFragmentManager");
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            restoreUIState();
        }
    }

    @Override
    public void onBackPressed() {
        if(!BackStackFragment.handleBackPressed(getSupportFragmentManager())){
            super.onBackPressed();
        } else {
            // TODO: find better solution
            goFullscreen();
        }
    }

    public int getUpperBoundary() {
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);

        // don't set preference to 0 if it is not set yet
        if (sharedPref.contains(getResources().getString(R.string.preference_upperboundary))) {
            return sharedPref.getInt(getResources().getString(R.string.preference_upperboundary), 0);
        }

        return 0;
    }

    public int getLowerBoundary() {
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);

        // don't set preference to 0 if it is not set yet
        if (sharedPref.contains(getResources().getString(R.string.preference_lowerboundary))) {
            return sharedPref.getInt(getResources().getString(R.string.preference_lowerboundary), 0);
        }

        return 0;
    }

    public void setOverlayVisibility(int visibility) {
        if (overlayFragment != null) {
            overlayFragment.setTotalVisibility(visibility);
            if (visibility == View.VISIBLE) {
                goFullscreen();
            } else {
                hideStatusBar();
            }
        } else {
            Timber.w("setting overlay visibility on null object");
        }
    }

    public boolean toggleOverlayVisibility() {
        return overlayFragment.toggleTotalVisibility();
    }

    public void shareOptograph(Optograph optograph) {
        String shareUrl = ImageUrlBuilder.buildWebViewerUrl(optograph.getShare_alias());

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject_web_viewer));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.share_body_web_viewer, shareUrl));
        sharingIntent.setType("text/plain");
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_via)));

        Timber.v("Initiated Sharing");
        MixpanelHelper.trackActionViewer2DShare(this);
    }

    public void prepareVRMode() {
        if (overlayFragment != null) {
            overlayFragment.hideDialog();
        }
    }

    public void prepareRecording() {
        hideStatusBar();
        hostFragment.replaceFragment(new RecordFragment(), true);
    }

    public void startRecording() {
        if (hostFragment.getCurrentFragment() instanceof RecordFragment) {
            ((RecordFragment) hostFragment.getCurrentFragment()).startRecording();
        } else {
            Timber.e("Tried to take picture but no record fragment available");
        }
    }

    public void finishRecording() {
        if (hostFragment.getCurrentFragment() instanceof RecordFragment) {
            ((RecordFragment) hostFragment.getCurrentFragment()).finishRecording();
        } else {
            Timber.e("Tried to finish recording, but no record fragment available");
        }
    }
}
