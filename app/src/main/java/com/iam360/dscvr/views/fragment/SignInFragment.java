package com.iam360.dscvr.views.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iam360.dscvr.R;
import com.iam360.dscvr.util.GeneralUtils;
import com.iam360.dscvr.util.MyViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mariel on 10/3/2016.
 */
public class SignInFragment extends Fragment {

    RelativeLayout loginTabBg;
    TextView loginText;
    RelativeLayout signupTabBg;
    TextView signupText;

    MyViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_sign_in, container, false);

        loginTabBg = (RelativeLayout) view.findViewById(R.id.login_tab);
        loginText = (TextView) view.findViewById(R.id.login_text);
        signupTabBg = (RelativeLayout) view.findViewById(R.id.signup_tab);
        signupText = (TextView) view.findViewById(R.id.signup_text);

        GeneralUtils utils = new GeneralUtils();
        utils.setFont(getContext(), loginText, Typeface.NORMAL);
        utils.setFont(getContext(), signupText, Typeface.NORMAL);

        viewPager = (MyViewPager) view.findViewById(R.id.viewpager_signin);
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
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new SigninFBFragment(), getResources().getString(R.string.log_in));
        adapter.addFragment(new SignUpFragment(), getResources().getString(R.string.sign_up));
        viewPager.setAdapter(adapter);
    }

    public void updateTabs(int position) {
        Log.d("myTag", " updateTabs position: " + position);
        loginTabBg.setBackgroundColor((position==0) ? getResources().getColor(R.color.bright) : getResources().getColor(R.color.btn_yellow));
        loginText.setTextColor((position == 0) ? getResources().getColor(R.color.btn_yellow) : getResources().getColor(R.color.bright));
        signupTabBg.setBackgroundColor((position == 1) ? getResources().getColor(R.color.bright) : getResources().getColor(R.color.btn_yellow));
        signupText.setTextColor((position == 1) ? getResources().getColor(R.color.btn_yellow) : getResources().getColor(R.color.bright));
    }

    public void swipeEnable(boolean enable) {
//        viewPager.setPageEnable(enable);
        loginTabBg.setClickable(enable);
        signupTabBg.setClickable(enable);
    }

    public static SignInFragment newInstance(String param1, String param2) {
        SignInFragment fragment = new SignInFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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
