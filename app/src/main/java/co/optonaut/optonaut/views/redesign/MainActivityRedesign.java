package co.optonaut.optonaut.views.redesign;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.views.MainFeedFragment;

/**
 * @author Nilan Marktanner
 * @date 2015-12-29
 */
public class MainActivityRedesign extends AppCompatActivity {

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
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.feed_placeholder, mainSnappyFeedFragment).commit();
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
