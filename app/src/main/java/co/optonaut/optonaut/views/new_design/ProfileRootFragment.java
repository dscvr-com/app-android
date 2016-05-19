package co.optonaut.optonaut.views.new_design;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.util.Cache;
import co.optonaut.optonaut.views.profile.ProfileFragment;
import co.optonaut.optonaut.views.profile.SigninFBFragment;

public class ProfileRootFragment extends Fragment {

    private Cache cache;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		/* Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.fragment_profile_root, container, false);

        cache = Cache.getInstance(getContext());

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        if (cache.getString(Cache.USER_TOKEN).isEmpty()) {
            transaction.replace(R.id.root_frame, SigninFBFragment.newInstance("", ""));
        } else
            transaction.replace(R.id.root_frame, ProfileFragment.newInstance(cache.getString(Cache.USER_ID)));

        transaction.commit();

        return view;
    }
}
