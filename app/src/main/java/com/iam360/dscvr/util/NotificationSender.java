package com.iam360.dscvr.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.iam360.dscvr.gcm.GCMRegistrationIntentService;
import com.iam360.dscvr.model.NotificationTriggerData;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.Person;
import com.iam360.dscvr.network.Api2Consumer;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Joven on 7/15/2016.
 */
public class NotificationSender {

    public static void triggerSendNotification(Optograph optograph, String type, String optoId){
        Api2Consumer apiConsumer;
        Cache cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new Api2Consumer(token.equals("") ? null : token, "triggerNotif");
        Log.d("MARK","triggerSendNotification type = "+type+" optoId = "+optoId+" owner_id = "+optograph.getPerson().getId());
        Log.d("MARK","triggerSendNotification follower_id = "+cache.getString(Cache.USER_ID));

        NotificationTriggerData data = new NotificationTriggerData(optograph.getPerson().getId(), cache.getString(Cache.USER_ID), optoId, type);
        apiConsumer.triggerNotif(data, new Callback<String>() {
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

    public static void triggerSendNotification(Person person, String type){
        Api2Consumer apiConsumer;
        Cache cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new Api2Consumer(token.equals("") ? null : token, "triggerNotif");
        Log.d("MARK","triggerSendNotification type = "+type+" owner_id = "+person.getId());
        Log.d("MARK","triggerSendNotification follower_id = "+cache.getString(Cache.USER_ID));
        NotificationTriggerData data = new NotificationTriggerData(person.getId(), cache.getString(Cache.USER_ID), "", type);
        apiConsumer.triggerNotif(data, new Callback<String>() {
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
//                Toast.makeText(context, "Google Play Service is not install/enabled in this device!", Toast.LENGTH_LONG).show();
                Log.d("MARK","Google Play Service is not install/enabled in this device!");
                GooglePlayServicesUtil.showErrorNotification(resultCode,context);

                //If play service is not supported
                //Displaying an error message
            } else {
                Log.d("MARK","This device does not support for Google Play Service!");
//                Toast.makeText(context, "This device does not support for Google Play Service!", Toast.LENGTH_LONG).show();
            }

            //If play service is available
        } else {
            //Starting intent to register device
            Intent itent = new Intent(context, GCMRegistrationIntentService.class);
            context.startService(itent);
        }
    }
}
