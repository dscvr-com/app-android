package co.optonaut.optonaut.views;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import co.optonaut.optonaut.FeedItemBinding;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.model.Person;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public class MainActivity extends AppCompatActivity {
    private final String FEED_FRAGMENT_TAG = "FEED_FRAGMENT";

    private MainPagerAdapter mainPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager(),
                MainActivity.this));

        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    public void openOptograph2DView(Optograph optograph) {
        Optograph2DFragment optograph2DFragment = new Optograph2DFragment();
        Bundle args = new Bundle();
        args.putParcelable("optograph", optograph);
        optograph2DFragment.setArguments(args);

        //getSupportFragmentManager().beginTransaction().
        //        replace(R.id.fragment_placeholder, optograph2DFragment).addToBackStack(null).commit();
    }

    public void openProfileFragment(Person person) {
        ProfileFragment profileFragment= new ProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable("person", person);
        profileFragment.setArguments(args);

        //getSupportFragmentManager().beginTransaction().
        //        replace(R.id.fragment_placeholder, profileFragment).addToBackStack(null).commit();
    }



    public static class MainPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 4;
        private String tabTitles[] = new String[] { "Feed", "Search", "Notifications", "Profile" };
        private Context context;

        public MainPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            return new FeedFragment();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }



}
