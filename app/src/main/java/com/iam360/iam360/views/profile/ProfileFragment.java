package com.iam360.iam360.views.profile;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.login.LoginManager;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.util.DBHelper;
import com.iam360.iam360.viewmodels.LocalOptographManager;
import com.iam360.iam360.views.new_design.ProfileActivity;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import com.iam360.iam360.BR;
import com.iam360.iam360.ProfileBinding;
import com.iam360.iam360.R;
import com.iam360.iam360.bus.BusProvider;
import com.iam360.iam360.bus.PersonReceivedEvent;
import com.iam360.iam360.model.LogInReturn;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.network.PersonManager;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.viewmodels.InfiniteScrollListener;
import com.iam360.iam360.viewmodels.OptographGridAdapter;
import com.iam360.iam360.views.dialogs.NetworkProblemDialog;
import com.iam360.iam360.views.new_design.MainActivity;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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
    private DBHelper mydb;

    private String follow, following;
    private int PICK_IMAGE_REQUEST = 1;

    private OptographGridAdapter optographFeedAdapter;
    private OptographLocalGridAdapter optographLocalGridAdapter;
    private NetworkProblemDialog networkProblemDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        mydb = new DBHelper(getContext());

        follow = getActivity().getResources().getString(R.string.profile_follow);
        following = getActivity().getResources().getString(R.string.profile_following);

        optographFeedAdapter = new OptographGridAdapter(getActivity());
        optographLocalGridAdapter = new OptographLocalGridAdapter(getActivity());
        networkProblemDialog = new NetworkProblemDialog();

        setPerson();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.profile_fragment, container, false);

        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(" ");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.logo_small_dark);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (person != null) {
            binding.setVariable(BR.person, person);
            binding.executePendingBindings();
            initializeProfileFeed();
        }

        binding.homeBtn.setOnClickListener(this);
        binding.signOut.setOnClickListener(this);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        binding.optographFeed.setLayoutManager(llm);
        binding.optographFeed.setAdapter(optographFeedAdapter);
        binding.optographFeed.setItemViewCacheSize(10);

        binding.optographFeed.addOnScrollListener(new InfiniteScrollListener(llm) {
            int yPos = 0;

            @Override
            public void onLoadMore() {
                loadMore();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                yPos += dy;

                if (yPos > binding.coordLayout.getHeight()) {
                    binding.toolbar.setVisibility(View.GONE);
                    binding.toolbarReplace.setVisibility(View.VISIBLE);
                } else {
                    binding.toolbar.setVisibility(View.VISIBLE);
                    binding.toolbarReplace.setVisibility(View.GONE);
                }
            }
        });

//        GridLayoutManager glm = new GridLayoutManager(getContext(),4);
//        glm.setOrientation(GridLayoutManager.VERTICAL);
//        binding.optographFeed.setLayoutManager(glm);
//        binding.optographLocal.setLayoutManager(llm);
//        binding.optographLocal.setAdapter(optographLocalGridAdapter);
//        binding.optographFeed.setItemViewCacheSize(10);
//
//        binding.optographLocal.addOnScrollListener(new InfiniteScrollListener(llm) {
//            int yPos = 0;
//
//            @Override
//            public void onLoadMore() {
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                yPos += dy;
//
//                if (yPos > binding.coordLayout.getHeight()) {
//                    binding.toolbar.setVisibility(View.GONE);
//                    binding.toolbarReplace.setVisibility(View.VISIBLE);
//                } else {
//                    binding.toolbar.setVisibility(View.VISIBLE);
//                    binding.toolbarReplace.setVisibility(View.GONE);
//                }
//            }
//        });

        binding.header.attachTo(binding.optographFeed);
//        binding.header.attachTo(binding.optographLocal);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);

        if(person == null) setPerson();
        else refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
    }

    private void updateHomeButton() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(isEditMode ? R.drawable.cancel : R.drawable.logo_small_dark);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        if (isCurrentUser && isEditMode) {
            menu.findItem(R.id.action_signout).setVisible(false);
            menu.findItem(R.id.action_save).setVisible(true);
//            menu.findItem(R.id.cancel_edit).setVisible(true);
        } else if (isCurrentUser && !isEditMode && ((getActivity() instanceof MainActivity &&
                ((MainActivity)getActivity()).getCurrentPage()==MainActivity.PROFILE_MODE) ||
                getActivity() instanceof ProfileActivity)) {
            menu.findItem(R.id.action_signout).setVisible(true);
            menu.findItem(R.id.action_save).setVisible(false);
            menu.findItem(R.id.cancel_edit).setVisible(false);
        } else {
            menu.findItem(R.id.action_signout).setVisible(false);
            menu.findItem(R.id.action_save).setVisible(false);
            menu.findItem(R.id.cancel_edit).setVisible(false);
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

//                ((MainActivityRedesign) getActivity()).removeCurrentFragment();
                backToSignInPage();
                LoginManager.getInstance().logOut();

                return true;
            case R.id.action_save:
                binding.personDesc.setVisibility(View.VISIBLE);
                binding.personName.setVisibility(View.VISIBLE);
                binding.personDescEdit.setVisibility(View.INVISIBLE);
                binding.personNameEdit.setVisibility(View.INVISIBLE);
                isEditMode = false;
                binding.getPerson().setText(binding.personDescEdit.getText().toString());
                binding.getPerson().setDisplay_name(binding.personNameEdit.getText().toString());
                binding.personName.setText(binding.personNameEdit.getText().toString());
                binding.personDesc.setText(binding.personDescEdit.getText().toString());
                PersonManager.updatePerson(binding.personNameEdit.getText().toString(), binding.personDescEdit.getText().toString());
                binding.editBtn.setVisibility(View.VISIBLE);
                getActivity().invalidateOptionsMenu();
                updateHomeButton();
                return true;
            case R.id.cancel_edit:
                binding.personDesc.setVisibility(View.VISIBLE);
                binding.personName.setVisibility(View.VISIBLE);
                binding.personDescEdit.setVisibility(View.INVISIBLE);
                binding.personNameEdit.setVisibility(View.INVISIBLE);
                isEditMode = false;
                binding.editBtn.setVisibility(View.VISIBLE);
                getActivity().invalidateOptionsMenu();
                updateHomeButton();
                return true;
            case android.R.id.home:
//                ((MainActivityRedesign)getActivity()).removeCurrentFragment();
                if (isEditMode && isCurrentUser) {
                    binding.personDesc.setVisibility(View.VISIBLE);
                    binding.personName.setVisibility(View.VISIBLE);
                    binding.personDescEdit.setVisibility(View.INVISIBLE);
                    binding.personNameEdit.setVisibility(View.INVISIBLE);
                    isEditMode = false;
                    binding.editBtn.setVisibility(View.VISIBLE);
                    getActivity().invalidateOptionsMenu();
                    updateHomeButton();
                } else if (getActivity() instanceof ProfileActivity) {
                    Log.d("myTag","ProfileActivity? "+(getActivity() instanceof ProfileActivity));
                    ((ProfileActivity)getActivity()).onBackPressed();
                } else if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).onBackPressed();
                else getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe
    public void receivePerson(PersonReceivedEvent personReceivedEvent) {
        Log.d("Optonaut", "Registered person " + personReceivedEvent.getPerson());
        person = personReceivedEvent.getPerson();

        if (person != null) {
            binding.setVariable(BR.person, person);
            binding.executePendingBindings();
            initializeProfileFeed();
        }
    }

    private void setPerson() {
        Bundle args = getArguments();
        if (args.containsKey("person")) {
            person = args.getParcelable("person");
        } else if (args.containsKey("id")) {
            PersonManager.loadPerson(args.getString("id"));
        } else {
            throw new RuntimeException();
        }
    }

    private void initializeProfileFeed() {

        Timber.d("USERID : %s", person.getId());
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(person.getDisplay_name());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(" ");

        if (person.getId().equals(cache.getString(Cache.USER_ID))) {
            isCurrentUser = true;
//            binding.personIsFollowed.setText(getActivity().getResources().getString(R.string.profile_edit));
            binding.personIsFollowed.setVisibility(View.GONE);
            binding.toolbarTitle.setText(getResources().getString(R.string.profile_my_profile));
//            binding.personIsFollowed.setBackgroundResource(R.drawable.messenger_share_btn);
            cache.save(Cache.USER_NAME,person.getDisplay_name());
        } else {
            binding.toolbarTitle.setText(getResources().getString(R.string.profile_text));
            binding.editBtn.setVisibility(View.GONE);
            binding.personIsFollowed.setVisibility(View.VISIBLE);
        }

        getActivity().invalidateOptionsMenu();

        binding.personAvatarAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    Intent intent = new Intent();
                    // Show only images, no videos or anything else
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    // Always show the chooser (if there are multiple options available)
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                }
            }
        });

        binding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCurrentUser) {
                    binding.personDesc.setVisibility(View.INVISIBLE);
                    binding.personName.setVisibility(View.INVISIBLE);
                    binding.personDescEdit.setVisibility(View.VISIBLE);
                    binding.personNameEdit.setVisibility(View.VISIBLE);
                    isEditMode = true;
                    getActivity().invalidateOptionsMenu();
                    updateHomeButton();
                    binding.editBtn.setVisibility(View.GONE);
                }
            }
        });

        binding.personIsFollowed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // edit profile
                if (isCurrentUser && isEditMode) {
                    binding.personDesc.setVisibility(View.VISIBLE);
                    binding.personName.setVisibility(View.VISIBLE);
                    binding.personDescEdit.setVisibility(View.INVISIBLE);
                    binding.personNameEdit.setVisibility(View.INVISIBLE);
                    isEditMode = false;
                    binding.personName.setText(binding.personNameEdit.getText().toString());
                    binding.personDesc.setText(binding.personDescEdit.getText().toString());
                    PersonManager.updatePerson(binding.personNameEdit.getText().toString(), binding.personDescEdit.getText().toString());
                    getActivity().invalidateOptionsMenu();
                } else if (isCurrentUser) {
                    binding.personDesc.setVisibility(View.INVISIBLE);
                    binding.personName.setVisibility(View.INVISIBLE);
                    binding.personDescEdit.setVisibility(View.VISIBLE);
                    binding.personNameEdit.setVisibility(View.VISIBLE);
                    isEditMode = true;
                    getActivity().invalidateOptionsMenu();
                } else if (binding.personIsFollowed.getText().equals(following) || binding.getPerson().is_followed()) {
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
                } else if (binding.personIsFollowed.getText().equals(follow) || !binding.getPerson().is_followed()) {
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

        initializeFeed();
//        ProfileGridFragment profileFeedFragment = ProfileGridFragment.newInstance(person);
//
//        getChildFragmentManager().beginTransaction().
//                replace(R.id.feed_placeholder, profileFeedFragment).addToBackStack(null).commit();
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
                .addFormDataPart("avatar_asset_id", avatar)
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
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).onBackPressed();
                else getActivity().finish();
                break;
            case R.id.sign_out:
                cache.save(Cache.USER_ID, "");
                cache.save(Cache.USER_TOKEN, "");

//                ((MainActivityRedesign) getActivity()).removeCurrentFragment();
                LoginManager.getInstance().logOut();

                backToSignInPage();

                break;
        }
    }

    /**
     * Restart activity to refresh user values
     */
    private void backToSignInPage() {
        getActivity().finish();
        startActivity(getActivity().getIntent());

//        if (getActivity() instanceof MainActivity) {
//
//            FragmentTransaction trans = getFragmentManager().beginTransaction();
//                /*
//                 * IMPORTANT: We use the "root frame" defined in
//				 * "root_fragment.xml" as the reference to replace fragment
//				 */
//            trans.replace(R.id.root_frame, SigninFBFragment.newInstance("", ""));
//
//				/*
//				 * IMPORTANT: The following lines allow us to add the fragment
//				 * to the stack and return to it later, by pressing back
//				 */
//            trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            trans.addToBackStack(null);
//
//            trans.commit();
//        }
    }

    private boolean isDeleted(Optograph optograph) {
        return optograph.getDeleted_at().isEmpty();
    }

    protected void initializeFeed() {
        //try to add filter for deleted optographs
        apiConsumer.getOptographsFromPerson(person.getId(), ApiConsumer.PROFILE_GRID_LIMIT)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
                    networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                    return null;
                })
                .subscribe(optographFeedAdapter::addItem);

        if(person.getId().equals(cache.getString(Cache.USER_ID))) {
            LocalOptographManager.getOptographs()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(optographFeedAdapter::addItem);
        }

    }

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

    protected void refresh() {

        apiConsumer.getOptographsFromPerson(person.getId(), 10)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
                    networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                    return null;
                })
                .subscribe(optographFeedAdapter::addItem);
    }
}
