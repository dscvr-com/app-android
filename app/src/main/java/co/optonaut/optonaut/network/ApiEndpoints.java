package co.optonaut.optonaut.network;

import java.util.List;

import co.optonaut.optonaut.model.Optograph;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public interface ApiEndpoints {
    @Headers({"Accept: application/json"})
    @GET("optographs")
    Call<List<Optograph>> listOptographs(@Header("Authorization") String authorization);
}