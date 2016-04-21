package co.optonaut.optonaut.views.deprecated;

import android.os.Bundle;

import co.optonaut.optonaut.model.Person;
import co.optonaut.optonaut.network.PersonManager;
import co.optonaut.optonaut.views.OptographListFragment;
import co.optonaut.optonaut.views.dialogs.NetworkProblemDialog;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Nilan Marktanner
 * @date 2015-12-15
 */
public class ProfileFeedFragment extends OptographListFragment {
    private Person person;
    private int position;
    private NetworkProblemDialog networkProblemDialog;

    public static ProfileFeedFragment newInstance(Person person, int position) {
        ProfileFeedFragment profileFeedFragment = new ProfileFeedFragment();
        Bundle args = new Bundle();
        args.putParcelable("person", person);
        args.putInt("position", position);
        profileFeedFragment.setArguments(args);
        return profileFeedFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkProblemDialog = new NetworkProblemDialog();

        Bundle args = getArguments();
        if (args.containsKey("person")) {
            person = args.getParcelable("person");
            position = args.getInt("position");
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
                .onErrorReturn(throwable -> {
                    networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                    return null;
                })
                .subscribe(optographFeedAdapter::addItem);

    }

    @Override
    protected void loadMore() {
        apiConsumer.getOptographsFromPerson(person.getId(), optographFeedAdapter.getOldest().getCreated_at())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
                    networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                    return null;
                })
                .subscribe(optographFeedAdapter::addItem);
    }

    @Override
    protected void refresh() {
        // TODO!
        // swipeContainer.setRefreshing(false);
    }
}
