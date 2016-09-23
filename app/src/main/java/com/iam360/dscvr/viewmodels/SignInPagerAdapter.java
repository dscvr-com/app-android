package com.iam360.dscvr.viewmodels;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.iam360.dscvr.views.fragment.SignUpFragment;
import com.iam360.dscvr.views.fragment.SigninFBFragment;

/**
 * Created by Mariel on 9/22/2016.
 */
public class SignInPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public SignInPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                SignUpFragment signUpFragment = new SignUpFragment();
                return signUpFragment;
            case 1:
                SigninFBFragment logInFragment = new SigninFBFragment();
                return logInFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
