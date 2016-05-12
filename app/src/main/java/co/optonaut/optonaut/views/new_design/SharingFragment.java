package co.optonaut.optonaut.views.new_design;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.bus.BusProvider;
import co.optonaut.optonaut.bus.PersonReceivedEvent;
import co.optonaut.optonaut.model.Person;
import co.optonaut.optonaut.network.PersonManager;
import co.optonaut.optonaut.util.Cache;

public class SharingFragment extends Fragment {
    public static final String TAG = SharingFragment.class.getSimpleName();
    private Person person;
    private Cache cache;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);

        Bundle args = getArguments();
        if(args != null) {
            if (args.containsKey("person")) {
                person = args.getParcelable("person");
            } else if (args.containsKey("id")) {
                PersonManager.loadPerson(args.getString("id"));
            } else {
                throw new RuntimeException();
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sharing, container, false);
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
    public void receivePerson(PersonReceivedEvent personReceivedEvent) {
        person = personReceivedEvent.getPerson();

        if(person != null) {
        }
    }

    public static SharingFragment newInstance() {
        SharingFragment sharingFragment = new SharingFragment();
        return sharingFragment;
    }

    public static SharingFragment newInstance(Person person) {

        SharingFragment sharingFragment = new SharingFragment();
        Bundle args = new Bundle();
        args.putParcelable("person", person);
        sharingFragment.setArguments(args);
        return sharingFragment;
    }

    public static SharingFragment newInstance(String id) {

        SharingFragment sharingFragment = new SharingFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        sharingFragment.setArguments(args);
        return sharingFragment;
    }
}
