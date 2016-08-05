package com.iam360.iam360.views.new_design;


import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.iam360.iam360.R;
import com.iam360.iam360.model.Gateway;
import com.iam360.iam360.network.Api2Consumer;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.GeneralUtils;
import com.iam360.iam360.views.dialogs.GenericOKDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class MailingListFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = MailingListFragment.class.getSimpleName();
    @Bind(R.id.type_text) TextView typeText;
    @Bind(R.id.get_text) TextView getText;
    @Bind(R.id.go_btn) Button goBtn;
    @Bind(R.id.send_btn) Button sendBtn;
    @Bind(R.id.reach_us_text) TextView reachUsText;
    @Bind(R.id.secret_code_text) EditText codeText;
    @Bind(R.id.email_text) TextView emailText;
    @Bind(R.id.app_email_text) TextView appemailText;

    private Cache cache;
    private Api2Consumer api2Consumer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        cache = Cache.open();
        api2Consumer = new Api2Consumer(null,"");
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
        utils.setFont(getContext(), goBtn, Typeface.BOLD);
        utils.setFont(getContext(), sendBtn, Typeface.BOLD);
        utils.setFont(getContext(), reachUsText);
        utils.setFont(getContext(), emailText);
        utils.setFont(getContext(), codeText);
        utils.setFont(getContext(), appemailText, Typeface.BOLD);

        emailText.setText(cache.getString(Cache.USER_EMAIL));

        checkStatus();

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
            case R.id.go_btn:
                if(!codeText.getText().toString().equals("")) useCode();
                break;
            case R.id.send_btn:
                requestCode();
                break;
        }

    }

    private void showPrompt(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(message);
        builder.setCancelable(true);

        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert1 = builder.create();
        alert1.show();
    }

    private void useCode() {
        Timber.d("useCode");
        goBtn.setEnabled(false);

        String uuid = cache.getString(Cache.USER_ID);
        String code = cache.getString(Cache.GATE_CODE);
        if (!uuid.equals("") && code.equals("")) {

            api2Consumer.useCode(new Gateway.UseCodeData(uuid, codeText.getText().toString().toUpperCase()), new Callback<Gateway.UseCodeResponse>() {
                @Override
                public void onResponse(Response<Gateway.UseCodeResponse> response, Retrofit retrofit) {

                    Bundle bundle = new Bundle();
                    bundle.putString(GenericOKDialog.MESSAGE_KEY, getResources().getString(R.string.dialog_network_retry));

                    Gateway.UseCodeResponse useCodeResponse = response.body();
                    showPrompt(useCodeResponse.getPrompt());

                    // gate open, start profile
                    if (useCodeResponse.getStatus().equals("ok")) {
                        cache.save(Cache.GATE_CODE, codeText.getText().toString());
                        startProfile();
                    } else if (useCodeResponse.getPrompt().contains("You are already an Elite User!")) {
                        // special case since elite user not handled yet in the server
                        cache.save(Cache.GATE_CODE, codeText.getText().toString());
                        startProfile();
                    }

                    Timber.d(useCodeResponse.toString());

                    goBtn.setEnabled(true);
                }

                @Override
                public void onFailure(Throwable t) {
                    Timber.d(t.getMessage());
                    showPrompt(getResources().getString(R.string.dialog_network_retry));

                    goBtn.setEnabled(true);
                }
            });
        }
    }

    private void requestCode() {
        Timber.d("requestCode");

        sendBtn.setEnabled(false);

        String uuid = cache.getString(Cache.USER_ID);
        String code = cache.getString(Cache.GATE_CODE);
        if (!uuid.equals("") && code.equals("")) {

            api2Consumer.requestCode(new Gateway.RequestCodeData(uuid), new Callback<Gateway.RequestCodeResponse>() {
                @Override
                public void onResponse(Response<Gateway.RequestCodeResponse> response, Retrofit retrofit) {

                    Gateway.RequestCodeResponse requestCodeResponse = response.body();

                    requestCodeResponse.getMessage();
                    requestCodeResponse.getRequestText();

                    if (requestCodeResponse.getStatus().equals("ok"))
                        getText.setText(requestCodeResponse.getRequestText());
                    else sendBtn.setEnabled(true);

                    showPrompt(requestCodeResponse.getPrompt());
                    Timber.d(requestCodeResponse.toString());
                }

                @Override
                public void onFailure(Throwable t) {
                    Timber.d(t.getMessage());
                    showPrompt(getResources().getString(R.string.dialog_network_retry));

                    sendBtn.setEnabled(false);
                }
            });
        }
    }

    private void checkStatus() {
        Timber.d("checkStatus");

        String uuid = cache.getString(Cache.USER_ID);
        String code = cache.getString(Cache.GATE_CODE);
        if (!uuid.equals("") && code.equals("")) {

            api2Consumer.checkStatus(new Gateway.CheckStatusData(uuid), new Callback<Gateway.CheckStatusResponse>() {
                @Override
                public void onResponse(Response<Gateway.CheckStatusResponse> response, Retrofit retrofit) {

                    Gateway.CheckStatusResponse checkStatusResponse = response.body();
                    checkStatusResponse.getStatus();

                    getText.setText(checkStatusResponse.getRequestText());
                    if (checkStatusResponse.getMessage().equals(Gateway.CheckStatusResponse.MESSAGE_2))
                        sendBtn.setEnabled(false);
                    if (checkStatusResponse.getMessage().equals(Gateway.CheckStatusResponse.MESSAGE_3)) {
                        // dummy gate code so that cache is not empty
                        cache.save(Cache.GATE_CODE, "GATE");
                        startProfile();
                    }

                    Timber.d(checkStatusResponse.toString());
                }

                @Override
                public void onFailure(Throwable t) {
                    Timber.d(t.getMessage());
                    showPrompt(getResources().getString(R.string.dialog_network_retry));

                }
            });
        } else {
            // already has code, proceed to profile
            startProfile();
        }
    }

    private void startProfile() {

        if(getContext() instanceof MainActivity) {
            getActivity().finish();
            startActivity(getActivity().getIntent());

        }
    }

}
