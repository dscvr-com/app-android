package com.iam360.dscvr.removed_social.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.stetho.Stetho;
import com.iam360.dscvr.R;
import com.iam360.dscvr.sensors.DefaultListeners;
import com.iam360.dscvr.sensors.GestureDetectors;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.MixpanelHelper;
import com.iam360.dscvr.viewmodels.OptographLocalGridAdapter;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    private Cache cache;
    private MainFeedFragment mainFeedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Stetho.initializeWithDefaults(this);
        initializeComponents();

        setContentView(R.layout.a_activity_main);
        getSupportFragmentManager().beginTransaction().add(R.id.container, mainFeedFragment).commit();

        MixpanelHelper.trackAppLaunch(this);
    }

    private void initializeComponents() {

        if(!Cache.cacheInitialized){
            cache = Cache.getInstance(this);
        }
        cache = Cache.open();

        Constants.initializeConstants(this);
        GestureDetectors.initialize(this);
        DefaultListeners.initialize(this);

        mainFeedFragment = new MainFeedFragment();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OptographLocalGridAdapter.DELETE_IMAGE && resultCode == RESULT_OK && data != null) {
            //TODO for delete
//        for (Fragment frag : adapterViewPager.profileRootFragment.getFragmentManager().getFragments()) {
//            if (frag instanceof ProfileFragmentExercise) {
//                String id = data.getStringExtra("id");
//                boolean local = data.getBooleanExtra("local",false);
//                ((ProfileFragmentExercise) frag).refreshAfterDelete(id,local);
//            } else if (frag instanceof MainFeedFragment) {
//                String id = data.getStringExtra("id");
//                boolean local = data.getBooleanExtra("local",false);
//                ((MainFeedFragment) frag).refreshAfterDelete(id,local);
//            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    //Unregistering receiver on activity paused
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        Timber.d("Workaround for bug 19917 value");
        // https://code.google.com/p/android/issues/detail?id=19917
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    public void toggleFeedFullScreen() {
        mainFeedFragment.toggleFullScreen();
    }

}