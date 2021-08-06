package com.safely.batch.connector.client.clientV2;


import com.google.common.util.concurrent.RateLimiter;
import com.safely.batch.connector.pmsV2.property.PmsPropertiesUidsV2;
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
public class PropertiesServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(PropertiesServiceV2.class);

    private static final int LIMIT = 20;

    private final PropertiesV2ApiClient propertiesV2ApiClient;

    private final RateLimiter rateLimiter;

    public PropertiesServiceV2(PropertiesV2ApiClient propertiesV2ApiClient,
                               @Qualifier("PmsApiRateLimiter") RateLimiter rateLimiter) {
        this.propertiesV2ApiClient = propertiesV2ApiClient;
        this.rateLimiter = rateLimiter;
    }

    public List<PmsPropertyV2> getProperties(String token, String agencyUid) throws Exception {

        List<PmsPropertiesUidsV2> pmsPropertiesUidV2s = getPropertiesUids(token, agencyUid);

        List<String> propertiesId = new ArrayList<>();

        for(PmsPropertiesUidsV2 pmsPropertiesUid : pmsPropertiesUidV2s){
            List<String> listId = pmsPropertiesUid.getPropertiesUids();
            propertiesId.addAll(listId);
        }

        List<PmsPropertyV2> properties = new ArrayList<>();

        try {
            rateLimiter.acquire();

            for (String idOfProperty : propertiesId){
                log.info("Loading property with id: {}", idOfProperty);
                Call<PmsPropertyV2> apiCall = propertiesV2ApiClient.getProperty(token, idOfProperty, agencyUid);
                Response<PmsPropertyV2> response = apiCall.execute();

                if (!response.isSuccessful() && response.code() != 409) {
                    log.error("Property call failed! Error Code: {}", response.code());
                    throw new Exception(response.message());
                }

                if (response.code() == 409) {
                    log.warn("GetProperty call failed, because property id={} has been removed from Hostfully system", idOfProperty);
                }

                PmsPropertyV2 property = response.body();
                properties.add(property);
            }
        } catch (Exception ex){
            log.error("Exception while calling one Property!", ex);
            throw ex;
        }

        return properties;
    }

    private List<PmsPropertiesUidsV2> getPropertiesUids(String token, String agencyUid) throws Exception {

        List<PmsPropertiesUidsV2> pmsPropertiesUidV2s = new ArrayList<>();

        int offset = 0;
        int retrievedCount = 0;
        int pageCount = 1;
        do {
            log.info("Loading page {} of listProperties.", pageCount);
            try {
                rateLimiter.acquire();

                Call<List<PmsPropertiesUidsV2>> apiCall = propertiesV2ApiClient
                        .listProperties(token, agencyUid, LIMIT, offset);
                Response<List<PmsPropertiesUidsV2>> response = apiCall.execute();

                if (!response.isSuccessful()) {
                    log.error("ListProperties call failed! Error Code: {}", response.code());
                    throw new Exception(response.message());
                }

                List<PmsPropertiesUidsV2> page = response.body();
                if (page != null) {
                    pmsPropertiesUidV2s.addAll(page);
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

        return pmsPropertiesUidV2s;
    }

}
