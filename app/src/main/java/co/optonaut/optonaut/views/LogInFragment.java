package co.optonaut.optonaut.views;

import android.app.Activity;
import android.content.Intent;
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

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.LogInReturn;
import co.optonaut.optonaut.model.SignInData;
import co.optonaut.optonaut.network.ApiConsumer;
import co.optonaut.optonaut.util.Cache;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Created by Mariel on 3/30/2016.
 */
public class LogInFragment extends Fragment{

    private EditText emailLogIn;
    private EditText passwordLogIn;
    private Button okButton;

    private ApiConsumer apiConsumer;
    private Cache cache;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_log_in, container, false);
        apiConsumer = new ApiConsumer(null);
        cache = Cache.open();

        emailLogIn = (EditText) view.findViewById(R.id.email_login);
        passwordLogIn = (EditText) view.findViewById(R.id.password_login);
        okButton = (Button) view.findViewById(R.id.ok_login);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                apiConsumer.logIn(new SignInData(emailLogIn.getText().toString(), passwordLogIn.getText().toString()),new Callback<LogInReturn>() {
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

                        Intent returnIntent = new Intent();
                        getActivity().setResult(Activity.RESULT_OK, returnIntent);
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
        });

        return view;

    }
}
