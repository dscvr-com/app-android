package com.iam360.iam360.views.new_design;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.iam360.iam360.R;
import com.iam360.iam360.gcm.GCMRegistrationIntentService;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.sensors.CoreMotionListener;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.Constants;
import com.iam360.iam360.views.GestureDetectors;
import com.iam360.iam360.views.SettingsActivity;
import com.iam360.iam360.views.profile.OptographLocalGridAdapter;
import com.iam360.iam360.views.profile.SigninFBFragment;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final int SHARING_MODE = 0;
    public static final int FEED_MODE = 1;
    public static final int PROFILE_MODE = 2;
    private MyPagerAdapter adapterViewPager;
    private ViewPager viewPager;

    private Cache cache;

    private int currentMode = 0;
    private int savePosition = 0;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!Cache.cacheInitialized){
            cache = Cache.getInstance(this);
        }
        cache = Cache.open();
        initializeComponents();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //Initializing our broadcast receiver
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            //When the broadcast received
            //We are sending the broadcast from GCMRegistrationIntentService
            @Override
            public void onReceive(Context context, Intent intent) {
                //If the broadcast has received with success
                //that means device is registered successfully
                if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_SUCCESS)) {
                    //Getting the registration token from the intent
                    String token = intent.getStringExtra("token");
                    //Displaying the token as toast
//                    Toast.makeText(getApplicationContext(), "Registration token:" + token, Toast.LENGTH_LONG).show();
                    Log.d("MARK", "Registration token:" + token);
                    //if the intent is not with success then displaying error messages
                } else if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR)) {
//                    Toast.makeText(getApplicationContext(), "GCM registration error!", Toast.LENGTH_LONG).show();
                    Log.d("MARK", "GCM registration error!");
                } else {
//                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_LONG).show();
                    Log.d("MARK", "GCM registration Error occurred!");
                }
            }
        };

        viewPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);
        viewPager.setCurrentItem(FEED_MODE, false);

//        Log.d("myTag", " upload: model: " + android.os.Build.MODEL + " make: " + Build.MANUFACTURER
//                + " ver: " + Build.VERSION.RELEASE);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                switch (position) {
                    case FEED_MODE:
                        adapterViewPager.mainFeedFragment.disableDrag();
                        break;
                    case PROFILE_MODE:
                        break;
                    case SHARING_MODE:
                        break;
                }
                currentMode = position;
            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case FEED_MODE:
                        adapterViewPager.mainFeedFragment.refresh();
                        break;
                    case PROFILE_MODE:
                        break;
                    case SHARING_MODE:
                        adapterViewPager.sharingFragment.updateOptograph();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (savePosition > state) {
//                    Timber.d("SWIPE RIGHT");
                } else {
//                    Timber.d("SWIPE LEFT");
                }
                savePosition = state;
            }
        });

//        new GeneralUtils().setStatusBarTranslucent(this, true);
//        setStatusBarTranslucent(true);
//        int statusBarHeight = new GeneralUtils().getStatusBarHeight(this);

    }

    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public void setPage(int page) {
        currentMode = page;
        viewPager.setCurrentItem(page, true);
    }

    public int getCurrentPage() {
        return currentMode;
    }

    public void dragSharePage() {
        viewPager.setCurrentItem(SHARING_MODE, true);

    }

    public void startSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void startImagePreview(UUID id, String imagePath) {
        Intent intent = new Intent(this, OptoImagePreviewActivity.class);
        intent.putExtra("id", id.toString());
        if (imagePath != null) intent.putExtra("path", imagePath);
        startActivity(intent);
    }

    public void setOptograph(Optograph optograph) {
        adapterViewPager.sharingFragment.setOptograph(optograph);
    }

    private void initializeComponents() {
//        cache = Cache.getInstance(this);
//        cache = Cache.open();

        Constants.initializeConstants(this);
        GestureDetectors.initialize(this);
        CoreMotionListener.initialize(this);

        // FB Track App Installs and App Opens
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
//        Toro.attach(this);

        Intent intent;
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getParcelable("person")!=null) {
            Person person = getIntent().getExtras().getParcelable("person");
            intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("person", person);
            startActivity(intent);
        }else if(getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getParcelable("opto")!=null){
            Optograph optograph = getIntent().getExtras().getParcelable("opto");
            intent = new Intent(this, OptographDetailsActivity.class);
            intent.putExtra("opto", optograph);
            startActivity(intent);
        }
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {
        private int NUM_ITEMS = 3;
        private SharingFragment sharingFragment;
        private MainFeedFragment mainFeedFragment;
        private ProfileRootFragment profileRootFragment;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            profileRootFragment = new ProfileRootFragment();
            mainFeedFragment = new MainFeedFragment();
            sharingFragment = new SharingFragment();
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return sharingFragment;
                case 1:
                    return mainFeedFragment;
                case 2:
                    return profileRootFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            if (object instanceof SharingFragment) {
            } else if (object instanceof SigninFBFragment) {
                return POSITION_NONE;
            }
            return POSITION_UNCHANGED;
//            return super.getItemPosition(object);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() != FEED_MODE) {
            setPage(FEED_MODE);
        } else super.onBackPressed();
    }

    public void onBack() {
        // TODO: how to update the page of profile after login
        onBackPressed();
    }

    @Override
    protected void onDestroy() {
//        Toro.detach(this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OptographLocalGridAdapter.PICK_IMAGE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            for (Fragment frag : adapterViewPager.profileRootFragment.getFragmentManager().getFragments()) {
                if (frag instanceof ProfileFragmentExercise) {
                    Uri uri = data.getData();

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        ((ProfileFragmentExercise) frag).setAvatar(bitmap);
                    } catch (IOException e) {
                        Log.d("myTag", "Error getAvatar message: " + e.getMessage());
                    }
                }
            }
        } else if (requestCode == OptographLocalGridAdapter.DELETE_IMAGE && resultCode == RESULT_OK &&
                data != null) {
            for (Fragment frag : adapterViewPager.profileRootFragment.getFragmentManager().getFragments()) {
                if (frag instanceof ProfileFragmentExercise) {
                    String id = data.getStringExtra("id");
                    boolean local = data.getBooleanExtra("local",false);
                    ((ProfileFragmentExercise) frag).refreshAfterDelete(id,local);
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.w("MainActivity", "onResume");
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));
    }


    //Unregistering receiver on activity paused
    @Override
    protected void onPause() {
        super.onPause();
        Log.w("MainActivity", "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    public void refresh() {
        adapterViewPager.profileRootFragment.refresh();
    }
}
