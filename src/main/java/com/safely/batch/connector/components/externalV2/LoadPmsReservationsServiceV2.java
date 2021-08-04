package com.safely.batch.connector.components.externalV2;

import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.client.clientV2.ReservationsServiceV2;
import com.safely.batch.connector.pmsV2.reservation.PmsReservationV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;

import java.util.List;
import java.util.Map;


@Service
public class LoadPmsReservationsServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(LoadPmsReservationsServiceV2.class);

    private static final String STEP_NAME = "load_reservations_from_pms";
    private static final String LOADED = "loaded";
    private final ReservationsServiceV2 reservationsServiceV2;


    public LoadPmsReservationsServiceV2(ReservationsServiceV2 reservationsServiceV2) {
        this.reservationsServiceV2 = reservationsServiceV2;

    }

    public void execute(JobContext jobContext, String apiKey) throws Exception {

        //check API version if it is false it means, that API V1
        if (jobContext.getApiVersion()){
            Map<String, Object> stepStatistics = new HashMap<>();

            log.info("OrganizationId: {}. Loading reservation photos from PMS", jobContext.getOrganizationId());
            String agencyUid = jobContext.getAgencyUid();

            List<PmsReservationV2> reservations = reservationsServiceV2.getReservations(apiKey, agencyUid);

            log.info("OrganizationId: {}. Loaded reservation photos from PMS.", jobContext.getOrganizationId());

            stepStatistics.put(LOADED, reservations.size());
            jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

            jobContext.setPmsReservationsV2(reservations);
        }
    }
}
