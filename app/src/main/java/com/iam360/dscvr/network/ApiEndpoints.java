package com.iam360.dscvr.network;

import com.iam360.dscvr.model.FBSignInData;
import com.iam360.dscvr.model.Follower;
import com.iam360.dscvr.model.GCMToken;
import com.iam360.dscvr.model.GeocodeDetails;
import com.iam360.dscvr.model.GeocodeReverse;
import com.iam360.dscvr.model.LogInReturn;
import com.iam360.dscvr.model.Notification;
import com.iam360.dscvr.model.OptoData;
import com.iam360.dscvr.model.OptoDataUpdate;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.Person;
import com.iam360.dscvr.model.SignInData;
import com.iam360.dscvr.model.SignUpReturn;
import com.iam360.dscvr.views.fragment.SharingFragment;
import com.squareup.okhttp.RequestBody;

import java.util.List;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
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

    @PUT("/persons/me")
    Call<Person> updatePerson(@Body PersonManager.UpdatePersonData data);

    @PUT("/persons/me")
    Call<Person> updatePersonSocial(@Body PersonManager.UpdatePersonSocialData data);

    @GET("persons/{id}/optographs")
    Observable<List<Optograph>> getOptographsFromPerson(@Path("id") String id, @Query("limit") int limit, @Query("older_than") String older_than);

    @GET("optographs/feed")
    Observable<List<Optograph>> getOptographsAsObservable(@Query("limit") int limit, @Query("older_than") String older_than);

    @POST("persons")
    Call<SignUpReturn> signUp(@Body SignInData data);

    @POST("persons/logout")
    Call<LogInReturn.EmptyResponse> logout();

    @POST("persons/login")
    Call<LogInReturn> logIn(@Body SignInData data);

    @PUT("persons/me")
    Call<String> sendGCMToken(@Body GCMToken data);

    @POST("persons/facebook/signin")
    Call<LogInReturn> fbLogin(@Body FBSignInData data);

    //    @Headers("Content-Type: application/json")
    @POST("optographs")
//    Call<Optograph> uploadOptoData(@Header("Authorization") String authorization,@Body OptographFeedAdapter.OptoData data);
    Call<Optograph> uploadOptoData(@Body OptoData data);

    @DELETE("optographs/{id}")
    Call<LogInReturn.EmptyResponse> deleteOptonaut(@Path("id") String id);

    @POST("optographs/{id}/upload-asset")
    Call<LogInReturn.EmptyResponse> uploadOptoImage(@Path("id") String id,@Body RequestBody asset);

    @POST("optographs/{id}/upload-asset-theta")
    Call<LogInReturn.EmptyResponse> uploadThetaImage(@Path("id") String id,@Body RequestBody asset);

    @PUT("/optographs/{id}")
    Call<LogInReturn.EmptyResponse> updateOptograph(@Path("id") String id,@Body OptoDataUpdate data);

    @POST("optographs/{id}/star")
    Call<LogInReturn.EmptyResponse> postStar(@Path("id") String id);

    @DELETE("optographs/{id}/star")
    Call<LogInReturn.EmptyResponse> deleteStar(@Path("id") String id);

    @POST("persons/{id}/follow")
    Call<LogInReturn.EmptyResponse> follow(@Path("id") String id, @Body String body);

    @DELETE("persons/{id}/follow")
    Call<LogInReturn.EmptyResponse> unfollow(@Path("id") String id);

    @POST("/persons/me/upload-profile-image")
    Call<LogInReturn.EmptyResponse> uploadAvatar(@Body RequestBody asset);

    @GET("locations/geocode-reverse")
    Call<List<GeocodeReverse>> getNearbyPlaces(@Query("lat") String lat, @Query("lon") String lon);

    @GET("locations/geocode-details/{placeid}")
    Call<GeocodeDetails> getLocationDetails(@Path("placeid") String placeid);

    @GET("persons/search")
    Call<List<Person>> getSearchResult(@Query("keyword") String key);

    @GET("persons/username_search")
    Call<List<Person>> getUserName(@Query("keyword") String key);

    @GET("persons/me")
    Call<Person> getUser();

    @GET("persons/followers")
    Observable<List<Follower>> getFollowers();

    @GET("persons/followers")
    Call<List<Follower>> getFollowersCall();

    @POST("optographs/share_facebook")
    Call<LogInReturn.EmptyResponse> shareFB(@Body SharingFragment.ShareFBData data);

    @GET("activities")
    Call<List<Notification>> getNotifications();

    @POST("activities/{id}/read")
    Call<LogInReturn.EmptyResponse> setNotificationToRead(@Path("id") String id);


}
