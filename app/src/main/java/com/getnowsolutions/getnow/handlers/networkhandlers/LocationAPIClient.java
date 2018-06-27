package com.getnowsolutions.getnow.handlers.networkhandlers;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by dilip on 3/4/17.
 */

public class LocationAPIClient {

    private static final String BASE_URL = "http://letsserviceproduction.us-west-2.elasticbeanstalk.com/";
    //private static final String BASE_URL = "http://custom-env.nxgcppwxby.us-west-2.elasticbeanstalk.com/";
    private static Retrofit retrofit = null;

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    public static Retrofit getClient() {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL.substring(0,BASE_URL.length()-1))
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
