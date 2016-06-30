package com.iam360.iam360.views.new_design;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.io.IOException;
import java.util.UUID;

import com.iam360.iam360.R;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.sensors.CoreMotionListener;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.Constants;
import com.iam360.iam360.views.GestureDetectors;
import com.iam360.iam360.views.SettingsActivity;
import com.iam360.iam360.views.profile.OptographLocalGridAdapter;
import com.iam360.iam360.views.profile.ProfileFragmentExercise;
import com.iam360.iam360.views.profile.SigninFBFragment;

import im.ene.lab.toro.Toro;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    public static final int SHARING_MODE = 0;
    public static final int FEED_MODE = 1;
    public static final int PROFILE_MODE = 2;
    private MyPagerAdapter adapterViewPager;
    private ViewPager viewPager;
    private Cache cache;
    private int currentMode = 0;
    private int savePosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        initializeComponents();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        viewPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);
        viewPager.setCurrentItem(FEED_MODE, false);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
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
                if(savePosition>state)
                {
                    Timber.d("SWIPE RIGHT");
                }else
                {
                    Timber.d("SWIPE LEFT");
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

    public void dragSharePage () {
        viewPager.setCurrentItem(SHARING_MODE,true);

    }

    public void startSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void startImagePreview(UUID id, String imagePath) {
        Intent intent = new Intent(this, OptoImagePreviewActivity.class);
        intent.putExtra("id", id.toString());
        if(imagePath != null) intent.putExtra("path", imagePath);
        startActivity(intent);
    }

    public void setOptograph(Optograph optograph) {
        adapterViewPager.sharingFragment.setOptograph(optograph);
    }

    private void initializeComponents() {

        // instatiate cache on start of application
        cache = Cache.getInstance(this);

        Constants.initializeConstants(this);
        GestureDetectors.initialize(this);
        CoreMotionListener.initialize(this);

        // FB Track App Installs and App Opens
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
//        Toro.attach(this);

    }

    public class MyPagerAdapter extends FragmentPagerAdapter {
        private int NUM_ITEMS = 3;
        private Cache cache;
        private SharingFragment sharingFragment;
        private MainFeedFragment mainFeedFragment;
        private ProfileRootFragment profileRootFragment;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            profileRootFragment = new ProfileRootFragment();
            mainFeedFragment = new MainFeedFragment();
            sharingFragment = new SharingFragment();
            cache = Cache.open();
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
            if(object instanceof  SharingFragment) {
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
        Log.d("myTag","MainActivity onBackPressed. Profile?"+(viewPager.getCurrentItem()==PROFILE_MODE));
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
            for (Fragment frag :adapterViewPager.profileRootFragment.getFragmentManager().getFragments()) {
                if (frag instanceof ProfileFragmentExercise) {
            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                ((ProfileFragmentExercise)frag).setAvatar(bitmap);
            } catch (IOException e) {
                Log.d("myTag","Error getAvatar message: "+e.getMessage());
            }
                }
            }
        }
    }
}
