package com.iam360.iam360.network;

import com.iam360.iam360.model.Gateway;
import com.iam360.iam360.model.NotificationTriggerData;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.model.StoryFeed;

import java.util.List;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;
import timber.log.Timber;

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

    @GET("story/merged/{id}")
    Call<StoryFeed> getStories(@Path("id") String id);

}
