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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.MessageDialog;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import com.iam360.iam360.R;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.opengl.Cube;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.ImageUrlBuilder;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        cache = Cache.open();

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

        setFont(toolbarText);
        setFont(shareText);

        return view;
    }

    private void setFont(TextView textView) {
        Typeface custom_font = Typeface.createFromAsset(getResources().getAssets(),"fonts/Avenir_LT_45_Book_0.ttf");
        textView.setTypeface(custom_font);
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
        this.optograph = optograph;
    }

    public void updateOptograph() {

        if(optograph != null) {
            setOptographPreview();
        }

    }

    public static SharingFragment newInstance() {
        SharingFragment sharingFragment = new SharingFragment();
        return sharingFragment;
    }


    private void setOptographPreview() {

        String uri = ImageUrlBuilder.buildPlaceholderUrl(optograph, true, Cube.FACES[Cube.FACE_AHEAD]);

        if(previewImg.getWidth() > 0 && previewImg.getHeight() > 0) {
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
                    /*Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                    sendIntent.setType("message/rfc822");
//                    sendIntent.setType("plain/text");
//                    sendIntent.setData(Uri.parse("test@gmail.com"));
//                    sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
//                    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "test@gmail.com" });
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject_web_viewer));
                    sendIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
                    getActivity().startActivity(sendIntent);*/
                    /*Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject_web_viewer));
                    i.putExtra(Intent.EXTRA_TEXT, shareUrl);
                    try {
                        startActivity(Intent.createChooser(i, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Snackbar.make(emailBtn, getResources().getString(R.string.share_email_error), Snackbar.LENGTH_SHORT).show();
                    }*/
                    break;
                case R.id.fb_share_btn:
                    ShareDialog shareDialog = new ShareDialog(this);
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentTitle(getResources().getString(R.string.share_subject_web_viewer))
                            .setContentDescription(
                                    optograph.getText())
                            .setContentUrl(Uri.parse(shareUrl))
                            .build();

                    shareDialog.show(linkContent);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_back:
                ((MainActivity)getActivity()).setPage(MainActivity.FEED_MODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_back).setVisible(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sharing_menu, menu);
    }
}
