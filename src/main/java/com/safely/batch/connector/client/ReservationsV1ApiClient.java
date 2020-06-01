package com.safely.batch.connector.client;

import com.safely.batch.connector.pms.ResponsePage;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ReservationsV1ApiClient {

  @GET("")
  Call<ResponsePage<Object>> listReservations(@Header("Authorization") String authorization);
}