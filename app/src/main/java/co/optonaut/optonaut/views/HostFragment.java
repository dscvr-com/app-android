package co.optonaut.optonaut.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.optonaut.optonaut.R;

/**
 * @author Nilan Marktanner
 * @date 2015-12-09
 */
public class HostFragment extends BackStackFragment {
    private String title;
    private Fragment fragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tab_fragment, container, false);
        if (fragment != null ) {
            replaceFragment(fragment, false);
        }
        return view;
    }

    public void replaceFragment(Fragment fragment, boolean addToBackstack) {
        if (addToBackstack) {
            getChildFragmentManager().beginTransaction().replace(R.id.root_frame, fragment).addToBackStack(null).commit();
        } else {
            getChildFragmentManager().beginTransaction().replace(R.id.root_frame, fragment).commit();
        }
    }

    public static HostFragment newInstance(Fragment fragment, String title) {
        HostFragment hostFragment = new HostFragment();
        hostFragment.fragment = fragment;
        hostFragment.title = title;
        return hostFragment;
    }

    public String getTitle() {
        return title;
    }
}
