package com.iam360.iam360.views.new_design;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

public class MailingListActivity extends AppCompatActivity implements View.OnClickListener {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.animator.from_right_to_left, R.animator.from_left_to_right);
        setContentView(R.layout.fragment_mailing_list);

        cache = Cache.open();
        api2Consumer = new Api2Consumer(null);

        ButterKnife.bind(this);

        goBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);

        GeneralUtils utils = new GeneralUtils();
        utils.setFont(this, typeText);
        utils.setFont(this, getText);
        utils.setFont(this, goBtn, Typeface.BOLD);
        utils.setFont(this, sendBtn, Typeface.BOLD);
        utils.setFont(this, reachUsText);
        utils.setFont(this, emailText);
        utils.setFont(this, codeText);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.animator.to_right, R.animator.to_left);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_btn:
                break;
            case R.id.send_btn:
                String uuid = cache.getString(Cache.USER_ID);
                String code = cache.getString(Cache.GATE_CODE);
                if (!uuid.equals("") && code.equals("")) {

                    api2Consumer.requestCode(new Gateway.RequestCodeData(uuid), new Callback<Gateway.RequestCodeResponse>() {
                        @Override
                        public void onResponse(Response<Gateway.RequestCodeResponse> response, Retrofit retrofit) {
                            if (!response.isSuccess()) {
                                return;
                            }

                            Bundle bundle = new Bundle();
                            bundle.putString(GenericOKDialog.MESSAGE_KEY, getResources().getString(R.string.dialog_network_retry));

                            Gateway.RequestCodeResponse requestCodeResponse = response.body();
                            requestCodeResponse.getStatus();
                            requestCodeResponse.getMessage();
                            requestCodeResponse.getRequestText();

                            Timber.d(response.toString());
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Timber.d(t.getMessage());
                        }
                    });
                }
                break;
        }

    }

    private void showDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

}
