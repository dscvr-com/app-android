package co.optonaut.optonaut.network;

import android.util.Log;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

import co.optonaut.optonaut.bus.BusProvider;
import co.optonaut.optonaut.bus.OptographsReceivedEvent;
import co.optonaut.optonaut.bus.PersonReceivedEvent;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.model.Person;
import co.optonaut.optonaut.util.RFC3339DateFormatter;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * @author Nilan Marktanner
 * @date 2015-12-09
 */
public class PersonManager {
    private static final String DEBUG_TAG = "Optonaut";
    private static final int LIMIT = 5;

    public static void loadPerson(String id) {
        Log.v(DEBUG_TAG, "Load Person " + id);

        ApiConsumer apiConsumer = new ApiConsumer();
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
}
