package com.safely.batch.connector.steps.saveReservationsToSafely;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Reservation;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.common.services.safely.SafelyConnectorReservationsService;
import com.safely.batch.connector.steps.JobContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class SaveReservationsToSafelyTasklet implements Tasklet {

  private static final Logger log = LoggerFactory.getLogger(SaveReservationsToSafelyTasklet.class);

  @Autowired
  public JobContext jobContext;

  @Autowired
  private SafelyConnectorReservationsService reservationsService;

  private static final String UPDATED = "UPDATED";
  private static final String CREATED = "CREATED";
  private static final String FAILED = "FAILED";
  private static final String FAILED_IDS = "FAILED_IDS";
  private static final String STEP_NAME = "SAVE_RESERVATIONS_TO_SAFELY";

  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    HashMap<String, Object> stepStatistics = new HashMap<>();

    Organization organization = jobContext.getOrganization();
    JWTToken token = jobContext.getSafelyToken();

    List<Reservation> newReservations = jobContext.getNewReservations();
    log.info("Writing {} new reservations for organization: {} - ({})", newReservations.size(),
        organization.getName(), organization.getEntityId());

    int successfullyCreated = 0;

    List<String> failedIds = new ArrayList<>();

    for (Reservation reservation : newReservations) {
      try {
        reservationsService.create(token.getIdToken(), reservation);
        successfullyCreated++;
      } catch(Exception e) {
        log.error("Failed to create reservation with referenceId {}", reservation.getReferenceId());
        failedIds.add(reservation.getReferenceId());
      }
    }

    List<Reservation> updatedReservations = jobContext.getUpdatedReservations();
    log.info("Writing {} updated reservations for organization: {} - ({})",
        updatedReservations.size(), organization.getName(), organization.getEntityId());

    int successfullyUpdated = 0;
    for (Reservation reservation : updatedReservations) {
      try{
        reservationsService.save(token.getIdToken(), reservation);
        successfullyUpdated++;
      }catch(Exception e) {
        log.error("Failed to update reservation with referenceId {}", reservation.getReferenceId());
        failedIds.add(reservation.getReferenceId());
      }
    }

    stepStatistics.put(CREATED, successfullyCreated);
    stepStatistics.put(UPDATED, successfullyUpdated);
    stepStatistics.put(FAILED, failedIds.size());
    stepStatistics.put(FAILED_IDS, failedIds);

    jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

    return RepeatStatus.FINISHED;
  }
}
