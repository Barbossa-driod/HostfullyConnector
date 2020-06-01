package com.safely.batch.connector.client;


import com.google.common.util.concurrent.RateLimiter;
import com.safely.batch.connector.pms.photo.PmsPhoto;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class PhotosService {

  private static final Logger log = LoggerFactory.getLogger(PhotosService.class);

  private static final int LIMIT = 100;
  private static final String AUTHENTICATION_BEARER_FORMAT = "Bearer %s";

  private final RateLimiter rateLimiter;
  private PhotosV1ApiClient photosV1ApiClient;

  public PhotosService(PhotosV1ApiClient photosV1ApiClient,
      @Qualifier("PmsApiRateLimiter") RateLimiter rateLimiter) {
    Assert.notNull(photosV1ApiClient, "PhotosV1ApiClient cannot be null!");
    Assert.notNull(rateLimiter, "RateLimiter cannot be null!");

    this.photosV1ApiClient = photosV1ApiClient;
    this.rateLimiter = rateLimiter;
  }

  public List<PmsPhoto> getPhotos(String token, String propertyKey, LocalDate modified)
      throws Exception {
    Assert.notNull(token, "Authentication token cannot be null!");
    Assert.notNull(propertyKey, "PropertyKey cannot be null");

    String authenticationToken = String.format(AUTHENTICATION_BEARER_FORMAT, token);
    List<PmsPhoto> photos = new ArrayList<>();
    String modifiedDateString =
        modified != null ? modified.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;

    int offset = 0;
    int retrievedCount = 0;
    int totalCount = 0;
    int pageCount = 1;
    do {
      log.info("Loading page {} of listPhotos.", pageCount);
      try {
//        rateLimiter.acquire();
//
//        Call<ResponsePage<PmsPhotos>> apiCall = photosV1ApiClient
//            .listPhotos(authenticationToken, propertyKey, LIMIT, offset, modifiedDateString);
//        Response<ResponsePage<PmsPhotos>> response = apiCall.execute();
//
//        if (!response.isSuccessful()) {
//          log.error("ListPhotos call failed! Error Code: {}", response.code());
//          throw new Exception(response.message());
//        }
//
//        ResponsePage<PmsPhotos> page = response.body();
//        totalCount = page.getCount();
//        retrievedCount = page.getResults().size();
//        photos.addAll(page.getResults());
//        offset = offset + retrievedCount;

      } catch (Exception ex) {
        log.error("Exception while calling ListPhotos!", ex);
        throw ex;
      }
      pageCount++;
    } while (photos.size() < totalCount || retrievedCount > 0);

    return photos;
  }
}
