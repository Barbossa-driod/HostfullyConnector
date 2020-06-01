package com.safely.batch.connector.client;

import com.safely.batch.connector.pms.ResponsePage;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface PropertiesV1ApiClient {

  @GET("")
  Call<ResponsePage<Object>> listProperties(@Header("Authorization") String authorization);
}
