package co.optonaut.optonaut.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.model.Person;
import co.optonaut.optonaut.network.ApiConsumer;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

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

        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), this);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        int i = 0;
    }

    public void openOptograph2DView(Optograph optograph) {
        TabFragment tabFragment = (TabFragment) pagerAdapter.getItem(viewPager.getCurrentItem());

        Optograph2DFragment optograph2DFragment = new Optograph2DFragment();
        Bundle args = new Bundle();
        args.putParcelable("optograph", optograph);
        optograph2DFragment.setArguments(args);

        tabFragment.replaceFragment(optograph2DFragment, true);
    }

    public void openProfileFragment(Person person) {
        TabFragment tabFragment = (TabFragment) pagerAdapter.getItem(viewPager.getCurrentItem());

        ProfileFragment profileFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable("person", person);
        profileFragment.setArguments(args);

        tabFragment.replaceFragment(profileFragment, true);
    }

    @Override
    public void onBackPressed()
    {
        if(!BaseFragment.handleBackPressed(getSupportFragmentManager())){
            super.onBackPressed();
        }
    }
}
