package co.optonaut.optonaut.views;

import android.os.Bundle;

import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.model.Person;
import co.optonaut.optonaut.network.PersonManager;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Nilan Marktanner
 * @date 2015-12-15
 */
public class ProfileFeedFragment extends OptographListFragment {
    private Person person;

    public static ProfileFeedFragment newInstance(Person person) {
        ProfileFeedFragment profileFeedFragment = new ProfileFeedFragment();
        Bundle args = new Bundle();
        args.putParcelable("person", person);
        profileFeedFragment.setArguments(args);
        return profileFeedFragment;
    }

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
    }

    @Override
    protected void initializeFeed() {
        apiConsumer.getOptographsFromPerson(person.getId())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(optographFeedAdapter::addItem);

    }

    @Override
    protected void loadMore() {
        apiConsumer.getOptographsFromPerson(person.getId(), optographFeedAdapter.getOldest().getCreated_at())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(optographFeedAdapter::addItem);
    }

    @Override
    protected void refresh() {
        // TODO!
        swipeContainer.setRefreshing(false);
    }
}
