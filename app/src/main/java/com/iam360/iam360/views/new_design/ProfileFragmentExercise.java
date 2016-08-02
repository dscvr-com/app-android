package com.iam360.iam360.views.new_design;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
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
                if (pos==OptographLocalGridAdapter.VIEW_SERVER)
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
            optographLocalGridAdapter.notifyItemRangeChanged(2,optographLocalGridAdapter.getItemCount()-2);
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
        setMessage(optographLocalGridAdapter.getMessage()==null?"":optographLocalGridAdapter.getMessage());
    }

    private void enableScroll(boolean enabled) {
        binding.overlayEdit.setVisibility(enabled?View.GONE:View.VISIBLE);
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

    protected void initializeFeed() {

        //try to add filter for deleted optographs
        optographLocalGridAdapter.setPerson(person);
        apiConsumer.getOptographsFromPerson(person.getId(), ApiConsumer.PROFILE_GRID_LIMIT)
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

        if(person.getId().equals(cache.getString(Cache.USER_ID))) {
            LocalOptographManager.getOptographs()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() -> updateMessage(null))
                    .subscribe(optographLocalGridAdapter::addItem);
        }
    }

    protected void loadMore() {
        apiConsumer.getOptographsFromPerson(person.getId(), optographLocalGridAdapter.getOldest().getCreated_at())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
//                    getFragmentManager().executePendingTransactions();
//                    if(!networkProblemDialog.isAdded())networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                    return null;
                })
                .subscribe(optographLocalGridAdapter::addItem);
    }

    protected void refresh() {

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

        if(person.getId().equals(cache.getString(Cache.USER_ID))) {
            LocalOptographManager.getOptographs()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() -> updateMessage(null))
                    .subscribe(optographLocalGridAdapter::addItem);
        }
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
}
