package com.iam360.iam360.views.new_design;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.MessageDialog;
import com.facebook.share.widget.ShareDialog;
import com.iam360.iam360.model.FBSignInData;
import com.iam360.iam360.model.LogInReturn;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.network.PersonManager;
import com.iam360.iam360.util.GeneralUtils;
import com.iam360.iam360.views.dialogs.GenericOKDialog;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import com.iam360.iam360.R;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.opengl.Cube;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.ImageUrlBuilder;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;
import twitter4j.Twitter;

public class SharingFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = SharingFragment.class.getSimpleName();
    private Person person;
    private Cache cache;
    private Optograph optograph;

    @Bind(R.id.preview_image) ImageView previewImg;
    @Bind(R.id.copy_share_btn) ImageButton copyBtn;
    @Bind(R.id.email_share_btn) ImageButton emailBtn;
    @Bind(R.id.fb_share_btn) ImageButton fbBtn;
    @Bind(R.id.twitter_share_btn) ImageButton twitterBtn;
    @Bind(R.id.messenger_share_btn) ImageButton messengerBtn;
    @Bind(R.id.share_text) TextView shareText;
    @Bind(R.id.toolbar_text) TextView toolbarText;
    @Bind(R.id.toolbar) Toolbar toolbar;

    /**
     * Register your here app https://dev.twitter.com/apps/new and get your
     * consumer key and secret
     */
    static String TWITTER_CONSUMER_KEY; // place your cosumer key here
    static String TWITTER_CONSUMER_SECRET; // place your consumer secret here

    private static Twitter mTwitter;

    private ApiConsumer apiConsumer;
    private CallbackManager callbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {

//        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        cache = Cache.open();

        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);

        Timber.d("SHARING TOKEN : " + token);

        TWITTER_CONSUMER_KEY = getString(R.string.twitter_consumer_key);
        TWITTER_CONSUMER_SECRET = getString(R.string.twitter_consumer_secret);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sharing, container, false);
        ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(" ");

        copyBtn.setOnClickListener(this);
        emailBtn.setOnClickListener(this);
        fbBtn.setOnClickListener(this);
        twitterBtn.setOnClickListener(this);
        messengerBtn.setOnClickListener(this);

        GeneralUtils utils = new GeneralUtils();
        utils.setFont(getContext(), toolbarText);
        utils.setFont(getContext(), shareText);

        return view;
    }

    @Override
    public void onResume() {
        updateOptograph();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setOptograph(Optograph optograph) {
        Timber.d("Set Optograph.");
        this.optograph = optograph;
    }

    public void updateOptograph() {
        Timber.d("Preview Update.");

        if(optograph != null) {
            setOptographPreview();
        } else {
            Timber.d("Empty Optograph.");
        }

    }

    public static SharingFragment newInstance() {
        SharingFragment sharingFragment = new SharingFragment();
        return sharingFragment;
    }

    public static SharingFragment newInstance(Optograph optograph) {
        SharingFragment sharingFragment = new SharingFragment();
        sharingFragment.setOptograph(optograph);
        return sharingFragment;
    }

    private void setOptographPreview() {

        Timber.d("Preview Set Opto.");
        String uri = ImageUrlBuilder.buildPlaceholderUrl(optograph, true, Cube.FACES[Cube.FACE_AHEAD]);

//        if(previewImg.getWidth() > 0 && previewImg.getHeight() > 0) {
//            Timber.d("Setting image.");

            previewImg.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        // Wait until layout to call Picasso
                        @Override
                        public void onGlobalLayout() {
                            // Ensure we call this only once
                            previewImg.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                            if (optograph.is_local()) {
                                Picasso.with(previewImg.getContext())
                                        .load(new File(uri))
                                        .resize(previewImg.getWidth(), previewImg.getHeight())
                                        .centerCrop()
                                        .into(previewImg);
                            } else {
                                Picasso.with(previewImg.getContext())
                                        .load(uri)
                                        .resize(previewImg.getWidth(), previewImg.getHeight())
                                        .centerCrop()
                                        .into(previewImg);
                            }
                        }
                    });
//        } else {
//            Timber.d("Preview image container is zero.");
//        }

    }

    @Override
    public void onClick(View v) {
        if(optograph != null) {
            String shareUrl = ImageUrlBuilder.buildWebViewerUrl(optograph.getShare_alias());

            switch (v.getId()) {
                case R.id.copy_share_btn:
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(getActivity().CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(getResources().getString(R.string.share_body_web_viewer), shareUrl);
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(copyBtn, getResources().getString(R.string.share_copy_to_clipboard), Snackbar.LENGTH_SHORT).show();
                    break;
                case R.id.email_share_btn:
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("message/rfc822");
                    final PackageManager pm = getActivity().getPackageManager();
                    final List<ResolveInfo> matches = pm.queryIntentActivities(intent, 0);
                    ResolveInfo best = null;
                    for (final ResolveInfo info : matches) {
                        if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase().contains("gmail")) {
                            best = info;
                            break;
                        }
                    }
                    if (best != null) {
                        intent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
                    }
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject_web_viewer));
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, shareUrl);

                    startActivity(intent);
                    break;
                case R.id.fb_share_btn:
                    Set<String> permissions = null;
                    if(AccessToken.getCurrentAccessToken() == null)
                        loginToFB(getResources().getString(R.string.share_subject_web_viewer) + "\n" + shareUrl);
//                        Snackbar.make(v, getActivity().getString(R.string.profile_login_first), Snackbar.LENGTH_SHORT).show();
                    else {
                        permissions = AccessToken.getCurrentAccessToken().getPermissions();

                        if (permissions.contains("publish_actions")) {
                            showInputDialog(getResources().getString(R.string.share_subject_web_viewer) + "\n" + shareUrl);
                            Timber.d("facebook publish permission NOT login");
                        } else {
                            loginToFB(getResources().getString(R.string.share_subject_web_viewer) + "\n" + shareUrl);
                            Timber.d("facebook publish permission login " + permissions.toString());
                        }
//                    ShareDialog shareDialog = new ShareDialog(this);
//                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
//                            .setContentTitle(getResources().getString(R.string.share_subject_web_viewer))
//                            .setContentDescription(
//                                    optograph.getText())
//                            .setContentUrl(Uri.parse(shareUrl))
//                            .build();
//
//                    shareDialog.show(linkContent);
                    }
                    break;
                case R.id.twitter_share_btn:
                    TwitterAuthConfig authConfig =  new TwitterAuthConfig(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
                    try {
                        TweetComposer.Builder builder = new TweetComposer.Builder(getActivity())
                                .text("\n"+getResources().getString(R.string.share_subject_web_viewer))
                                .url(new URL(shareUrl));
                        builder.show();
                    } catch (MalformedURLException e) {
                        Log.d("myTag","ERROR twitter share: "+e.getMessage());
                    }
                    /*final TwitterSession session = TwitterCore.getInstance().getSessionManager()
                            .getActiveSession();
                    final Intent intent = new ComposerActivity.Builder(getActivity())
                            .session(session)
                            .createIntent();
                    startActivity(intent);*/
                    break;
                case R.id.messenger_share_btn:
                    MessageDialog messageDialog = new MessageDialog(this);
                    ShareLinkContent linkCont = new ShareLinkContent.Builder()
                            .setContentTitle(getResources().getString(R.string.share_subject_web_viewer))
                            .setContentDescription(optograph.getText())
                            .setContentUrl(Uri.parse(shareUrl))
                            .build();
                    messageDialog.show(linkCont);
                    break;
            }
        }
    }

    private void loginToFB(String text) {
        final List<String> PUBLISH_PERMISSIONS = Arrays.asList("publish_actions");
        LoginManager.getInstance().logInWithPublishPermissions(this, PUBLISH_PERMISSIONS);
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("myTag", "success login on fb: " + loginResult.getAccessToken().getUserId());

                apiConsumer.fbLogIn(new FBSignInData(loginResult.getAccessToken().getUserId(), loginResult.getAccessToken().getToken()), new Callback<LogInReturn>() {
                    @Override
                    public void onResponse(Response<LogInReturn> response, Retrofit retrofit) {

                        if (!response.isSuccess()) {
                            Toast toast = Toast.makeText(getActivity(), "Failed to log in.", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return;
                        }
                        LogInReturn login = response.body();
                        if (login == null) {
                            Toast toast = Toast.makeText(getActivity(), "Failed to log in.", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return;
                        }

                        Timber.d("FB Token : " + loginResult.getAccessToken().getToken());

                        cache.save(Cache.USER_ID, login.getId());
                        cache.save(Cache.USER_TOKEN, login.getToken());
                        cache.save(Cache.USER_FB_ID, loginResult.getAccessToken().getUserId());
                        cache.save(Cache.USER_FB_TOKEN, loginResult.getAccessToken().getToken());
                        cache.save(Cache.USER_FB_LOGGED_IN, true);
                        PersonManager.updatePerson();
                        showInputDialog(getResources().getString(R.string.share_subject_web_viewer) + "\n" + text);

                    }

                    @Override
                    public void onFailure(Throwable t) {
                        LoginManager.getInstance().logOut();

                        Bundle bundle = new Bundle();
                        bundle.putString(GenericOKDialog.MESSAGE_KEY, getResources().getString(R.string.dialog_network_retry));

                        GenericOKDialog genericOKDialog = new GenericOKDialog();
                        genericOKDialog.setArguments(bundle);
                        genericOKDialog.show(getFragmentManager(), "Error");
                    }
                });
            }

            @Override
            public void onCancel() {
                Log.d("myTag", "oncancel login on fb.");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("myTag", "onError login on fb. " + error.getMessage());
                Snackbar.make(fbBtn, getActivity().getString(R.string.profile_login_again), Snackbar.LENGTH_SHORT).show();
                if (error instanceof FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut();
                    }
                }
            }
        });
    }

    private void showInputDialog(String text) {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.dialog_input, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.caption);
        final TextView cancelBtn = (TextView) promptView.findViewById(R.id.cancel_btn);
        final TextView postBtn = (TextView) promptView.findViewById(R.id.post_btn);

        editText.setText(text);
        alertDialogBuilder.setCancelable(false);
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

        postBtn.setOnClickListener(v -> {
            alert.cancel();
            apiConsumer.shareFB(new ShareFBData(optograph.getId(), editText.getText().toString().equals("") ? " " : editText.getText().toString()), new Callback<LogInReturn.EmptyResponse>() {
                @Override
                public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {

                    Bundle bundle = new Bundle();
                    bundle.putString(GenericOKDialog.MESSAGE_KEY, getResources().getString(R.string.dialog_fb_success));

                    GenericOKDialog genericOKDialog = new GenericOKDialog();
                    genericOKDialog.setArguments(bundle);
                    genericOKDialog.show(getFragmentManager(), "Error");
                }

                @Override
                public void onFailure(Throwable t) {
                }
            });
        });
        cancelBtn.setOnClickListener(v -> {
            alert.cancel();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_back:
                if(getContext() instanceof MainActivity)
                    ((MainActivity)getActivity()).setPage(MainActivity.FEED_MODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if(getContext() instanceof MainActivity) menu.findItem(R.id.action_back).setVisible(true);
        else menu.findItem(R.id.action_back).setVisible(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sharing_menu, menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    public static class ShareFBData {
        final String optograph_id;
        final String caption;

        public ShareFBData(String optograph_id, String caption) {
            this.optograph_id = optograph_id;
            this.caption = caption;
        }
    }
}
