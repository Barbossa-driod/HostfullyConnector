package com.safely.batch.connector.steps.computeReservationsChangelist;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Reservation;
import com.safely.api.domain.enumeration.ConnectorOperationMode;
import com.safely.batch.connector.pms.reservation.PmsReservation;
import com.safely.batch.connector.steps.JobContext;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputeReservationsChangeListTasklet implements Tasklet {

  private static final Logger log = LoggerFactory
      .getLogger(ComputeReservationsChangeListTasklet.class);

  @Autowired
  public JobContext jobContext;

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    Organization organization = jobContext.getOrganization();

    log.info("Processing reservations to find changes for organization: {} - ({})",
        organization.getName(), organization.getId());
    processReservations(jobContext);

    return RepeatStatus.FINISHED;
  }

  protected JobContext processReservations(JobContext jobContext) throws Exception {
    List<Reservation> safelyReservations = jobContext.getCurrentSafelyReservations();
    List<Reservation> pmsReservations = jobContext.getPmsSafelyReservations();

    //Add all the safely reservation to a Map so we can look them up by Reference ID
    Map<String, Reservation> safelyReservationLookup = new HashMap<>();
    for (Reservation safelyReservation : safelyReservations) {
      safelyReservationLookup.put(safelyReservation.getReferenceId(), safelyReservation);
    }

    //Add all the PMS reservation to a Map so we can look them up by Reference ID
    Map<String, Reservation> pmsReservationLookup = new HashMap<>();
    for (Reservation pmsReservation : pmsReservations) {
      pmsReservationLookup.put(pmsReservation.getReferenceId(), pmsReservation);
    }

    //Find all new reservations
    List<Reservation> newReservations = new ArrayList<>();
    List<Reservation> updatedReservations = new ArrayList<>();

    for (Reservation pmsReservation : pmsReservations) {
      Reservation safelyReservation = safelyReservationLookup.get(pmsReservation.getReferenceId());

      //we could possibly use the modified date as well
      if (safelyReservation == null) {
        newReservations.add(pmsReservation);
      }
      //use either hashcode or Modified date to detect changes in a reservation
      else if (safelyReservation.getLastModifiedDate() != null && !safelyReservation
          .getLastModifiedDate().equals(pmsReservation.getLastModifiedDate())){
          updateReservation(safelyReservation, pmsReservation);
          updatedReservations.add(safelyReservation);
      } else if (!safelyReservation.getPmsObjectHashcode()
          .equals(pmsReservation.getPmsObjectHashcode())) {
        updateReservation(pmsReservation, safelyReservation);
        updatedReservations.add(safelyReservation);
      }
    }
    // we could add some logic around deleted reservations but I do not see this in MyVR or Lightmaker

    jobContext.setNewReservations(newReservations);
    jobContext.setUpdatedReservations(updatedReservations);

    return jobContext;
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