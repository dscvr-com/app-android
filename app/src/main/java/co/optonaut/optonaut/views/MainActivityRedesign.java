package co.optonaut.optonaut.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.util.DBHelper;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.model.Person;
import co.optonaut.optonaut.record.RecordFragment;
import co.optonaut.optonaut.sensors.CoreMotionListener;
import co.optonaut.optonaut.util.Cache;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.util.ImageUrlBuilder;
import co.optonaut.optonaut.util.MixpanelHelper;
import co.optonaut.optonaut.views.feed.MainFeedFragment;
import co.optonaut.optonaut.views.record.OptoImagePreviewFragment;
import co.optonaut.optonaut.views.profile.SigninFBFragment;
import co.optonaut.optonaut.views.profile.ProfileFeedFragment;
import co.optonaut.optonaut.views.profile.ProfileFragment;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-12-29
 */
public class MainActivityRedesign extends AppCompatActivity {
    private static final int REQUEST_SHARE = 1;
    ;
    private MixpanelAPI mixpanelAPI;

    private HostFragment hostFragment;
    private OverlayNavigationFragment overlayFragment;

    private boolean isStatusBarVisible;
    public boolean fromFragment=false;

    private Cache cache;

    private DBHelper mydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initialize constants
        Constants.initializeConstants(this);
        GestureDetectors.initialize(this);
        CoreMotionListener.initialize(this);

        // FB Track App Installs and App Opens
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        try {

            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }

        } catch(PackageManager.NameNotFoundException e) {
            Log.e("name not found", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        }

        // instatiate cache on start of application
        cache = Cache.getInstance(this);

        super.onCreate(savedInstanceState);
        mydb = new DBHelper(this);

        Log.d("myTag"," user_id: "+cache.getString(Cache.USER_ID)+" token: "+cache.getString(Cache.USER_TOKEN));

        setContentView(R.layout.activity_main_redesign);

        if (findViewById(R.id.feed_placeholder) != null) {
            if (savedInstanceState != null) {
                return;
            }

            MainFeedFragment mainSnappyFeedFragment = new MainFeedFragment();
            hostFragment = HostFragment.newInstance(mainSnappyFeedFragment, MainFeedFragment.TAG);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.feed_placeholder, hostFragment).commit();

            overlayFragment = new OverlayNavigationFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.feed_placeholder, overlayFragment).commit();
        }

        initializeMixpanel();
        goFullscreen();
    }

    private void initializeMixpanel() {
        this.mixpanelAPI = MixpanelAPI.getInstance(this, MixpanelHelper.MIXPANEL_TOKEN);
    }

    private void goFullscreen() {
        // go full fullscreen mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        isStatusBarVisible = true;
    }

    private void hideStatusBar() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        isStatusBarVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        findFragments();
        restoreUIState();
    }

    @Override
    public void onDestroy() {
        // flush mixpanel events before destroying
        if (mixpanelAPI != null) {
            mixpanelAPI.flush();
        }
        super.onDestroy();
    }

    private void restoreUIState() {
        if (isStatusBarVisible) {
            goFullscreen();
        } else {
            hideStatusBar();
        }
    }

    private void findFragments() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (hostFragment == null && fragment instanceof HostFragment) {
                hostFragment = (HostFragment) fragment;
            } else if (overlayFragment == null && fragment instanceof OverlayNavigationFragment) {
                overlayFragment = (OverlayNavigationFragment) fragment;
            } else {
                Timber.w("unknown fragment in SupportFragmentManager");
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            restoreUIState();
        }
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null && !fromFragment) {
                for (Fragment fragment : fragmentList) {
                    if (fragment instanceof SigninFBFragment) {
                        Log.d("myTag", "instanceof SigninFB");
                        super.onBackPressed();
                        return;
                    }
                }
        }
        if (fragmentList != null && !fromFragment) {
            for (Fragment fragment : fragmentList) {
                if (fragment instanceof OptoImagePreviewFragment) {
                    Log.d("myTag", "instanceof PreviewFrag");
                    ((OptoImagePreviewFragment)fragment).onBackPressed();
                    return;
                }
            }
        }
        if(!BackStackFragment.handleBackPressed(getSupportFragmentManager())) {
            super.onBackPressed();
        } else {
            // TODO: find better solution
            goFullscreen();
        }
    }

    public int getUpperBoundary() {
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);

        // don't set preference to 0 if it is not set yet
        if (sharedPref.contains(getResources().getString(R.string.preference_upperboundary))) {
            return sharedPref.getInt(getResources().getString(R.string.preference_upperboundary), 0);
        }

        return 0;
    }

    public int getLowerBoundary() {
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);

        // don't set preference to 0 if it is not set yet
        if (sharedPref.contains(getResources().getString(R.string.preference_lowerboundary))) {
            return sharedPref.getInt(getResources().getString(R.string.preference_lowerboundary), 0);
        }

        return 0;
    }

    public void setOverlayVisibility(int visibility) {
        if (overlayFragment != null) {
            overlayFragment.setTotalVisibility(visibility);
            if (visibility == View.VISIBLE) {
                goFullscreen();
            } else {
                hideStatusBar();
            }
        } else {
            Timber.w("setting overlay visibility on null object");
        }
    }

    public boolean toggleOverlayVisibility() {
        return overlayFragment.toggleTotalVisibility();
    }

    public void shareOptograph(Optograph optograph) {
        String shareUrl = ImageUrlBuilder.buildWebViewerUrl(optograph.getShare_alias());

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject_web_viewer));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.share_body_web_viewer, shareUrl));
        sharingIntent.setType("text/plain");
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_via)));

        Timber.v("Initiated Sharing");
        MixpanelHelper.trackActionViewer2DShare(this);
    }

    public void prepareVRMode() {
        if (overlayFragment != null) {
            overlayFragment.hideDialog();
        }
    }

    public void retryRecording() {
        overlayFragment.switchToPreviewRecordMode(Constants.MODE_CENTER);
//        onBackPressed();
    }

    public void prepareRecording(int mode) {
        hideStatusBar();

        Bundle bundle = new Bundle();
        bundle.putInt("mode", mode);
        RecordFragment recordFragment = new RecordFragment();
        recordFragment.setArguments(bundle);

        hostFragment.replaceFragment(recordFragment, true, null);
    }

    /**
     * Send the person object or the ID of the person, whichever is available. Set the other as null.
     * @param person
     * @param id
     */
    public void startProfile(Person person, String id) {
//        hideStatusBar();

        overlayFragment.setToolbarVisibility(View.INVISIBLE);

        Bundle bundle = new Bundle();
        if(person != null) bundle.putParcelable("person", person);
        else if(id != null) bundle.putString("id", id);
        ProfileFragment profileFragment = new ProfileFragment();
        profileFragment.setArguments(bundle);
        hostFragment.replaceFragment(profileFragment, true, ProfileFragment.TAG);

    }

    public void startProfileFeed(Person person, int position) {
        overlayFragment.setToolbarVisibility(View.VISIBLE);

        Bundle bundle = new Bundle();
        bundle.putParcelable("person", person);
        bundle.putInt("position", position);
        ProfileFeedFragment profileFeedFragment = new ProfileFeedFragment();
        profileFeedFragment.setArguments(bundle);
        hostFragment.replaceFragment(profileFeedFragment, true, null);

    }

    /**
     * @param popPreviousFragment true if needs to remove the previous fragment
     */
    public void prepareProfile(boolean popPreviousFragment) {
        if(!cache.getString(Cache.USER_TOKEN).equals("")) {
            clearFragmentStack(ProfileFragment.TAG);
            if(popPreviousFragment) hostFragment.popBackStackImmediate();
            startProfile(null, cache.getString(Cache.USER_ID));
        } else {
            Bundle bundle = new Bundle();
            SigninFBFragment signinFBFragment = new SigninFBFragment();
            signinFBFragment.setArguments(bundle);
            hostFragment.addFragment(signinFBFragment, true, null);
        }
    }

    public void profileDialog() {
        hostFragment.getFragmentManager().beginTransaction().add(R.id.feed_placeholder, new SigninFBFragment())
                .addToBackStack("profileDialog").commit();
    }

    /**
     *
     * @param id randomized ID for the optograph
     * @param imagePath absolute path if image is not from iam360 i.e Theta upload. Null if from iam360
     */
    public void startImagePreview(UUID id, String imagePath) {
        hideStatusBar();
//        hostFragment.replaceFragment(new OptoImagePreviewFragment(),true);
        Bundle bundle = new Bundle();
        bundle.putString("id", id.toString());
        if(imagePath != null) bundle.putString("path", imagePath);
        OptoImagePreviewFragment prevFrag = new OptoImagePreviewFragment();
        prevFrag.setArguments(bundle);
        hostFragment.getFragmentManager().beginTransaction().replace(R.id.feed_placeholder,prevFrag)
                .addToBackStack("preview").commit();
    }
    
    public void startRecording() {
        if (hostFragment.getCurrentFragment() instanceof RecordFragment) {
            ((RecordFragment) hostFragment.getCurrentFragment()).startRecording();
        } else {
            Timber.e("Tried to take picture but no record fragment available");
        }
    }

    public void cancelRecording() {
        if (hostFragment.getCurrentFragment() instanceof RecordFragment) {
            ((RecordFragment) hostFragment.getCurrentFragment()).cancelRecording();
        } else {
            clearFragmentStack(ProfileFragment.TAG);
            Timber.e("Tried to cancel recording, but no record fragment available");
        }
    }

    public void removeCurrentFragment() {
        hostFragment.popBackStackImmediate();
    }


    /**
     * Pop up stack up to the fragment to be retained. For Home and Profile
     * @param removeFragment fragment stack to be removeFragment
     */
    public void clearFragmentStack(String removeFragment) {
        hostFragment.popBackStack(removeFragment);
    }

    public void backToFeed(boolean cancel) {
        overlayFragment.switchToFeedMode(cancel);
        onBackPressed();
    }

    public void backToFeed() {
        overlayFragment.switchToFeedModeFromPreview();
        fromFragment = true;
        onBackPressed();
    }

    public void startPreview(UUID id) {
        overlayFragment.switchToImagePreview(id);
        onBackPressed();
    }

    public void setAngleRotation(float rotation) {
        overlayFragment.setAngleRotation(rotation);
    }

    public void setArrowRotation(float rotation) {
        overlayFragment.setArrowRotation(rotation);
    }

    public void setProgressLocation(float progress) {
        overlayFragment.setProgress(progress);
    }

    public void setArrowVisible(boolean visible) {
        overlayFragment.setArrowVisible(visible);
    }

    public void setGuideLinesVisible(boolean visible) {
        overlayFragment.setGuideLinesVisible(visible);
    }

}
