package com.safely.batch.connector.client.clientV2;


import com.google.common.util.concurrent.RateLimiter;
import com.safely.batch.connector.pmsV2.property.PmsPropertyPhotoV2;
import com.safely.batch.connector.pmsV2.property.PmsPropertyV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;


@Service
public class PropertiesPhotoServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(PropertiesPhotoServiceV2.class);

    private final PropertiesPhotosV2ApiClient propertiesPhotosV2ApiClient;

    private final RateLimiter rateLimiter;

    public PropertiesPhotoServiceV2(PropertiesPhotosV2ApiClient propertiesPhotosV2ApiClient,
                                    @Qualifier("PmsApiRateLimiter") RateLimiter rateLimiter) {

        this.propertiesPhotosV2ApiClient = propertiesPhotosV2ApiClient;
        this.rateLimiter = rateLimiter;
    }

    public void getPropertiesPhotos(String token, List<PmsPropertyV2> pmsProperties) throws Exception {

        List<PmsPropertyPhotoV2> pmsPropertyPhotoV2 = new ArrayList<>();

        try {
            rateLimiter.acquire();

            for (PmsPropertyV2 property: pmsProperties){

                Call<List<PmsPropertyPhotoV2>> apiCall = propertiesPhotosV2ApiClient.getPhotos(token, property.getUid());
                Response<List<PmsPropertyPhotoV2>> response = apiCall.execute();

                if (!response.isSuccessful()) {
                    log.error("ListPropertiesPhotos call failed! Error Code: {}", response.code());
                    throw new Exception(response.message());
                }

                pmsPropertyPhotoV2 = response.body();

                property.setPhotos(pmsPropertyPhotoV2);

            }
        } catch (Exception ex){
            log.error("Exception while calling ListPropertiesPhotos!", ex);
            throw ex;
        }
    }
}
