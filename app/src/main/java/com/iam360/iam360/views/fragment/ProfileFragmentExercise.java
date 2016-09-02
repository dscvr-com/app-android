package com.iam360.iam360.views.fragment;

import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
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
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.facebook.login.LoginManager;
import com.google.gson.Gson;
import com.iam360.iam360.ProfileExerciseBinding;
import com.iam360.iam360.R;
import com.iam360.iam360.bus.BusProvider;
import com.iam360.iam360.bus.PersonReceivedEvent;
import com.iam360.iam360.gcm.Optographs;
import com.iam360.iam360.model.Location;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.network.PersonManager;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.DBHelper;
import com.iam360.iam360.viewmodels.InfiniteScrollListener;
import com.iam360.iam360.viewmodels.LocalOptographManager;
import com.iam360.iam360.viewmodels.OptographLocalGridAdapter;
import com.iam360.iam360.views.activity.MainActivity;
import com.iam360.iam360.views.activity.ProfileActivity;
import com.iam360.iam360.views.dialogs.NetworkProblemDialog;
import com.squareup.otto.Subscribe;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

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

        binding.homeBtn.setImageResource(getActivity() instanceof ProfileActivity? R.drawable.back_arrow : R.drawable.logo_mini_icn);

        if (person != null) {
//            binding.setVariable(BR.person, person);
            binding.executePendingBindings();
            initializeProfileFeed();
        }

        binding.homeBtn.setOnClickListener(this);
        binding.saveBtn.setOnClickListener(this);
        binding.cancelBtn.setOnClickListener(this);
        binding.overflowBtn.setOnClickListener(this);

//        setAdapter();

        return binding.getRoot();
    }

    private void setAdapter() {

        binding.optographFeed.setAdapter(optographLocalGridAdapter);
        GridLayoutManager manager = new GridLayoutManager(getContext(),OptographLocalGridAdapter.COLUMNS);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int pos = optographLocalGridAdapter.getItemViewType(position);
                if (pos == OptographLocalGridAdapter.VIEW_SERVER)
                    return 1;
                return OptographLocalGridAdapter.COLUMNS;
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
                if (optographLocalGridAdapter.isOnEditMode()) enableScroll(false);
                View view = binding.optographFeed.getChildAt(1);
                if (view==null)return;
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
                    setTab();
                } else {
                    binding.toolbarLayout.setVisibility(View.VISIBLE);
//                    binding.toolbar.setVisibility(View.VISIBLE);
                    binding.toolbarReplace.setVisibility(View.GONE);
                }
            }
        });

    }

    private void setTab() {
        if (optographLocalGridAdapter.isTab(OptographLocalGridAdapter.ON_IMAGE)) {
            binding.imageText.setTextColor(Color.parseColor("#ffbc00"));
            binding.imageSelector.setVisibility(View.VISIBLE);
            binding.followerText.setTextColor(Color.parseColor("#ffffff"));
            binding.followerSelector.setVisibility(View.INVISIBLE);
            binding.notificationText.setTextColor(Color.parseColor("#ffffff"));
            binding.notificationSelector.setVisibility(View.INVISIBLE);
        } else if (optographLocalGridAdapter.isTab(OptographLocalGridAdapter.ON_FOLLOWER)) {
            binding.imageText.setTextColor(Color.parseColor("#ffffff"));
            binding.imageSelector.setVisibility(View.INVISIBLE);
            binding.followerText.setTextColor(Color.parseColor("#ffbc00"));
            binding.followerSelector.setVisibility(View.VISIBLE);
            binding.notificationText.setTextColor(Color.parseColor("#ffffff"));
            binding.notificationSelector.setVisibility(View.INVISIBLE);
        } else {
            binding.imageText.setTextColor(Color.parseColor("#ffffff"));
            binding.imageSelector.setVisibility(View.INVISIBLE);
            binding.followerText.setTextColor(Color.parseColor("#ffffff"));
            binding.followerSelector.setVisibility(View.INVISIBLE);
            binding.notificationText.setTextColor(Color.parseColor("#ffbc00"));
            binding.notificationSelector.setVisibility(View.VISIBLE);
        }

        if (!isCurrentUser && optographLocalGridAdapter.isTab(OptographLocalGridAdapter.ON_IMAGE)) {
            binding.followerTab.setVisibility(View.GONE);
            binding.notificationTab.setVisibility(View.GONE);
            binding.imageText.setTextColor(Color.parseColor("#ffffff"));
            binding.imageSelector.setVisibility(View.INVISIBLE);
        } else if (optographLocalGridAdapter.isTab(OptographLocalGridAdapter.ON_IMAGE)){
            binding.followerTab.setVisibility(View.VISIBLE);
            binding.notificationTab.setVisibility(View.VISIBLE);
            binding.imageText.setTextColor(Color.parseColor("#ffbc00"));
            binding.imageSelector.setVisibility(View.VISIBLE);
        }
    }

    public void setAvatar(Bitmap bitmap) {
        optographLocalGridAdapter.avatarUpload(bitmap);
    }

    public void refreshAfterDelete(String id,boolean isLocal) {
        optographLocalGridAdapter.refreshAfterDelete(id, isLocal);
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
        if (isCurrentUser) {
            optographLocalGridAdapter = new OptographLocalGridAdapter(getActivity(), OptographLocalGridAdapter.ON_NOTIFICATION);
        } else optographLocalGridAdapter = new OptographLocalGridAdapter(getActivity(), OptographLocalGridAdapter.ON_IMAGE);
        setAdapter();
        updateHomeButton();

        initializeFeed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_btn:
                if (getActivity() instanceof ProfileActivity) {
                    Log.d("MARK", "ProfileActivity? " + (getActivity() instanceof ProfileActivity));
                    ((ProfileActivity)getActivity()).onBackPressed();
                } else if (getActivity() instanceof MainActivity) {
                    Log.d("MARK", "MainActivity? " + (getActivity() instanceof ProfileActivity));
                    ((MainActivity) getActivity()).onBackPressed();
                }else {
                    Log.d("MARK", "else Acti? " + (getActivity() instanceof ProfileActivity));
                    getActivity().finish();
                }
                break;
            case R.id.save_btn:
                isEditMode = false;

                Rect r = new Rect();
                binding.getRoot().getWindowVisibleDisplayFrame(r);
                int screenHeight = binding.getRoot().getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
                }
                optographLocalGridAdapter.saveUpdate();
                updateHomeButton();
                break;
            case R.id.cancel_btn:
                isEditMode = false;

                Rect r1 = new Rect();
                binding.getRoot().getWindowVisibleDisplayFrame(r1);
                int screenHeight1 = binding.getRoot().getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight1 = screenHeight1 - r1.bottom;

                if (keypadHeight1 > screenHeight1 * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    InputMethodManager imm1 = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm1.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
                }
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

                        // delete all database content
                        mydb.deleteAllTable();

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
            if (!myGetData(args.getString("id"))) {
                PersonManager.loadPerson(args.getString("id"));
            }
        } else {
            throw new RuntimeException();
        }
    }

    private boolean myGetData(String id) {
        Person person1 = new Person();
        Cursor res = mydb.getData(id,DBHelper.PERSON_TABLE_NAME,"id");

        if (res==null || res.getCount()==0) return false;
        res.moveToFirst();
        person1.setId(res.getString(res.getColumnIndex("id")));
        person1.setCreated_at(res.getString(res.getColumnIndex("created_at")));
        person1.setDeleted_at(res.getString(res.getColumnIndex("deleted_at")));
        person1.setDisplay_name(res.getString(res.getColumnIndex("display_name")));
        person1.setUser_name(res.getString(res.getColumnIndex("user_name")));
        person1.setEmail(res.getString(res.getColumnIndex("email")));
        person1.setText(res.getString(res.getColumnIndex("text")));
        person1.setElite_status(res.getString(res.getColumnIndex("elite_status")).equalsIgnoreCase("true"));
        person1.setAvatar_asset_id(res.getString(res.getColumnIndex("avatar_asset_id")));
        person1.setOptographs_count(res.getInt(res.getColumnIndex("optographs_count")));
        person1.setFollowers_count(res.getInt(res.getColumnIndex("followers_count")));
        person1.setFollowed_count(res.getInt(res.getColumnIndex("followed_count")));
        person1.setIs_followed(res.getString(res.getColumnIndex("is_followed")).equalsIgnoreCase("true"));
        person1.setFacebook_user_id(res.getString(res.getColumnIndex("facebook_user_id")));
        person1.setFacebook_token(res.getString(res.getColumnIndex("facebook_token")));
        person1.setTwitter_token(res.getString(res.getColumnIndex("twitter_token")));
        person1.setTwitter_secret(res.getString(res.getColumnIndex("twitter_secret")));

        person = person1;
        return true;
    }

    private void insertPerson(Person person) {
        Cursor res = mydb.getData(person.getId(),DBHelper.PERSON_TABLE_NAME,"id");

        if (res==null || res.getCount()==0) {
            mydb.insertPerson(person.getId(), person.getCreated_at(), person.getEmail(), person.getDeleted_at(), person.isElite_status(),
                    person.getDisplay_name(), person.getUser_name(), person.getText(), person.getAvatar_asset_id(), person.getFacebook_user_id(), person.getOptographs_count(),
                    person.getFollowers_count(), person.getFollowed_count(), person.is_followed(), person.getFacebook_token(), person.getTwitter_token(), person.getTwitter_secret());
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
            insertPerson(person);
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

        if (optographLocalGridAdapter.isTab(OptographLocalGridAdapter.ON_NOTIFICATION)) {
            optographLocalGridAdapter.notifyItemRangeChanged(2, optographLocalGridAdapter.getItemCount() - 2);
        }
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
        enableScroll(!optographLocalGridAdapter.isOnEditMode());
        setMessage(optographLocalGridAdapter.getMessage() == null ? "" : optographLocalGridAdapter.getMessage());
    }

    private void enableScroll(boolean enabled) {
        binding.overlayEdit.setVisibility(enabled ? View.GONE : View.VISIBLE);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.overlayEdit.getLayoutParams();
//        params.width = 200; params.leftMargin = 100;
        View view = binding.optographFeed.getChildAt(1);
        if (view==null) return;
        params.topMargin = view.getTop()+binding.toolbarLayout.getHeight();
        binding.overlayEdit.requestLayout();
    }

    public void setMessage(String message) {
        binding.tabMessage.setVisibility((message.isEmpty() || optographLocalGridAdapter.getItemCount()-2>0)?View.GONE:View.VISIBLE);
        binding.tabMessage.setText(message);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.tabMessage.getLayoutParams();
        View view = binding.optographFeed.getChildAt(1);
        if (view==null) return;
        params.topMargin = view.getTop()+binding.toolbarLayout.getHeight()+view.getHeight();
        binding.tabMessage.requestLayout();
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
                    ((ProfileActivity)getActivity()).onBackPressed();
                } else if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).onBackPressed();
                else getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void initializeFeed() {
        Log.d("Caching", "initializeFeed");
        optographLocalGridAdapter.setPerson(person);
        Cursor cursor = mydb.getUserOptographs(person.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, ApiConsumer.PROFILE_GRID_LIMIT);

        if (cursor != null) {
            Log.d("Caching", "initializeFeed cursor not null");
            cursor.moveToFirst();
            if (cursor.getCount() != 0) {
                cur2Json(cursor)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnCompleted(() -> {
                            apiConsumer.getOptographsFromPerson(person.getId(), ApiConsumer.PROFILE_GRID_LIMIT)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnCompleted(() -> updateMessage(null))
                                    .onErrorReturn(throwable -> {
                                        updateMessage(getResources().getString(R.string.profile_net_prob));
                                        return null;
                                    })
                                    .subscribe(optographLocalGridAdapter::addItem);
                        })
                        .onErrorReturn(throwable -> {
                            Log.d("myTag", " Error: message: " + throwable.getMessage());
                            if (!networkProblemDialog.isAdded())
                                networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                            return null;
                        })
                        .subscribe(optographLocalGridAdapter::addItem);
                return;
            }
        }
//        else {
            Log.d("Caching", "initializeFeed cursor null");
            apiConsumer.getOptographsFromPerson(person.getId(), ApiConsumer.PROFILE_GRID_LIMIT)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() -> updateMessage(null))
                    .onErrorReturn(throwable -> {
                        updateMessage(getResources().getString(R.string.profile_net_prob));
                        return null;
                    })
                    .subscribe(optographLocalGridAdapter::addItem);
//        }

            //try to add filter for deleted optographs
//            if(person.getId().equals(cache.getString(Cache.USER_ID))) {
//                LocalOptographManager.getOptographs()
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .doOnCompleted(() -> updateMessage(null))
//                        .subscribe(optographLocalGridAdapter::addItem);
//            }

    }

    public void loadMore() {

        Cursor cursor = mydb.getUserOptographs(person.getId() , DBHelper.OPTO_TABLE_NAME_FEEDS, ApiConsumer.PROFILE_GRID_LIMIT, optographLocalGridAdapter.getOldest().getCreated_at());

        if(cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() != 0) {
                cur2Json(cursor)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnCompleted(() -> {
                            apiConsumer.getOptographsFromPerson(person.getId(), optographLocalGridAdapter.getOldest().getCreated_at())
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .onErrorReturn(throwable -> {
                                        return null;
                                    })
                                    .subscribe(optographLocalGridAdapter::addItem);
                        })
                        .onErrorReturn(throwable -> {
                            Log.d("myTag", " Error: message: " + throwable.getMessage());
                            if (!networkProblemDialog.isAdded())
                                networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                            return null;
                        })
                        .subscribe(optographLocalGridAdapter::addItem);
                return;
            }
        }
//        else {
            apiConsumer.getOptographsFromPerson(person.getId(), optographLocalGridAdapter.getOldest().getCreated_at())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorReturn(throwable -> {
                        return null;
                    })
                    .subscribe(optographLocalGridAdapter::addItem);
//        }
    }

    public void refresh() {
        Log.d("Caching", "refresh");

        Cursor cursor = null;
        if (!isCurrentUser) cursor = mydb.getUserOptographs(person.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, ApiConsumer.PROFILE_GRID_LIMIT);
        else cursor = mydb.getUserOptographs(person.getId() , DBHelper.OPTO_TABLE_NAME_FEEDS, ApiConsumer.PROFILE_GRID_LIMIT);

        if(cursor != null) {
            cursor.moveToFirst();
            Log.d("Caching", "cursor not null count: "+cursor.getCount());
            if (cursor.getCount() != 0) {
                cur2Json(cursor)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnCompleted(() -> {
                            apiConsumer.getOptographsFromPerson(person.getId(), 10)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnCompleted(() -> updateMessage(null))
                                    .onErrorReturn(throwable -> {
//                    getFragmentManager().executePendingTransactions();
//                    if(!networkProblemDialog.isAdded())networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                                        updateMessage(getResources().getString(R.string.profile_net_prob));
                                        return null;
                                    })
                                    .subscribe(optographLocalGridAdapter::addItem);
                        })
                        .onErrorReturn(throwable -> {
                            Log.d("myTag", " Error: message: " + throwable.getMessage());
                            if (!networkProblemDialog.isAdded())
                                networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                            return null;
                        })
                        .subscribe(optographLocalGridAdapter::addItem);
                return;
            }
        }
//        else {
            Log.d("Caching", "cursor null or cursor item is zero");
            apiConsumer.getOptographsFromPerson(person.getId(), 10)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() -> updateMessage(null))
                    .onErrorReturn(throwable -> {
//                    getFragmentManager().executePendingTransactions();
//                    if(!networkProblemDialog.isAdded())networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                        updateMessage(getResources().getString(R.string.profile_net_prob));
                        return null;
                    })
                    .subscribe(optographLocalGridAdapter::addItem);
//        }

//        if(person.getId().equals(cache.getString(Cache.USER_ID))) {
//            LocalOptographManager.getOptographs()
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .doOnCompleted(() -> updateMessage(null))
//                    .subscribe(optographLocalGridAdapter::addItem);
//        }
    }

    private void updateMessage(String message) {
        Log.d("myTag"," setMessage: onImage? "+optographLocalGridAdapter.isTab(OptographLocalGridAdapter.ON_IMAGE)+
                " item==0? "+(optographLocalGridAdapter.getItemCount()-2==0));
        Log.d("myTag"," setMessage: count: "+optographLocalGridAdapter.getItemCount());
        if (optographLocalGridAdapter.getItemCount()-2==0  && optographLocalGridAdapter.isTab(OptographLocalGridAdapter.ON_IMAGE)) {
            optographLocalGridAdapter.setMessage((message==null)?getResources().getString(R.string.profile_no_image):message);
            updateHomeButton();
        } else if (optographLocalGridAdapter.isTab(OptographLocalGridAdapter.ON_IMAGE)) {
            optographLocalGridAdapter.setMessage("");
            updateHomeButton();
        }
    }

    /**
    public Observable<Optograph> cur2Json(Cursor cursor) {
        List<Optograph> optographs = new LinkedList<>();
        cursor.moveToFirst();
        for(int a=0; a < cursor.getCount(); a++){
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        String columnName = cursor.getColumnName(i);
                        rowObject.put(columnName,
                                cursor.getString(i));
                        Timber.d("CURSOR : " + columnName + " " + cursor.getString(i));
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            }

            String json = rowObject.toString();
            Log.d("MARK","List<Optograph> opto = "+json);
            Gson gson = new Gson();
            Optographs data = gson.fromJson(json, Optographs.class);

            Optograph opto = new Optograph(data.optograph_id);

            Person person = new Person();
            if(data.optograph_person_id !=null && !data.optograph_person_id.equals("")){
                Cursor res = mydb.getData(data.optograph_person_id, DBHelper.PERSON_TABLE_NAME,"id");
                res.moveToFirst();
                if (res.getCount()!= 0) {
                    person.setId(res.getString(res.getColumnIndex("id")));
                    person.setCreated_at(res.getString(res.getColumnIndex("created_at")));
                    person.setDisplay_name(res.getString(res.getColumnIndex("display_name")));
//                        Log.d("MARK","cur2Json user_name = "+res.getString(res.getColumnIndex("user_name")));
                    person.setUser_name(res.getString(res.getColumnIndex("user_name")));
                    person.setText(res.getString(res.getColumnIndex("text")));
                    person.setAvatar_asset_id(res.getString(res.getColumnIndex("avatar_asset_id")));
                }
            }

            opto.setPerson(person);
            opto.setCreated_at(data.optograph_created_at);
            opto.setIs_starred(data.optograph_is_starred);
            opto.setDeleted_at(data.optograph_deleted_at);
            opto.setStitcher_version(data.optograph_stitcher_version);
            opto.setText(data.optograph_text);
            opto.setViews_count(data.optograph_views_count);
            opto.setIs_staff_picked(data.optograph_is_staff_pick);
            opto.setShare_alias(data.optograph_share_alias);
            opto.setIs_private(data.optograph_is_private);
            opto.setIs_published(data.optograph_is_published);
            opto.setLeft_texture_asset_id(data.optograph_left_texture_asset_id);
            opto.setRight_texture_asset_id(data.optograph_right_texture_asset_id);
            opto.setIs_local(false);

            Location location = new Location();
            if(data.optograph_location_id !=null && !data.optograph_location_id.equals("")){
                Cursor res = mydb.getData(data.optograph_location_id, DBHelper.LOCATION_TABLE_NAME,"id");
                res.moveToFirst();
                if (res.getCount()!= 0) {
                    location.setId(res.getString(res.getColumnIndex("id")));
                    location.setCreated_at(res.getString(res.getColumnIndex("created_at")));
                    location.setText(res.getString(res.getColumnIndex("text")));
                    location.setCountry(res.getString(res.getColumnIndex("id")));
                    location.setCountry_short(res.getString(res.getColumnIndex("country")));
                    location.setPlace(res.getString(res.getColumnIndex("place")));
                    location.setRegion(res.getString(res.getColumnIndex("region")));
                    location.setPoi(Boolean.parseBoolean(res.getString(res.getColumnIndex("poi"))));
                    location.setLatitude(res.getString(res.getColumnIndex("latitude")));
                    location.setLongitude(res.getString(res.getColumnIndex("longitude")));
                }
            }
            opto.setLocation(location);

            opto.setOptograph_type(data.optograph_type);
            opto.setStars_count(data.optograph_stars_count);
            opto.setComments_count(data.optograph_comments_count);
            opto.setHashtag_string(data.optograph_hashtag_string);

            optographs.add(opto);

            cursor.moveToNext();
        }

        cursor.close();

        return Observable.from(optographs);
    }
     **/

    public Observable<Optograph> cur2Json(Cursor cursor) {

        Log.d("Caching", "cur2Json");
//        JSONArray resultSet = new JSONArray();
        List<Optograph> optographs = new LinkedList<>();
        cursor.moveToFirst();

        for(int a=0; a < cursor.getCount(); a++){
            Optograph opto = null;
            String personId = null;
            try {
                opto = new Optograph(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_ID)));
                opto.setCreated_at(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_CREATED_AT)));
                opto.setIs_starred(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_STARRED)) == 1 ? true : false);
                opto.setDeleted_at(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_DELETED_AT)));
                opto.setStitcher_version(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_STITCHER_VERSION)));
                opto.setText(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_TEXT)));
                opto.setViews_count(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_STARS_COUNT)));
                opto.setIs_staff_picked(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_STAFF_PICK)) == 1 ? true : false);
                opto.setShare_alias(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_SHARE_ALIAS)));
                opto.setIs_private(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_PRIVATE)) == 1 ? true : false);
                opto.setIs_published(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_PUBLISHED)) == 1 ? true : false);
                opto.setOptograph_type(cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_TYPE)));
                opto.setStars_count(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_STARS_COUNT)));
                opto.setShould_be_published(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED)) == 1 ? true : false);
                opto.setIs_local(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_LOCAL)) == 1 ? true : false);
                opto.setIs_data_uploaded(cursor.getInt(cursor.getColumnIndex(DBHelper.OPTOGRAPH_IS_DATA_UPLOADED)) == 1 ? true : false);
                personId = cursor.getString(cursor.getColumnIndex(DBHelper.OPTOGRAPH_PERSON_ID));

            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }

            Person person = new Person();
            if(personId !=null && !personId.equals("")){
                Cursor res = mydb.getData(personId, DBHelper.PERSON_TABLE_NAME,"id");
                res.moveToFirst();
                if (res.getCount()!= 0) {
                    person.setId(res.getString(res.getColumnIndex("id")));
                    person.setCreated_at(res.getString(res.getColumnIndex("created_at")));
                    person.setDeleted_at(res.getString(res.getColumnIndex("deleted_at")));
                    person.setDisplay_name(res.getString(res.getColumnIndex("display_name")));
                    person.setUser_name(res.getString(res.getColumnIndex("user_name")));
                    person.setText(res.getString(res.getColumnIndex("email")));
                    person.setEmail(res.getString(res.getColumnIndex("text")));
                    person.setElite_status(res.getInt(res.getColumnIndex("elite_status")) == 1 ? true : false);
                    person.setAvatar_asset_id(res.getString(res.getColumnIndex("avatar_asset_id")));
                    person.setOptographs_count(res.getInt(res.getColumnIndex("optographs_count")));
                    person.setFollowers_count(res.getInt(res.getColumnIndex("followers_count")));
                    person.setFollowed_count(res.getInt(res.getColumnIndex("followed_count")));
                    person.setIs_followed(res.getInt(res.getColumnIndex("is_followed")) == 1 ? true : false);
                    person.setFacebook_user_id(res.getString(res.getColumnIndex("facebook_user_id")));
                    person.setFacebook_token(res.getString(res.getColumnIndex("facebook_token")));
                    person.setTwitter_token(res.getString(res.getColumnIndex("twitter_token")));
                    person.setTwitter_secret(res.getString(res.getColumnIndex("twitter_secret")));
                }
            }
//                if(!person.is_followed()){
//                    continue;
//                }
            opto.setPerson(person);

            Location location = new Location();
            if(opto != null && opto.getLocation().getId() !=null && !opto.getLocation().getId().equals("")){
                Cursor res = mydb.getData(opto.getLocation().getId(), DBHelper.LOCATION_TABLE_NAME,"id");
                res.moveToFirst();
                if (res.getCount()!= 0) {
                    location.setId(res.getString(res.getColumnIndex("id")));
                    location.setCreated_at(res.getString(res.getColumnIndex("created_at")));
                    location.setText(res.getString(res.getColumnIndex("text")));
                    location.setCountry(res.getString(res.getColumnIndex("id")));
                    location.setCountry_short(res.getString(res.getColumnIndex("country")));
                    location.setPlace(res.getString(res.getColumnIndex("place")));
                    location.setRegion(res.getString(res.getColumnIndex("region")));
                    location.setPoi(Boolean.parseBoolean(res.getString(res.getColumnIndex("poi"))));
                    location.setLatitude(res.getString(res.getColumnIndex("latitude")));
                    location.setLongitude(res.getString(res.getColumnIndex("longitude")));
                }
            }
            opto.setLocation(location);

            Log.d("Caching", "Opto " + opto.is_local() + " " + opto.isShould_be_published());
            optographs.add(opto);

            cursor.moveToNext();
        }

        cursor.close();

        return Observable.from(optographs);
    }

}
