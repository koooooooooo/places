package com.ko.nearbuildings.net;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface ApiService {

    @GET("/json")
    void getPlaces(@Query("location") String location, @Query("radius") int radius,
                   @Query("key") String key, Callback<PlaceResponse> callback);
}
