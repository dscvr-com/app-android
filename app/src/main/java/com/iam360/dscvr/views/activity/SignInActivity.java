package com.iam360.dscvr.views.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iam360.dscvr.R;
import com.iam360.dscvr.util.GeneralUtils;
import com.iam360.dscvr.util.MyViewPager;
import com.iam360.dscvr.views.fragment.SignUpFragment;
import com.iam360.dscvr.views.fragment.SigninFBFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mariel on 9/22/2016.
 */
public class SignInActivity extends AppCompatActivity {

    RelativeLayout loginTabBg;
    TextView loginText;
    RelativeLayout signupTabBg;
    TextView signupText;

    MyViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle("");

        loginTabBg = (RelativeLayout) findViewById(R.id.login_tab);
        loginText = (TextView) findViewById(R.id.login_text);
        signupTabBg = (RelativeLayout) findViewById(R.id.signup_tab);
        signupText = (TextView) findViewById(R.id.signup_text);

        GeneralUtils utils = new GeneralUtils();
        utils.setFont(this, loginText, Typeface.NORMAL);
        utils.setFont(this, signupText, Typeface.NORMAL);

        viewPager = (MyViewPager) findViewById(R.id.viewpager_signin);
        setupViewPager(viewPager);

//        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs_signin);
//        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(0);
        updateTabs(viewPager.getCurrentItem());

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateTabs(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        loginTabBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0,true);
            }
        });
        signupTabBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1,true);
            }
        });
    }

    public void swipeEnable(boolean enable) {
        viewPager.setPageEnable(enable);
        loginTabBg.setClickable(enable);
        signupTabBg.setClickable(enable);
    }

    public void updateTabs(int position) {
        Log.d("myTag"," updateTabs position: "+position);
        loginTabBg.setBackgroundColor((position==0) ? getResources().getColor(R.color.bright) : getResources().getColor(R.color.btn_yellow));
        loginText.setTextColor((position==0) ? getResources().getColor(R.color.btn_yellow) : getResources().getColor(R.color.bright));
        signupTabBg.setBackgroundColor((position==1) ? getResources().getColor(R.color.bright) : getResources().getColor(R.color.btn_yellow));
        signupText.setTextColor((position==1) ? getResources().getColor(R.color.btn_yellow) : getResources().getColor(R.color.bright));
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new SigninFBFragment(), getResources().getString(R.string.log_in));
        adapter.addFragment(new SignUpFragment(), getResources().getString(R.string.sign_up));
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.signin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
