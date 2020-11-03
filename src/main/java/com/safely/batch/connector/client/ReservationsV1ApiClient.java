package com.safely.batch.connector.client;

import com.safely.batch.connector.pms.reservation.PmsReservation;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

import java.util.List;

public interface ReservationsV1ApiClient {

    @Headers({
            "Accept: application/json"
    })
    @GET("v1/leads/")
    Call<List<PmsReservation>> listReservations(@Header("X-HOSTFULLY-APIKEY") String hostfullyAPIKey,
                                                @Query("agencyUid") String agencyUid, @Query("limit") int limit, @Query("offset") int offset);
}