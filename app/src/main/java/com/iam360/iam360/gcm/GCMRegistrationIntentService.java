package com.iam360.iam360.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.iam360.iam360.R;
import com.iam360.iam360.model.GCMToken;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.util.Cache;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Joven on 7/11/2016.
 */
public class GCMRegistrationIntentService extends IntentService {
    public static final String REGISTRATION_SUCCESS = "RegistrationSuccess";
    public static final String REGISTRATION_ERROR = "RegistrationError";
    public static final String REGISTRATION_TOKEN_SENT = "RegistrationTokenSent";

    public GCMRegistrationIntentService() {
        super("");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w("GCMRegIntentService", "GCMRegistrationIntentService intent:" + intent);
        registerGCM();
    }

    private void registerGCM() {
        Intent registrationComplete = null;
        String token = null;
        try {
            InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.w("GCMRegIntentService", "token:" + token);

            sendRegistrationTokenToServer(token);
            registrationComplete = new Intent(REGISTRATION_SUCCESS);
            registrationComplete.putExtra("token", token);
        } catch (Exception e) {
            Log.w("GCMRegIntentService", "Registration error");
            registrationComplete = new Intent(REGISTRATION_ERROR);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void sendRegistrationTokenToServer(final String token) {
        Cache cache = Cache.open();
        String userToken = cache.getString(Cache.USER_TOKEN);
        ApiConsumer apiConsumer = new ApiConsumer(userToken);
        cache.save(Cache.GCM_TOKEN, token);
        apiConsumer.gcmTokenToServer(new GCMToken(token), new Callback<String>(){

            @Override
            public void onResponse(Response<String> response, Retrofit retrofit) {
                Log.d("MARK","sendRegistrationTokenToServer response.isSuccess() = "+response.isSuccess());
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("MARK","sendRegistrationTokenToServer failed = "+t.getMessage());
            }
        });
    }
}
