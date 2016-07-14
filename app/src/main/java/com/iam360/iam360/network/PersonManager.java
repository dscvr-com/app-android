package com.iam360.iam360.network;

import android.util.Log;

import java.io.IOException;

import com.iam360.iam360.bus.BusProvider;
import com.iam360.iam360.bus.PersonReceivedEvent;
import com.iam360.iam360.model.LogInReturn;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.util.Cache;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-12-09
 */
public class PersonManager {
    private static final String DEBUG_TAG = "Optonaut";
    private static final int LIMIT = 5;

    public static void loadPerson(String id) {
        Log.v(DEBUG_TAG, "Load Person " + id);

        Cache cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        ApiConsumer apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        try {
            apiConsumer.getPerson(id, new Callback<Person>() {
                @Override
                public void onResponse(Response<Person> response, Retrofit retrofit) {
                    Person person = response.body();
                    BusProvider.getInstance().post(new PersonReceivedEvent(person));
                }

                @Override
                public void onFailure(Throwable t) {
                    // TODO: either fire OptographsFailedToReceive event or work with Android Job Queue
                    Log.v(DEBUG_TAG, "Failed to load person!");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void savePersonInfoToCache() {
        Log.v(DEBUG_TAG, "savePersonInfoToCache");

        Cache cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        ApiConsumer apiConsumer = new ApiConsumer(token.equals("") ? null : token);
            apiConsumer.getUser(new Callback<Person>() {
                @Override
                public void onResponse(Response<Person> response, Retrofit retrofit) {
                    Person person = response.body();
                    cache.save(Cache.USER_EMAIL, person.getEmail());
                    cache.save(Cache.USER_NAME, person.getUser_name());
                    Log.v(DEBUG_TAG, "User email : " + person.getEmail());
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.v(DEBUG_TAG, "Failed to load person!");
                }
            });
    }

    public static void updatePerson(String displayName, String text, String userName) {

        Cache cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        ApiConsumer apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        try {
            apiConsumer.updatePerson(new UpdatePersonData(displayName, text, userName), new Callback<Person>() {
                @Override
                public void onResponse(Response<Person> response, Retrofit retrofit) {
                    if(response.isSuccess()) Timber.d("Updated person : " + response.body());
                    else Timber.d("Updated person : failed.");
                }

                @Override
                public void onFailure(Throwable t) {
                    Timber.e("Failed to update person : %s.", displayName);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updatePerson() {
        Cache cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        ApiConsumer apiConsumer = new ApiConsumer(token.equals("")?null:token);
        try {
            apiConsumer.updatePersonSocial(new UpdatePersonSocialData(cache.getString(Cache.USER_FB_ID),
                    cache.getString(Cache.USER_FB_TOKEN), cache.getString(Cache.USER_TWITTER_TOKEN),
                    cache.getString(Cache.USER_TWITTER_SECRET)), new Callback<Person>() {
                @Override
                public void onResponse(Response<Person> response, Retrofit retrofit) {
                    Timber.v("success updating social data of user? " + response.isSuccess());
                }

                @Override
                public void onFailure(Throwable t) {
                    Timber.v("failed to update social data of user: " + t.toString());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logoutPerson() {
        Cache cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        ApiConsumer apiConsumer = new ApiConsumer(token.equals("")?null:token);
        try {
            apiConsumer.logout(new Callback<LogInReturn.EmptyResponse>() {
                @Override
                public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                    Timber.v("Logged out." + response.isSuccess());
                }

                @Override
                public void onFailure(Throwable t) {
                    Timber.v("Failed loggin" + t.toString());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class UpdatePersonData {
        final String display_name;
        final String text;
        final String user_name;

        public UpdatePersonData(String display_name, String text, String user_name) {
            this.display_name = display_name;
            this.text = text;
            this.user_name = user_name;
        }

    }

    static class UpdatePersonSocialData{
        final String facebook_user_id;
        final String facebook_token;
        final String twitter_token;
        final String twitter_secret;

        public UpdatePersonSocialData(String facebook_user_id, String facebook_token, String twitter_token, String twitter_secret) {
            this.facebook_user_id = facebook_user_id;
            this.facebook_token = facebook_token;
            this.twitter_token = twitter_token;
            this.twitter_secret = twitter_secret;
        }
    }
}
