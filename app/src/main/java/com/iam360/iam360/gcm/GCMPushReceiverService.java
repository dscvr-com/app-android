package com.iam360.iam360.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;
import com.iam360.iam360.R;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.views.new_design.MainActivity;
import com.iam360.iam360.views.new_design.OptographDetailsActivity;
import com.iam360.iam360.views.new_design.ProfileActivity;

/**
 * Created by Joven on 7/11/2016.
 */
public class GCMPushReceiverService extends GcmListenerService {

    //This method will be called on every new message received
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String title = data.getString("title");
        sendNotification(message, title);
    }

    private void sendNotification(String message, String title) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        builder.setSmallIcon(R.mipmap.ic_launcher);
        Intent intent = new Intent(this, MainActivity.class);
        if(true){
            intent = new Intent(this, OptographDetailsActivity.class);
            Optograph optograph = null;
            intent.putExtra("opto", optograph);
        }else{
            intent = new Intent(this, ProfileActivity.class);
            Person person = null;
            intent.putExtra("person", person);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle(title);
        builder.setContentText(message);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());

        getPersons();
    }

    private void getPersons(){
        String json = "{\n" +
                "\t\t\"id\": \"75cbd3f9-f7b2-4eb9-924d-d5191f70c633\",\n" +
                "\t\t\"created_at\": \"2016-07-05T11:35:51.577454Z\",\n" +
                "\t\t\"updated_at\": \"0001-01-01T00:00:00Z\",\n" +
                "\t\t\"deleted_at\": null,\n" +
                "\t\t\"wants_newsletter\": false,\n" +
                "\t\t\"display_name\": \"Richard\",\n" +
                "\t\t\"user_name\": \"6ecd477c4651430292f9bf0aeedc599f\",\n" +
                "\t\t\"text\": \"\",\n" +
                "\t\t\"avatar_asset_id\": \"7ad93132-87a7-4e79-9fb8-b7ea7622bf91\",\n" +
                "\t\t\"optographs\": null,\n" +
                "\t\t\"optographs_count\": 0,\n" +
                "\t\t\"followers_count\": 2,\n" +
                "\t\t\"followed_count\": 0,\n" +
                "\t\t\"is_followed\": false\n" +
                "\t}";
        Gson gson = new Gson();
        Persons data = gson.fromJson(json, Persons.class);
        Log.d("MARK","data id = "+data.id);
    }

    private class Persons{
        private String id;
        private String created_at;
        private String deleted_at;
        private String display_name;
        private String user_name;
        private String email;
        private String text;
        private String avatar_asset_id;
        private int optographs_count;
        private int followers_count;
        private int followed_count;
        private boolean is_followed;
        private String facebook_user_id;
        private String facebook_token;
        private String twitter_token;
        private String twitter_secret;
    }
}