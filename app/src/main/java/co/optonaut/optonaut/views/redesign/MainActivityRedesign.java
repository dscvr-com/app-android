package co.optonaut.optonaut.views.redesign;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import javax.xml.transform.Result;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.sensors.CoreMotionListener;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.util.ResultCodes;
import co.optonaut.optonaut.views.GestureDetectors;
import co.optonaut.optonaut.views.HostFragment;
import co.optonaut.optonaut.views.MainFeedFragment;

/**
 * @author Nilan Marktanner
 * @date 2015-12-29
 */
public class MainActivityRedesign extends AppCompatActivity {
    private HostFragment hostFragment;
    private OverlayNavigationFragment overlayFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: initialize in Application
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

        goFullscreen();
    }

    private void goFullscreen() {
        // go full fullscreen mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        /*
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        */
    }

    @Override
    public void onResume() {
        super.onResume();
        findFragments();
    }

    private void findFragments() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (hostFragment == null && fragment instanceof HostFragment) {
                hostFragment = (HostFragment) fragment;
            } else if (overlayFragment == null && fragment instanceof OverlayNavigationFragment) {
                overlayFragment = (OverlayNavigationFragment) fragment;
            } else {
                Log.w(Constants.DEBUG_TAG, "Unknown Fragment in SupportFragmentManager");
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        goFullscreen();
    }

    @Override
    public void onBackPressed() {
        // do nothing
        // TODO: retain fragment state
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
        } else {
            Log.w(Constants.DEBUG_TAG, "Setting overlay visibility on null object");
        }
    }

    public boolean toggleOverlayVisibility() {
        return overlayFragment.toggleTotalVisibility();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri imageUri;
        switch (requestCode) {
            case ResultCodes.RESULT_GALLERY :
                if (null != data) {
                    imageUri = data.getData();
                    Log.v(Constants.DEBUG_TAG, "uri: " + imageUri.toString());
                    // TODO: hit API endpoint with this Uri, then start Share-Intent
                    String result_url = "random_url";
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject_web_viewer));
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.share_body_web_viewer, result_url));
                    sharingIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_via)));
                }
                break;
            default:
                break;
        }
    }
}
