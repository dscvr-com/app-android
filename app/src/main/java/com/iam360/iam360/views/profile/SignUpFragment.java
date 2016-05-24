package com.iam360.iam360.views.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.iam360.iam360.R;
import com.iam360.iam360.model.LogInReturn;
import com.iam360.iam360.model.SignInData;
import com.iam360.iam360.model.SignUpReturn;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.util.Cache;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Created by Mariel on 3/30/2016.
 */
public class SignUpFragment extends Fragment{

    private ApiConsumer apiConsumer;
    private Cache cache;
    private EditText emailSignUp;
    private EditText passwordSignUp;
    private Button okButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        apiConsumer = new ApiConsumer(null);
        cache = Cache.open();
        emailSignUp = (EditText) view.findViewById(R.id.email_signup);
        passwordSignUp = (EditText) view.findViewById(R.id.password_signup);
        okButton = (Button) view.findViewById(R.id.ok_signup);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!emailSignUp.getText().toString().isEmpty()) {
                    apiConsumer.signUp(new SignInData(emailSignUp.getText().toString(), passwordSignUp.getText().toString()),new Callback<SignUpReturn>() {
                        @Override
                        public void onResponse(Response<SignUpReturn> response, Retrofit retrofit) {
                            Timber.d(response.isSuccess() + " " + response.body());

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
                        }
                    });
                }
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

                getActivity().setResult(2);
                getActivity().finish();

            }

            @Override
            public void onFailure(Throwable t) {
                Toast toast = Toast.makeText(getActivity(), "Failed to log in.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }
        });
    }

}
