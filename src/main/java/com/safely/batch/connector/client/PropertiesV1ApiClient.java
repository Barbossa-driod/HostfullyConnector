package com.safely.batch.connector.client;

import com.safely.batch.connector.pms.property.PmsProperty;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PropertiesV1ApiClient {

  @Headers({
      "Accept: application/json"
  })
  @GET("v1/properties/")
  Call<List<PmsProperty>> listProperties(@Header("X-HOSTFULLY-APIKEY") String hostfullyAPIKey,
      @Query("agencyUid") String agencyUid, @Query("limit") int limit, @Query("offset") int offset);

  @Headers({
      "Accept: application/json"
  })
  @GET("v1/properties/{id}")
  Call<PmsProperty> getProperty(@Header("X-HOSTFULLY-APIKEY") String hostfullyAPIKey,
      @Path("id") String propertyId, @Query("agencyUid") String agencyUid);

}
