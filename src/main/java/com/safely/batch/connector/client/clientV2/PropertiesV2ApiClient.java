package com.safely.batch.connector.client.clientV2;


import com.safely.batch.connector.pmsV2.property.PmsPropertiesUidsV2;
import com.safely.batch.connector.pmsV2.property.PmsPropertyV2;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;


public interface PropertiesV2ApiClient {

    @Headers({
            "Accept: application/json"
    })
    @GET("v2/properties/")
    Call<List<PmsPropertiesUidsV2>> listProperties(@Header("X-HOSTFULLY-APIKEY") String hostfullyAPIKey,
                                                   @Query("agencyUid") String agencyUid, @Query("limit") int limit, @Query("offset") int offset);

    @Headers({
            "Accept: application/json"
    })
    @GET("v2/properties/{id}")
    Call<PmsPropertyV2> getProperty(@Header("X-HOSTFULLY-APIKEY") String hostfullyAPIKey,
                                    @Path("id") String propertyId, @Query("agencyUid") String agencyUid);

}
