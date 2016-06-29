package com.iam360.iam360.views.profile;

import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
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
import com.iam360.iam360.BR;
import com.iam360.iam360.ProfileExerciseBinding;
import com.iam360.iam360.R;
import com.iam360.iam360.bus.BusProvider;
import com.iam360.iam360.bus.PersonReceivedEvent;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.network.PersonManager;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.DBHelper;
import com.iam360.iam360.viewmodels.InfiniteScrollListener;
import com.iam360.iam360.viewmodels.LocalOptographManager;
import com.iam360.iam360.views.dialogs.NetworkProblemDialog;
import com.iam360.iam360.views.new_design.MainActivity;
import com.iam360.iam360.views.new_design.ProfileActivity;
import com.squareup.otto.Subscribe;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Mariel on 6/24/2016.
 */
public class ProfileFragmentExercise extends Fragment implements View.OnClickListener {
    public static final String TAG = ProfileFragmentExercise.class.getSimpleName();
    private Person person;
    private ProfileExerciseBinding binding;
    private Cache cache;

    private Button button;
    private boolean isCurrentUser = false;
    private boolean isEditMode = false;
    private ApiConsumer apiConsumer;
    private DBHelper mydb;

    private int PICK_IMAGE_REQUEST = 1;

    private OptographLocalGridAdapter optographLocalGridAdapter;
    private NetworkProblemDialog networkProblemDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        cache = Cache.open();
        String token = cache.getString(cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("")?null:token);
        mydb = new DBHelper(getContext());

        optographLocalGridAdapter = new OptographLocalGridAdapter(getActivity(),OptographLocalGridAdapter.ON_IMAGE);
        networkProblemDialog = new NetworkProblemDialog();

        setPerson();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.profile_fragment_exercise,container, false);

        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(" ");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(getActivity() instanceof ProfileActivity ? R.drawable.back_arrow : R.drawable.logo_small_dark);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Log.d("myTag", TAG + " person null? " + (person == null));
        if (person != null) {
//            binding.setVariable(BR.person, person);
            binding.executePendingBindings();
            initializeProfileFeed();
        }

        binding.homeBtn.setOnClickListener(this);
        binding.signOut.setOnClickListener(this);

        binding.optographFeed.setAdapter(optographLocalGridAdapter);
        GridLayoutManager manager = new GridLayoutManager(getContext(),4);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
//                Log.d("myTag",TAG+" position: "+position+" getSpanSize: "+optographLocalGridAdapter.getItemViewType(position));
                int pos = optographLocalGridAdapter.getItemViewType(position);
                if (pos==OptographLocalGridAdapter.VIEW_HEADER || pos == OptographLocalGridAdapter.SECOND_HEADER
                        || pos == OptographLocalGridAdapter.VIEW_LOCAL || pos == OptographLocalGridAdapter.VIEW_FOLLOWER) return 4;
                return 1;
            }
        });
        manager.setOrientation(GridLayoutManager.VERTICAL);
        binding.optographFeed.setLayoutManager(manager);
        binding.optographFeed.setItemViewCacheSize(10);

        binding.optographFeed.addOnScrollListener(new InfiniteScrollListener(manager) {
            int yPos = 0;

            @Override
            public void onLoadMore() {
                Log.d("myTag", TAG + " onLoadMore");
                loadMore();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                yPos += dy;
                Log.d("myTag", TAG + " onScrolled yPos: " + yPos + " dy: " + dy + " toolHeight: " + binding.toolbarTitle.getHeight());

                /*if (yPos > binding.toolbarTitle.getHeight()) {
                    binding.toolbar.setVisibility(View.GONE);
                    binding.toolbarReplace.setVisibility(View.VISIBLE);
                } else {
                    binding.toolbar.setVisibility(View.VISIBLE);
                    binding.toolbarReplace.setVisibility(View.GONE);
                }

                View view = binding.optographFeed.getChildAt(1);
                View view1 = binding.optographFeed.getChildAt(0);
                int grr = view.getTop()+dy;
                Log.d("myTag",TAG+" view top: "+view.getTop()+" height: "+view1.getMeasuredHeight()+" getTop+=dy= "+
                        grr+" yPos: "+yPos);
                if (binding.optographFeed.getChildAdapterPosition(view)==1 && binding.optographFeed.getChildAdapterPosition(view1)==0 &&
                        yPos>view1.getMeasuredHeight()) {
                    binding.toolbar.setVisibility(View.GONE);
                    binding.toolbarReplace.setVisibility(View.VISIBLE);
                } else {
                    binding.toolbar.setVisibility(View.VISIBLE);
                    binding.toolbarReplace.setVisibility(View.GONE);
                }*/
            }
        });

        return binding.getRoot();
    }

    private void initializeProfileFeed() {

        if (person.getId().equals(cache.getString(Cache.USER_ID))) {
            isCurrentUser = true;
            binding.toolbarTitle.setText(getResources().getString(R.string.profile_my_profile));
            cache.save(Cache.USER_NAME,person.getDisplay_name());
        } else {
            binding.toolbarTitle.setText(getResources().getString(R.string.profile_text));
        }

        getActivity().invalidateOptionsMenu();

        initializeFeed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_btn:
                if (getActivity() instanceof MainActivity) {
                    getActivity().onBackPressed();
                } else {
                    getActivity().finish();
                }
                break;
            case R.id.sign_out:
                cache.save(Cache.USER_ID,"");
                cache.save(Cache.USER_TOKEN,"");

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

    public static ProfileFragmentExercise newInstance(Person person) {

        ProfileFragmentExercise profileFragmentExercise = new ProfileFragmentExercise();
        Bundle args = new Bundle();
        args.putParcelable("person", person);
        profileFragmentExercise.setArguments(args);
        return profileFragmentExercise;
    }

    public static ProfileFragmentExercise newInstance(String id) {

        ProfileFragmentExercise profileFragmentExercise = new ProfileFragmentExercise();
        Bundle args = new Bundle();
        args.putString("id", id);
        profileFragmentExercise.setArguments(args);
        return profileFragmentExercise;
    }

    @Subscribe
    public void receivePerson(PersonReceivedEvent personReceivedEvent) {
        person = personReceivedEvent.getPerson();

        if (person != null) {
            binding.executePendingBindings();
            initializeProfileFeed();
        }
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

    public void updateHomeButton() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(optographLocalGridAdapter.isOnEditMode() ? R.drawable.cancel : R.drawable.logo_small_dark);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        if (isCurrentUser && optographLocalGridAdapter.isOnEditMode()) {
            menu.findItem(R.id.action_signout).setVisible(false);
            menu.findItem(R.id.action_save).setVisible(true);
//            menu.findItem(R.id.cancel_edit).setVisible(true);
        } else if (isCurrentUser && !optographLocalGridAdapter.isOnEditMode() && ((getActivity() instanceof MainActivity &&
                ((MainActivity)getActivity()).getCurrentPage()!=MainActivity.SHARING_MODE) ||
                getActivity() instanceof ProfileActivity)) {
            menu.findItem(R.id.action_signout).setVisible(true);
            menu.findItem(R.id.action_save).setVisible(false);
            menu.findItem(R.id.cancel_edit).setVisible(false);
        } else {
            menu.findItem(R.id.action_signout).setVisible(false);
            menu.findItem(R.id.action_save).setVisible(false);
            menu.findItem(R.id.cancel_edit).setVisible(false);
        }
        updateHomeButton();
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
                isEditMode = false;
                optographLocalGridAdapter.saveUpdate();
                getActivity().invalidateOptionsMenu();
                return true;
            case R.id.cancel_edit:
                isEditMode = false;
                optographLocalGridAdapter.setEditMode(isEditMode);
                getActivity().invalidateOptionsMenu();
                return true;
            case android.R.id.home:
                if (optographLocalGridAdapter.isOnEditMode() && isCurrentUser) {
                    isEditMode = false;
                    optographLocalGridAdapter.setEditMode(isEditMode);
                    getActivity().invalidateOptionsMenu();
                } else if (getActivity() instanceof ProfileActivity) {
                    Log.d("myTag", "ProfileActivity? " + (getActivity() instanceof ProfileActivity));
                    ((ProfileActivity)getActivity()).onBackPressed();
                } else if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).onBackPressed();
                else getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void initializeFeed() {
        //try to add filter for deleted optographs
        optographLocalGridAdapter.setPerson(person);
        Log.d("myTag",TAG+" inside initializeFeed equal? "+(person.getId().equals(cache.getString(Cache.USER_ID))));
        apiConsumer.getOptographsFromPerson(person.getId(), ApiConsumer.PROFILE_GRID_LIMIT)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
                    networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                    return null;
                })
                .subscribe(optographLocalGridAdapter::addItem);

        if(person.getId().equals(cache.getString(Cache.USER_ID))) {
            LocalOptographManager.getOptographs()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(optographLocalGridAdapter::addItem);
        }

    }

    protected void loadMore() {
        apiConsumer.getOptographsFromPerson(person.getId(), optographLocalGridAdapter.getOldest().getCreated_at())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
                    networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                    return null;
                })
                .subscribe(optographLocalGridAdapter::addItem);
    }

    protected void refresh() {

        apiConsumer.getOptographsFromPerson(person.getId(), 10)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
                    networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                    return null;
                })
                .subscribe(optographLocalGridAdapter::addItem);
    }
}
