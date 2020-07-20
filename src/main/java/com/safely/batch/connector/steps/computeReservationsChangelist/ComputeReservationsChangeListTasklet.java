package com.safely.batch.connector.steps.computeReservationsChangelist;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Reservation;
import com.safely.batch.connector.steps.JobContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class ComputeReservationsChangeListTasklet implements Tasklet {

  private static final Logger log = LoggerFactory
      .getLogger(ComputeReservationsChangeListTasklet.class);

  @Autowired
  public JobContext jobContext;

  private static final String UPDATED = "updated";
  private static final String CREATED = "created";
  private static final String PROCESSED = "processed";
  private static final String STEP_NAME = "compute_reservations_change_list";

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    Organization organization = jobContext.getOrganization();

    log.info("Processing reservations to find changes for organization: {} - ({})",
        organization.getName(), organization.getId());
    processReservations(jobContext, chunkContext);
    return RepeatStatus.FINISHED;
  }

  protected JobContext processReservations(JobContext jobContext, ChunkContext chunkContext)
      throws Exception {

    Map<String, Object> stepStatistics = new HashMap<>();

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
      try {
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
        } else {
          if (!safelyReservation.equals(pmsReservation)) {
            updateReservation(safelyReservation, pmsReservation);
            updatedReservations.add(safelyReservation);
            }
        }
      } catch (Exception e){
        String message = String
            .format("Failed to compute changes for reservation with referenceId %s",
                pmsReservation.getReferenceId());
        log.error(message);
        Exception wrapperException = new Exception(message, e);
        chunkContext.getStepContext().getStepExecution().addFailureException(wrapperException);
      }
    }
    // we could add some logic around deleted reservations but I do not see this in MyVR or Lightmaker

    jobContext.setNewReservations(newReservations);
    jobContext.setUpdatedReservations(updatedReservations);

    stepStatistics.put(CREATED, newReservations.size());
    stepStatistics.put(UPDATED, updatedReservations.size());
    stepStatistics.put(PROCESSED, pmsReservations.size());
    jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

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