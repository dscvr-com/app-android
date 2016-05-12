package co.optonaut.optonaut.views.profile;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.ImageView;

import com.facebook.login.LoginManager;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import co.optonaut.optonaut.BR;
import co.optonaut.optonaut.ProfileBinding;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.bus.BusProvider;
import co.optonaut.optonaut.bus.PersonReceivedEvent;
import co.optonaut.optonaut.model.LogInReturn;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.model.Person;
import co.optonaut.optonaut.network.ApiConsumer;
import co.optonaut.optonaut.network.PersonManager;
import co.optonaut.optonaut.util.Cache;
import co.optonaut.optonaut.views.MainActivityRedesign;
import co.optonaut.optonaut.views.new_design.MainActivity;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-12-04
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = ProfileFragment.class.getSimpleName();
    private Person person;
    private ProfileBinding binding;
    private Cache cache;

    private Button button;
    private boolean isCurrentUser = false;
    private boolean isEditMode = false;
    private ApiConsumer apiConsumer;

    private String follow, following;
    private int PICK_IMAGE_REQUEST = 1;

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
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (person != null) {
            binding.setVariable(BR.person, person);
            binding.executePendingBindings();
            initializeProfileFeed();
        }

        binding.homeBtn.setOnClickListener(this);
        binding.signOut.setOnClickListener(this);

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
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(person.getDisplay_name());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(" ");

        if(person.getId().equals(cache.getString(Cache.USER_ID))) {
            isCurrentUser = true;
            binding.personIsFollowed.setText(getActivity().getResources().getString(R.string.profile_edit));
//            binding.personIsFollowed.setBackgroundResource(R.drawable.messenger_share_btn);
        }

        getActivity().invalidateOptionsMenu();

        binding.personAvatarAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEditMode) {
                    Intent intent = new Intent();
                    // Show only images, no videos or anything else
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    // Always show the chooser (if there are multiple options available)
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                }
            }
        });

        binding.personIsFollowed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // edit profile
                if (isCurrentUser) {
                    binding.personDesc.setVisibility(View.INVISIBLE);
                    binding.personName.setVisibility(View.INVISIBLE);
                    binding.personDescEdit.setVisibility(View.VISIBLE);
                    binding.personNameEdit.setVisibility(View.VISIBLE);
                    isEditMode = true;
                    getActivity().invalidateOptionsMenu();
                } else if (binding.personIsFollowed.getText().equals(following)) {
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
                } else if (binding.personIsFollowed.getText().equals(follow)) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                binding.personAvatarAsset.setImageBitmap(bitmap);
                uploadAvatar(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadAvatar(Bitmap bitmap) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bos);
        byte[] data = bos.toByteArray();

        String avatar = UUID.randomUUID().toString();
        RequestBody fbody = RequestBody.create(MediaType.parse("image/jpeg"), data);
        RequestBody fbodyMain = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("avatar_asset", "avatar.jpg", fbody)
                .addFormDataPart("avatar_asset_id",  avatar)
                .build();

        Timber.d("Avatar " + avatar);
        apiConsumer.uploadAvatar(fbodyMain, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                Timber.d("Response : " + response.message());
                binding.getPerson().setAvatar_asset_id(avatar);
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("OnFailure: " + t.getMessage());
            }
        });

    }

    public static ProfileFragment newInstance(Person person) {

        ProfileFragment profileFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable("person", person);
        profileFragment.setArguments(args);
        return profileFragment;
    }

    public static ProfileFragment newInstance(String id) {

        ProfileFragment profileFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        profileFragment.setArguments(args);
        return profileFragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_btn:
                ((MainActivity)getActivity()).onBackPressed();
                break;
            case R.id.sign_out:
                cache.save(Cache.USER_ID, "");
                cache.save(Cache.USER_TOKEN, "");

                ((MainActivityRedesign) getActivity()).removeCurrentFragment();
                LoginManager.getInstance().logOut();
                break;
        }
    }
}
