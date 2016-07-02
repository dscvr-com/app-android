package com.iam360.iam360.views.new_design;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Button;
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
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.MessageDialog;
import com.iam360.iam360.R;
import com.iam360.iam360.model.FBSignInData;
import com.iam360.iam360.model.LogInReturn;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.network.Api2Consumer;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.network.PersonManager;
import com.iam360.iam360.opengl.Cube;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.GeneralUtils;
import com.iam360.iam360.util.ImageUrlBuilder;
import com.iam360.iam360.views.dialogs.GenericOKDialog;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;
import twitter4j.Twitter;

public class MailingListFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = MailingListFragment.class.getSimpleName();
    @Bind(R.id.type_text) TextView typeText;
    @Bind(R.id.get_text) TextView getText;
    @Bind(R.id.go_btn) Button goBtn;
    @Bind(R.id.send_btn) Button sendBtn;
    @Bind(R.id.reach_us_text) TextView reachUsText;
    @Bind(R.id.secret_code_text) EditText codeText;
    @Bind(R.id.email_text) EditText emailText;

    private Cache cache;
    private Api2Consumer api2Consumer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        cache = Cache.open();
        api2Consumer = new Api2Consumer(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mailing_list, container, false);
        ButterKnife.bind(this, view);

        goBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);

        GeneralUtils utils = new GeneralUtils();
        utils.setFont(getContext(), typeText);
        utils.setFont(getContext(), getText);
        utils.setFont(getContext(), goBtn, Typeface.DEFAULT_BOLD);
        utils.setFont(getContext(), sendBtn, Typeface.DEFAULT_BOLD);
        utils.setFont(getContext(), reachUsText);
        utils.setFont(getContext(), emailText);
        utils.setFont(getContext(), codeText);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public static MailingListFragment newInstance() {
        MailingListFragment mailingListFragment = new MailingListFragment();
        return mailingListFragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default: break;
        }
    }

}
