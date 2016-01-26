package co.optonaut.optonaut.views.redesign;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.util.ImageUrlBuilder;
import co.optonaut.optonaut.views.HostFragment;
import co.optonaut.optonaut.views.MainFeedFragment;

/**
 * @author Nilan Marktanner
 * @date 2015-12-29
 */
public class MainActivityRedesign extends AppCompatActivity {
    HostFragment hostFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize constants
        Constants.initializeConstants(this);

        setContentView(R.layout.activity_main_redesign);

        if (findViewById(R.id.feed_placeholder) != null) {
            if (savedInstanceState != null) {
                return;
            }

            MainFeedFragment mainSnappyFeedFragment = new MainFeedFragment();
            hostFragment = HostFragment.newInstance(mainSnappyFeedFragment, "Main Snappy Feed Fragment");
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.feed_placeholder, hostFragment).commit();

            // TODO: add overlay layout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.feed_placeholder, new OverlayNavigationFragment()).commit();
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
}
