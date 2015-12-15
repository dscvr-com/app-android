package co.optonaut.optonaut.network;

import android.util.Log;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.model.Person;
import co.optonaut.optonaut.util.RFC3339DateFormatter;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public class ApiConsumer {

    private static final String DEBUG_TAG = "Optonaut";

    private static final String BASE_URL = "https://api-staging.optonaut.co/";
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6ImQyYmVhNmI3LWQxYzktNDEyMi04YTJmLTlkMDFmNTAzZjY2ZCJ9._sVJmnCvSyDeoxoSaD4EkEGisyblUvkb1PufUz__uOY";

    private static final int DEFAULT_LIMIT = 5;

    OkHttpClient client;

    Retrofit retrofit;

    ApiEndpoints service;

    public ApiConsumer() {
        client = new OkHttpClient();

        client.interceptors().add(new Interceptor() {
            @Override
            public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder().addHeader("User-Agent", "Retrofit-Sample-App").build();
                Request request = chain.request();

                Log.v(DEBUG_TAG, request.headers().toString());
                Log.v(DEBUG_TAG, request.toString());

                com.squareup.okhttp.Response response = chain.proceed(request);
                Log.v(DEBUG_TAG, response.headers().toString());
                Log.v(DEBUG_TAG, response.toString());

                return chain.proceed(newRequest);
            }
        });

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();

        service =  retrofit.create(ApiEndpoints.class);
    }


    private String getAuthorizationToken() {
        return "Bearer " + TOKEN;
    }

    public void getOptographs(Callback<List<Optograph>> callback) throws IOException {
        getOptographs(DEFAULT_LIMIT, getNow(), callback);
    }

    public void getOptographs(int limit, Callback<List<Optograph>> callback) throws IOException {
        getOptographs(limit, getNow(), callback);
    }

    public void getOptographs(String older_than, Callback<List<Optograph>> callback) throws IOException {
        getOptographs(DEFAULT_LIMIT, older_than, callback);
    }

    public void getOptographs(int limit, String older_than, Callback<List<Optograph>> callback) throws IOException {
        Call<List<Optograph>> call = service.listOptographs(limit, older_than);
        Log.d(DEBUG_TAG, "Get Optograph request fired: get " + limit + " optographs older than " + older_than);
        call.enqueue(callback);
    }

    public void getPerson(String id, Callback<Person> callback) throws IOException {
        Call<Person> call = service.getPerson(id);
        Log.d(DEBUG_TAG, "Get Person request fired: get person " + id);
        call.enqueue(callback);
    }

    public Observable<Optograph> getOptographs(int limit, String older_than) {
        Log.d(DEBUG_TAG, "Get Observable of Optograph request fired: get " + limit + " optographs older than " + older_than);
        return service.listOptographsAsObservable(limit, older_than).flatMap(Observable::from);
    }

    public Observable<Optograph> getOptographs(int limit) {
        return getOptographs(limit, getNow());
    }

    public Observable<Optograph> searchOptographs(int limit, String older_than, String keyword) {
        Log.d(DEBUG_TAG, "Get Observable of Optograph request fired: get " + limit + " optographs older than " + older_than + " fitting keyword " + keyword);
        return service.searchOptographs(limit, older_than, keyword).flatMap(Observable::from);
    }

    public Observable<Optograph> searchOptographs(int limit, String keyword) {
        return searchOptographs(limit, getNow(), keyword);
    }




    private String getNow() {
        return RFC3339DateFormatter.toRFC3339String(DateTime.now());
    }

}
