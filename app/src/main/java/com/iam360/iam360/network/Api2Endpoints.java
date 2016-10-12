package com.iam360.iam360.network;

import com.iam360.iam360.model.Gateway;
import com.iam360.iam360.model.NotificationTriggerData;
import com.iam360.iam360.model.SendStory;
import com.iam360.iam360.model.SendStoryResponse;
import com.iam360.iam360.model.StoryFeed;
import com.squareup.okhttp.RequestBody;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

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

//    https://mapi.dscvr.com/story/merged/7753e6e9-23c6-46ec-9942-35a5ea744ece?feedpage=1&feedsize=5&youpage=1&yousize=5
    @GET("story/merged/{id}")
    Call<StoryFeed> getStories(@Path("id") String id, @Query("feedpage") int feedpage, @Query("feedsize") int feedsize, @Query("youpage") int youpage, @Query("yousize") int yousize);

    @POST("story/v2/part1")
    Call<SendStoryResponse> sendStories(@Body SendStory data);

    @POST("story/v2/part2")
    Call<String> uploadBgm(@Body RequestBody asset);

}