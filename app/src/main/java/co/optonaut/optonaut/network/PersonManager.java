package co.optonaut.optonaut.network;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.google.gson.JsonObject;

import java.io.IOException;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.bus.BusProvider;
import co.optonaut.optonaut.bus.PersonReceivedEvent;
import co.optonaut.optonaut.model.LogInReturn;
import co.optonaut.optonaut.model.Person;
import co.optonaut.optonaut.model.SignUpReturn;
import co.optonaut.optonaut.util.Cache;
import co.optonaut.optonaut.views.SignInActivity;
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

    public static void updatePerson(String displayName, String text) {

        Cache cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        ApiConsumer apiConsumer = new ApiConsumer(token.equals("") ? null : token);
        try {
            apiConsumer.updatePerson(new UpdatePersonData(displayName, text), new Callback<Person>() {
                @Override
                public void onResponse(Response<Person> response, Retrofit retrofit) {
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

    static class UpdatePersonData {
        final String display_name;
        final String text;

        public UpdatePersonData(String display_name, String text) {
            this.display_name = display_name;
            this.text = text;
        }
    }
}
