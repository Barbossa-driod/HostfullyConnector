package com.safely.batch.connector.client.clientV2;


import com.safely.batch.connector.pmsV2.property.PmsPropertyPhotoV2;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

import java.util.List;


public interface PropertiesPhotosV2ApiClient {

    @Headers({
            "Accept: application/json"
    })
    @GET("v2/photos")
    Call<List<PmsPropertyPhotoV2>> getPhotos(@Header("X-HOSTFULLY-APIKEY") String hostfullyAPIKey,
                                             @Query("propertyUid") String propertyUid);

}
