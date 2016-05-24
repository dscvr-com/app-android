package com.iam360.iam360.views.profile;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import com.iam360.iam360.BR;
import com.iam360.iam360.ProfileBinding;
import com.iam360.iam360.R;
import com.iam360.iam360.bus.BusProvider;
import com.iam360.iam360.bus.PersonReceivedEvent;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.network.PersonManager;

/**
 * @author Nilan Marktanner
 * @date 2015-12-04
 */
public class ProfileOptographsFragment extends Fragment {
    private final String TAG = ProfileOptographsFragment.class.getSimpleName();
    private static final String DEBUG_TAG = "Optonaut";
    private static final String PROFILE_FEED_FRAGMENT_TAG = "PROFILE_FEED_FRAGMENT_TAG";
    Person person;
    ProfileBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args.containsKey("person")) {
            person = args.getParcelable("person");
        } else if (args.containsKey("id")) {
            PersonManager.loadPerson(args.getString("id"));
        } else {
            throw new RuntimeException();
        }


        Log.d(TAG, "Person1 : " + person.getDisplay_name());
//        PersonManager.loadPerson("3a5dcf44-d3bf-42d7-ba84-20096e48e48c");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.profile_fragment, container, false);
        binding.setVariable(BR.person, person);
        binding.executePendingBindings();

        if (person != null) {
            initializeProfileFeed();
        }

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void reveicePerson(PersonReceivedEvent personReceivedEvent) {
        Log.d("Optonaut", "Registered person");
        person = personReceivedEvent.getPerson();
        binding.setVariable(BR.person, person);


        initializeProfileFeed();

        binding.executePendingBindings();
    }

    private void initializeProfileFeed() {
        ProfileFeedFragment profileFeedFragment = ProfileFeedFragment.newInstance(person, 0);

        getChildFragmentManager().beginTransaction().
                replace(R.id.feed_placeholder, profileFeedFragment).addToBackStack(null).commit();
    }

    public static ProfileOptographsFragment newInstance(Person person) {
        ProfileOptographsFragment profileFragment = new ProfileOptographsFragment();
        Bundle args = new Bundle();
        args.putParcelable("person", person);
        profileFragment.setArguments(args);
        return profileFragment;
    }



}
