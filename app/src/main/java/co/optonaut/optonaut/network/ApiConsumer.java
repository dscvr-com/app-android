package co.optonaut.optonaut.network;

import android.util.Log;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.util.List;

import co.optonaut.optonaut.model.Optograph;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public class ApiConsumer {

    private static final String DEBUG_TAG = "Optonaut";

    private static final String BASE_URL = "https://api-staging.optonaut.co/";
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6ImQyYmVhNmI3LWQxYzktNDEyMi04YTJmLTlkMDFmNTAzZjY2ZCJ9._sVJmnCvSyDeoxoSaD4EkEGisyblUvkb1PufUz__uOY";

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

                Log.d(DEBUG_TAG, request.headers().toString());
                Log.d(DEBUG_TAG, request.toString());

                com.squareup.okhttp.Response response = chain.proceed(request);
                Log.d(DEBUG_TAG, response.headers().toString());
                Log.d(DEBUG_TAG, response.toString());

                return chain.proceed(newRequest);
            }
        });

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        service =  retrofit.create(ApiEndpoints.class);
    }


    private String getAuthorizationToken() {
        return "Bearer " + TOKEN;
    }

    public void getOptographs(Callback<List<Optograph>> callback) throws IOException {
        Call<List<Optograph>> call = service.listOptographs();

        Log.d(DEBUG_TAG, "Request fired!");

        call.enqueue(callback);
    }

    public void getOptographs(int limit, Callback<List<Optograph>> callback) throws IOException {
        Call<List<Optograph>> call = service.listOptographsWithLimit(limit);
        Log.d(DEBUG_TAG, "Request fired!");
        call.enqueue(callback);
    }
}
