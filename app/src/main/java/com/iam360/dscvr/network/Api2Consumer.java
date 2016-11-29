package com.iam360.dscvr.network;

import android.util.Log;

import com.iam360.dscvr.model.Gateway;
import com.iam360.dscvr.model.LogInReturn;
import com.iam360.dscvr.model.MapiResponseObject;
import com.iam360.dscvr.model.NotificationTriggerData;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.SendStory;
import com.iam360.dscvr.model.SendStoryResponse;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.RFC3339DateFormatter;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.joda.time.DateTime;

import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public class Api2Consumer {
    private static final String BASE_URL = "http://noel.dscvr.com/api"; //"https://mapi.dscvr.com/api/"; //
    private static final String BASE_URL2 = "http://noel.dscvr.com/"; //"https://mapi.dscvr.com/"; //
    //http://noel.dscvr.com/

    private static final int DEFAULT_LIMIT = 5;
    public static final int PROFILE_GRID_LIMIT = 12;

    OkHttpClient client;

    Retrofit retrofit;

    Api2Endpoints service;
    Cache cache;
    String token = null;

    private boolean flag = false;
    private boolean finish = false;

    /**
     *
     * @param token
     * @param type triggerNotif or any string for @BASE_URL2, empty string or null for @BASE_URL
     */
    public Api2Consumer(String token, String type) {

        Timber.d("Api2Consumer");
        this.token = token;
        client = new OkHttpClient();
//        client.setConnectTimeout(10, TimeUnit.MINUTES);
//        client.setReadTimeout(10, TimeUnit.MINUTES);
        cache = Cache.open();

        client.interceptors().add(new Interceptor() {
            @Override
            public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                Request newRequest;

                if(token!=null){// must have condition if the route uses auth token
                    Log.d("myTag","auth token add as Header " + token);
                    newRequest = chain.request().newBuilder()
                            .addHeader("User-Agent", "Retrofit-Sample-App")
                            .addHeader("Authorization", "Bearer "+token)
                            .build();
                }else{
                    newRequest = chain.request().newBuilder()
                            .addHeader("User-Agent", "Retrofit-Sample-App")
                            .build();
                }

//                Request request = chain.request();

                Timber.v(newRequest.headers().toString());
                Timber.v(newRequest.toString());

//                com.squareup.okhttp.Response response = chain.proceed(request);
//                Timber.v(response.headers().toString());
//                Timber.v(response.toString());

                return chain.proceed(newRequest);
            }
        });

        String bs_URL = BASE_URL;
        if(type != null && (type.equals("triggerNotif") || !type.equals(""))){
            bs_URL = BASE_URL2;
        }

        retrofit = new Retrofit.Builder()
                .baseUrl(bs_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();

        service =  retrofit.create(Api2Endpoints.class);
    }

    public void checkStatus(Gateway.CheckStatusData data, Callback<Gateway.CheckStatusResponse> callback) {
        Call<Gateway.CheckStatusResponse> call = service.checkStatus(data);
        call.enqueue(callback);
    }

    public void requestCode(Gateway.RequestCodeData data, Callback<Gateway.RequestCodeResponse> callback) {
        Call<Gateway.RequestCodeResponse> call = service.requestCode(data);
        call.enqueue(callback);
    }

    public void useCode(Gateway.UseCodeData data, Callback<Gateway.UseCodeResponse> callback) {
        Call<Gateway.UseCodeResponse> call = service.useCode(data);
        call.enqueue(callback);
    }

    public void triggerNotif(NotificationTriggerData data, Callback<String> callback) {
        Call<String> call = service.triggerNotif(data);
        call.enqueue(callback);
    }

    public Observable<Optograph> getStories(int limit, String older_than) {
        return service.getStories(limit, older_than).flatMap(Observable::from);
    }

    public void uploadBgm(RequestBody data, Callback<LogInReturn.EmptyResponse> callback) {
        Call<LogInReturn.EmptyResponse> call = service.uploadBgm(data);
        call.enqueue(callback);
    }

    public void deleteStory(String storyId, Callback<MapiResponseObject> callback) {
        Call<MapiResponseObject> call = service.deleteStory(storyId);
        call.enqueue(callback);
    }

    public void sendStories(SendStory sendStory, Callback<SendStoryResponse> callback) {
        Call<SendStoryResponse> call = service.sendStories(sendStory);
        call.enqueue(callback);
    }

    public void updateStories(String storyId, SendStory sendStory, Callback<SendStoryResponse> callback) {
        Call<SendStoryResponse> call = service.updateStories(storyId, sendStory);
        call.enqueue(callback);
    }

    public Observable<Optograph> getStoryFeeds(int limit, String older_than) {
        Timber.i("get optographs request: %s older than %s", limit, older_than);
        return service.getStoryFeeds(limit, older_than).flatMap(Observable::from).onErrorResumeNext(Observable.error(new Throwable()));
    }

    public Observable<Optograph> getStoryFeeds(int limit) {
        return getStoryFeeds(limit, getNow());
    }

    public Observable<Optograph> getStoryFeeds() {
        return getStoryFeeds(DEFAULT_LIMIT, getNow());
    }

    private String getNow() {
        return RFC3339DateFormatter.toRFC3339String(DateTime.now());
    }


    public Observable<Optograph> getOptographsFromPerson(String id, int limit, String older_than) {
        return service.getStoriesProfile(id, limit, older_than).flatMap(Observable::from);
    }

    public Observable<Optograph> getOptographsFromPerson(String id, String older_than) {
        return getOptographsFromPerson(id, DEFAULT_LIMIT, older_than);
    }

    public Observable<Optograph> getOptographsFromPerson(String id, int limit) {
        return getOptographsFromPerson(id, limit, getNow());
    }

    public Observable<Optograph> getOptographsFromPerson(String id) {
        return getOptographsFromPerson(id, DEFAULT_LIMIT, getNow());
    }
}
