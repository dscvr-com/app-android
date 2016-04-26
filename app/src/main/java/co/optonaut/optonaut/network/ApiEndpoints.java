package co.optonaut.optonaut.network;

import com.squareup.okhttp.RequestBody;

import java.util.List;

import co.optonaut.optonaut.model.FBSignInData;
import co.optonaut.optonaut.model.LogInReturn;
import co.optonaut.optonaut.model.OptoData;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.model.Person;
import co.optonaut.optonaut.model.SignInData;
import co.optonaut.optonaut.model.SignUpReturn;
import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
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

    @POST("persons")
    Call<SignUpReturn> signUp(@Body SignInData data);

    @POST("persons/login")
    Call<LogInReturn> logIn(@Body SignInData data);

    @POST("persons/facebook/signin")
    Call<LogInReturn> fbLogin(@Body FBSignInData data);

//    @Headers("Content-Type: application/json")
    @POST("optographs")
//    Call<Optograph> uploadOptoData(@Header("Authorization") String authorization,@Body OptographFeedAdapter.OptoData data);
    Call<Optograph> uploadOptoData(@Body OptoData data);

    @POST("optographs/{id}/upload-asset")
    Call<LogInReturn.EmptyResponse> uploadOptoImage(@Path("id") String id,@Body RequestBody asset);

    @POST("optographs/{id}/star")
    Call<LogInReturn.EmptyResponse> postStar(@Path("id") String id);

    @DELETE("optographs/{id}/star")
    Call<LogInReturn.EmptyResponse> deleteStar(@Path("id") String id);
}
