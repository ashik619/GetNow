package com.getnowsolutions.getnow.handlers.networkhandlers;

/*
  Created by DilipAti on 23/11/16.
 */

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIInterface {

    @GET("autocomplete/json")
    Call<JsonObject> getPlaces(@Query("location") String latLng, @Query("input") String input,
                               @Query("key") String key,@Query("types") String types,
                               @Query("radius") String radius);
    @GET("details/json")
    Call<JsonObject> getPlaceDetails(@Query("placeid") String placeid, @Query("key") String key);

    @POST("/api/driver/startTrip")
    Call<JsonObject> startTrip(@Body JsonObject request);

    @GET("/api/bus/findNearBuses")
    Call<JsonObject> findNearBuses(@Query("lat") String lat, @Query("lng") String lng);


}