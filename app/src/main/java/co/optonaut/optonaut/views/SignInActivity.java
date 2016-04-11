package co.optonaut.optonaut.views;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.LogInReturn;
import co.optonaut.optonaut.model.SignUpReturn;
import co.optonaut.optonaut.network.ApiConsumer;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Mariel on 3/30/2016.
 */
public class SignInActivity extends AppCompatActivity {

    private EditText emailSignUp;
    private EditText passwordSignUp;
    private EditText emailLogIn;
    private EditText passwordLogIn;
    private Button okButton;

    protected ApiConsumer apiConsumer;

    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiConsumer = new ApiConsumer(null);
        setContentView(R.layout.activity_sign_in_temporary);
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

        emailSignUp = (EditText) findViewById(R.id.email_signup);
        passwordSignUp = (EditText) findViewById(R.id.password_signup);
        emailLogIn = (EditText) findViewById(R.id.email_login);
        passwordLogIn = (EditText) findViewById(R.id.password_login);
        okButton = (Button) findViewById(R.id.signin_ok);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!emailSignUp.getText().toString().isEmpty()) {
                    apiConsumer.signUp(new SignInData(emailSignUp.getText().toString(), passwordSignUp.getText().toString()),new Callback<SignUpReturn>() {
                        @Override
                        public void onResponse(Response<SignUpReturn> response, Retrofit retrofit) {
                            Log.d("myTag", "response on Sign up: " + response.toString());
                            Log.d("myTag", "response message: " + response.message());
                            Log.d("myTag", "response body: " + response.body());
                            Log.d("myTag", "response raw: " + response.raw().toString());
                            Log.d("myTag", "sign up isSuccess? " + response.isSuccess());
                            if (!response.isSuccess()) {
                                Log.d("myTag", "response errorBody: " + response.errorBody());
                                Toast toast = Toast.makeText(SignInActivity.this, "This email address seems to be already taken. Please try another one or login using your existing account.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();
                                return;
                            }
                            SignUpReturn signUpReturn = response.body();
                            if (signUpReturn==null) {
                                Log.d("myTag","parsing the JSON body failed.");
                                Toast toast = Toast.makeText(SignInActivity.this, "This email address seems to be already taken. Please try another one or login using your existing account.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();
                                return;
                            }
                            Log.d("myTag","id: "+signUpReturn.getMessage());
                            flag = true;
                            Log.d("myTag","sign up successful.");
                            finish();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Log.d("myTag","failed Signing up "+t.getMessage());
                            Toast toast = Toast.makeText(SignInActivity.this, "This email address seems to be already taken. Please try another one or login using your existing account.", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER,0,0);
                            toast.show();
                        }
                    });
                } else {
                    apiConsumer.logIn(new SignInData(emailLogIn.getText().toString(), passwordLogIn.getText().toString()),new Callback<LogInReturn>() {
                        @Override
                        public void onResponse(Response<LogInReturn> response, Retrofit retrofit) {
                            Log.d("myTag", "response on logIn: " + response.toString());
                            Log.d("myTag", "response message: " + response.message());
                            Log.d("myTag", "response body: " + response.body());
                            Log.d("myTag", "response raw: " + response.raw().toString());
                            Log.d("myTag", "log in isSuccess? " + response.isSuccess());
                            if (!response.isSuccess()) {
                                Log.d("myTag", "response errorBody: " + response.errorBody());
                                Toast toast = Toast.makeText(SignInActivity.this, "Failed to log in.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();
                                return;
                            }
                            LogInReturn login = response.body();
                            if (login==null) {
                                Log.d("myTag","parsing the JSON body failed.");
                                Toast toast = Toast.makeText(SignInActivity.this, "Failed to log in.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();
                                return;
                            }
                            Log.d("myTag","id: "+login.getId()+" token: "+login.getToken()+" oBV: "+login.getOnBoardingVersion());
                            flag = true;
                            Log.d("myTag","login successful.");
                            finish();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Log.d("myTag","failed Logging in "+t.getMessage());
                            Toast toast = Toast.makeText(SignInActivity.this, "Failed to log in.", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER,0,0);
                            toast.show();
                        }
                    });
                }
            }
        });
    }

    public class SignInData {
        final String email;
        final String password;

        SignInData(String email, String password) {
            this.email = email;
            this.password = password;
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
