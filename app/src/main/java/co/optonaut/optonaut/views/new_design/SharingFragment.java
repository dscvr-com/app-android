package co.optonaut.optonaut.views.new_design;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.bus.BusProvider;
import co.optonaut.optonaut.bus.PersonReceivedEvent;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.model.Person;
import co.optonaut.optonaut.network.PersonManager;
import co.optonaut.optonaut.opengl.Cube;
import co.optonaut.optonaut.util.Cache;
import co.optonaut.optonaut.util.ImageUrlBuilder;
import timber.log.Timber;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        cache = Cache.open();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sharing, container, false);
        ButterKnife.bind(this, view);

        copyBtn.setOnClickListener(this);
        emailBtn.setOnClickListener(this);
        fbBtn.setOnClickListener(this);
        twitterBtn.setOnClickListener(this);
        messengerBtn.setOnClickListener(this);

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
        if (optograph.is_local()) {
            Picasso.with(previewImg.getContext())
                    .load(new File(uri))
                    .into(previewImg);
        } else {
            Picasso.with(previewImg.getContext())
                    .load(uri)
                    .into(previewImg);
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
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject_web_viewer));
                    i.putExtra(Intent.EXTRA_TEXT, shareUrl);
                    try {
                        startActivity(Intent.createChooser(i, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Snackbar.make(emailBtn, getResources().getString(R.string.share_email_error), Snackbar.LENGTH_SHORT).show();
                    }
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
                    break;
                case R.id.messenger_share_btn:
                    break;
            }
        }
    }
}
