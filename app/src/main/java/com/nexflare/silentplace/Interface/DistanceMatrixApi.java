package com.nexflare.silentplace.Interface;

import android.support.annotation.Keep;

import com.nexflare.silentplace.Model.DistanceMatrixResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by 15103068 on 24-07-2017.
 */
@Keep
public interface DistanceMatrixApi {

    @GET("/maps/api/distancematrix/json")
    Call<DistanceMatrixResult> getDistance(@Query("origins") String origins, @Query("destinations") String destinations, @Query("key") String key);
}
