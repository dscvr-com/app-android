package co.optonaut.optonaut.views;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.optonaut.optonaut.BR;
import co.optonaut.optonaut.ProfileBinding;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.bus.BusProvider;
import co.optonaut.optonaut.model.Person;

/**
 * @author Nilan Marktanner
 * @date 2015-12-04
 */
public class ProfileFragment extends Fragment {
    private static final String DEBUG_TAG = "Optonaut";
    private static final String PROFILE_FEED_FRAGMENT_TAG = "PROFILE_FEED_FRAGMENT_TAG";
    Person person;
    ProfileBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        person = args.getParcelable("person");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // hide Action Bar to simulate "Fullscreen Mode"
        ((MainActivity) getActivity()).getSupportActionBar().hide();

        BusProvider.getInstance().register(this);

        binding = DataBindingUtil.inflate(inflater, R.layout.profile_fragment, container, false);
        binding.setVariable(BR.person, person);
        binding.executePendingBindings();

        FeedFragment feedFragment = new FeedFragment();
        getChildFragmentManager().beginTransaction().
                replace(R.id.feed_placeholder, feedFragment).addToBackStack(null).commit();

        return binding.getRoot();
    }



}
