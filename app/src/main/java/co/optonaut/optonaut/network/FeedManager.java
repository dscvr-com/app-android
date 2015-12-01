package co.optonaut.optonaut.network;

import android.util.Log;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

import co.optonaut.optonaut.bus.BusProvider;
import co.optonaut.optonaut.bus.OptographsReceivedEvent;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.util.RFC3339DateFormatter;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * @author Nilan Marktanner
 * @date 2015-12-01
 */
public class FeedManager {
    private static final String DEBUG_TAG = "Optonaut";
    private static final int LIMIT = 5;

    public static void reinitializeFeed() {
        loadOlderThan(RFC3339DateFormatter.toRFC3339String(DateTime.now()));
    }

    public static void loadOlderThan(String older_than) {
        Log.v(DEBUG_TAG, "Load next optographs older than " + older_than);

        ApiConsumer apiConsumer = new ApiConsumer();
        try {
            apiConsumer.getOptographs(LIMIT, older_than, new Callback<List<Optograph>>() {
                @Override
                public void onResponse(Response<List<Optograph>> response, Retrofit retrofit) {
                    List<Optograph> optographs = response.body();
                    BusProvider.getInstance().post(new OptographsReceivedEvent(optographs));
                }

                @Override
                public void onFailure(Throwable t) {
                    // TODO: either fire OptographsFailedToReceive event or work with Android Job Queue
                    Log.v(DEBUG_TAG, "Failed to reinitialize feed!");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
