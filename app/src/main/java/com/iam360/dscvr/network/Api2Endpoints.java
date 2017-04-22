package com.iam360.dscvr.network;

import com.iam360.dscvr.model.Gateway;
import com.iam360.dscvr.model.MotorConfig;
import com.iam360.dscvr.model.LogInReturn;
import com.iam360.dscvr.model.MapiResponseObject;
import com.iam360.dscvr.model.NotificationTriggerData;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.SendStory;
import com.iam360.dscvr.model.SendStoryResponse;
import com.squareup.okhttp.RequestBody;

import java.util.List;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public interface Api2Endpoints {

    @POST("check_status")
    Call<Gateway.CheckStatusResponse> checkStatus(@Body Gateway.CheckStatusData data);

    @POST("request_code")
    Call<Gateway.RequestCodeResponse> requestCode(@Body Gateway.RequestCodeData data);

    @POST("use_code")
    Call<Gateway.UseCodeResponse> useCode(@Body Gateway.UseCodeData data);

    @POST("notification/create")
    Call<String> triggerNotif(@Body NotificationTriggerData data);

    @GET("config/motor")
    Call<List<MotorConfig>> getMotorConfig();

//    https://mapi.dscvr.com/story/merged/7753e6e9-23c6-46ec-9942-35a5ea744ece?feedpage=1&feedsize=5&youpage=1&yousize=5
    @GET("/story/profile")
    Observable<List<Optograph>> getStories(@Query("limit") int limit, @Query("older_than") String older_than);

    @GET("/story/person/{id}")
    Observable<List<Optograph>> getStoriesProfile(@Path("id") String id, @Query("limit") int limit, @Query("older_than") String older_than);


    @POST("story/create")
    Call<SendStoryResponse> sendStories(@Body SendStory data);

    @POST("story/upload")
    Call<LogInReturn.EmptyResponse> uploadBgm(@Body RequestBody asset);

    @DELETE("story/{storyId}/")
    Call<MapiResponseObject> deleteStory(@Path("storyId") String storyId);

    @PUT("story/{storyId}/")
    Call<SendStoryResponse> updateStories(@Path("storyId") String storyId, @Body SendStory data);

    @GET("story/feed")
    Observable<List<Optograph>> getStoryFeeds(@Query("limit") int limit, @Query("older_than") String older_than);

}