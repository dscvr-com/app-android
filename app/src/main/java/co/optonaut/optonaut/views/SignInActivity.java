package co.optonaut.optonaut.views;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import butterknife.Bind;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.LogInReturn;
import co.optonaut.optonaut.model.SignUpReturn;
import co.optonaut.optonaut.network.ApiConsumer;
import co.optonaut.optonaut.util.Cache;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Mariel on 3/30/2016.
 */
public class SignInActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = "SignInActivityPage";
    private EditText emailSignUp;
    private EditText passwordSignUp;
    private EditText emailLogIn;
    private EditText passwordLogIn;
    private Button okButton;
    private LoginButton fbButton;

    protected ApiConsumer apiConsumer;
    CallbackManager callbackManager;

    private Cache cache;

    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiConsumer = new ApiConsumer(null);
        setContentView(R.layout.activity_sign_in_temporary);

        cache = Cache.open();

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        emailSignUp = (EditText) findViewById(R.id.email_signup);
        passwordSignUp = (EditText) findViewById(R.id.password_signup);
        emailLogIn = (EditText) findViewById(R.id.email_login);
        passwordLogIn = (EditText) findViewById(R.id.password_login);
        okButton = (Button) findViewById(R.id.signin_ok);
        fbButton = (LoginButton) findViewById(R.id.fb_button);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs_signin);
        tabLayout.addTab(tabLayout.newTab().setText("Sign Up"));
        tabLayout.addTab(tabLayout.newTab().setText("Log In"));

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager_signin);
        final SignInPagerAdapter adapter = new SignInPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });*/

        fbButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.d(TAG, loginResult.getAccessToken().getToken() + " " + loginResult.getAccessToken().getUserId());

                apiConsumer.fbLogIn(new FBSignInData(loginResult.getAccessToken().getUserId(), loginResult.getAccessToken().getToken()), new Callback<LogInReturn>() {
                    @Override
                    public void onResponse(Response<LogInReturn> response, Retrofit retrofit) {
                        Log.d(TAG, "Response : " + response.toString());

                        if (!response.isSuccess()) {
                            Toast toast = Toast.makeText(SignInActivity.this, "Failed to log in.", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER,0,0);
                            toast.show();
                            return;
                        }
                        LogInReturn login = response.body();
                        if (login==null) {
                            Toast toast = Toast.makeText(SignInActivity.this, "Failed to log in.", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER,0,0);
                            toast.show();
                            return;
                        }
                        Log.d(TAG, "Login : " + login.getId() + " " + login.getToken());

                        cache.save(Cache.USER_ID, login.getId());
                        cache.save(Cache.USER_TOKEN, login.getToken());

                        flag = true;
                        finish();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.d(TAG, "Failure " + t.toString());
                    }
                });

            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signin_ok:

                if (!emailSignUp.getText().toString().isEmpty()) {
                    apiConsumer.signUp(new SignInData(emailSignUp.getText().toString(), passwordSignUp.getText().toString()),new Callback<SignUpReturn>() {
                        @Override
                        public void onResponse(Response<SignUpReturn> response, Retrofit retrofit) {
                            if (!response.isSuccess()) {
                                Toast toast = Toast.makeText(SignInActivity.this, "This email address seems to be already taken. Please try another one or login using your existing account.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();
                                return;
                            }
                            SignUpReturn signUpReturn = response.body();
                            if (signUpReturn==null) {
                                Toast toast = Toast.makeText(SignInActivity.this, "This email address seems to be already taken. Please try another one or login using your existing account.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();
                                return;
                            }
                            flag = true;
                            finish();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Toast toast = Toast.makeText(SignInActivity.this, "This email address seems to be already taken. Please try another one or login using your existing account.", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER,0,0);
                            toast.show();
                        }
                    });
                } else {
                    apiConsumer.logIn(new SignInData(emailLogIn.getText().toString(), passwordLogIn.getText().toString()),new Callback<LogInReturn>() {
                        @Override
                        public void onResponse(Response<LogInReturn> response, Retrofit retrofit) {
                            if (!response.isSuccess()) {
                                Toast toast = Toast.makeText(SignInActivity.this, "Failed to log in.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();
                                return;
                            }
                            LogInReturn login = response.body();
                            if (login==null) {
                                Toast toast = Toast.makeText(SignInActivity.this, "Failed to log in.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();
                                return;
                            }

                            cache.save(Cache.USER_ID, login.getId());
                            cache.save(Cache.USER_TOKEN, login.getToken());

                            flag = true;
                            finish();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Toast toast = Toast.makeText(SignInActivity.this, "Failed to log in.", Toast.LENGTH_SHORT);
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

    public class SignInData {
        final String email;
        final String password;

        SignInData(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public class FBSignInData {
        final String facebook_user_id;
        final String facebook_token;

        FBSignInData(String facebook_user_id, String facebook_token) {
            this.facebook_user_id = facebook_user_id;
            this.facebook_token = facebook_token;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.signin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
