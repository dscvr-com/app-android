package co.optonaut.optonaut.views.redesign;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.util.Constants;
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
        // initialize constants
        Constants.initializeConstants(this);
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
}
