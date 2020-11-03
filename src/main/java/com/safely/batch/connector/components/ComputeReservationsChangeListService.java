package com.safely.batch.connector.components;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Reservation;
import com.safely.batch.connector.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
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

        log.info("Processing reservations to find changes for organization: {} - ({})",
                organization.getName(), organization.getId());

        Map<String, Object> stepStatistics = new HashMap<>();

        List<Reservation> safelyReservations = jobContext.getCurrentSafelyReservations();
        List<Reservation> pmsReservations = jobContext.getPmsSafelyReservations();

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
                    newReservations.add(pmsReservation);
                } else if (!safelyReservation.equals(pmsReservation)) {
                    updateReservation(safelyReservation, pmsReservation);
                    updatedReservations.add(safelyReservation);
                }
            } catch (Exception e) {
                String message = String
                        .format("Failed to compute updates for reservation with referenceId %s",
                                pmsReservation.getReferenceId());
                log.error(message, e);
                erroredReservations.add(pmsReservation.getReferenceId());
            }
        }

        jobContext.setNewReservations(newReservations);
        jobContext.setUpdatedReservations(updatedReservations);

        stepStatistics.put(CREATED, newReservations.size());
        stepStatistics.put(UPDATED, updatedReservations.size());
        stepStatistics.put(PROCESSED, pmsReservations.size());
        stepStatistics.put(FAILED_IDS, erroredReservations);
        stepStatistics.put(FAILED, erroredReservations.size());
        jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);
    }

    protected Reservation updateReservation(Reservation safelyReservation,
                                            Reservation pmsReservation) {
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