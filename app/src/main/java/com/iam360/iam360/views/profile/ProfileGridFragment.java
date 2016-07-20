package com.iam360.iam360.views.profile;

import android.os.Bundle;

import com.iam360.iam360.model.Person;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.network.PersonManager;
import com.iam360.iam360.views.feed.OptographGridFragment;
import com.iam360.iam360.views.dialogs.NetworkProblemDialog;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Nilan Marktanner
 * @date 2015-12-15
 */
public class ProfileGridFragment extends OptographGridFragment {
    private Person person;
    private NetworkProblemDialog networkProblemDialog;

    public static ProfileGridFragment newInstance(Person person) {
        ProfileGridFragment profileFeedFragment = new ProfileGridFragment();
        Bundle args = new Bundle();
        args.putParcelable("person", person);
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
        } else if (args.containsKey("id")) {
            PersonManager.loadPerson(args.getString("id"));
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    protected void initializeFeed() {
        apiConsumer.getOptographsFromPerson(person.getId(), ApiConsumer.PROFILE_GRID_LIMIT)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
                    if(!networkProblemDialog.isAdded())networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
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
                    if(networkProblemDialog.isAdded())networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
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
