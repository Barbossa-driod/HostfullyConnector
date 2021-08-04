package com.safely.batch.connector.components.externalV2;

import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.client.clientV2.ReservationsOrdersServiceV2;
import com.safely.batch.connector.pmsV2.reservation.PmsReservationV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;

import java.util.List;
import java.util.Map;

@Service
public class LoadReservationsOrdersServiceV2 {
    private static final Logger log = LoggerFactory.getLogger(LoadReservationsOrdersServiceV2.class);

    private static final String STEP_NAME = "load_reservationsOrders_from_pms";
    private final ReservationsOrdersServiceV2 reservationsOrdersServiceV2;

    public LoadReservationsOrdersServiceV2(ReservationsOrdersServiceV2 reservationsOrdersServiceV2) {
        this.reservationsOrdersServiceV2 = reservationsOrdersServiceV2;
    }

    public void execute(JobContext jobContext, String apiKey) throws Exception {

        //check API version if it is false it means, that API V1
        if (jobContext.getApiVersion()){
            Map<String, Object> stepStatistics = new HashMap<>();

            log.info("OrganizationId: {}. Loading reservation order from PMS", jobContext.getOrganizationId());

            List<PmsReservationV2> reservations = jobContext.getPmsReservationsV2();

            reservationsOrdersServiceV2.getOrder(apiKey, reservations);

            log.info("OrganizationId: {}. Loaded reservation order from PMS.", jobContext.getOrganizationId());
            jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

            jobContext.setPmsReservationsV2(reservations);
        }
    }

}
