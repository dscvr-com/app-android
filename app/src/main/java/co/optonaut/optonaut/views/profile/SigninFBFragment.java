package co.optonaut.optonaut.views.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.w3c.dom.Text;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.FBSignInData;
import co.optonaut.optonaut.model.LogInReturn;
import co.optonaut.optonaut.model.SignInData;
import co.optonaut.optonaut.model.SignUpReturn;
import co.optonaut.optonaut.network.ApiConsumer;
import co.optonaut.optonaut.util.Cache;
import co.optonaut.optonaut.views.dialogs.GenericOKDialog;
import co.optonaut.optonaut.views.MainActivityRedesign;
import co.optonaut.optonaut.views.new_design.MainActivity;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class SigninFBFragment extends Fragment implements View.OnClickListener{

    private LoginButton fbButton;
    private TextView useExistingBtn;

    private EditText userNameText;
    private EditText passwordText;
    private ImageButton loginButton;
    private ImageButton registerButton;
    private TextView resetPasswordButton;

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

        fbButton = (LoginButton) view.findViewById(R.id.fb_button);
        fbButton.setReadPermissions("email");
        fbButton.setFragment(this);

        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);

        fbButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Timber.d("FB Result : " + loginResult.getAccessToken().getToken() + " " + loginResult.getAccessToken().getUserId());

                loginButton.setClickable(false);
                registerButton.setClickable(false);
                apiConsumer.fbLogIn(new FBSignInData(loginResult.getAccessToken().getUserId(), loginResult.getAccessToken().getToken()), new Callback<LogInReturn>() {
                    @Override
                    public void onResponse(Response<LogInReturn> response, Retrofit retrofit) {
                        Timber.d("Response : " + response.toString());

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
                        Timber.d("Login : " + login.getId() + " " + login.getToken());

                        cache.save(Cache.USER_ID, login.getId());
                        cache.save(Cache.USER_TOKEN, login.getToken());
                        cache.save(Cache.USER_FB_ID, loginResult.getAccessToken().getUserId());
                        cache.save(Cache.USER_FB_TOKEN, loginResult.getAccessToken().getToken());
                        cache.save(Cache.USER_FB_LOGGED_IN, true);

//                        ((MainActivity) getActivity()).onBackPressed();
                        ((MainActivity) getActivity()).onBack();

                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Timber.d("Failure " + t.toString());
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
                Timber.d("onCancel");
            }

            @Override
            public void onError(FacebookException e) {
                Timber.d("onError");
            }
        });

        return view;
    }


    private void login(String email, String password) {

        apiConsumer.logIn(new SignInData(email, password),new Callback<LogInReturn>() {
            @Override
            public void onResponse(Response<LogInReturn> response, Retrofit retrofit) {
                if (!response.isSuccess()) {
                    Toast toast = Toast.makeText(getActivity(), "Failed to log in.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                    return;
                }
                LogInReturn login = response.body();
                if (login==null) {
                    Toast toast = Toast.makeText(getActivity(), "Failed to log in.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                    return;
                }

                Timber.d(response.toString());

                cache.save(Cache.USER_ID, login.getId());
                cache.save(Cache.USER_TOKEN, login.getToken());

//                getActivity().setResult(2);
//                getActivity().finish();
                Log.d("myTag","success login. id: "+cache.getString(Cache.USER_ID)+" token: "+cache.getString(Cache.USER_TOKEN));
//                ((MainActivity)getActivity()).onBackPressed();
                ((MainActivity)getActivity()).onBack();
//                ((MainActivity)getActivity()).setPage(MainActivity.PROFILE_MODE);
            }

            @Override
            public void onFailure(Throwable t) {
                Toast toast = Toast.makeText(getActivity(), "Failed to log in.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == signInRequestCode)
            if(resultCode == Activity.RESULT_OK) loggedIn = true;

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(loggedIn) ((MainActivityRedesign) getActivity()).prepareProfile(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.use_existing_btn:
                Intent intent= new Intent(getActivity(), SignInActivity.class);
                startActivityForResult(intent, signInRequestCode);
                break;*/
            case R.id.login_button:
                Log.d("myTag","login clicked.");
                registerButton.setClickable(false);
                fbButton.setClickable(false);
                login(userNameText.getText().toString(), passwordText.getText().toString());
                break;
            case R.id.register_button:
                Log.d("myTag","register clicked.");
                if (!userNameText.getText().toString().isEmpty()) {
                    loginButton.setClickable(false);
                    fbButton.setClickable(false);
                    apiConsumer.signUp(new SignInData(userNameText.getText().toString(), passwordText.getText().toString()),new Callback<SignUpReturn>() {
                        @Override
                        public void onResponse(Response<SignUpReturn> response, Retrofit retrofit) {
                            if (!response.isSuccess()) {
                                Toast toast = Toast.makeText(getActivity(), "This email address seems to be already taken. Please try another one or login using your existing account.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();
                                return;
                            }
                            SignUpReturn signUpReturn = response.body();
                            if (signUpReturn==null) {
                                Toast toast = Toast.makeText(getActivity(), "Account successfully created.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();

                                Log.d("myTag","success sign up.");
                                login(userNameText.getText().toString(), passwordText.getText().toString());
                                return;
                            }

                            Timber.d(response.toString());
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Toast toast = Toast.makeText(getActivity(), "This email address seems to be already taken. Please try another one or login using your existing account.", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER,0,0);
                            toast.show();
                        }
                    });
                }
                break;
            default:
                break;
        }
    }
}
