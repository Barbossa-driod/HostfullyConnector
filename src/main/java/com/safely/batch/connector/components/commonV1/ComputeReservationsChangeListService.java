package com.safely.batch.connector.components.commonV1;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Property;
import com.safely.api.domain.Reservation;
import com.safely.api.domain.enumeration.ReservationStatus;
import com.safely.batch.connector.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ComputeReservationsChangeListService {

    private static final Logger log = LoggerFactory.getLogger(ComputeReservationsChangeListService.class);

    private static final String UPDATED = "updated";
    private static final String CREATED = "created";
    private static final String FAILED = "failed";
    private static final String FAILED_IDS = "failed_ids";
    private static final String PROCESSED = "processed";
    private static final String STEP_NAME = "compute_reservations_change_list";

    public void execute(JobContext jobContext) {
        Organization organization = jobContext.getOrganization();

        log.info("OrganizationId: {}. Processing reservations to find changes for organization with name: {}",
                organization.getEntityId(), organization.getName());

        Map<String, Object> stepStatistics = new HashMap<>();

        List<Property> safelyProperties = jobContext.getCurrentSafelyProperties();
        List<Property> newProperties = jobContext.getNewProperties();;
        List<Reservation> safelyReservations = jobContext.getCurrentSafelyReservations();
        List<Reservation> pmsReservations = jobContext.getPmsSafelyReservations();

        //Add all the safely properties to a Map for easy lookup by Reference Id
        Map<String, Property> safelyPropertyLookup = new HashMap<>();
        for (Property safelyProperty : safelyProperties) {
            safelyPropertyLookup.put(safelyProperty.getReferenceId(), safelyProperty);
        }
        
        //Add all the new properties to a Map for easy lookup by Reference Id
        Map<String, Property> newPropertyLookup = new HashMap<>();
        for (Property newProperty : newProperties) {
        	newPropertyLookup.put(newProperty.getReferenceId(), newProperty);
        }
        
        //Add all the safely reservation to a Map so we can look them up by Reference ID
        Map<String, Reservation> safelyReservationLookup = new HashMap<>();
        for (Reservation safelyReservation : safelyReservations) {
            safelyReservationLookup.put(safelyReservation.getReferenceId(), safelyReservation);
        }

        //Find all new reservations
        List<Reservation> newReservations = new ArrayList<>();
        List<Reservation> updatedReservations = new ArrayList<>();
        List<String> erroredReservations = new ArrayList<>();

        for (Reservation pmsReservation : pmsReservations) {
            try {
                Reservation safelyReservation = safelyReservationLookup.get(pmsReservation.getReferenceId());

                //we could possibly use the modified date as well
                if (safelyReservation == null) {
                    String propertyPmsID = pmsReservation.getPropertyReferenceId();
                    if (pmsReservation.getStatus() == ReservationStatus.ACTIVE && safelyPropertyLookup.get(propertyPmsID) == null && newPropertyLookup.get(propertyPmsID) == null) {
                    	String guestName = pmsReservation.getGuests().get(0).getFirstName() + " " + pmsReservation.getGuests().get(0).getLastName(); 
                    	log.error("OrganizationId: {}. Property id={} (organization id = {}) has been reserved id={} booked on {}, for period {} - {} by guest {}. Booked  can't be loaded nor from Hostfully neither from Safely.",
                    			jobContext.getOrganizationId(), propertyPmsID, jobContext.getOrganization().getLegacyOrganizationId(), pmsReservation.getReferenceId(), pmsReservation.getCreateDate(), pmsReservation.getArrivalDate(),
                    			pmsReservation.getDepartureDate(), guestName);
                    }
                    newReservations.add(pmsReservation);
                } else if (!safelyReservation.equals(pmsReservation)) {
                	if (safelyReservation.getDepartureDate().plusDays(7).isAfter(LocalDate.now())) {
	                    updateReservation(safelyReservation, pmsReservation);
	                    updatedReservations.add(safelyReservation);
                	}
                }
                
            } catch (Exception e) {
                String message = String.format("OrganizationId: %s. Failed to compute updates for reservation with referenceId %s",
                        jobContext.getOrganizationId(), pmsReservation.getReferenceId());
                log.error(message, e);
                erroredReservations.add(pmsReservation.getReferenceId());
            }
        }

        log.info("OrganizationId: {}. Found {} new reservations.", jobContext.getOrganizationId(), newReservations.size());
        log.info("OrganizationId: {}. Found {} updated reservations.", jobContext.getOrganizationId(), updatedReservations.size());

        jobContext.setNewReservations(newReservations);
        jobContext.setUpdatedReservations(updatedReservations);

        stepStatistics.put(CREATED, newReservations.size());
        stepStatistics.put(UPDATED, updatedReservations.size());
        stepStatistics.put(PROCESSED, pmsReservations.size());
        stepStatistics.put(FAILED_IDS, erroredReservations);
        stepStatistics.put(FAILED, erroredReservations.size());
        jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);
    }

    protected Reservation updateReservation(Reservation safelyReservation, Reservation pmsReservation) {
        safelyReservation.setOrganizationId(pmsReservation.getOrganizationId());
        safelyReservation.setReferenceId(pmsReservation.getReferenceId());
        safelyReservation.setLegacyOrganizationId(pmsReservation.getLegacyOrganizationId());

        // property data
        safelyReservation.setPropertyReferenceId(pmsReservation.getPropertyReferenceId());
        safelyReservation.setPropertyName(pmsReservation.getPropertyName());

        // category values
        safelyReservation.setCategory1(pmsReservation.getCategory1());
        safelyReservation.setCategory2(pmsReservation.getCategory2());
        safelyReservation.setCategory3(pmsReservation.getCategory3());
        safelyReservation.setCategory4(pmsReservation.getCategory4());

        // guest counts
        safelyReservation.setAdults(pmsReservation.getAdults());
        safelyReservation.setChildren(pmsReservation.getChildren());
        safelyReservation.setInfants(pmsReservation.getInfants());
        safelyReservation.setPets(pmsReservation.getPets());
        safelyReservation.setSmoker(pmsReservation.getSmoker());

        // list of guests
        safelyReservation.setGuests(pmsReservation.getGuests());

        // price values
        safelyReservation.setCurrency(pmsReservation.getCurrency());
        safelyReservation.setPriceNightly(pmsReservation.getPriceNightly());
        safelyReservation.setPriceTotal(pmsReservation.getPriceTotal());

        // classification types
        safelyReservation.setReservationType(pmsReservation.getReservationType());
        safelyReservation.setBookingChannelType(pmsReservation.getBookingChannelType());

        // reservation dates
        safelyReservation.setArrivalDate(pmsReservation.getArrivalDate());
        safelyReservation.setDepartureDate(pmsReservation.getDepartureDate());
        safelyReservation.setPmsUpdateDate(pmsReservation.getPmsUpdateDate());

        // reservation status
        safelyReservation.setStatus(pmsReservation.getStatus());
        safelyReservation.setPmsStatus(pmsReservation.getPmsStatus());

        safelyReservation.setPmsObjectHashcode(pmsReservation.getPmsObjectHashcode());

        safelyReservation.setLastModifiedDate(Instant.now());

        return safelyReservation;
    }
}
