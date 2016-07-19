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
import com.iam360.iam360.model.Location;
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
        Log.d("MARK","onMessageReceived BundleData = "+data.toString());
        String message = data.getString("message");
        String title = data.getString("title");
        String type = data.getString("type");
        String json = data.getString("data");
        sendNotification(message, title, type, json);
    }

    private void sendNotification(String message, String title, String type, String json) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        builder.setSmallIcon(R.mipmap.ic_launcher);
        Intent intent = new Intent(this, MainActivity.class);
        if(type.equals("opto")){
            intent = new Intent(this, OptographDetailsActivity.class);
            Optograph optograph = getOpto(json);
            intent.putExtra("opto", optograph);
        }else{
            intent = new Intent(this, ProfileActivity.class);
            Person person = getPersons(json);
            intent.putExtra("person", person);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle(title);
        builder.setContentText(message);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    private Person getPersons(String json){
        json = "{\n" +
                "               \"person_id\": \"8230b4e8-8478-4b5b-a20e-9efbacff5516\",\n" +
                "               \"person_created_at\": \"2016-04-25 13:45:45.416436+00\",\n" +
                "               \"person_display_name\": \"sakuraLi\",\n" +
                "               \"person_user_name\": \"mariela\",\n" +
                "               \"person_text\": \"i :heart: anime\",\n" +
                "               \"person_avatar_asset_id\": \"8a46c4d3-6d3a-4f4e-aa76-05216469202c\",\n" +
                "               \"person_invite_activation_id\": null,\n" +
                "               \"person_invite_activation_at\": null,\n" +
                "               \"person_invite_activation_activated\": \"f\",\n" +
                "               \"person_optographs_count\": \"118\",\n" +
                "               \"person_followers_count\": \"2\",\n" +
                "               \"person_followed_count\": \"5\"\n" +
                "           }";
        Gson gson = new Gson();
        Persons data = gson.fromJson(json, Persons.class);
        Person person = new Person();
        person.setId(data.person_id);
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
        json ="{\n" +
                "               \"optograph_id\": \"4bba6d92-37d9-40a1-826f-077b0dc6cb6c\",\n" +
                "               \"optograph_created_at\": \"2016-07-18 08:52:44.019+00\",\n" +
                "               \"optograph_updated_at\": \"2016-07-18 08:53:06.97308+00\",\n" +
                "               \"optograph_deleted_at\": null,\n" +
                "               \"optograph_text\": \"\",\n" +
                "               \"optograph_views_count\": \"0\",\n" +
                "               \"optograph_left_texture_asset_id\": \"00000000-0000-0000-0000-000000000000\",\n" +
                "               \"optograph_right_texture_asset_id\": \"00000000-0000-0000-0000-000000000000\",\n" +
                "               \"optograph_is_private\": \"f\",\n" +
                "               \"optograph_is_staff_pick\": \"f\",\n" +
                "               \"optograph_is_published\": \"t\",\n" +
                "               \"optograph_share_alias\": \"fflqnt\",\n" +
                "               \"optograph_stitcher_version\": \"0.7.0\",\n" +
                "               \"optograph_direction_phi\": \"3.33891\",\n" +
                "               \"optograph_direction_theta\": \"-1.5708\",\n" +
                "               \"optograph_type\": \"optograph_1\",\n" +
                "               \"optograph_platform\": \"iOS 9.3.2\",\n" +
                "               \"optograph_model\": \"iPhone 6s\",\n" +
                "               \"optograph_make\": \"Apple\",\n" +
                "               \"optograph_stars_count\": \"0\",\n" +
                "               \"optograph_comments_count\": \"0\",\n" +
                "               \"optograph_hashtag_string\": \"\",\n" +
                "               \"person_id\": \"ee09dd7f-ada4-4446-81a4-66d3a85ed3b7\",\n" +
                "               \"person_created_at\": \"2015-10-20 10:25:03.621051+00\",\n" +
                "               \"person_display_name\": \"lijoseph\",\n" +
                "               \"person_user_name\": \"lijoseph\",\n" +
                "               \"person_text\": \"\",\n" +
                "               \"person_avatar_asset_id\": \"409774a0-e33a-4f95-9ae0-e24faa8065b0\",\n" +
                "               \"person_elite_status\": \"t\",\n" +
                "               \"person_followers_count\": \"2\",\n" +
                "               \"person_followed_count\": \"3\",\n" +
                "               \"location_id\": \"00000000-0000-0000-0000-000000000000\",\n" +
                "               \"location_created_at\": \"2015-11-25 21:03:00.694582+00\",\n" +
                "               \"location_text\": \"Not available\",\n" +
                "               \"location_country\": \"Not available\",\n" +
                "               \"location_country_short\": \"\",\n" +
                "               \"location_place\": \"\",\n" +
                "               \"location_region\": \"\",\n" +
                "               \"location_poi\": \"t\",\n" +
                "               \"location_latitude\": \"0\",\n" +
                "               \"location_longitude\": \"0\"\n" +
                "           }";
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


    private class Persons{
        String person_id;
        String person_created_at;
        String person_display_name;
        String person_user_name;
        String person_text;
        String person_avatar_asset_id;
        String person_invite_activation_id;
        String person_invite_activation_at;
        String person_invite_activation_activated;
        int person_optographs_count;
        int person_followers_count;
        int person_followed_count;
    }

    private class Optographs{
        String optograph_id;
        String optograph_created_at;
        String optograph_updated_at;
        String optograph_deleted_at;
        String optograph_stitcher_version;
        String optograph_text;
        int optograph_views_count;
        String optograph_left_texture_asset_id;
        String optograph_right_texture_asset_id;
        boolean optograph_is_private;
        boolean optograph_is_staff_pick;
        boolean optograph_is_published;
        String optograph_share_alias;
        String optograph_direction_phi;
        String optograph_direction_theta;
        String optograph_type;
        String optograph_platform;
        String optograph_model;
        String optograph_make;
        int optograph_stars_count;
        int optograph_comments_count;
        String optograph_hashtag_string;
        String person_id;
        String person_created_at;
        String person_display_name;
        String person_user_name;
        String person_text;
        String person_avatar_asset_id;
        String person_elite_status;
        String person_followers_count;
        String person_followed_count;
        String location_id;
        String location_created_at;
        String location_text;
        String location_country;
        String location_country_short;
        String location_place;
        String location_region;
        boolean location_poi;
        String location_latitude;
        String location_longitude;
    }
}