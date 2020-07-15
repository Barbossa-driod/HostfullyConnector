package com.safely.batch.connector.client;

import com.google.common.util.concurrent.RateLimiter;
import com.safely.batch.connector.pms.property.PmsProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import retrofit2.Call;
import retrofit2.Response;

@Service
public class PropertiesService {

  private static final Logger log = LoggerFactory.getLogger(PropertiesService.class);

  private static final int LIMIT = 100;
  private static final String AUTHENTICATION_BEARER_FORMAT = "Bearer %s";

  private final RateLimiter rateLimiter;
  private PropertiesV1ApiClient propertiesV1ApiClient;

  public PropertiesService(PropertiesV1ApiClient propertiesV1ApiClient,
      @Qualifier("PmsApiRateLimiter") RateLimiter rateLimiter) {
    Assert.notNull(propertiesV1ApiClient, "PropertiesV1ApiClient cannot be null!");
    Assert.notNull(rateLimiter, "RateLimiter cannot be null!");

    this.propertiesV1ApiClient = propertiesV1ApiClient;
    this.rateLimiter = rateLimiter;
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
        properties.addAll(page);

        retrievedCount = page.size();

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