package com.iam360.dscvr.views.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.iam360.dscvr.R;
import com.iam360.dscvr.model.Person;
import com.iam360.dscvr.network.ApiConsumer;
import com.iam360.dscvr.network.PersonManager;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.GeneralUtils;
import com.iam360.dscvr.views.dialogs.NetworkProblemDialog;

import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class CreateUsernameActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener {
    @Bind(R.id.username) EditText userName;
    @Bind(R.id.header_text) TextView headerText;
    @Bind(R.id.username_check) TextView userNameCheck;
    @Bind(R.id.symbol) TextView symbol;
    @Bind(R.id.create_btn) Button createBtn;

    private ApiConsumer apiConsumer;
    private Cache cache;
    private NetworkProblemDialog networkProblemDialog;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_username);

        cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        int onboardingVersion = cache.getInt(Cache.ONBOARDING_VERSION);
        networkProblemDialog = new NetworkProblemDialog();

        Timber.d("ONBOARDING_VERSION : " + onboardingVersion);
        if(onboardingVersion > 0) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        ButterKnife.bind(this);

        GeneralUtils utils = new GeneralUtils();
        utils.setFont(this, userName, Typeface.BOLD);
        utils.setFont(this, symbol, Typeface.BOLD);
        utils.setFont(this, headerText);
        utils.setFont(this, userNameCheck, Typeface.ITALIC);
        utils.setFont(this, createBtn,Typeface.BOLD);

//        userName.setText(cache.getString(Cache.USER_NAME));
        userName.addTextChangedListener(this);
        createBtn.setOnClickListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // minimum username length is 4
        if(s.length() >= 5) {
            userNameCheck.setText("");
            userNameCheck.setTextColor(getResources().getColor(R.color.text_light));
            apiConsumer.getSearchUsername(s.toString(), new Callback<List<Person>>() {
                @Override
                public void onResponse(Response<List<Person>> response, Retrofit retrofit) {
                    Timber.d("isSuccess : " + response.isSuccess());
                    Timber.d("body : " + response.body());
                    if (response.isSuccess()) {
                        checkIfValidUserName(response.body());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    if (networkProblemDialog.isAdded() || t.getMessage().contains("iterable must not be null"))
                        return;
                    networkProblemDialog.show(getSupportFragmentManager(), "networkProblemDialog");
                }
            });
        } else {
            userNameCheck.setText(s.length()==0?"":getResources().getString(R.string.create_username_min_char));
            userNameCheck.setTextColor(getResources().getColor(R.color.text_light));
            createBtn.setTextColor(getResources().getColor(R.color.text_dark));
            createBtn.setEnabled(false);
            createBtn.setBackgroundResource(R.drawable.orange_btn);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void checkIfValidUserName(List<Person> persons) {

        if(persons != null && persons.size() > 0) {
            userNameCheck.setTextColor(getResources().getColor(R.color.text_light));
            userNameCheck.setText(getResources().getString(R.string.create_username_taken));
            createBtn.setTextColor(getResources().getColor(R.color.text_dark));
            createBtn.setEnabled(false);
            createBtn.setBackgroundResource(R.drawable.gray_btn);
        } else {
            userNameCheck.setTextColor(getResources().getColor(R.color.text_yellow));
            userNameCheck.setText(getResources().getString(R.string.create_username_available));
            createBtn.setTextColor(getResources().getColor(R.color.text_dark));
            createBtn.setEnabled(true);
            createBtn.setBackgroundResource(R.drawable.yellow_btn);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_btn:
                try {
                    apiConsumer.updatePerson(new PersonManager.UpdatePersonData(null, null, userName.getText().toString()), new Callback<Person>() {
                        @Override
                        public void onResponse(Response<Person> response, Retrofit retrofit) {
                            Timber.d("Update person : " + response.isSuccess() + " body:" + response.body());
                            if(response.isSuccess()) {
                                cache.save(Cache.ONBOARDING_VERSION, 1);
                                showPrompt(getResources().getString(R.string.create_username_sucessful), true);
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            showPrompt(getResources().getString(R.string.create_username_failed), false);
                            Timber.e("Failed to update person.");
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }                break;
        }
    }

    private void showPrompt(String message, boolean success) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setCancelable(true);

        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        if (success) {
                            cache.save(Cache.USER_NAME, userName.getText().toString());
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
