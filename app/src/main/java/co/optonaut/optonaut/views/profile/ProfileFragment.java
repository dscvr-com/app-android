package co.optonaut.optonaut.views.profile;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.login.LoginManager;
import com.squareup.otto.Subscribe;

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
import co.optonaut.optonaut.views.MainActivityRedesign;
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
    private Person person;
    private ProfileBinding binding;
    private Cache cache;

    private Button button;
    private boolean isCurrentUser = false;
    private boolean isEditMode = false;
    private ApiConsumer apiConsumer;

    private String follow, following;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);

        follow = getActivity().getResources().getString(R.string.profile_follow);
        following = getActivity().getResources().getString(R.string.profile_following);

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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
            case R.id.action_signout:
                // logs out
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
            case android.R.id.home:
                ((MainActivityRedesign)getActivity()).removeCurrentFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe
    public void receivePerson(PersonReceivedEvent personReceivedEvent) {
        Log.d("Optonaut", "Registered person " + personReceivedEvent.getPerson());
        person = personReceivedEvent.getPerson();

        if(person != null) {
            binding.setVariable(BR.person, person);
            binding.executePendingBindings();
            initializeProfileFeed();
        }
    }

    private void initializeProfileFeed() {

        Timber.d("USERID : %s", person.getId());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(person.getDisplay_name());

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
                } else if(binding.personIsFollowed.getText().equals(following)) {
                    apiConsumer.unfollow(person.getId(), new Callback<LogInReturn.EmptyResponse>() {
                        @Override
                        public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                            Timber.d("Unfollow : " + response);
                            binding.getPerson().setIs_followed(false);
                            binding.getPerson().setFollowers_count(binding.getPerson().getFollowers_count() - 1);
                            binding.invalidateAll();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Timber.e("Error on unfollowing.");
                        }
                    });
                } else if(binding.personIsFollowed.getText().equals(follow)) {
                    apiConsumer.follow(person.getId(), new Callback<LogInReturn.EmptyResponse>() {
                        @Override
                        public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                            Timber.d("Follow : " + response.message() + " " + retrofit.baseUrl() + " ");
                            binding.getPerson().setIs_followed(true);
                            binding.getPerson().setFollowers_count(binding.getPerson().getFollowers_count() + 1);
                            binding.invalidateAll();
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
