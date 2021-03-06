package com.safely.batch.connector.components.externalV1;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.client.clientV1.PropertiesService;
import com.safely.batch.connector.client.clientV1.ReservationsService;
import com.safely.batch.connector.pmsV1.property.PmsProperty;
import com.safely.batch.connector.pmsV1.reservation.PmsReservation;
import com.safely.batch.connector.pmsV1.reservation.PmsReservationProperty;

@Service
public class LoadPmsReservationsService {

    private static final Logger log = LoggerFactory.getLogger(LoadPmsReservationsService.class);

    private static final String STEP_NAME = "load_reservations_from_pms";
    private static final String LOADED = "loaded";
    private final ReservationsService reservationsService;
    private final PropertiesService propertiesService;

    public LoadPmsReservationsService(ReservationsService reservationsService, PropertiesService propertiesService) {
        this.reservationsService = reservationsService;
        this.propertiesService = propertiesService;
    }

    public void execute(JobContext jobContext, String apiKey) throws Exception {

        //check API version if it is false it means, that API V1
        if (!jobContext.getApiVersion()){
            Map<String, Object> stepStatistics = new HashMap<>();

            log.info("OrganizationId: {}. Loading reservations from PMS", jobContext.getOrganizationId());
            String agencyUid = jobContext.getAgencyUid();
            List<PmsReservation> serverReservations = reservationsService.getReservations(apiKey, agencyUid);

            // get properties and convert to map
            List<PmsProperty> properties = jobContext.getPmsProperties();
            Map<String, PmsProperty> propertyMapById = new HashMap<>();
            for (PmsProperty property : properties) {
                propertyMapById.put(property.getUid(), property);
            }

            Set<String> failedProperties = new TreeSet<String>();

            // check to ensure all properties for reservations exist, if one is not in list, load it singly

            for (PmsReservation reservation : serverReservations) {
                if (reservation.getCheckOutDate() != null && reservation.getCheckOutDate().plusDays(7).isBefore(LocalDate.now())) {
                    continue;
                }
                PmsReservationProperty reservationProperty = reservation.getProperty();
                if (reservationProperty == null) {
                    continue;
                }
                if (propertyMapById.containsKey(reservationProperty.getUid())) {
                    continue;
                }

                // missing property
                String uid = reservationProperty.getUid();
                try {
                    PmsProperty newProperty = null;
                    if (!failedProperties.contains(uid)) {
                        newProperty = propertiesService.getProperty(apiKey, uid, agencyUid);
                    }
                    if (newProperty != null) {
                        propertyMapById.put(uid, newProperty);
                        properties.add(newProperty);
                    } else {
                        failedProperties.add(uid);
                    }
                } catch (Exception ex) {
                    log.error("OrganizationId: {}. Error while trying to load inactive property with id: {}", jobContext.getOrganizationId(), uid);
                    log.error(ex.getMessage(), ex);
                }
            }

            log.info("OrganizationId: {}. Loaded {} reservations from PMS.", jobContext.getOrganizationId(), serverReservations.size());
            scanReservationTypes(serverReservations);

            stepStatistics.put(LOADED, serverReservations.size());
            jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

            jobContext.setPmsReservations(serverReservations);
            jobContext.setPmsProperties(properties);
        }


    }

    private void scanReservationTypes(List<PmsReservation> reservations) {

        Map<String, String> source = new HashMap<>();
        Map<String, String> status = new HashMap<>();

        for (PmsReservation reservation : reservations) {
            if (reservation != null) {
                if (!source.containsKey(reservation.getSource())) {
                    source.put(reservation.getSource(), reservation.getSource());
                }
                if (!status.containsKey(reservation.getStatus())) {
                    status.put(reservation.getStatus(), reservation.getStatus());
                }
            }
        }

        log.info("Reservation source:");
        for (Entry<String, String> entry : source.entrySet()) {
            log.info("{}: {}", entry.getKey(), entry.getValue());
        }
        log.info("Reservation status:");
        for (Entry<String, String> entry : status.entrySet()) {
            log.info("{}: {}", entry.getKey(), entry.getValue());
        }
    }
}
