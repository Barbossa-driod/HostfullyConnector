package com.safely.batch.connector.client.clientV2;


import com.safely.batch.connector.pmsV2.orders.OrderV2;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

import java.util.List;


public interface ReservationsOrdersV2ApiClient {

    @Headers({
            "Accept: application/json"
    })
    @GET("v2/photos")
    Call<List<OrderV2>> getOrder(@Header("X-HOSTFULLY-APIKEY") String hostfullyAPIKey,
                                 @Query("leadUid") String leadUid);

}
