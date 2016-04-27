package co.optonaut.optonaut.views.deprecated;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import co.optonaut.optonaut.BR;
import co.optonaut.optonaut.ProfileBinding;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.bus.BusProvider;
import co.optonaut.optonaut.bus.PersonReceivedEvent;
import co.optonaut.optonaut.model.LogInReturn;
import co.optonaut.optonaut.model.Person;
import co.optonaut.optonaut.network.ApiConsumer;
import co.optonaut.optonaut.network.PersonManager;
import co.optonaut.optonaut.util.Cache;
import co.optonaut.optonaut.views.redesign.MainActivityRedesign;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-12-04
 */
public class ProfileFragment extends Fragment {
    public static final String TAG = ProfileFragment.class.getSimpleName();
    private static final String DEBUG_TAG = "Optonaut";
    private static final String PROFILE_FEED_FRAGMENT_TAG = "PROFILE_FEED_FRAGMENT_TAG";
    private Person person;
    private ProfileBinding binding;
    private Cache cache;

    private Button button;
    private boolean isCurrentUser = false;
    private boolean isEditMode = false;
    private ApiConsumer apiConsumer;

    //290fae3e-6d30-41a8-8331-4eeafbdcd206
    @Override
    public void onCreate(Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);

        Timber.d("USERID : %s", cache.getString(Cache.USER_ID));

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.profile_fragment, container, false);

        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (person != null) {
            binding.setVariable(BR.person, person);
            binding.executePendingBindings();
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
//        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        if (isCurrentUser && isEditMode) {
            menu.findItem(R.id.action_signout).setVisible(false);
            menu.findItem(R.id.action_save).setVisible(true);
        } else if (isCurrentUser && !isEditMode) {
            menu.findItem(R.id.action_signout).setVisible(true);
            menu.findItem(R.id.action_save).setVisible(false);
        } else {
            menu.findItem(R.id.action_signout).setVisible(false);
            menu.findItem(R.id.action_save).setVisible(false);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_signout:                // logs out
                // remove user cache, remove this fragment
                cache.save(Cache.USER_ID, "");
                cache.save(Cache.USER_TOKEN, "");

                ((MainActivityRedesign) getActivity()).removeCurrentFragment();
                LoginManager.getInstance().logOut();

                return true;
            case R.id.action_save:
                binding.personDesc.setVisibility(View.VISIBLE);
                binding.personName.setVisibility(View.VISIBLE);
                binding.personDescEdit.setVisibility(View.INVISIBLE);
                binding.personNameEdit.setVisibility(View.INVISIBLE);
                isEditMode = false;
                binding.personName.setText(binding.personNameEdit.getText().toString());
                binding.personDesc.setText(binding.personDescEdit.getText().toString());
                PersonManager.updatePerson(binding.personNameEdit.getText().toString(), binding.personDescEdit.getText().toString());
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe
    public void reveicePerson(PersonReceivedEvent personReceivedEvent) {
        Log.d("Optonaut", "Registered person " + personReceivedEvent.getPerson());
        person = personReceivedEvent.getPerson();
        binding.setVariable(BR.person, person);
        binding.executePendingBindings();
        initializeProfileFeed();
    }

    private void initializeProfileFeed() {
        if(person.getId().equals(cache.getString(Cache.USER_ID))) {
            isCurrentUser = true;
            binding.personIsFollowed.setText(getActivity().getResources().getString(R.string.profile_edit));
        }

        getActivity().invalidateOptionsMenu();

        binding.personIsFollowed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // edit profile
                if(isCurrentUser) {
                    binding.personDesc.setVisibility(View.INVISIBLE);
                    binding.personName.setVisibility(View.INVISIBLE);
                    binding.personDescEdit.setVisibility(View.VISIBLE);
                    binding.personNameEdit.setVisibility(View.VISIBLE);
                    isEditMode = true;
                    getActivity().invalidateOptionsMenu();
                } else if(person.is_followed()) {
                    apiConsumer.unfollow(cache.getString(Cache.USER_ID), new Callback<LogInReturn.EmptyResponse>() {
                        @Override
                        public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                            Timber.d("Response : " + response);
                            binding.personIsFollowed.setText(getActivity().getResources().getString(R.string.profile_follow));
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Timber.e("Error on unfollowing.");
                        }
                    });
                } else if(!person.is_followed()) {
                    apiConsumer.follow(cache.getString(Cache.USER_ID), new Callback<LogInReturn.EmptyResponse>() {
                        @Override
                        public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                            Timber.d("Response : " + response);
                            binding.personIsFollowed.setText(getActivity().getResources().getString(R.string.profile_following));
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Timber.e("Error on following.");
                        }
                    });
                }

            }
        });

        ProfileGridFragment profileFeedFragment = ProfileGridFragment.newInstance(person);

        getChildFragmentManager().beginTransaction().
                replace(R.id.feed_placeholder, profileFeedFragment).addToBackStack(null).commit();
    }

    public static ProfileFragment newInstance(Person person) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable("person", person);
        profileFragment.setArguments(args);
        return profileFragment;
    }



}
