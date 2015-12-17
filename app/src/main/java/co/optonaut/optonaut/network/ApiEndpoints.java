package co.optonaut.optonaut.network;

import java.util.List;

import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.model.Person;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public interface ApiEndpoints {
    @Headers({"Accept: application/json"})
    @GET("optographs")
    Call<List<Optograph>> listOptographsWithAuthentification(@Header("Authorization") String authorization);

    @GET("optographs/feed")
    Call<List<Optograph>> getOptographs(@Query("limit") int limit, @Query("older_than") String older_than);

    @GET("optographs/search")
    Observable<List<Optograph>> searchOptographs(@Query("limit") int limit,  @Query("older_than") String older_than, @Query("keyword") String keyword);

    @GET("persons/{id}")
    Call<Person> getPerson(@Path("id") String id);

    @GET("persons/{id}/optographs")
    Observable<List<Optograph>> getOptographsFromPerson(@Path("id") String id, @Query("limit") int limit, @Query("older_than") String older_than);

    @GET("optographs/feed")
    Observable<List<Optograph>> getOptographsAsObservable(@Query("limit") int limit, @Query("older_than") String older_than);

}