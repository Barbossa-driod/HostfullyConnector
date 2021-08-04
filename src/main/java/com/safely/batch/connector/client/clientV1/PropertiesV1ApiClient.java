package com.safely.batch.connector.client.clientV1;

import com.safely.batch.connector.pmsV1.property.PmsProperty;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

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
