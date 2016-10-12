package com.iam360.dscvr.views.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.iam360.dscvr.R;
import com.iam360.dscvr.model.FBSignInData;
import com.iam360.dscvr.model.LogInReturn;
import com.iam360.dscvr.model.Person;
import com.iam360.dscvr.model.SignInData;
import com.iam360.dscvr.model.SignUpReturn;
import com.iam360.dscvr.network.ApiConsumer;
import com.iam360.dscvr.network.PersonManager;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.MixpanelHelper;
import com.iam360.dscvr.util.NotificationSender;
import com.iam360.dscvr.views.activity.CreateUsernameActivity;
import com.iam360.dscvr.views.activity.MainActivity;
import com.iam360.dscvr.views.activity.SignInActivity;
import com.iam360.dscvr.views.dialogs.GenericOKDialog;

import java.util.Arrays;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Created by Mariel on 9/22/2016.
 */
public class SignUpFragment extends Fragment implements View.OnClickListener{

    private ApiConsumer apiConsumer;
    private CallbackManager callbackManager;
    private Cache cache;
    private EditText emailSignUp;
    private EditText passwordSignUp;
    private Button okButton;
    private ImageButton fbButton;
    private ProgressBar progressBar;
    private RelativeLayout progressLayout;

    private Person person;
    private int signInRequestCode = 2;
    private boolean loggedIn = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        apiConsumer = new ApiConsumer(null);
        cache = Cache.open();
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        emailSignUp = (EditText) view.findViewById(R.id.email_signup);
        passwordSignUp = (EditText) view.findViewById(R.id.password_signup);
        okButton = (Button) view.findViewById(R.id.ok_signup);
        fbButton = (ImageButton) view.findViewById(R.id.fb_button_signup);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        progressLayout = (RelativeLayout) view.findViewById(R.id.progress_layout);

        okButton.setOnClickListener(this);
        fbButton.setOnClickListener(this);

        return view;
    }

    private void setButtonsClickable(boolean clickable) {
        if(getContext() instanceof SignInActivity)((SignInActivity)getActivity()).swipeEnable(clickable);
        else if (getContext() instanceof MainActivity) ((MainActivity)getActivity()).swipeEnable(clickable);
        okButton.setClickable(clickable);
        fbButton.setClickable(clickable);
    }

    public void savePersonInfoToCache() {
        Timber.d("savePersonInfoToCache");

        String token = cache.getString(Cache.USER_TOKEN);
        ApiConsumer apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        apiConsumer.getUser(new Callback<Person>() {
            @Override
            public void onResponse(Response<Person> response, Retrofit retrofit) {
                person = response.body();
                cache.save(Cache.USER_EMAIL, person.getEmail());
                cache.save(Cache.USER_NAME, person.getUser_name());
                cache.save(Cache.ONBOARDING_VERSION, person.getOnboardingVersion());
                Timber.d("User email : " + person.getEmail());

                progressLayout.setVisibility(View.GONE);
                startCreateUserPage();
            }

            @Override
            public void onFailure(Throwable t) {
                progressLayout.setVisibility(View.GONE);
                Timber.d("Failed to load person!");
            }
        });
    }

    private void startCreateUserPage() {
        Timber.d("startCreateUserPage " + person);
        if(person != null)
            MixpanelHelper.identify(getActivity(), person);

        if(getContext() instanceof MainActivity) {
            getActivity().finish();
            NotificationSender.sendGCMRegService(getContext());
            startActivity(getActivity().getIntent());
        } else {
            Intent intent = new Intent(getActivity(), CreateUsernameActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == signInRequestCode)
            if (resultCode == Activity.RESULT_OK) loggedIn = true;

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void login(String email, String password) {

        apiConsumer.logIn(new SignInData(email, password),new Callback<LogInReturn>() {
            @Override
            public void onResponse(Response<LogInReturn> response, Retrofit retrofit) {
                if (!response.isSuccess()) {
                    Toast toast = Toast.makeText(getActivity(), "Failed to log in.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                    setButtonsClickable(true);
                    progressLayout.setVisibility(View.GONE);
                    return;
                }
                LogInReturn login = response.body();
                if (login==null) {
                    Toast toast = Toast.makeText(getActivity(), "Failed to log in.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                    setButtonsClickable(true);
                    progressLayout.setVisibility(View.GONE);
                    return;
                }

                Timber.d(response.toString());

                cache.save(Cache.USER_ID, login.getId());
                cache.save(Cache.USER_TOKEN, login.getToken());

                getActivity().setResult(2);
                getActivity().finish();

            }

            @Override
            public void onFailure(Throwable t) {
                Toast toast = Toast.makeText(getActivity(), "Failed to log in.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
                setButtonsClickable(true);
                progressLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fb_button_signup:
                setButtonsClickable(false);
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile", "user_friends"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("myTag", "success login on fb: " + loginResult.getAccessToken().getUserId() + " token: " + loginResult.getAccessToken().getToken());

                        progressLayout.setVisibility(View.VISIBLE);
                        apiConsumer.fbLogIn(new FBSignInData(loginResult.getAccessToken().getUserId(), loginResult.getAccessToken().getToken()), new Callback<LogInReturn>() {
                            @Override
                            public void onResponse(Response<LogInReturn> response, Retrofit retrofit) {
                                if (!response.isSuccess()) {
                                    Toast toast = Toast.makeText(getActivity(), getString(R.string.failed_signup), Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                    setButtonsClickable(true);
                                    progressLayout.setVisibility(View.GONE);
                                    return;
                                }
                                LogInReturn login = response.body();
                                if (login == null) {
                                    Toast toast = Toast.makeText(getActivity(), getString(R.string.failed_signup), Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                    setButtonsClickable(true);
                                    progressLayout.setVisibility(View.GONE);
                                    return;
                                }

                                Timber.d("FB Token : " + loginResult.getAccessToken().getToken());

                                cache.save(Cache.USER_ID, login.getId());
                                cache.save(Cache.USER_TOKEN, login.getToken());
                                cache.save(Cache.USER_FB_ID, loginResult.getAccessToken().getUserId());
                                cache.save(Cache.USER_FB_TOKEN, loginResult.getAccessToken().getToken());
                                cache.save(Cache.USER_FB_LOGGED_IN, true);

                                // TODO save avatar
//                                http://stackoverflow.com/questions/32310878/how-to-get-facebook-profile-image-in-android

//                        ((MainActivity) getActivity()).onBackPressed();
//                        ((MainActivity) getActivity()).onBack();
                                PersonManager.updatePerson();
                                savePersonInfoToCache();
//                                PersonManager.savePersonInfoToCache();
//                                startProfileFragment();
//                                startCreateUserPage();

                                NotificationSender.sendGCMRegService(getContext());

                            }

                            @Override
                            public void onFailure(Throwable t) {
                                LoginManager.getInstance().logOut();

                                setButtonsClickable(true);

                                Bundle bundle = new Bundle();
                                bundle.putString(GenericOKDialog.MESSAGE_KEY, getResources().getString(R.string.dialog_network_retry));

                                GenericOKDialog genericOKDialog = new GenericOKDialog();
                                genericOKDialog.setArguments(bundle);
                                genericOKDialog.show(getFragmentManager(), "Error");
                                progressLayout.setVisibility(View.GONE);
                            }
                        });
                    }

                    @Override
                    public void onCancel() {
                        Log.d("myTag", "oncancel login on fb.");
                        setButtonsClickable(true);
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d("myTag", "onError login on fb. " + error.getMessage());
                        Snackbar.make(v, getActivity().getString(R.string.profile_login_again), Snackbar.LENGTH_SHORT).show();
                        if (error instanceof FacebookAuthorizationException) {
                            if (AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }
                        }
                        setButtonsClickable(true);
                    }
                });
                break;
            case R.id.ok_signup:
                if (!emailSignUp.getText().toString().isEmpty()) {
                    setButtonsClickable(false);
                    progressLayout.setVisibility(View.VISIBLE);
                    apiConsumer.signUp(new SignInData(emailSignUp.getText().toString(), passwordSignUp.getText().toString()),new Callback<SignUpReturn>() {
                        @Override
                        public void onResponse(Response<SignUpReturn> response, Retrofit retrofit) {
                            Timber.d(response.isSuccess() + " " + response.body());

                            if (!response.isSuccess()) {
                                Toast toast = Toast.makeText(getActivity(), "This email address seems to be already taken. Please try another one or login using your existing account.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();
                                setButtonsClickable(true);
                                progressLayout.setVisibility(View.GONE);
                                return;
                            }
                            SignUpReturn signUpReturn = response.body();
                            if (signUpReturn==null) {
                                Toast toast = Toast.makeText(getActivity(), "Account successfully created.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();

                                login(emailSignUp.getText().toString(), passwordSignUp.getText().toString());
                                return;
                            }

                            Timber.d(response.toString());
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Toast toast = Toast.makeText(getActivity(), "This email address seems to be already taken. Please try another one or login using your existing account.", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER,0,0);
                            toast.show();
                            setButtonsClickable(true);
                            progressLayout.setVisibility(View.GONE);
                        }
                    });
                }
                break;
        }
    }
}
