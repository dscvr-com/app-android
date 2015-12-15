package co.optonaut.optonaut.views;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.model.Person;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public class MainActivity extends AppCompatActivity {
    private final String FEED_FRAGMENT_TAG = "FEED_FRAGMENT";

    private PagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), this);

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void openOptograph2DView(Optograph optograph) {
        HostFragment hostFragment = (HostFragment) pagerAdapter.getItem(viewPager.getCurrentItem());
        hostFragment.replaceFragment(Optograph2DFragment.newInstance(optograph), true);
    }

    public void openProfileFragment(Person person) {
        HostFragment hostFragment = (HostFragment) pagerAdapter.getItem(viewPager.getCurrentItem());
        hostFragment.replaceFragment(ProfileFragment.newInstance(person), true);
    }

    public void openSearchFeed(String keyword) {
        HostFragment hostFragment = (HostFragment) pagerAdapter.getItem(viewPager.getCurrentItem());
        hostFragment.replaceFragment(SearchFeedFragment.newInstance(keyword), true);
    }

    @Override
    public void onBackPressed()
    {
        if(!BackStackFragment.handleBackPressed(getSupportFragmentManager())){
            super.onBackPressed();
        }
    }
}
