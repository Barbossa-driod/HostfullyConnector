package com.safely.batch.connector.components;

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
import com.safely.batch.connector.client.PropertiesService;
import com.safely.batch.connector.client.ReservationsService;
import com.safely.batch.connector.pms.property.PmsProperty;
import com.safely.batch.connector.pms.reservation.PmsReservation;
import com.safely.batch.connector.pms.reservation.PmsReservationProperty;

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
        Map<String, Object> stepStatistics = new HashMap<>();

        log.info("Loading reservations from PMS");
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
                	if (reservation.isActive()) {
	                	if (reservation.isOngoing() || reservation.isPending()) {
	                		log.error("Reservation id={} in active status booked at {} for period {} - {}, guest:{}. Property for this reservation id={} can't be loaded from Hostfully system. Please contact PM", 
	                				reservation.getUid(), reservation.getCreated(), reservation.getCheckInDate(), reservation.getCheckOutDate(), reservation.getFirstName() + " " + reservation.getLastName(), uid);
	                	}
                	}
                }
            } catch (Exception ex) {
                log.error("Error while trying to load inactive property with id: {}", uid);
                log.error(ex.getMessage(), ex);
            }
        }

        log.info("Finished Loading {} from PMS ", serverReservations.size());
        scanReservationTypes(serverReservations);

        stepStatistics.put(LOADED, serverReservations.size());
        jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

        jobContext.setPmsReservations(serverReservations);
        jobContext.setPmsProperties(properties);
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
