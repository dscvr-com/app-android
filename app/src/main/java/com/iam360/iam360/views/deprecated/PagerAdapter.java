package com.iam360.iam360.views.deprecated;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import java.util.ArrayList;
import java.util.List;

import com.iam360.iam360.R;
import com.iam360.iam360.views.HostFragment;
import com.iam360.iam360.views.feed.MainFeedFragment;
import com.iam360.iam360.views.profile.ProfileFragment;

/**
 * @author Nilan Marktanner
 * @date 2015-12-09
 */
public class PagerAdapter extends SmartFragmentStatePagerAdapter {
    private final List<String> tabTitles = new ArrayList<String>() {{
        add("Feed");
        add("Search");
        add("Profile");
    }};
    private final int[] imageResId =  {
            R.mipmap.ic_voicemail_white_24dp,
            R.mipmap.ic_search_white_24dp,
            R.mipmap.ic_account_circle_white_24dp
    };;

    private final Context context;

    private List<Fragment> tabs = new ArrayList<>();

    public PagerAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        this.context = context;

        initializeTabs();
    }

    private void initializeTabs() {
        tabs.add(HostFragment.newInstance(new MainFeedFragment(), tabTitles.get(0)));
        tabs.add(HostFragment.newInstance(new SearchFragment(), tabTitles.get(1)));

        ProfileFragment profileFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("id", "1064fd0e-833b-4a6b-b4bc-d90a03074eba");
        profileFragment.setArguments(args);
        tabs.add(HostFragment.newInstance(profileFragment, tabTitles.get(2)));
    }

    @Override
    public Fragment getItem(int position) {
        return tabs.get(position);
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Drawable image = ContextCompat.getDrawable(context, imageResId[position]);
        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
        SpannableString sb = new SpannableString(" ");
        ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }

}
