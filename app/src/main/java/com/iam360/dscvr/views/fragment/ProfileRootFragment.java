package com.iam360.dscvr.views.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iam360.dscvr.R;
import com.iam360.dscvr.util.Cache;

import timber.log.Timber;

public class ProfileRootFragment extends Fragment {

    private Cache cache;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		/* Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.fragment_profile_root, container, false);

        cache = Cache.getInstance(getContext());

        FragmentTransaction transaction = getFragmentManager().beginTransaction();


        if (cache.getString(Cache.USER_TOKEN).isEmpty()) {
//            transaction.replace(R.id.root_frame, SigninFBFragment.newInstance("", ""));
            transaction.replace(R.id.root_frame, SignInFragment.newInstance("", ""));
        } else {
            Timber.d("Not logged in.");
            if (cache.getString(Cache.GATE_CODE).isEmpty()) {
                transaction.replace(R.id.root_frame, MailingListFragment.newInstance());
            } else
                transaction.replace(R.id.root_frame, ProfileFragmentExercise.newInstance(cache.getString(Cache.USER_ID)));
        }

        transaction.commit();

        return view;
    }

    public void refresh() {
        for (Fragment frag:getFragmentManager().getFragments()) {
            if (frag instanceof ProfileFragmentExercise) ((ProfileFragmentExercise) frag).refresh();
        }
    }

    public void switchToProfilePage() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.root_frame, ProfileFragmentExercise.newInstance(cache.getString(Cache.USER_ID)));
        transaction.commit();
    }

    public void swipeEnable(boolean clickable) {
        for (Fragment frag:getFragmentManager().getFragments()) {
            if (frag instanceof SignInFragment) ((SignInFragment) frag).swipeEnable(clickable);
        }
    }
}
