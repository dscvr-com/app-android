package co.optonaut.optonaut.views;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Mariel on 3/30/2016.
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
                LogInFragment logInFragment = new LogInFragment();
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
