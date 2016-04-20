package co.optonaut.optonaut.views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import butterknife.Bind;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.FBSignInData;
import co.optonaut.optonaut.model.LogInReturn;
import co.optonaut.optonaut.model.SignInData;
import co.optonaut.optonaut.network.ApiConsumer;
import co.optonaut.optonaut.util.Cache;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class SigninFBFragment extends Fragment implements View.OnClickListener{

    private LoginButton fbButton;
    private TextView useExistingBtn;

    private ApiConsumer apiConsumer;
    private CallbackManager callbackManager;

    private Cache cache;

    public SigninFBFragment() {
    }

    public static SigninFBFragment newInstance(String param1, String param2) {
        SigninFBFragment fragment = new SigninFBFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
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

//        cache.save(Cache.USER_ID, "3a5dcf44-d3bf-42d7-ba84-20096e48e48c");
//        cache.save(Cache.USER_ID, "31448130-2fda-4f0b-a5ff-4fcfb834ba88");
//        cache.save(Cache.USER_TOKEN, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjNhNWRjZjQ0LWQzYmYtNDJkNy1iYTg0LTIwMDk2ZTQ4ZTQ4YyJ9.g3nQFDwnIc-QslKAqInz6SuTagtZ3BSwWfwY_1-zHCM");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signin_fb, container, false);

        useExistingBtn = (TextView) view.findViewById(R.id.use_existing_btn);
        useExistingBtn.setOnClickListener(this);

        fbButton = (LoginButton) view.findViewById(R.id.fb_button);
        fbButton.setReadPermissions("email");
        fbButton.setFragment(this);

        fbButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Timber.d("FB Result : " + loginResult.getAccessToken().getToken() + " " + loginResult.getAccessToken().getUserId());

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

                        getActivity().getSupportFragmentManager().beginTransaction().remove(getParentFragment()).commit();

                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Timber.d("Failure " + t.toString());
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

//        fbButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                // App code
//                Log.d(TAG, loginResult.getAccessToken().getToken() + " " + loginResult.getAccessToken().getUserId());
//
//                apiConsumer.fbLogIn(new FBSignInData(loginResult.getAccessToken().getUserId(), loginResult.getAccessToken().getToken()), new Callback<LogInReturn>() {
//                    @Override
//                    public void onResponse(Response<LogInReturn> response, Retrofit retrofit) {
//                        Log.d(TAG, "Response : " + response.toString());
//
//                        if (!response.isSuccess()) {
//                            Toast toast = Toast.makeText(getActivity(), "Failed to log in.", Toast.LENGTH_SHORT);
//                            toast.setGravity(Gravity.CENTER,0,0);
//                            toast.show();
//                            return;
//                        }
//                        LogInReturn login = response.body();
//                        if (login==null) {
//                            Toast toast = Toast.makeText(getActivity(), "Failed to log in.", Toast.LENGTH_SHORT);
//                            toast.setGravity(Gravity.CENTER,0,0);
//                            toast.show();
//                            return;
//                        }
//                        Log.d(TAG, "Login : " + login.getId() + " " + login.getToken());
//
//                        cache.save(Cache.USER_ID, login.getId());
//                        cache.save(Cache.USER_TOKEN, login.getToken());
//
//                    }
//
//                    @Override
//                    public void onFailure(Throwable t) {
//                        Log.d(TAG, "Failure " + t.toString());
//                    }
//                });
//
//            }
//
//            @Override
//            public void onCancel() {
//            }
//
//            @Override
//            public void onError(FacebookException exception) {
//                Log.d(TAG, exception.toString());
//            }
//        });



        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.use_existing_btn:
                Intent intent= new Intent(getActivity(), SignInActivity.class);
                getActivity().startActivity(intent);
                break;
            default:
                break;
        }
    }
}
