package com.iam360.dscvr.network;

import com.iam360.dscvr.model.Gateway;
import com.iam360.dscvr.model.MotorConfig;
import com.iam360.dscvr.model.NotificationTriggerData;

import java.util.List;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

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

}
