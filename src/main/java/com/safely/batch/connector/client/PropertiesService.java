package com.safely.batch.connector.client;

import com.google.common.util.concurrent.RateLimiter;
import com.safely.batch.connector.pms.property.PmsProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

@Service
public class PropertiesService {

    private static final Logger log = LoggerFactory.getLogger(PropertiesService.class);

    private static final int LIMIT = 20;

    private final RateLimiter rateLimiter;
    private PropertiesV1ApiClient propertiesV1ApiClient;

    public PropertiesService(PropertiesV1ApiClient propertiesV1ApiClient,
                             @Qualifier("PmsApiRateLimiter") RateLimiter rateLimiter) {
        Assert.notNull(propertiesV1ApiClient, "PropertiesV1ApiClient cannot be null!");
        Assert.notNull(rateLimiter, "RateLimiter cannot be null!");

        this.propertiesV1ApiClient = propertiesV1ApiClient;
        this.rateLimiter = rateLimiter;
    }

    public PmsProperty getProperty(String token, String propertyId, String agencyUid)
            throws Exception {
        Assert.notNull(token, "Authentication token cannot be null!");
        Assert.notNull(propertyId, "PropertyId cannot be null!");
        Assert.notNull(agencyUid, "agencyUid cannot be null!");

        log.info("Loading property with id: {}", propertyId);
        try {
            rateLimiter.acquire();

            Call<PmsProperty> apiCall = propertiesV1ApiClient.getProperty(token, propertyId, agencyUid);
            Response<PmsProperty> response = apiCall.execute();

            if (!response.isSuccessful() && response.code() != 409) {
                log.error("GetProperty call failed! Error Code: {}", response.code());
                throw new Exception(response.message());
            }
            
            if (response.code() == 409) {
                log.info("GetProperty call failed, because property id={} has been removed from Hostfully system", propertyId);
                return null;
            }

            return response.body();
        } catch (Exception ex) {
            log.error("Exception while calling GetProperty!", ex);
            throw ex;
        }
    }

    public List<PmsProperty> getProperties(String token, String agencyUid) throws Exception {
        Assert.notNull(token, "Authentication token cannot be null!");
        Assert.notNull(agencyUid, "agencyUid cannot be null!");

        List<PmsProperty> properties = new ArrayList<>();

        int offset = 0;
        int retrievedCount = 0;
        int pageCount = 1;
        do {
            log.info("Loading page {} of listProperties.", pageCount);
            try {
                rateLimiter.acquire();

                Call<List<PmsProperty>> apiCall = propertiesV1ApiClient
                        .listProperties(token, agencyUid, LIMIT, offset);
                Response<List<PmsProperty>> response = apiCall.execute();

                if (!response.isSuccessful()) {
                    log.error("ListProperties call failed! Error Code: {}", response.code());
                    throw new Exception(response.message());
                }

                List<PmsProperty> page = response.body();
                if (page != null) {
                    properties.addAll(page);
                    retrievedCount = page.size();
                } else {
                    retrievedCount = 0;
                }

                offset = offset + retrievedCount;

            } catch (Exception ex) {
                log.error("Exception while calling ListProperties!", ex);
                throw ex;
            }
            pageCount++;
        } while (retrievedCount > 0 && retrievedCount == LIMIT);

        return properties;
    }
}