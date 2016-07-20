package com.iam360.iam360.views.new_design;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
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
import com.iam360.iam360.views.profile.OptographLocalGridAdapter;
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
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

//        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(" ");
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(getActivity() instanceof ProfileActivity ? R.drawable.back_arrow : R.drawable.logo_small_dark);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.homeBtn.setImageResource(getActivity() instanceof ProfileActivity? R.drawable.back_arrow : R.drawable.logo_small_dark);

        if (person != null) {
//            binding.setVariable(BR.person, person);
            binding.executePendingBindings();
            initializeProfileFeed();
        }

        binding.homeBtn.setOnClickListener(this);
        binding.saveBtn.setOnClickListener(this);
        binding.cancelBtn.setOnClickListener(this);
        binding.overflowBtn.setOnClickListener(this);

        binding.optographFeed.setAdapter(optographLocalGridAdapter);
        GridLayoutManager manager = new GridLayoutManager(getContext(),OptographLocalGridAdapter.COLUMNS);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
//                Log.d("myTag",TAG+" position: "+position+" getSpanSize: "+optographLocalGridAdapter.getItemViewType(position));
                int pos = optographLocalGridAdapter.getItemViewType(position);
                if (pos==OptographLocalGridAdapter.VIEW_HEADER || pos == OptographLocalGridAdapter.SECOND_HEADER
                        || pos == OptographLocalGridAdapter.VIEW_LOCAL || pos == OptographLocalGridAdapter.VIEW_FOLLOWER)
                    return OptographLocalGridAdapter.COLUMNS;
                return 1;
            }
        });
        manager.setOrientation(GridLayoutManager.VERTICAL);
        binding.optographFeed.setLayoutManager(manager);
        binding.optographFeed.setItemViewCacheSize(10);

        binding.optographFeed.addOnScrollListener(new InfiniteScrollListener(manager) {
            int yPos = 0;
            float height01=0;

            @Override
            public void onLoadMore() {
                Log.d("myTag", TAG + " onLoadMore");
                if(optographLocalGridAdapter.isTab(OptographLocalGridAdapter.ON_IMAGE))loadMore();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                View view = binding.optographFeed.getChildAt(1);
                yPos += dy;
                float top = view.getY();
//                Log.d("myTag"," header: height01: "+height01+" top: "+top+" top+height="+(top+view.getHeight()));
                if((top + view.getHeight())>height01) {
                    height01 = top + view.getHeight();
                }
//                    Log.d("myTag", " header: onScrolled 0Height: " +binding.optographFeed.getChildAt(1).getHeight()+
//                            " 1Height: "+ view.getHeight() +" dy: "+dy+" yPos: "+yPos+" 01: "+height01);
                if (height01 <= yPos) {
                    binding.toolbar.setVisibility(View.GONE);
                    binding.toolbarLayout.setVisibility(View.GONE);
                    binding.toolbarReplace.setVisibility(View.VISIBLE);
                    String tab;
                    if (optographLocalGridAdapter.isTab(OptographLocalGridAdapter.ON_FOLLOWER)) tab = getResources().getString(R.string.profile_header_follower);
                    else if (optographLocalGridAdapter.isTab(OptographLocalGridAdapter.ON_NOTIFICATION)) tab = getResources().getString(R.string.profile_header_notif);
                    else tab = getResources().getString(R.string.profile_header_image);
                    binding.tabTitle.setText(tab);
                } else {
                    binding.toolbarLayout.setVisibility(View.VISIBLE);
//                    binding.toolbar.setVisibility(View.VISIBLE);
                    binding.toolbarReplace.setVisibility(View.GONE);
                }
            }
        });

        return binding.getRoot();
    }

    public void setAvatar(Bitmap bitmap) {
        optographLocalGridAdapter.avatarUpload(bitmap);
    }

    public void refreshAfterDelete(String id,boolean isLocal) {
        optographLocalGridAdapter.refreshAfterDelete(id,isLocal);
    }

    private void initializeProfileFeed() {

        if (person.getId().equals(cache.getString(Cache.USER_ID))) {
            isCurrentUser = true;
//            binding.toolbarTitle.setText(getResources().getString(R.string.profile_my_profile));
            binding.pageTitle.setText(getResources().getString(R.string.profile_my_profile));
            cache.save(Cache.USER_DISPLAY_NAME, person.getDisplay_name());
        } else {
            isCurrentUser = false;
//            binding.toolbarTitle.setText(getResources().getString(R.string.profile_text));
            binding.pageTitle.setText(getResources().getString(R.string.profile_text));
        }

//        getActivity().invalidateOptionsMenu();
        updateHomeButton();

        initializeFeed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_btn:
                if (getActivity() instanceof ProfileActivity) {
                    Log.d("myTag", "ProfileActivity? " + (getActivity() instanceof ProfileActivity));
                    ((ProfileActivity)getActivity()).onBackPressed();
                } else if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).onBackPressed();
                else getActivity().finish();
                break;
            case R.id.save_btn:
                isEditMode = false;
                optographLocalGridAdapter.saveUpdate();
                updateHomeButton();
                break;
            case R.id.cancel_btn:
                isEditMode = false;
                optographLocalGridAdapter.setEditMode(isEditMode);
                binding.cancelBtn.setVisibility(View.VISIBLE);
                binding.homeBtn.setVisibility(View.GONE);
                updateHomeButton();
                break;
            case R.id.overflow_btn:
                PopupMenu popupMenu = new PopupMenu(getActivity(), binding.overflowBtn);
                popupMenu.getMenuInflater()
                        .inflate(R.menu.profile_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // logs out

                        PersonManager.logoutPerson();
                        LoginManager.getInstance().logOut();
                        backToSignInPage();

                        // remove user cache, remove this fragment
                        cache.save(Cache.USER_ID, "");
                        cache.save(Cache.USER_TOKEN, "");
                        cache.save(Cache.GATE_CODE, "");

                        return true;
                    }
                });
                popupMenu.show();
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

       Log.d("myTag"," delete: PFE onResume person null? "+(person==null));
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
//        if (getActivity() instanceof MainActivity)
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(optographLocalGridAdapter.isOnEditMode() ? R.drawable.cancel : R.drawable.logo_small_dark);
        if (isCurrentUser && optographLocalGridAdapter.isOnEditMode()) {
            binding.homeBtn.setVisibility(View.GONE);
            binding.overflowBtn.setVisibility(View.GONE);
            binding.cancelBtn.setVisibility(View.VISIBLE);
            binding.saveBtn.setVisibility(View.VISIBLE);
        } else if (isCurrentUser && !optographLocalGridAdapter.isOnEditMode() && ((getActivity() instanceof MainActivity &&
                ((MainActivity)getActivity()).getCurrentPage()!=MainActivity.SHARING_MODE) ||
                getActivity() instanceof ProfileActivity)) {
            binding.homeBtn.setVisibility(View.VISIBLE);
            binding.overflowBtn.setVisibility(View.VISIBLE);
            binding.cancelBtn.setVisibility(View.GONE);
            binding.saveBtn.setVisibility(View.GONE);
        } else {
            binding.homeBtn.setVisibility(View.VISIBLE);
            binding.overflowBtn.setVisibility(View.GONE);
            binding.cancelBtn.setVisibility(View.GONE);
            binding.saveBtn.setVisibility(View.GONE);
        }
        if (optographLocalGridAdapter.isOnEditMode()) {
            binding.cancelBtn.setVisibility(View.VISIBLE);
            binding.homeBtn.setVisibility(View.GONE);
        } else {
            binding.cancelBtn.setVisibility(View.GONE);
            binding.homeBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

//        if (isCurrentUser && optographLocalGridAdapter.isOnEditMode()) {
//            menu.findItem(R.id.action_signout).setVisible(false);
//            menu.findItem(R.id.action_save).setVisible(true);
////            menu.findItem(R.id.cancel_edit).setVisible(true);
//        } else if (isCurrentUser && !optographLocalGridAdapter.isOnEditMode() && ((getActivity() instanceof MainActivity &&
//                ((MainActivity)getActivity()).getCurrentPage()!=MainActivity.SHARING_MODE) ||
//                getActivity() instanceof ProfileActivity)) {
//            menu.findItem(R.id.action_signout).setVisible(true);
//            menu.findItem(R.id.action_save).setVisible(false);
//            menu.findItem(R.id.cancel_edit).setVisible(false);
//        } else {
//            menu.findItem(R.id.action_signout).setVisible(false);
//            menu.findItem(R.id.action_save).setVisible(false);
//            menu.findItem(R.id.cancel_edit).setVisible(false);
//        }
        menu.findItem(R.id.action_signout).setVisible(false);
        updateHomeButton();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_signout:
                // logs out
                // remove user cache, remove this fragment
//                cache.save(Cache.USER_ID, "");
//                cache.save(Cache.USER_TOKEN, "");
//                cache.save(Cache.GATE_CODE, "");
//
////                ((MainActivityRedesign) getActivity()).removeCurrentFragment();
//                PersonManager.logoutPerson();
//                LoginManager.getInstance().logOut();
//                backToSignInPage();

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
                    if(!networkProblemDialog.isAdded())networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
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
                    if(!networkProblemDialog.isAdded())networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                    return null;
                })
                .subscribe(optographLocalGridAdapter::addItem);
    }

    protected void refresh() {

        apiConsumer.getOptographsFromPerson(person.getId(), 10)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
                    if(!networkProblemDialog.isAdded())networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                    return null;
                })
                .subscribe(optographLocalGridAdapter::addItem);

        if(person.getId().equals(cache.getString(Cache.USER_ID))) {
            LocalOptographManager.getOptographs()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(optographLocalGridAdapter::addItem);
        }

    }
}
