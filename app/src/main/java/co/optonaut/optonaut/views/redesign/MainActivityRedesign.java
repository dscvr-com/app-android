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

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("");

        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setTypeface(Constants.getInstance().getDefaultTypeface());
        searchButton.setText(String.valueOf((char) 0xe91f));
        searchButton.setOnClickListener(v -> {
            Snackbar.make(v, getResources().getString(R.string.feature_next_version), Snackbar.LENGTH_LONG).show();
        });

        ImageView header = (ImageView) findViewById(R.id.header);
        header.setImageDrawable(Constants.getInstance().getMainIcon());

        Button notificationButton = (Button) findViewById(R.id.notification_button);
        notificationButton.setTypeface(Constants.getInstance().getDefaultTypeface());
        notificationButton.setText(String.valueOf((char) 0xe90f));
        notificationButton.setOnClickListener(v -> {
            Snackbar.make(v, getResources().getString(R.string.feature_next_version), Snackbar.LENGTH_LONG).show();
        });
        setSupportActionBar(toolbar);

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
