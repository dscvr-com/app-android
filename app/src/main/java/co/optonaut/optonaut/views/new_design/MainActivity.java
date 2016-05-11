package co.optonaut.optonaut.views.new_design;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.sensors.CoreMotionListener;
import co.optonaut.optonaut.util.Cache;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.util.DBHelper;
import co.optonaut.optonaut.views.GestureDetectors;
import co.optonaut.optonaut.views.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    public static final int FEED_MODE = 0;
    public static final int PROFILE_MODE = 1;
    private FragmentPagerAdapter adapterViewPager;
    private ViewPager viewPager;
    private Cache cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        initializeComponents();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        viewPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);

    }

    public void setPage(int page) {
        viewPager.setCurrentItem(page, true);
    }

    private void initializeComponents() {

        // instatiate cache on start of application
        cache = Cache.getInstance(this);

        Constants.initializeConstants(this);
        GestureDetectors.initialize(this);
        CoreMotionListener.initialize(this);

        // FB Track App Installs and App Opens
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

    }

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return new MainFeedFragment();
                case 1: // Fragment # 0 - This will show FirstFragment
                    return ProfileFragment.newInstance("c0d5cb2b-7f8a-4de9-a5de-6f7c6cf1cf1a");

                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

    }
}
