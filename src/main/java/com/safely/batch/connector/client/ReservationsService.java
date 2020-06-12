package com.safely.batch.connector.client;

import com.google.common.util.concurrent.RateLimiter;
import com.safely.batch.connector.pms.reservation.PmsReservation;
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
public class ReservationsService {

  private static final Logger log = LoggerFactory.getLogger(ReservationsService.class);

  private static final int LIMIT = 100;
  private static final String AUTHENTICATION_BEARER_FORMAT = "Bearer %s";

  private ReservationsV1ApiClient reservationsV1ApiClient;
  private DateTimeFormatter formatter;
  private final RateLimiter rateLimiter;

  public ReservationsService(ReservationsV1ApiClient reservationsV1ApiClient,
      @Qualifier("PmsApiRateLimiter") RateLimiter rateLimiter) {
    Assert.notNull(reservationsV1ApiClient, "ReservationsV1ApiClient cannot be null!");
    Assert.notNull(rateLimiter, "RateLimiter cannot be null!");

    this.reservationsV1ApiClient = reservationsV1ApiClient;
    this.rateLimiter = rateLimiter;
    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  }

  public List<PmsReservation> getReservations(String token, LocalDate bookingDate,
      LocalDate modified) throws Exception {
    Assert.notNull(token, "Authentication token cannot be null!");

    String authenticationToken = String.format(AUTHENTICATION_BEARER_FORMAT, token);
    List<PmsReservation> reservations = new ArrayList<>();
    String modifiedDateString = modified != null ? modified.format(formatter) : null;
    String bookedDateString = bookingDate != null ? bookingDate.format(formatter) : null;

    int offset = 0;
    int retrievedCount = 0;
    int totalCount = 0;
    int pageCount = 1;
    do {
      log.info("Loading page {} of listReservations.", pageCount);
      try {
        rateLimiter.acquire();

//        Call<ResponsePage<PmsReservation>> apiCall = reservationsV1ApiClient
//            .listReservations(authenticationToken, bookedDateString, LIMIT, offset,
//                modifiedDateString);
//        Response<ResponsePage<PmsReservation>> response = apiCall.execute();
//
//        if (!response.isSuccessful()) {
//          log.error("ListReservations call failed! Error Code: {}", response.code());
//          throw new Exception(response.message());
//        }
//
//        ResponsePage<PmsReservation> page = response.body();
//        totalCount = page.getCount();
//        retrievedCount = page.getResults().size();
//        reservations.addAll(page.getResults());
//        offset = offset + retrievedCount;

      } catch (Exception ex) {
        log.error("Exception while calling ListReservations!", ex);
        throw ex;
      }
      pageCount++;
    } while (reservations.size() < totalCount || retrievedCount > 0);

    return reservations;
  }
}
