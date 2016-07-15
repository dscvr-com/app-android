package com.iam360.iam360.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.iam360.iam360.gcm.GCMRegistrationIntentService;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.network.ApiConsumer;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Joven on 7/15/2016.
 */
public class NotificationSender {

    public static void triggerSendNotification(Optograph optograph, String type){
        ApiConsumer apiConsumer;
        Cache cache = Cache.open();;
        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        apiConsumer.triggerNotif(optograph.getPerson().getId(), cache.getString(Cache.USER_ID), type, new Callback<String>() {
            @Override
            public void onResponse(Response<String> response, Retrofit retrofit) {
                Log.d("MARK","triggerSendNotification sent success = "+type);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("MARK","triggerSendNotification sent failed = "+type);
            }
        });
    }

    public static void sendGCMRegService(Context context){
        //Checking play service is available or not
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        Log.d("MARK","sendGCMRegService resultCode = "+resultCode);
        Log.d("MARK","ConnectionResult.SUCCESS = "+ConnectionResult.SUCCESS);

        //if play service is not available
        if (ConnectionResult.SUCCESS != resultCode) {
            //If play service is supported but not installed
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                //Displaying message that play service is not installed
                Toast.makeText(context, "Google Play Service is not install/enabled in this device!", Toast.LENGTH_LONG).show();
                GooglePlayServicesUtil.showErrorNotification(resultCode,context);

                //If play service is not supported
                //Displaying an error message
            } else {
                Toast.makeText(context, "This device does not support for Google Play Service!", Toast.LENGTH_LONG).show();
            }

            //If play service is available
        } else {
            //Starting intent to register device
            Intent itent = new Intent(context, GCMRegistrationIntentService.class);
            context.startService(itent);
        }
    }

}
