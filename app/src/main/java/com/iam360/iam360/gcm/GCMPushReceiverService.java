package com.iam360.iam360.gcm;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;
import com.iam360.iam360.R;
import com.iam360.iam360.model.Location;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.views.new_design.OptographDetailsActivity;
import com.iam360.iam360.views.new_design.ProfileActivity;
import com.iam360.iam360.views.new_design.SplashActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;
import timber.log.Timber;

/**
 * Created by Joven on 7/11/2016.
 */


/*
{status=200,
data_type=person,
data={"person_followed_count":"2",
"person_invite_activation_at":null,
"person_followers_count":"0",
"person_display_name":"sandra",
"person_text":"",
"person_invite_activation_activated":"f",
"person_optographs_count":"0",
"person_invite_activation_id":null,
"person_avatar_asset_id":"b53d6412-28b0-4209-9f3b-d44a4be1f1f6",
"person_user_name":"sandra",
"person_id":"34d68d96-0d32-4d54-8c8f-a7754bde5762",
"person_created_at":"2016-07-25T13:41:10.495429Z"},
title=DSCVR, message=sandra followed you on DSCVR., collapse_key=updated_state}
 */

public class GCMPushReceiverService extends GcmListenerService {

    private boolean isAppRunning = false;
    private int badgeCount;

    //This method will be called on every new message received
    @Override
    public void onMessageReceived(String from, Bundle data) {

        isAppRunning = checkIfAppRunning();
        addBadgeCount();
        Log.d("MARK","onMessageReceived BundleData = "+data.toString());
        String message = data.getString("message");
        String title = data.getString("title");
        String type = data.getString("data_type");
        String json = data.getString("data");
        sendNotification(message, title, type, json);
    }

    private void sendNotification(String message, String title, String type, String json) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.logo_mini_icn);
        Intent intent;
        String avatarId;
        String userId;
        if(type.equals("optograph")){
//            intent = new Intent(this, SplashActivity.class);
            if(isAppRunning){
                intent = new Intent(this, OptographDetailsActivity.class);
            }else{
                intent = new Intent(this, SplashActivity.class);
            }
            Optograph optograph = getOpto(json);
            userId = optograph.getPerson().getId();
            avatarId = optograph.getPerson().getAvatar_asset_id();
            intent.putExtra("opto", optograph);
            intent.putExtra("notif", true);
            intent.removeExtra("person");
        }else{
//            intent = new Intent(this, SplashActivity.class);
            if(isAppRunning){
                intent = new Intent(this, ProfileActivity.class);
            }else{
                intent = new Intent(this, SplashActivity.class);
            }
            Person person = getPersons(json);
            userId = person.getId();
            avatarId = person.getAvatar_asset_id();
            intent.putExtra("notif", true);
            intent.putExtra("person", person);
            intent.removeExtra("opto");
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Bitmap bitmap = getBitmapFromURL("https://bucket.dscvr.com/persons/"+userId+"/"+avatarId+".jpg");
//        Bitmap bitmap = getBitmapFromURL("https://bucket.dscvr.com/persons/c0d5cb2b-7f8a-4de9-a5de-6f7c6cf1cf1a/f6a348f5-53bf-42d0-904a-420e081a5872.jpg");
//        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setLargeIcon(bitmap);
        builder.setContentTitle(title);
        builder.setContentText(message);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        builder.setAutoCancel(true);
        builder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(badgeCount, builder.build());
        Timber.d("MARK notify : " + badgeCount);
    }

    private void addBadgeCount() {
        Cache cache;

        if(!isAppRunning) Cache.getInstance(this);
        cache = Cache.open();
        badgeCount = cache.getInt(Cache.NOTIF_COUNT);
        ShortcutBadger.applyCount(getApplicationContext(), ++badgeCount);
        cache.save(Cache.NOTIF_COUNT, badgeCount);
    }

    public boolean checkIfAppRunning(){
        Context appContext = getBaseContext();
        ActivityManager activityManager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> services = activityManager
                .getRunningTasks(Integer.MAX_VALUE);
        boolean isActivityFound = false;

        if (services.get(0).topActivity.getPackageName().toString()
                .equalsIgnoreCase(appContext.getPackageName().toString())) {
            isActivityFound = true;
        }

        if (isActivityFound) {
            Log.d("MARK","App Alive");
            return true;
        } else {
            Log.d("MARK","App Dead");
            return false;
        }
    }


    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("MARK","getBitmapFromURL error = "+e);
            return BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        }
    }

    private Person getPersons(String json){
//        json = "{\n" +
//                "               \"person_id\": \"8230b4e8-8478-4b5b-a20e-9efbacff5516\",\n" +
//                "               \"person_created_at\": \"2016-04-25 13:45:45.416436+00\",\n" +
//                "               \"person_display_name\": \"sakuraLi\",\n" +
//                "               \"person_user_name\": \"mariela\",\n" +
//                "               \"person_text\": \"i :heart: anime\",\n" +
//                "               \"person_avatar_asset_id\": \"8a46c4d3-6d3a-4f4e-aa76-05216469202c\",\n" +
//                "               \"person_invite_activation_id\": null,\n" +
//                "               \"person_invite_activation_at\": null,\n" +
//                "               \"person_invite_activation_activated\": \"f\",\n" +
//                "               \"person_optographs_count\": \"118\",\n" +
//                "               \"person_followers_count\": \"2\",\n" +
//                "               \"person_followed_count\": \"5\"\n" +
//                "           }";
        Gson gson = new Gson();
        Persons data = gson.fromJson(json, Persons.class);
        Person person = new Person();
        person.setId(data.person_id);
        person.setAvatar_asset_id(data.person_avatar_asset_id);
        person.setCreated_at(data.person_created_at);
        person.setDisplay_name(data.person_display_name);
        person.setUser_name(data.person_user_name);
        person.setText(data.person_text);
        person.setOptographs_count(data.person_optographs_count);
        person.setFollowers_count(data.person_followers_count);
        person.setFollowed_count(data.person_followed_count);

        Log.d("MARK","data id = "+data.person_id);
        return person;
    }

    private Optograph getOpto(String json){
//        json ="{\n" +
//                "               \"optograph_id\": \"4bba6d92-37d9-40a1-826f-077b0dc6cb6c\",\n" +
//                "               \"optograph_created_at\": \"2016-07-18 08:52:44.019+00\",\n" +
//                "               \"optograph_updated_at\": \"2016-07-18 08:53:06.97308+00\",\n" +
//                "               \"optograph_deleted_at\": null,\n" +
//                "               \"optograph_text\": \"\",\n" +
//                "               \"optograph_views_count\": \"0\",\n" +
//                "               \"optograph_left_texture_asset_id\": \"00000000-0000-0000-0000-000000000000\",\n" +
//                "               \"optograph_right_texture_asset_id\": \"00000000-0000-0000-0000-000000000000\",\n" +
//                "               \"optograph_is_private\": \"f\",\n" +
//                "               \"optograph_is_staff_pick\": \"f\",\n" +
//                "               \"optograph_is_published\": \"t\",\n" +
//                "               \"optograph_share_alias\": \"fflqnt\",\n" +
//                "               \"optograph_stitcher_version\": \"0.7.0\",\n" +
//                "               \"optograph_direction_phi\": \"3.33891\",\n" +
//                "               \"optograph_direction_theta\": \"-1.5708\",\n" +
//                "               \"optograph_type\": \"optograph_1\",\n" +
//                "               \"optograph_platform\": \"iOS 9.3.2\",\n" +
//                "               \"optograph_model\": \"iPhone 6s\",\n" +
//                "               \"optograph_make\": \"Apple\",\n" +
//                "               \"optograph_stars_count\": \"0\",\n" +
//                "               \"optograph_comments_count\": \"0\",\n" +
//                "               \"optograph_hashtag_string\": \"\",\n" +
//                "               \"person_id\": \"ee09dd7f-ada4-4446-81a4-66d3a85ed3b7\",\n" +
//                "               \"person_created_at\": \"2015-10-20 10:25:03.621051+00\",\n" +
//                "               \"person_display_name\": \"lijoseph\",\n" +
//                "               \"person_user_name\": \"lijoseph\",\n" +
//                "               \"person_text\": \"\",\n" +
//                "               \"person_avatar_asset_id\": \"409774a0-e33a-4f95-9ae0-e24faa8065b0\",\n" +
//                "               \"person_elite_status\": \"t\",\n" +
//                "               \"person_followers_count\": \"2\",\n" +
//                "               \"person_followed_count\": \"3\",\n" +
//                "               \"location_id\": \"00000000-0000-0000-0000-000000000000\",\n" +
//                "               \"location_created_at\": \"2015-11-25 21:03:00.694582+00\",\n" +
//                "               \"location_text\": \"Not available\",\n" +
//                "               \"location_country\": \"Not available\",\n" +
//                "               \"location_country_short\": \"\",\n" +
//                "               \"location_place\": \"\",\n" +
//                "               \"location_region\": \"\",\n" +
//                "               \"location_poi\": \"t\",\n" +
//                "               \"location_latitude\": \"0\",\n" +
//                "               \"location_longitude\": \"0\"\n" +
//                "           }";
        Gson gson = new Gson();
        Optographs data = gson.fromJson(json, Optographs.class);
        Optograph opto = new Optograph(data.optograph_id);
        opto.setCreated_at(data.optograph_created_at);
        opto.setDeleted_at(data.optograph_deleted_at);
        opto.setStitcher_version(data.optograph_stitcher_version);
        opto.setText(data.optograph_text);
        opto.setViews_count(data.optograph_views_count);
        opto.setIs_staff_picked(data.optograph_is_staff_pick);
        opto.setShare_alias(data.optograph_share_alias);
        opto.setIs_private(data.optograph_is_private);
        opto.setIs_published(data.optograph_is_published);
        opto.setLeft_texture_asset_id(data.optograph_left_texture_asset_id);
        opto.setRight_texture_asset_id(data.optograph_right_texture_asset_id);
        opto.setIs_local(false);
        Location location = new Location();
        location.setId(data.location_id);
        location.setCreated_at(data.location_created_at);
        location.setText(data.location_text);
        location.setCountry(data.location_country);
        location.setCountry_short(data.location_country_short);
        location.setPlace(data.location_place);
        location.setRegion(data.location_region);
        location.setPoi(data.location_poi);
        location.setLatitude(data.location_latitude);
        location.setLongitude(data.location_longitude);
        opto.setLocation(location);
        Person person = new Person();
        person.setId(data.person_id);
        person.setCreated_at(data.person_created_at);
        person.setDisplay_name(data.person_display_name);
        person.setUser_name(data.person_user_name);
        person.setText(data.person_text);
        person.setAvatar_asset_id(data.person_avatar_asset_id);
        opto.setPerson(person);
        opto.setOptograph_type(data.optograph_type);
        opto.setStars_count(data.optograph_stars_count);
        opto.setComments_count(data.optograph_comments_count);
        opto.setHashtag_string(data.optograph_hashtag_string);
        Log.d("MARK","data id = "+data.optograph_id);
        return opto;
    }

}