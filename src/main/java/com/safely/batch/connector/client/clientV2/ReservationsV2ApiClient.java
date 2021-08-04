package com.safely.batch.connector.client.clientV2;


import com.safely.batch.connector.pmsV2.reservation.PmsReservationV2;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

import java.util.List;


public interface ReservationsV2ApiClient {

    @Headers({
            "Accept: application/json"
    })
    @GET("v2/leads/")
    Call<List<PmsReservationV2>> listReservations(@Header("X-HOSTFULLY-APIKEY") String hostfullyAPIKey,
                                                  @Query("agencyUid") String agencyUid, @Query("limit") int limit, @Query("offset") int offset);
}
