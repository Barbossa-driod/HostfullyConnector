package com.safely.batch.connector.client.clientV2;


import com.safely.batch.connector.pmsV2.reservation.PmsReservationV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;


import java.util.ArrayList;
import java.util.List;


@Service
public class ReservationsServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(ReservationsServiceV2.class);

    private static final int LIMIT = 20;

    private ReservationsV2ApiClient reservationsV2ApiClient;

    public ReservationsServiceV2(ReservationsV2ApiClient reservationsV2ApiClient) {
        this.reservationsV2ApiClient = reservationsV2ApiClient;
    }

    public List<PmsReservationV2> getReservations(String token, String agencyUid) throws Exception {

        List<PmsReservationV2> reservations = new ArrayList<>();

        int offset = 0;
        int retrievedCount = 0;
        int pageCount = 1;
        do {
            log.info("Loading page {} of listReservations.", pageCount);
            try {
                Response<List<PmsReservationV2>> response = null;
                int attempts = 0;
                do {
                    try {
                        Call<List<PmsReservationV2>> apiCall = reservationsV2ApiClient
                                .listReservations(token, agencyUid, LIMIT, offset);
                        response = apiCall.execute();
                    } catch (Exception ex) {
                        log.warn("Error while attempting to load reservations try #{}. Exception: {}", (attempts + 1), ex != null ? ex.getMessage() : ex);
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

                List<PmsReservationV2> page = response.body();

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
