package com.safely.batch.connector.client;

import com.google.common.util.concurrent.RateLimiter;
import com.safely.batch.connector.pms.reservation.PmsReservation;
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
public class ReservationsService {

    private static final Logger log = LoggerFactory.getLogger(ReservationsService.class);

    private static final int LIMIT = 20;
    private final RateLimiter rateLimiter;
    private ReservationsV1ApiClient reservationsV1ApiClient;

    public ReservationsService(ReservationsV1ApiClient reservationsV1ApiClient,
                               @Qualifier("PmsApiRateLimiter") RateLimiter rateLimiter) {
        Assert.notNull(reservationsV1ApiClient, "ReservationsV1ApiClient cannot be null!");
        Assert.notNull(rateLimiter, "RateLimiter cannot be null!");

        this.reservationsV1ApiClient = reservationsV1ApiClient;
        this.rateLimiter = rateLimiter;
    }

    public List<PmsReservation> getReservations(String token, String agencyUid) throws Exception {
        Assert.notNull(token, "Authentication token cannot be null!");
        Assert.notNull(agencyUid, "agencyUid cannot be null!");

        List<PmsReservation> reservations = new ArrayList<>();

        int offset = 0;
        int retrievedCount = 0;
        int pageCount = 1;
        do {
            log.info("Loading page {} of listReservations.", pageCount);
            try {
                rateLimiter.acquire();
                Response<List<PmsReservation>> response = null;
                int attempts = 0;
                do {
                    try {
                        Call<List<PmsReservation>> apiCall = reservationsV1ApiClient
                                .listReservations(token, agencyUid, LIMIT, offset);
                        response = apiCall.execute();
                    } catch (Exception ex) {
//            log.error("Error while attempting to load reservations.", ex);
//            if (attempts >= 3) {
//              throw ex;
//            }
                        attempts++;
                    }
                } while (response == null && attempts <= 3);

                if (response == null) {
                	log.error("ListReservations call failed (offset: {}, size: {})! No response", offset, LIMIT);
                    throw new Exception("Failed to load reservations after 3 tries");
                }

                if (!response.isSuccessful()) {
                    log.error("ListReservations call failed (offset: {}, size: {})! Error Code: {}", offset, LIMIT, response.code());
                    throw new Exception(response.message());
                }

                List<PmsReservation> page = response.body();

                if (page != null) {
                    reservations.addAll(page);
                    retrievedCount = page.size();
                } else {
                    retrievedCount = 0;
                }

                offset = offset + retrievedCount;

            } catch (Exception ex) {
                log.error("Exception while calling ListReservations! {}", ex.getMessage());
                throw ex;
            }
            pageCount++;
        } while (retrievedCount > 0 && retrievedCount == LIMIT);

        return reservations;
    }
}
