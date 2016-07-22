package com.iam360.iam360.views.profile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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

import java.util.Arrays;

import com.iam360.iam360.R;
import com.iam360.iam360.model.FBSignInData;
import com.iam360.iam360.model.LogInReturn;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.model.SignInData;
import com.iam360.iam360.model.SignUpReturn;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.network.PersonManager;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.NotificationSender;
import com.iam360.iam360.views.MainActivityRedesign;
import com.iam360.iam360.views.dialogs.GenericOKDialog;
import com.iam360.iam360.views.new_design.CreateUsernameActivity;
import com.iam360.iam360.views.new_design.MainActivity;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class SigninFBFragment extends Fragment implements View.OnClickListener {

    private ImageButton fbButton;
    private TextView useExistingBtn;

    private EditText userNameText;
    private EditText passwordText;
    private ImageButton loginButton;
    private ImageButton registerButton;
    private TextView resetPasswordButton;
    private ProgressBar progressBar;

    private ApiConsumer apiConsumer;
    private CallbackManager callbackManager;

    private int signInRequestCode = 2;
    private boolean loggedIn = false;

    private Cache cache;

    public SigninFBFragment() {
    }

    public static SigninFBFragment newInstance(String param1, String param2) {
        SigninFBFragment fragment = new SigninFBFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        cache = Cache.open();

        apiConsumer = new ApiConsumer(null);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signin_fb, container, false);

//        useExistingBtn = (TextView) view.findViewById(R.id.use_existing_btn);
//        useExistingBtn.setOnClickListener(this);
        userNameText = (EditText) view.findViewById(R.id.username_edit);
        passwordText = (EditText) view.findViewById(R.id.password_edit);
        loginButton = (ImageButton) view.findViewById(R.id.login_button);
        registerButton = (ImageButton) view.findViewById(R.id.register_button);
        resetPasswordButton = (TextView) view.findViewById(R.id.reset_password);
        fbButton = (ImageButton) view.findViewById(R.id.fb_button);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        fbButton.setOnClickListener(this);
        return view;
    }


    private void login(String email, String password) {

        apiConsumer.logIn(new SignInData(email, password), new Callback<LogInReturn>() {
            @Override
            public void onResponse(Response<LogInReturn> response, Retrofit retrofit) {
                if (!response.isSuccess()) {
                    Toast toast = Toast.makeText(getActivity(), "Failed to log in.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    setButtonsClickable(true);
                    return;
                }
                LogInReturn login = response.body();
                if (login == null) {
                    Toast toast = Toast.makeText(getActivity(), "Failed to log in.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    setButtonsClickable(true);
                    return;
                }

                Timber.d(response.toString());

                cache.save(Cache.USER_ID, login.getId());
                cache.save(Cache.USER_TOKEN, login.getToken());

                Log.d("myTag", "success login. id: " + cache.getString(Cache.USER_ID) + " token: " + cache.getString(Cache.USER_TOKEN));
//                startProfileFragment();
                startCreateUserPage();
            }

            @Override
            public void onFailure(Throwable t) {
                Toast toast = Toast.makeText(getActivity(), "Failed to log in.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                setButtonsClickable(true);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == signInRequestCode)
            if (resultCode == Activity.RESULT_OK) loggedIn = true;

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (loggedIn) ((MainActivityRedesign) getActivity()).prepareProfile(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.use_existing_btn:
                Intent intent= new Intent(getActivity(), SignInActivity.class);
                startActivityForResult(intent, signInRequestCode);
                break;*/
            case R.id.login_button:
                Log.d("myTag", "login clicked.");
                setButtonsClickable(false);
                login(userNameText.getText().toString(), passwordText.getText().toString());
                break;
            case R.id.register_button:
                Log.d("myTag", "register clicked.");
                if (!userNameText.getText().toString().isEmpty()) {
                    setButtonsClickable(false);
                    apiConsumer.signUp(new SignInData(userNameText.getText().toString(), passwordText.getText().toString()), new Callback<SignUpReturn>() {
                        @Override
                        public void onResponse(Response<SignUpReturn> response, Retrofit retrofit) {
                            if (!response.isSuccess()) {
                                Toast toast = Toast.makeText(getActivity(), "This email address seems to be already taken. Please try another one or login using your existing account.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                setButtonsClickable(true);
                                return;
                            }
                            SignUpReturn signUpReturn = response.body();
                            if (signUpReturn == null) {
                                Toast toast = Toast.makeText(getActivity(), "Account successfully created.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                                Log.d("myTag", "success sign up.");
                                login(userNameText.getText().toString(), passwordText.getText().toString());
                                return;
                            }

                            Timber.d(response.toString());
                            setButtonsClickable(true);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Toast toast = Toast.makeText(getActivity(), "This email address seems to be already taken. Please try another one or login using your existing account.", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            setButtonsClickable(true);
                        }
                    });
                }
                break;
            case R.id.fb_button:
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email","public_profile","user_friends"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("myTag", "success login on fb: " + loginResult.getAccessToken().getUserId()+" token: "+loginResult.getAccessToken().getToken());

                        progressBar.setVisibility(View.VISIBLE);
                        apiConsumer.fbLogIn(new FBSignInData(loginResult.getAccessToken().getUserId(), loginResult.getAccessToken().getToken()), new Callback<LogInReturn>() {
                            @Override
                            public void onResponse(Response<LogInReturn> response, Retrofit retrofit) {

                                if (!response.isSuccess()) {
                                    Toast toast = Toast.makeText(getActivity(), "Failed to log in.", Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                    setButtonsClickable(true);
                                    return;
                                }
                                LogInReturn login = response.body();
                                if (login == null) {
                                    Toast toast = Toast.makeText(getActivity(), "Failed to log in.", Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                    setButtonsClickable(true);
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
                        Snackbar.make(v, getActivity().getString(R.string.profile_login_again), Snackbar.LENGTH_SHORT).show();
                        if (error instanceof FacebookAuthorizationException) {
                            if (AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

    public void savePersonInfoToCache() {
        Timber.d("savePersonInfoToCache");

        String token = cache.getString(Cache.USER_TOKEN);
        ApiConsumer apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        apiConsumer.getUser(new Callback<Person>() {
            @Override
            public void onResponse(Response<Person> response, Retrofit retrofit) {
                Person person = response.body();
                cache.save(Cache.USER_EMAIL, person.getEmail());
                cache.save(Cache.USER_NAME, person.getUser_name());
                Timber.d("User email : " + person.getEmail());

                progressBar.setVisibility(View.GONE);
                startCreateUserPage();
            }

            @Override
            public void onFailure(Throwable t) {
                progressBar.setVisibility(View.GONE);
                Timber.d("Failed to load person!");
            }
        });
    }

    private void setButtonsClickable(boolean clickable) {
        registerButton.setClickable(clickable);
        loginButton.setClickable(clickable);
        fbButton.setClickable(clickable);
    }

    private void startProfileFragment() {

        if(getContext() instanceof MainActivity) {
            getActivity().finish();
            startActivity(getActivity().getIntent());
        } else {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void startCreateUserPage() {

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

}
